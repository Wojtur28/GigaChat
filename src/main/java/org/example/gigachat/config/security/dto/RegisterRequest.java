package org.example.gigachat.config.security.dto;


public record RegisterRequest(String username,
                              String firstName,
                              String lastName,
                              String email,
                              String password) {
}
