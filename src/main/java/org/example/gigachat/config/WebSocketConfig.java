package org.example.gigachat.config;

import lombok.RequiredArgsConstructor;
import org.example.gigachat.handler.ChatWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class WebSocketConfig {

    private final ChatWebSocketHandler chatWebSocketHandler;

    @Bean
    public SimpleUrlHandlerMapping webSocketHandlerMapping() {
        return new SimpleUrlHandlerMapping(Map.of("/ws/chat", chatWebSocketHandler), -1);
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
