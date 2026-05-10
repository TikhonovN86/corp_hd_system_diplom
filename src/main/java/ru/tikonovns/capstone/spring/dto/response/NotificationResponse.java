package ru.tikonovns.capstone.spring.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NotificationResponse {

    private Long id;

    private Long ticketId;
    private String ticketNumber;

    private String title;
    private String message;

    private Boolean isRead;
    private LocalDateTime createdAt;
}