package org.example.gigachat.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.example.gigachat.message.ChatService;
import org.example.gigachat.message.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final Map<WebSocketSession, String> sessions = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String roomId = extractRoomId(session);
        sessions.put(session, session.getId());
        System.out.println("New WebSocket connection: " + roomId);

        Flux<String> messagesFlux = chatService.getMessagesForRoom(roomId)
                .map(this::toJson)
                .concatWith(Flux.never());

        return session.send(messagesFlux.map(session::textMessage))
                .and(session.receive().doOnNext(message -> {
                    System.out.println("Got message from room: " + message.getPayload());
                }).doFinally(signalType -> {
                    System.out.println("WebSocket is closed for room: " + roomId);
                    sessions.remove(session);
                }))
                .then();
    }

    private String extractRoomId(WebSocketSession session) {
        String query = session.getHandshakeInfo().getUri().getQuery();
        if (query != null && query.startsWith("roomId=")) {
            return query.substring(7);
        }
        return "general";
    }

    private String toJson(Message message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Błąd serializacji JSON", e);
        }
    }
}
