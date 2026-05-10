package ru.tikonovns.capstone.spring.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TicketResponse {

    private Long id;
    private String ticketNumber;

    //Кнопки.
    private TicketActionsResponse availableActions;

    private Long initiatorId;
    private String initiatorUsername;
    private String initiatorFullName;

    private Long sectionId;
    private String sectionCode;
    private String sectionName;

    private Long serviceDirectionId;
    private String serviceDirectionCode;
    private String serviceDirectionName;

    private Long ticketTypeId;
    private String ticketTypeCode;
    private String ticketTypeName;

    private String status;

    private Long workGroupId;
    private String workGroupCode;
    private String workGroupName;

    private Long assigneeId;
    private String assigneeUsername;
    private String assigneeFullName;

    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime deadline;

    private Boolean slaOverdue;
}