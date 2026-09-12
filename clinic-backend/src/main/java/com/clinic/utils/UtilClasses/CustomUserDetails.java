package com.clinic.utils.UtilClasses;

import com.clinic.models.User;
import com.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomUserDetails{

    private final UserRepository userRepository;

    public String getLoggedInUserFullName() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<User> user = userRepository.findByEmail(email);
        return user.map(User::getFullName)
                .orElse(email);
    }
}
