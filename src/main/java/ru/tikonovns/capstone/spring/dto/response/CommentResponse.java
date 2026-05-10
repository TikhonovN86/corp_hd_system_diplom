package ru.tikonovns.capstone.spring.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CommentResponse {

    private Long id;
    private Long ticketId;
    private Long authorId;
    private String authorUsername;
    private String authorFullName;
    private String content;
    private Boolean isPrivate;
    private LocalDateTime createdAt;
}