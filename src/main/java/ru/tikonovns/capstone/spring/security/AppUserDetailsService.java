package ru.tikonovns.capstone.spring.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import ru.tikonovns.capstone.spring.entity.User;
import ru.tikonovns.capstone.spring.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameWithRoleAndGroups(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Пользователь с username '" + username + "' не найден"
                ));

        return AppUserPrincipal.fromUser(user);
    }
}