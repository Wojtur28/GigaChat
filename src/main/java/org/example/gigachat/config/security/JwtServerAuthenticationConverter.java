package org.example.gigachat.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {

    private final JwtService jwtService;
    private static final String BEARER = "Bearer ";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith(BEARER))
                .map(header -> header.substring(BEARER.length()))
                .flatMap(token -> createUserDetails(token)
                        .map(userDetails -> new JwtToken(token, userDetails))
                );
    }

    private Mono<UserDetails> createUserDetails(String token) {
        return jwtService.extractUsername(token)
                .zipWith(jwtService.extractRoles(token))
                .map(tuple -> User.builder()
                        .username(tuple.getT1())
                        .authorities(tuple.getT2().stream().map(SimpleGrantedAuthority::new).toList())
                        .password("")
                        .build()
                );
    }
}
