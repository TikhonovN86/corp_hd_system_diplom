package ru.tikonovns.capstone.spring.dto.response.dictionary;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDictionaryResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
}