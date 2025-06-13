package org.example.gigachat.dto.response;

import java.time.Instant;

public record MessageResponse(String id,
                              String authorId,
                              String content,
                              Instant timestamp) {}
