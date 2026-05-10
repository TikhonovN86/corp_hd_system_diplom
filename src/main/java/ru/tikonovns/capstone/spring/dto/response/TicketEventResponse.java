package ru.tikonovns.capstone.spring.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TicketEventResponse {

    private Long id;
    private Long ticketId;

    private Long authorId;
    private String authorUsername;
    private String authorFullName;

    private String eventType;
    private String eventMessage;
    private LocalDateTime createdAt;
}