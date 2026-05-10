package ru.tikonovns.capstone.spring.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
public class CurrentUserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String roleCode;
    private Set<String> workGroupCodes;
}