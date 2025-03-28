package org.example.gigachat.message;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class ChatService {
    private final MessageRepository messageRepository;
    private final Map<String, Sinks.Many<Message>> conversationSinks = new ConcurrentHashMap<>();

    public Mono<Message> sendMessage(Message message) {
        message.setId(null);
        message.setTimestamp(Instant.now());
        return messageRepository.save(message)
                .doOnSuccess(savedMessage -> getConversationSink(savedMessage.getConversationId()).tryEmitNext(savedMessage));
    }

    public Flux<Message> getMessagesForConversation(String conversationId) {
        return getConversationSink(conversationId).asFlux();
    }

    private Sinks.Many<Message> getConversationSink(String conversationId) {
        return conversationSinks.computeIfAbsent(conversationId, k -> Sinks.many().multicast().onBackpressureBuffer());
    }

    public Flux<Message> getMessagesByConversation(String conversationId) {
        return messageRepository.findByConversationId(conversationId);
    }

}
