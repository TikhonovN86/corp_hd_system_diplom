package ru.tikonovns.capstone.spring.service;

import org.springframework.stereotype.Service;
import ru.tikonovns.capstone.spring.entity.Ticket;
import ru.tikonovns.capstone.spring.entity.TicketType;
import ru.tikonovns.capstone.spring.exception.BusinessValidationException;

import java.time.LocalDateTime;

import static ru.tikonovns.capstone.spring.utils.constants.TicketStatus.STATUS_RESOLVED;

@Service
public class SlaService {

    public LocalDateTime calculateDeadline(LocalDateTime createdAt, TicketType ticketType) {
        if (createdAt == null) {
            throw new BusinessValidationException("Дата создания обращения не может быть пустой");
        }

        if (ticketType == null) {
            throw new BusinessValidationException("Тип обращения не может быть пустым");
        }

        if (ticketType.getDefaultSlaHours() == null) {
            throw new BusinessValidationException("SLA для типа обращения не задан");
        }

        return createdAt.plusHours(ticketType.getDefaultSlaHours());
    }

    public Boolean isOverdue(Ticket ticket) {
        if (ticket == null) {
            throw new BusinessValidationException("Обращение не может быть пустым");
        }

        if (ticket.getDeadline() == null) {
            throw new BusinessValidationException("Крайний срок обращения не задан");
        }

        if (STATUS_RESOLVED.equals(ticket.getStatus())) {
            if (ticket.getResolvedAt() == null) {
                return false;
            }

            return !ticket.getResolvedAt().isBefore(ticket.getDeadline());
        }

        return !LocalDateTime.now().isBefore(ticket.getDeadline());
    }
}