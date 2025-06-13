package org.example.gigachat.dto.response;

import org.example.gigachat.core.conversation.ConversationType;

import java.time.Instant;
import java.util.List;

public record ConversationResponse(
    String id,
    ConversationType type,
    List<String> participantEmails,
    String name,
    Instant createdAt,
    Instant updatedAt
) {}
