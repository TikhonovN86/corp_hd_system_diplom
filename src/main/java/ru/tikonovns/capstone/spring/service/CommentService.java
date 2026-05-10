package ru.tikonovns.capstone.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tikonovns.capstone.spring.entity.Comment;
import ru.tikonovns.capstone.spring.entity.Ticket;
import ru.tikonovns.capstone.spring.entity.User;
import ru.tikonovns.capstone.spring.exception.AccessDeniedException;
import ru.tikonovns.capstone.spring.exception.BusinessValidationException;
import ru.tikonovns.capstone.spring.exception.NotFoundException;
import ru.tikonovns.capstone.spring.repository.CommentRepository;
import ru.tikonovns.capstone.spring.repository.TicketRepository;
import ru.tikonovns.capstone.spring.repository.UserRepository;
import ru.tikonovns.capstone.spring.security.CurrentUserService;
import ru.tikonovns.capstone.spring.security.TicketAccessService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.tikonovns.capstone.spring.utils.constants.UserRole.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final TicketAccessService ticketAccessService;
    private final TicketEventService ticketEventService;
    private final NotificationService notificationService;

    public List<Comment> getCommentsForCurrentUser(Long ticketId) {
        Ticket ticket = ticketRepository.findByIdWithAllReferences(ticketId)
                .orElseThrow(() -> new NotFoundException("Обращение не найдено"));

        ticketAccessService.checkCanView(ticket);

        if (ROLE_INITIATOR.equals(currentUserService.getCurrentRoleCode())) {
            return commentRepository.findPublicCommentsByTicketIdOrderByCreatedAtAsc(ticketId);
        }

        return commentRepository.findAllByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    @Transactional
    public Comment addPublicComment(Long ticketId, String content) {
        return addPublicComment(ticketId, content, true);
    }

    @Transactional
    public Comment addPublicComment(Long ticketId, String content, boolean notifyInitiator) {
        validateCommentContent(content);

        Ticket ticket = ticketRepository.findByIdWithAllReferences(ticketId)
                .orElseThrow(() -> new NotFoundException("Обращение не найдено"));

        ticketAccessService.checkCanAddPublicComment(ticket);

        User author = userRepository.findById(currentUserService.getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setContent(content.trim());
        comment.setIsPrivate(false);
        comment.setCreatedAt(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);

        ticketEventService.logPublicComment(ticket, author, saved.getContent());

        if (notifyInitiator) {
            notificationService.notifyPublicComment(ticket, author, saved.getContent());
        }

        return saved;
    }

    @Transactional
    public Comment addPrivateComment(Long ticketId, String content) {
        validateCommentContent(content);

        Ticket ticket = ticketRepository.findByIdWithAllReferences(ticketId)
                .orElseThrow(() -> new NotFoundException("Обращение не найдено"));

        ticketAccessService.checkCanAddPrivateComment(ticket);

        User author = userRepository.findById(currentUserService.getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setContent(content.trim());
        comment.setIsPrivate(true);
        comment.setCreatedAt(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);

        ticketEventService.logPrivateComment(ticket, author, saved.getContent());

        return saved;
    }

    @Transactional
    public Comment addCancelComment(Long ticketId) {
        Ticket ticket = ticketRepository.findByIdWithAllReferences(ticketId)
                .orElseThrow(() -> new NotFoundException("Обращение не найдено"));

        ticketAccessService.checkCanAddPublicComment(ticket);

        if (!ticket.getInitiator().getId().equals(currentUserService.getCurrentUserId())) {
            throw new AccessDeniedException("Отменить обращение может только инициатор");
        }

        return addPublicComment(ticketId, "Обращение отменено инициатором");
    }

    private void validateCommentContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessValidationException("Комментарий не может быть пустым");
        }
    }
}