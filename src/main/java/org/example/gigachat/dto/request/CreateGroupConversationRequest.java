package org.example.gigachat.dto.request;

import java.util.List;

public record CreateGroupConversationRequest(
        String name,
        List<String> participantEmails
) { }
