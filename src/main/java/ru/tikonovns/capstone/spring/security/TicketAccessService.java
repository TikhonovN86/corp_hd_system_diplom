package ru.tikonovns.capstone.spring.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tikonovns.capstone.spring.entity.Ticket;

import static ru.tikonovns.capstone.spring.utils.constants.TicketStatus.*;
import static ru.tikonovns.capstone.spring.utils.constants.UserRole.*;
import static ru.tikonovns.capstone.spring.utils.constants.WorkGroupCode.*;

@Service
@RequiredArgsConstructor
public class TicketAccessService {

    private final CurrentUserService currentUserService;

    public boolean canView(Ticket ticket) {
        String roleCode = currentUserService.getCurrentRoleCode();
        Long currentUserId = currentUserService.getCurrentUserId();

        if (ROLE_INITIATOR.equals(roleCode)) {
            return ticket.getInitiator() != null
                    && ticket.getInitiator().getId().equals(currentUserId);
        }

        if (ROLE_DISPATCHER.equals(roleCode)) {
            return ticket.getWorkGroup() != null
                    && DISPATCHER_GROUP_CODE.equals(ticket.getWorkGroup().getCode())
                    && currentUserService.isMemberOfWorkGroup(DISPATCHER_GROUP_CODE);
        }

        if (ROLE_EXECUTOR.equals(roleCode)) {
            return ticket.getWorkGroup() != null
                    && currentUserService.isMemberOfWorkGroup(ticket.getWorkGroup().getCode());
        }

        return false;
    }

    public boolean canEdit(Ticket ticket) {
        return !isResolved(ticket) && canView(ticket);
    }

    public boolean canCreateTicket() {
        return ROLE_INITIATOR.equals(currentUserService.getCurrentRoleCode());
    }

    public boolean canChangeWorkGroup(Ticket ticket) {
        return !isResolved(ticket)
                && ROLE_DISPATCHER.equals(currentUserService.getCurrentRoleCode())
                && canView(ticket);
    }

    public boolean canAssignEmployee(Ticket ticket) {
        if (isResolved(ticket)) {
            return false;
        }

        String roleCode = currentUserService.getCurrentRoleCode();
        return (ROLE_DISPATCHER.equals(roleCode) || ROLE_EXECUTOR.equals(roleCode))
                && canView(ticket);
    }

    public boolean canChangeTicketType(Ticket ticket) {
        if (isResolved(ticket)) {
            return false;
        }

        String roleCode = currentUserService.getCurrentRoleCode();
        return (ROLE_DISPATCHER.equals(roleCode) || ROLE_EXECUTOR.equals(roleCode))
                && canView(ticket);
    }

    public boolean canAddPublicComment(Ticket ticket) {
        return !isResolved(ticket) && canView(ticket);
    }

    public boolean canAddPrivateComment(Ticket ticket) {
        if (isResolved(ticket)) {
            return false;
        }

        String roleCode = currentUserService.getCurrentRoleCode();
        return (ROLE_DISPATCHER.equals(roleCode) || ROLE_EXECUTOR.equals(roleCode))
                && canView(ticket);
    }

    public boolean canTakeInWork(Ticket ticket) {
        if (isResolved(ticket)) {
            return false;
        }

        String roleCode = currentUserService.getCurrentRoleCode();

        if (ROLE_DISPATCHER.equals(roleCode)) {
            return ticket.getWorkGroup() != null
                    && DISPATCHER_GROUP_CODE.equals(ticket.getWorkGroup().getCode())
                    && (STATUS_NEW.equals(ticket.getStatus()) || STATUS_UNASSIGNED.equals(ticket.getStatus()))
                    && canView(ticket);
        }

        if (ROLE_EXECUTOR.equals(roleCode)) {
            return ticket.getWorkGroup() != null
                    && currentUserService.isMemberOfWorkGroup(ticket.getWorkGroup().getCode())
                    && STATUS_NEW.equals(ticket.getStatus())
                    && canView(ticket);
        }

        return false;
    }

    public boolean canReturnToDispatchers(Ticket ticket) {
        return !isResolved(ticket)
                && ROLE_EXECUTOR.equals(currentUserService.getCurrentRoleCode())
                && canView(ticket);
    }

    public boolean canResolve(Ticket ticket) {
        if (isResolved(ticket)) {
            return false;
        }

        String roleCode = currentUserService.getCurrentRoleCode();
        return (ROLE_DISPATCHER.equals(roleCode) || ROLE_EXECUTOR.equals(roleCode))
                && canView(ticket);
    }

    public void checkCanView(Ticket ticket) {
        if (!canView(ticket)) {
            throw new SecurityException("Нет прав на просмотр обращения");
        }
    }

    public void checkCanEdit(Ticket ticket) {
        if (!canEdit(ticket)) {
            throw new SecurityException("Нет прав на редактирование обращения");
        }
    }

    public void checkCanCreateTicket() {
        if (!canCreateTicket()) {
            throw new SecurityException("Нет прав на создание обращения");
        }
    }


    public void checkCanChangeWorkGroup(Ticket ticket) {
        if (!canChangeWorkGroup(ticket)) {
            throw new SecurityException("Нет прав на изменение рабочей группы");
        }
    }

    public void checkCanAssignEmployee(Ticket ticket) {
        if (!canAssignEmployee(ticket)) {
            throw new SecurityException("Нет прав на назначение ответственного сотрудника");
        }
    }

    public void checkCanChangeTicketType(Ticket ticket) {
        if (!canChangeTicketType(ticket)) {
            throw new SecurityException("Нет прав на изменение типа обращения");
        }
    }

    public void checkCanAddPublicComment(Ticket ticket) {
        if (!canAddPublicComment(ticket)) {
            throw new SecurityException("Нет прав на добавление комментария");
        }
    }

    public void checkCanAddPrivateComment(Ticket ticket) {
        if (!canAddPrivateComment(ticket)) {
            throw new SecurityException("Нет прав на добавление приватного комментария");
        }
    }

    public void checkCanTakeInWork(Ticket ticket) {
        if (!canTakeInWork(ticket)) {
            throw new SecurityException("Нет прав на перевод обращения в работу");
        }
    }

    public void checkCanReturnToDispatchers(Ticket ticket) {
        if (!canReturnToDispatchers(ticket)) {
            throw new SecurityException("Нет прав на возврат обращения диспетчерам");
        }
    }

    public void checkCanResolve(Ticket ticket) {
        if (!canResolve(ticket)) {
            throw new SecurityException("Нет прав на закрытие обращения");
        }
    }

    private boolean isResolved(Ticket ticket) {
        return STATUS_RESOLVED.equals(ticket.getStatus());
    }
}