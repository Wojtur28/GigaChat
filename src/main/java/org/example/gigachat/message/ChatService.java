package org.example.gigachat.message;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.util.Comparator;

@Service
@AllArgsConstructor
public class ChatService {
    private final Sinks.Many<Message> messageSink = Sinks.many().multicast().directAllOrNothing();
    private final Flux<Message> messageFlux = messageSink.asFlux().publish().autoConnect();
    private final MessageRepository messageRepository;

    public Flux<Message> getMessagesByConversation(String conversationId) {
        return messageRepository.findByConversationId(conversationId)
                .sort(Comparator.comparing(Message::getTimestamp));
    }

    public Flux<Message> getMessagesForConversation(String conversationId) {
        return messageFlux.filter(msg -> conversationId.equals(msg.getConversationId()));
    }

    public Mono<Message> sendMessage(Message message) {
        if (message.getTimestamp() == null) {
            message.setTimestamp(Instant.now());
        }
        message.setId(null);
        return messageRepository
                .save(message)
                .doOnNext(messageSink::tryEmitNext);
    }
}

