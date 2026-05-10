package ru.tikonovns.capstone.spring.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TicketActionsResponse {

    private boolean canTakeInWork;
    private boolean canResolve;
    private boolean canReturnToDispatchers;
    private boolean canAssignEmployee;
    private boolean canChangeTicketType;
    private boolean canAddPublicComment;
    private boolean canAddPrivateComment;
}