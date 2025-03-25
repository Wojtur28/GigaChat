package org.example.gigachat.config.security;

import org.example.gigachat.config.security.dto.LoginResponse;
import org.example.gigachat.config.security.dto.LoginRequest;
import org.example.gigachat.config.security.dto.RegisterResponse;
import org.example.gigachat.config.security.dto.RegisterRequest;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<LoginResponse> login (LoginRequest loginRequest);

    Mono<RegisterResponse> register (RegisterRequest registerRequest);
}
