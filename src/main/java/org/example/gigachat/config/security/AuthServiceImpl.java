package org.example.gigachat.config.security;

import lombok.AllArgsConstructor;
import org.example.gigachat.config.security.dto.LoginResponse;
import org.example.gigachat.config.security.dto.LoginUser;
import org.example.gigachat.config.security.dto.RegisterResponse;
import org.example.gigachat.config.security.dto.RegisterUser;
import org.example.gigachat.user.Role;
import org.example.gigachat.user.User;
import org.example.gigachat.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;

@AllArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public Mono<LoginResponse> login(LoginUser loginUser) {
        return userRepository.findByEmail(loginUser.email())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found with email: " + loginUser.email())))
                .filter(u -> passwordEncoder.matches(loginUser.password(), u.getPassword()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid email or password")))
                .map(u -> new LoginResponse(jwtService.generateToken(u)));
    }

    @Override
    public Mono<RegisterResponse> register(RegisterUser registerUser) {
        return userRepository.findByEmail(registerUser.email())
                .flatMap(existing -> Mono.<RegisterResponse>error(new IllegalArgumentException("User already exists with this email")))
                .switchIfEmpty(Mono.defer(() -> {
                    String encodedPassword = passwordEncoder.encode(registerUser.password());
                    User newUser = new User();
                    newUser.setEmail(registerUser.email());
                    newUser.setPassword(encodedPassword);
                    newUser.setRoles(Collections.singleton(Role.ROLE_USER));
                    return userRepository.save(newUser)
                            .map(savedUser -> new RegisterResponse("User registered successfully"));
                }));
    }

}
