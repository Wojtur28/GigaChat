package org.example.gigachat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.gigachat.config.security.JwtService;
import org.example.gigachat.core.message.MessageService;
import org.example.gigachat.core.user.UserRepository;
import org.example.gigachat.dto.response.MessageResponse;
import org.example.gigachat.dto.request.SendMessageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final MessageService messageService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String conversationId = extractConversationId(session);
        String token = extractToken(session);

        if (conversationId == null || token == null) {
            log.warn("Missing conversationId or token, closing session");
            return session.close();
        }

        log.info("WebSocket connected: conversationId={}, token=***", conversationId);

        Flux<MessageResponse> historical = messageService.getMessagesByConversation(conversationId);
        Flux<MessageResponse> live = messageService.getMessagesForConversation(conversationId);
        Flux<MessageResponse> outgoingMessages = Flux.concat(historical, live);

        Flux<String> outgoingJson = outgoingMessages.map(this::toJson);

        Flux<?> incoming = session.receive()
                .doOnNext(msg -> log.info("Received WS message: {}", msg.getPayloadAsText()))
                .flatMap(wsMsg -> {
                    try {
                        SendMessageRequest req = fromJson(wsMsg.getPayloadAsText());
                        return jwtService.extractUsername(token)
                                .flatMap(userEmail -> userRepository.findByEmail(userEmail))
                                .map(user -> User.builder()
                                        .username(user.getEmail())
                                        .password("")
                                        .authorities(List.of())
                                        .build())
                                .map(userDetails -> new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()))
                                .map(auth -> new SecurityContextImpl(auth))
                                .flatMap(securityContext ->
                                        messageService.sendMessage(req, conversationId)
                                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                                )
                                .onErrorResume(e -> {
                                    log.warn("Failed to send message: {}", e.getMessage());
                                    return Mono.empty();
                                });
                    } catch (Exception e) {
                        log.warn("Failed to parse incoming WebSocket message", e);
                        return Mono.empty();
                    }
                })
                .doOnError(e -> log.error("WebSocket error: ", e));

        return session.send(outgoingJson.map(session::textMessage))
                .and(incoming.then())
                .doOnTerminate(() -> log.info("WebSocket session terminated"));
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
        return null;
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

    private String toJson(MessageResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Failed to serialize MessageResponse", e);
            return "{}";
        }
    }

    private SendMessageRequest fromJson(String payload) {
        try {
            return objectMapper.readValue(payload, SendMessageRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid input", e);
        }
    }
}
