package org.example.gigachat.dto.request;

import org.example.gigachat.core.conversation.ConversationType;

import java.util.List;

public record ConversationRequest(
    ConversationType type,
    List<String> participantEmails,
    String name
) {}
