package ru.tikonovns.capstone.spring.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public boolean isAuthenticated() {
        Authentication authentication = getAuthentication();

        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public AppUserPrincipal getPrincipal() {
        Authentication authentication = getAuthentication();

        if (!isAuthenticated()) {
            throw new IllegalStateException("Пользователь не авторизован");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof AppUserPrincipal appUserPrincipal)) {
            throw new IllegalStateException("Текущий principal не является AppUserPrincipal");
        }

        return appUserPrincipal;
    }

    public Long getCurrentUserId() {
        return getPrincipal().getId();
    }

    public String getCurrentUsername() {
        return getPrincipal().getUsername();
    }

    public String getCurrentRoleCode() {
        return getPrincipal().getRoleCode();
    }

    public boolean hasRole(String roleCode) {
        return getPrincipal().getRoleCode().equals(roleCode);
    }

    public boolean isMemberOfWorkGroup(String workGroupCode) {
        return getPrincipal().isInWorkGroup(workGroupCode);
    }
}