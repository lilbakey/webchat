package com.bakeev.website.config;

import com.bakeev.website.jwt.JwtHandshakeHandler;
import com.bakeev.website.jwt.JwtHandshakeInterceptor;
import com.bakeev.website.service.JwtService;
import com.bakeev.website.service.OnlineUserService;
import com.bakeev.website.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final UserService userService;

    public WebSocketConfig(JwtService jwtService,
                           UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new JwtHandshakeHandler(jwtService))
                .addInterceptors(new JwtHandshakeInterceptor(jwtService))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/user", "/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    List<String> authHeader = accessor.getNativeHeader("Authorization");
                    String token = null;
                    if (authHeader != null && !authHeader.isEmpty()) {
                        token = authHeader.get(0).replace("Bearer ", "");
                    }
                    if (token != null) {
                        try {
                            String username = jwtService.extractUserName(token);
                            UserDetails userDetails = userService
                                    .userDetailsService()
                                    .loadUserByUsername(username);
                            if (username != null && jwtService.isTokenValid(token, userDetails)) {
                                var auth = new UsernamePasswordAuthenticationToken(
                                        username, null, List.of()
                                );
                                accessor.setUser(auth);
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid token in websocket connection: " + e.getMessage());
                        }
                    }
                }
                return message;
            }
        });
    }
}
