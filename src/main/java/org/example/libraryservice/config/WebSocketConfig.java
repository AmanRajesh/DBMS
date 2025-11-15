// src/main/java/com/libraryservice/config/WebSocketConfig.java
package org.example.libraryservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint for WebSocket connections
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // "/topic" for public broadcasts, "/queue" for user-specific messages
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefix for server-bound messages (e.g., from client to @MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");
        // Prefix for user-specific destinations
        registry.setUserDestinationPrefix("/user");
    }
}