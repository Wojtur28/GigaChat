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

import java.time.Instant;
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
        String conversationId = extractConversationId(session);
        sessions.put(session, session.getId());
        System.out.println("New WebSocket connection: " + conversationId);

        Flux<String> historicalMessages = chatService.getMessagesByConversation(conversationId)
                .map(this::toJson);

        Flux<String> liveMessages = chatService.getMessagesForConversation(conversationId)
                .map(this::toJson);

        Flux<String> combinedMessages = Flux.concat(historicalMessages, liveMessages);

        return session.send(combinedMessages.map(session::textMessage))
                .and(session.receive()
                        .flatMap(webSocketMessage -> {
                            Message message = fromJson(webSocketMessage.getPayloadAsText());
                            message.setConversationId(conversationId);
                            message.setTimestamp(Instant.now());
                            return chatService.sendMessage(message);
                        })
                        .then());
    }

    private String extractConversationId(WebSocketSession session) {
        String query = session.getHandshakeInfo().getUri().getQuery();
        if (query != null && query.startsWith("conversationId=")) {
            return query.substring(7);
        }
        return "general";
    }

    private String toJson(Message message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error with serialization: ", e);
        }
    }

    private Message fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, Message.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error with deserialization: ", e);
        }
    }

}
