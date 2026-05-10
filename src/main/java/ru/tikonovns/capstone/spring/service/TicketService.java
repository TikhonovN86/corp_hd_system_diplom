package ru.tikonovns.capstone.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tikonovns.capstone.spring.entity.*;
import ru.tikonovns.capstone.spring.exception.AccessDeniedException;
import ru.tikonovns.capstone.spring.exception.BusinessValidationException;
import ru.tikonovns.capstone.spring.exception.NotFoundException;
import ru.tikonovns.capstone.spring.repository.*;
import ru.tikonovns.capstone.spring.security.CurrentUserService;
import ru.tikonovns.capstone.spring.security.TicketAccessService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static ru.tikonovns.capstone.spring.utils.constants.TicketStatus.*;
import static ru.tikonovns.capstone.spring.utils.constants.UserRole.*;
import static ru.tikonovns.capstone.spring.utils.constants.WorkGroupCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final ServiceDirectionRepository serviceDirectionRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final WorkGroupRepository workGroupRepository;

    private final CurrentUserService currentUserService;
    private final TicketAccessService ticketAccessService;
    private final CommentService commentService;
    private final TicketEventService ticketEventService;
    private final NotificationService notificationService;
    private final SlaService slaService;

    public Ticket getById(Long ticketId) {
        Ticket ticket = ticketRepository.findByIdWithAllReferences(ticketId)
                .orElseThrow(() -> new NotFoundException("Обращение не найдено"));

        ticketAccessService.checkCanView(ticket);
        return ticket;
    }

    public List<Ticket> getTicketsForCurrentUser(boolean hideResolved) {
        String roleCode = currentUserService.getCurrentRoleCode();
        Long currentUserId = currentUserService.getCurrentUserId();

        List<Ticket> tickets;

        switch (roleCode) {
            case ROLE_INITIATOR -> tickets = ticketRepository.findAllByInitiatorIdOrderByCreatedAtDesc(currentUserId);
            case ROLE_DISPATCHER ->
                    tickets = ticketRepository.findAllByWorkGroupCodeOrderByCreatedAtDesc(DISPATCHER_GROUP_CODE);
            case ROLE_EXECUTOR -> {
                String executorGroupCode = currentUserService.getPrincipal()
                        .getWorkGroupCodes()
                        .stream()
                        .filter(code -> !DISPATCHER_GROUP_CODE.equals(code))
                        .findFirst()
                        .orElseThrow(() -> new BusinessValidationException(
                                "У исполнителя не найдена рабочая группа"
                        ));

                tickets = ticketRepository.findAllByWorkGroupCodeOrderByCreatedAtDesc(executorGroupCode);
            }
            case null, default -> throw new BusinessValidationException("Неизвестная роль пользователя");
        }

        if (!hideResolved) {
            return tickets;
        }

        return tickets.stream()
                .filter(ticket -> !STATUS_RESOLVED.equals(ticket.getStatus()))
                .toList();
    }

    @Transactional
    public Ticket createTicket(Long sectionId, Long serviceDirectionId, String description) {
        ticketAccessService.checkCanCreateTicket();

        if (description == null || description.isBlank()) {
            throw new BusinessValidationException("Описание обращения не может быть пустым");
        }

        User initiator = userRepository.findById(currentUserService.getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NotFoundException("Раздел не найден"));

        ServiceDirection serviceDirection = serviceDirectionRepository.findByIdWithReferences(serviceDirectionId)
                .orElseThrow(() -> new NotFoundException("Направление обслуживания не найдено"));

        if (!serviceDirection.getSection().getId().equals(section.getId())) {
            throw new BusinessValidationException("Направление обслуживания не относится к выбранному разделу");
        }

        WorkGroup dispatcherGroup = workGroupRepository.findByCode(DISPATCHER_GROUP_CODE)
                .orElseThrow(() -> new NotFoundException("Группа диспетчеров не найдена"));

        TicketType ticketType = serviceDirection.getDefaultTicketType();

        LocalDateTime now = LocalDateTime.now();

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setInitiator(initiator);
        ticket.setSection(section);
        ticket.setServiceDirection(serviceDirection);
        ticket.setTicketType(ticketType);
        ticket.setStatus(STATUS_NEW);
        ticket.setWorkGroup(dispatcherGroup);
        ticket.setAssignee(null);
        ticket.setDescription(description.trim());
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        ticket.setResolvedAt(null);
        ticket.setDeadline(slaService.calculateDeadline(now, ticketType));

        Ticket saved = ticketRepository.save(ticket);

        ticketEventService.logCreated(saved, initiator);
        ticketEventService.logGroupAssigned(saved, initiator);
        ticketEventService.logTypeChanged(saved, initiator);
        ticketEventService.logSlaRecalculated(saved, initiator);

        return saved;
    }

    @Transactional
    public Ticket takeInWork(Long ticketId) {
        Ticket ticket = ticketRepository.findByIdWithAllReferences(ticketId)
                .orElseThrow(() -> new NotFoundException("Обращение не найдено"));

        ticketAccessService.checkCanTakeInWork(ticket);

        if (STATUS_RESOLVED.equals(ticket.getStatus())) {
            throw new BusinessValidationException("Нельзя перевести в работу уже решенное обращение");
        }

        if (STATUS_IN_PROGRESS.equals(ticket.getStatus())) {
            return ticket;
        }

        String currentRole = currentUserService.getCurrentRoleCode();

        if (ROLE_DISPATCHER.equals(currentRole)) {
            if (!DISPATCHER_GROUP_CODE.equals(ticket.getWorkGroup().getCode())) {
                throw new BusinessValidationException("Диспетчер может взять в работу только обращение своей группы");
            }

            if (!STATUS_NEW.equals(ticket.getStatus()) && !STATUS_UNASSIGNED.equals(ticket.getStatus())) {
                throw new BusinessValidationException(
                        "Диспетчер может взять в работу только обращение в статусе NEW или UNASSIGNED"
                );
            }
        }

        if (ROLE_EXECUTOR.equals(currentRole)) {
            if (!STATUS_NEW.equals(ticket.getStatus())) {
                throw new BusinessValidationException("Исполнитель может взять в работу только обращение в статусе NEW");
            }

            if (!currentUserService.isMemberOfWorkGroup(ticket.getWorkGroup().getCode())) {
                throw new BusinessValidationException("Исполнитель не состоит в рабочей группе обращения");
            }
        }

        User currentUser = userRepository.findByUsernameWithRoleAndGroups(currentUserService.getCurrentUsername())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (ticket.getAssignee() == null) {

            boolean currentUserInTicketGroup = currentUser.getUserWorkGroups()
                    .stream()
                    .anyMatch(uwg -> uwg.getWorkGroup().getId().equals(ticket.getWorkGroup().getId()));

            if (!currentUserInTicketGroup) {
                throw new BusinessValidationException("Пользователь не состоит в рабочей группе обращения");
            }

            ticket.setAssignee(currentUser);
        } else if (!ticket.getAssignee().getId().equals(currentUser.getId())) {

            throw new BusinessValidationException("Взять обращение в работу может только назначенный сотрудник");
        }

        ticket.setStatus(STATUS_IN_PROGRESS);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);

        ticketEventService.logStatusChanged(saved, currentUser);
        ticketEventService.logEmployeeAssigned(saved, currentUser);

        return saved;
    }

    @Transactional
    public Ticket returnToDispatchers(Long ticketId, String privateComment) {

        if (privateComment == null || privateComment.isBlank()) {
            throw new BusinessValidationException(
                    "Для возврата обращения необходимо указать причину приватным комментарием"
            );
        }

        Ticket ticket = ticketRepository.findByIdWithAllReferences(ticketId)
                .orElseThrow(() -> new NotFoundException("Обращение не найдено"));

        ticketAccessService.checkCanReturnToDispatchers(ticket);

        if (STATUS_RESOLVED.equals(ticket.getStatus())) {
            throw new BusinessValidationException("Нельзя вернуть на диспетчеров решенное обращение");
        }

        if (!STATUS_NEW.equals(ticket.getStatus()) && !STATUS_IN_PROGRESS.equals(ticket.getStatus())) {
            throw new BusinessValidationException("Возврат на диспетчеров допустим только из статусов NEW или IN_PROGRESS");
        }

        commentService.addPrivateComment(ticketId, privateComment);

        WorkGroup dispatcherGroup = workGroupRepository.findByCode(DISPATCHER_GROUP_CODE)
                .orElseThrow(() -> new NotFoundException("Группа диспетчеров не найдена"));

        ticket.setWorkGroup(dispatcherGroup);
        ticket.setAssignee(null);
        ticket.setStatus(STATUS_UNASSIGNED);
        ticket.setUpdatedAt(LocalDateTime.now());

        User currentUser = getCurrentUserOrThrow();

        Ticket saved = ticketRepository.save(ticket);

        ticketEventService.logGroupAssigned(saved, currentUser);
        ticketEventService.logStatusChanged(saved, currentUser);
        ticketEventService.logEmployeeAssigned(saved, currentUser);

        return saved;
    }

    @Transactional
    public Ticket resolveTicket(Long ticketId, String publicComment) {

        if (publicComment == null || publicComment.isBlank()) {
            throw new BusinessValidationException("Для решения обращения необходимо добавить публичный комментарий");
        }

        Ticket ticket = ticketRepository.findByIdWithAllReferences(ticketId)
                .orElseThrow(() -> new NotFoundException("Обращение не найдено"));

        ticketAccessService.checkCanResolve(ticket);

        if (STATUS_RESOLVED.equals(ticket.getStatus())) {
            throw new BusinessValidationException("Обращение уже решено");
        }

        if (!STATUS_IN_PROGRESS.equals(ticket.getStatus())) {
            throw new BusinessValidationException("Для решения обращения его необходимо перевести в работу");
        }

        if (ticket.getAssignee() == null) {
            throw new BusinessValidationException("Для решения обращения должен быть назначен ответственный сотрудник");
        }

        commentService.addPublicComment(ticketId, publicComment, false);

        ticket.setStatus(STATUS_RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);

        User currentUser = getCurrentUserOrThrow();

        ticketEventService.logStatusChanged(saved, currentUser);
        notificationService.notifyResolved(saved, publicComment);

        return saved;
    }

    @Transactional
    public Ticket changeTicketType(Long ticketId, Long ticketTypeId) {
        Ticket ticket = ticketRepository.findByIdWithAllReferences(ticketId)
                .orElseThrow(() -> new NotFoundException("Обращение не найдено"));

        ticketAccessService.checkCanChangeTicketType(ticket);

        if (STATUS_RESOLVED.equals(ticket.getStatus())) {
            throw new BusinessValidationException("Нельзя изменить тип у решенного обращения");
        }

        TicketType newType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new NotFoundException("Тип обращения не найден"));

        if (ticket.getTicketType() != null && ticket.getTicketType().getId().equals(newType.getId())) {
            return ticket;
        }

        ticket.setTicketType(newType);
        ticket.setDeadline(slaService.calculateDeadline(ticket.getCreatedAt(), newType));
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);

        User currentUser = getCurrentUserOrThrow();
        ticketEventService.logTypeChanged(saved, currentUser);
        ticketEventService.logSlaRecalculated(saved, currentUser);

        return saved;
    }

    @Transactional
    public Ticket assignEmployee(Long ticketId, Long assigneeId) {
        Ticket ticket = ticketRepository.findByIdWithAllReferences(ticketId)
                .orElseThrow(() -> new NotFoundException("Обращение не найдено"));

        ticketAccessService.checkCanAssignEmployee(ticket);

        if (STATUS_RESOLVED.equals(ticket.getStatus())) {
            throw new BusinessValidationException("Нельзя назначить ответственного для решенного обращения");
        }

        User assignee = userRepository.findByIdWithRoleAndGroups(assigneeId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        boolean assigneeInTicketGroup = assignee.getUserWorkGroups()
                .stream()
                .anyMatch(uwg -> uwg.getWorkGroup().getId().equals(ticket.getWorkGroup().getId()));

        if (!assigneeInTicketGroup) {
            throw new BusinessValidationException("Назначаемый сотрудник не состоит в рабочей группе обращения");
        }

        ticket.setAssignee(assignee);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);

        User currentUser = getCurrentUserOrThrow();
        ticketEventService.logEmployeeAssigned(saved, currentUser);

        return saved;
    }

    @Transactional
    public Ticket changeWorkGroup(Long ticketId, Long workGroupId) {
        Ticket ticket = ticketRepository.findByIdWithAllReferences(ticketId)
                .orElseThrow(() -> new NotFoundException("Обращение не найдено"));

        ticketAccessService.checkCanChangeWorkGroup(ticket);

        WorkGroup newWorkGroup = workGroupRepository.findById(workGroupId)
                .orElseThrow(() -> new NotFoundException("Рабочая группа не найдена"));

        ticket.setWorkGroup(newWorkGroup);

        if (ticket.getAssignee() != null) {
            User assignee = userRepository.findByIdWithRoleAndGroups(ticket.getAssignee().getId())
                    .orElseThrow(() -> new NotFoundException("Ответственный сотрудник не найден"));

            boolean assigneeInNewGroup = assignee.getUserWorkGroups()
                    .stream()
                    .anyMatch(uwg -> uwg.getWorkGroup().getId().equals(newWorkGroup.getId()));

            if (!assigneeInNewGroup) {
                ticket.setAssignee(null);
            }
        }

        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);

        User currentUser = getCurrentUserOrThrow();
        ticketEventService.logGroupAssigned(saved, currentUser);
        ticketEventService.logEmployeeAssigned(saved, currentUser);

        return saved;
    }

    @Transactional
    public Ticket transferToWorkGroup(Long ticketId, Long workGroupId) {

        Ticket ticket = ticketRepository.findByIdWithAllReferences(ticketId)
                .orElseThrow(() -> new NotFoundException("Обращение не найдено"));

        ticketAccessService.checkCanChangeWorkGroup(ticket);

        if (STATUS_RESOLVED.equals(ticket.getStatus())) {
            throw new BusinessValidationException("Нельзя передать решенное обращение");
        }

        if (!ROLE_DISPATCHER.equals(currentUserService.getCurrentRoleCode())) {
            throw new AccessDeniedException("Передать обращение на другую рабочую группу может только диспетчер");
        }

        if (!DISPATCHER_GROUP_CODE.equals(ticket.getWorkGroup().getCode())) {
            throw new BusinessValidationException("Передать можно только обращение, находящееся на группе диспетчеров");
        }

        WorkGroup newWorkGroup = workGroupRepository.findById(workGroupId)
                .orElseThrow(() -> new NotFoundException("Рабочая группа не найдена"));

        if (ticket.getWorkGroup().getId().equals(newWorkGroup.getId())) {
            throw new BusinessValidationException("Указана текущая рабочая группа обращения");
        }

        if (DISPATCHER_GROUP_CODE.equals(newWorkGroup.getCode())) {
            throw new BusinessValidationException(
                    "Для передачи обращения необходимо указать рабочую группу, отличную от группы диспетчеров"
            );
        }

        ticket.setWorkGroup(newWorkGroup);
        ticket.setAssignee(null);
        ticket.setStatus(STATUS_NEW);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);

        User currentUser = getCurrentUserOrThrow();
        ticketEventService.logGroupAssigned(saved, currentUser);
        ticketEventService.logStatusChanged(saved, currentUser);
        ticketEventService.logEmployeeAssigned(saved, currentUser);

        return saved;
    }

    private User getCurrentUserOrThrow() {
        return userRepository.findByUsernameWithRoleAndGroups(currentUserService.getCurrentUsername())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private String generateTicketNumber() {
        return "SD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}