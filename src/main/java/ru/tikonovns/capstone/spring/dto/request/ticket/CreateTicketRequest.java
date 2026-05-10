package ru.tikonovns.capstone.spring.dto.request.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTicketRequest {

    @NotNull
    private Long sectionId;

    @NotNull
    private Long serviceDirectionId;

    @NotBlank
    private String description;
}