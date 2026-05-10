package ru.tikonovns.capstone.spring.dto.request.ticket;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeTicketTypeRequest {

    @NotNull
    private Long ticketTypeId;
}