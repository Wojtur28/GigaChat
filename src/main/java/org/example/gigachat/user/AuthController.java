package org.example.gigachat.user;

import lombok.RequiredArgsConstructor;
import org.example.gigachat.config.security.AuthServiceImpl;
import org.example.gigachat.config.security.dto.LoginResponse;
import org.example.gigachat.config.security.dto.LoginRequest;
import org.example.gigachat.config.security.dto.RegisterResponse;
import org.example.gigachat.config.security.dto.RegisterRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/login")
    Mono<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/register")
    Mono<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }
}
