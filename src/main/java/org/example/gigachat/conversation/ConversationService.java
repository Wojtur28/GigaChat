package org.example.gigachat.conversation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@AllArgsConstructor
public class ConversationService {
    private final ConversationRepository conversationRepository;

    public Mono<Conversation> createConversation(Conversation conversation) {
        conversation.setId(null);
        conversation.setCreatedAt(Instant.now());
        conversation.setUpdatedAt(Instant.now());
        return conversationRepository.save(conversation);
    }

    public Mono<Conversation> updateConversation(Conversation conversation) {
        conversation.setUpdatedAt(Instant.now());
        return conversationRepository.save(conversation);
    }

    public Mono<Conversation> getConversationById(String conversationId) {
        return conversationRepository.findById(conversationId);
    }
}
