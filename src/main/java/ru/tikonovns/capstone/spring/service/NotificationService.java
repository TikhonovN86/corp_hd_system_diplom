package ru.tikonovns.capstone.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tikonovns.capstone.spring.entity.Notification;
import ru.tikonovns.capstone.spring.entity.Ticket;
import ru.tikonovns.capstone.spring.entity.User;
import ru.tikonovns.capstone.spring.exception.AccessDeniedException;
import ru.tikonovns.capstone.spring.exception.BusinessValidationException;
import ru.tikonovns.capstone.spring.exception.NotFoundException;
import ru.tikonovns.capstone.spring.repository.NotificationRepository;
import ru.tikonovns.capstone.spring.repository.UserRepository;
import ru.tikonovns.capstone.spring.security.CurrentUserService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.tikonovns.capstone.spring.utils.constants.UserRole.ROLE_DISPATCHER;
import static ru.tikonovns.capstone.spring.utils.constants.UserRole.ROLE_EXECUTOR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public List<Notification> getNotificationsForCurrentUser() {
        Long currentUserId = currentUserService.getCurrentUserId();
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(currentUserId);
    }

    public List<Notification> getUnreadNotificationsForCurrentUser() {
        Long currentUserId = currentUserService.getCurrentUserId();
        return notificationRepository.findUnreadByUserIdOrderByCreatedAtDesc(currentUserId);
    }

    public long getUnreadCountForCurrentUser() {
        Long currentUserId = currentUserService.getCurrentUserId();
        return notificationRepository.countByUserIdAndIsReadFalse(currentUserId);
    }

    @Transactional
    public void notifyPublicComment(Ticket ticket, User author, String commentText) {
        validateTicket(ticket);
        validateAuthor(author);

        if (commentText == null || commentText.isBlank()) {
            throw new BusinessValidationException("Текст комментария не может быть пустым");
        }

        String authorRoleCode = extractRoleCode(author);

        boolean allowedAuthor =
                ROLE_DISPATCHER.equals(authorRoleCode) || ROLE_EXECUTOR.equals(authorRoleCode);

        if (!allowedAuthor) {
            return;
        }

        User initiator = ticket.getInitiator();
        if (initiator == null) {
            throw new BusinessValidationException("У обращения отсутствует инициатор");
        }

        Notification notification = new Notification();
        notification.setUser(initiator);
        notification.setTicket(ticket);
        notification.setTitle("Новый комментарий");
        notification.setMessage(
                "Обращение " + safeTicketNumber(ticket) + ": " + commentText.trim()
        );
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    @Transactional
    public void notifyResolved(Ticket ticket, String publicComment) {
        validateTicket(ticket);

        User initiator = ticket.getInitiator();
        if (initiator == null) {
            throw new BusinessValidationException("У обращения отсутствует инициатор");
        }

        Notification notification = new Notification();
        notification.setUser(initiator);
        notification.setTicket(ticket);
        notification.setTitle("Обращение решено");
        notification.setMessage(
                "Обращение " + safeTicketNumber(ticket) + " решено: " + publicComment.trim()
        );
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Уведомление не найдено"));

        Long currentUserId = currentUserService.getCurrentUserId();

        if (!notification.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Нельзя изменить чужое уведомление");
        }

        if (Boolean.TRUE.equals(notification.getIsRead())) {
            return;
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    private void validateTicket(Ticket ticket) {
        if (ticket == null) {
            throw new BusinessValidationException("ticket не может быть null");
        }
    }

    private void validateAuthor(User author) {
        if (author == null) {
            throw new BusinessValidationException("author не может быть null");
        }
    }

    private String extractRoleCode(User author) {
        if (author.getRole() == null || author.getRole().getCode() == null || author.getRole().getCode().isBlank()) {
            throw new BusinessValidationException("У автора комментария отсутствует роль");
        }

        return author.getRole().getCode();
    }

    private String safeTicketNumber(Ticket ticket) {
        if (ticket.getTicketNumber() == null || ticket.getTicketNumber().isBlank()) {
            return "[без номера]";
        }
        return ticket.getTicketNumber();
    }
}