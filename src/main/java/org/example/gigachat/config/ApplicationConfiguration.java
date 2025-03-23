package org.example.gigachat.config;

import lombok.AllArgsConstructor;
import org.example.gigachat.exception.UserNotFoundException;
import org.example.gigachat.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import reactor.core.publisher.Mono;

@Configuration
@AllArgsConstructor
public class ApplicationConfiguration {
    private final UserRepository userRepository;

    /*
    TODO: Implement the AuditorAware interface
    @Bean
    public AuditorAware<User> auditorProvider() {
        return new AuditorAwareImpl();
    }*/

    @Bean
    ReactiveUserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email)
                .map(user -> (UserDetails) user)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with email: " + email)));
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

