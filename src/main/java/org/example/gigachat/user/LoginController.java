package org.example.gigachat.user;

import lombok.RequiredArgsConstructor;
import org.example.gigachat.config.security.AuthServiceImpl;
import org.example.gigachat.config.security.dto.LoginResponse;
import org.example.gigachat.config.security.dto.LoginUser;
import org.example.gigachat.config.security.dto.RegisterResponse;
import org.example.gigachat.config.security.dto.RegisterUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final AuthServiceImpl authService;

    @PostMapping("/login")
    Mono<LoginResponse> login(@RequestBody LoginUser loginUser) {
        return authService.login(loginUser);
    }

    @PostMapping("/register")
    Mono<RegisterResponse> register(@RequestBody RegisterUser registerUser) {
        return authService.register(registerUser);
    }
}
