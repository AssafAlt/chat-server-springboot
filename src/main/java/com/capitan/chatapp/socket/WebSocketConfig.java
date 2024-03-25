package com.capitan.chatapp.socket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.support.ChannelInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final Map<String, String> connectedUsers = new ConcurrentHashMap<>();

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();
    }

    /*
     * @Override
     * public void configureClientInboundChannel(ChannelRegistration registration) {
     * registration.interceptors(new UserChannelInterceptor());
     * }
     * 
     * public class UserChannelInterceptor implements ChannelInterceptor {
     * 
     * @Override
     * public Message<?> preSend(Message<?> message, MessageChannel channel) {
     * StompHeaderAccessor accessor = StompHeaderAccessor.getAccessor(message,
     * StompHeaderAccessor.class);
     * if (StompCommand.CONNECT.equals(accessor.getCommand())) {
     * String userId = accessor.getFirstNativeHeader("user-id");
     * String username = accessor.getFirstNativeHeader("username");
     * System.out.println("User connected: " + username);
     * }
     * return message;
     * }
     * }
     */

}
