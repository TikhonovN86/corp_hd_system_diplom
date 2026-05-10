package ru.tikonovns.capstone.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tikonovns.capstone.spring.entity.Ticket;
import ru.tikonovns.capstone.spring.entity.TicketEvent;
import ru.tikonovns.capstone.spring.entity.User;
import ru.tikonovns.capstone.spring.exception.BusinessValidationException;
import ru.tikonovns.capstone.spring.exception.NotFoundException;
import ru.tikonovns.capstone.spring.repository.TicketEventRepository;
import ru.tikonovns.capstone.spring.repository.TicketRepository;
import ru.tikonovns.capstone.spring.security.TicketAccessService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.tikonovns.capstone.spring.utils.constants.TicketEventType.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketEventService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TicketEventRepository ticketEventRepository;
    private final TicketRepository ticketRepository;
    private final TicketAccessService ticketAccessService;

    public List<TicketEvent> getEventsForCurrentUser(Long ticketId) {
        Ticket ticket = ticketRepository.findByIdWithAllReferences(ticketId)
                .orElseThrow(() -> new NotFoundException("Обращение не найдено"));

        ticketAccessService.checkCanView(ticket);

        return ticketEventRepository.findAllByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    @Transactional
    public TicketEvent logCreated(Ticket ticket, User author) {
        return createEvent(
                ticket,
                author,
                CREATED,
                "Создано обращение № " + ticket.getTicketNumber()
        );
    }

    @Transactional
    public TicketEvent logStatusChanged(Ticket ticket, User author) {
        return createEvent(
                ticket,
                author,
                STATUS_CHANGED,
                "Статус обращения: " + ticket.getStatus()
        );
    }

    @Transactional
    public TicketEvent logGroupAssigned(Ticket ticket, User author) {
        String groupLabel = ticket.getWorkGroup() != null
                ? buildWorkGroupLabel(ticket)
                : "[ПУСТО]";

        return createEvent(
                ticket,
                author,
                GROUP_ASSIGNED,
                "Рабочая группа: " + groupLabel
        );
    }

    @Transactional
    public TicketEvent logPublicComment(Ticket ticket, User author, String commentText) {
        return createEvent(
                ticket,
                author,
                COMMENT_ADDED,
                "Добавлен комментарий: " + safeText(commentText)
        );
    }

    @Transactional
    public TicketEvent logPrivateComment(Ticket ticket, User author, String commentText) {
        return createEvent(
                ticket,
                author,
                PRIVATE_COMMENT_ADDED,
                "Добавлен приватный комментарий: " + safeText(commentText)
        );
    }

    @Transactional
    public TicketEvent logTypeChanged(Ticket ticket, User author) {
        String ticketTypeLabel = ticket.getTicketType() != null
                ? ticket.getTicketType().getName()
                : "[ПУСТО]";

        return createEvent(
                ticket,
                author,
                TYPE_CHANGED,
                "Тип обращения: " + ticketTypeLabel
        );
    }

    @Transactional
    public TicketEvent logSlaRecalculated(Ticket ticket, User author) {
        String deadline = ticket.getDeadline() != null
                ? ticket.getDeadline().format(DATE_TIME_FORMATTER)
                : "[ПУСТО]";

        return createEvent(
                ticket,
                author,
                SLA_RECALCULATED,
                "SLA пересчитан: " + deadline
        );
    }

    @Transactional
    public TicketEvent logEmployeeAssigned(Ticket ticket, User author) {
        String assigneeLabel = ticket.getAssignee() != null
                ? ticket.getAssignee().getUsername()
                : "[ПУСТО]";

        return createEvent(
                ticket,
                author,
                EMPLOYEE_ASSIGNED,
                "Ответственный сотрудник: " + assigneeLabel
        );
    }

    private TicketEvent createEvent(
            Ticket ticket,
            User author,
            String eventType,
            String eventMessage
    ) {
        if (ticket == null) {
            throw new BusinessValidationException("ticket не может быть null");
        }
        if (author == null) {
            throw new BusinessValidationException("author не может быть null");
        }
        if (eventType == null || eventType.isBlank()) {
            throw new BusinessValidationException("eventType не может быть пустым");
        }
        if (eventMessage == null || eventMessage.isBlank()) {
            throw new BusinessValidationException("eventMessage не может быть пустым");
        }

        TicketEvent event = new TicketEvent();
        event.setTicket(ticket);
        event.setAuthor(author);
        event.setEventType(eventType);
        event.setEventMessage(eventMessage);
        event.setCreatedAt(LocalDateTime.now());

        return ticketEventRepository.save(event);
    }

    private String safeText(String text) {
        if (text == null) {
            return "";
        }
        return text.trim();
    }

    private String buildWorkGroupLabel(Ticket ticket) {
        if (ticket.getWorkGroup() == null) {
            return "[ПУСТО]";
        }

        String code = ticket.getWorkGroup().getCode();
        String name = ticket.getWorkGroup().getName();

        if (code != null && !code.isBlank() && name != null && !name.isBlank()) {
            return code + " (" + name + ")";
        }
        if (code != null && !code.isBlank()) {
            return code;
        }
        if (name != null && !name.isBlank()) {
            return name;
        }

        return "[ПУСТО]";
    }
}