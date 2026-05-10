package ru.tikonovns.capstone.spring.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.tikonovns.capstone.spring.dto.response.CurrentUserResponse;
import ru.tikonovns.capstone.spring.security.AppUserPrincipal;
import ru.tikonovns.capstone.spring.security.CurrentUserService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CurrentUserController {

    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    public CurrentUserResponse me() {
        AppUserPrincipal principal = currentUserService.getPrincipal();

        return CurrentUserResponse.builder()
                .id(principal.getId())
                .username(principal.getUsername())
                .fullName(principal.getFullName())
                .email(principal.getEmail())
                .roleCode(principal.getRoleCode())
                .workGroupCodes(principal.getWorkGroupCodes())
                .build();
    }
}