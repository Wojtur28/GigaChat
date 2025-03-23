package org.example.gigachat.config.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface TokenProvider {
    String generateToken(UserDetails userDetails);
}
