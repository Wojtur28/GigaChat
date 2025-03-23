package org.example.gigachat.config.security.dto;


public record RegisterUser(String username,
                           String firstName,
                           String lastName,
                           String email,
                           String password) {
}
