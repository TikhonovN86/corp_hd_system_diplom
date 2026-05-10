package ru.tikonovns.capstone.spring.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddPrivateCommentRequest {

    @NotBlank
    private String content;
}