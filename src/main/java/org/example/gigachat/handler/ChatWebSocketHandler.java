package org.example.gigachat.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.example.gigachat.config.security.JwtService;
import org.example.gigachat.message.ChatService;
import org.example.gigachat.message.Message;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ChatService chatService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String conversationId = extractConversationId(session);
        Flux<Message> historical = chatService.getMessagesByConversation(conversationId);
        Flux<Message> live = chatService.getMessagesForConversation(conversationId);
        Flux<Message> combined = Flux.concat(historical, live);
        Mono<Void> output = session.send(combined.map(msg -> session.textMessage(toJson(msg))));
        Mono<Void> input = session.receive()
                .flatMap(webSocketMessage -> {
                    Message incoming = fromJson(webSocketMessage.getPayloadAsText());
                    incoming.setConversationId(conversationId);
                    incoming.setTimestamp(Instant.now());
                    String token = extractToken(session);
                    if (token != null) {
                        String username = jwtService.extractUsername(token);
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                username, null, Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                    return chatService.sendMessage(incoming);
                })
                .then();
        return output.and(input);
    }

    private String extractConversationId(WebSocketSession session) {
        String query = session.getHandshakeInfo().getUri().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("conversationId=")) {
                    return param.substring("conversationId=".length());
                }
            }
        }
        return "general";
    }

    private String extractToken(WebSocketSession session) {
        String query = session.getHandshakeInfo().getUri().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    return param.substring("token=".length());
                }
            }
        }
        return null;
    }

    private String toJson(Message message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing message", e);
        }
    }

    private Message fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, Message.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing message", e);
        }
    }
}
