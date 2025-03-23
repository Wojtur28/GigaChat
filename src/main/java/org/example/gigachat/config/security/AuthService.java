package org.example.gigachat.config.security;

import org.example.gigachat.config.security.dto.LoginResponse;
import org.example.gigachat.config.security.dto.LoginUser;
import org.example.gigachat.config.security.dto.RegisterResponse;
import org.example.gigachat.config.security.dto.RegisterUser;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<LoginResponse> login (LoginUser loginUser);

    Mono<RegisterResponse> register (RegisterUser registerUser);
}
