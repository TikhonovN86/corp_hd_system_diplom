package ru.tikonovns.capstone.spring.dto.request.ticket;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferToWorkGroupRequest {

    //dispatcher - 1, executor - 2;
    @NotNull
    private Long workGroupId;
}