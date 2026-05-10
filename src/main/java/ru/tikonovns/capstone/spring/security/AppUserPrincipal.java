package ru.tikonovns.capstone.spring.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.tikonovns.capstone.spring.entity.User;
import ru.tikonovns.capstone.spring.entity.UserWorkGroup;
import ru.tikonovns.capstone.spring.entity.WorkGroup;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class AppUserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String middleName;
    private final String roleCode;
    private final Set<String> workGroupCodes;

    private final Collection<? extends GrantedAuthority> authorities;

    public static AppUserPrincipal fromUser(User user) {

        String roleCode = user.getRole().getCode();

        Set<String> workGroupCodes = user.getUserWorkGroups()
                .stream()
                .map(UserWorkGroup::getWorkGroup)
                .filter(Objects::nonNull)
                .map(WorkGroup::getCode)
                .collect(Collectors.toSet());

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(roleCode)
        );

        return new AppUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getMiddleName(),
                roleCode,
                workGroupCodes,
                authorities
        );
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (lastName != null) {
            sb.append(lastName);
        }
        if (firstName != null) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append(firstName);
        }
        if (middleName != null && !middleName.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append(middleName);
        }
        return sb.toString();
    }

    public boolean isInWorkGroup(String workGroupCode) {
        return workGroupCodes.contains(workGroupCode);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
