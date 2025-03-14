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
    private final Map<String, Sinks.Many<Message>> roomSinks = new ConcurrentHashMap<>();

    public Flux<Message> getMessagesByRoom(String roomId) {
        return messageRepository.findByRoomId(roomId);
    }

    public Mono<Message> sendMessage(Message message) {
        message.setId(null);
        message.setTimestamp(Instant.now());

        return messageRepository.save(message)
                .doOnSuccess(savedMessage -> getRoomSink(savedMessage.getRoomId()).tryEmitNext(savedMessage));
    }

    public Flux<Message> getMessagesForRoom(String roomId) {
        return getRoomSink(roomId).asFlux();
    }

    private Sinks.Many<Message> getRoomSink(String roomId) {
        return roomSinks.computeIfAbsent(roomId, k -> Sinks.many().multicast().onBackpressureBuffer());
    }
}
