package ru.tikonovns.capstone.spring.dto.request.ticket;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResolveTicketRequest {

    @NotBlank
    private String publicComment;
}