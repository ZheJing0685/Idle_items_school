package com.idleitems.school.config;

import com.idleitems.school.security.JwtUtil;
import com.idleitems.school.websocket.StompAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.Arrays;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;
    
    @Value("${websocket.max-message-size:131072}")
    private int maxMessageSize;
    
    @Value("${websocket.max-buffer-size:524288}")
    private int maxBufferSize;
    
    private final JwtUtil jwtUtil;
    private final StompAuthInterceptor stompAuthInterceptor;

    @Bean
    public TaskScheduler webSocketTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("WebSocket-");
        scheduler.setPoolSize(4);
        return scheduler;
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{4000, 4000}) // 心跳间隔4秒
                .setTaskScheduler(webSocketTaskScheduler());
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthInterceptor);
    }
    
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(maxMessageSize);
        registry.setSendBufferSizeLimit(maxBufferSize);
        registry.setSendTimeLimit(20 * 1000); // 20秒
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        
        // 同时支持SockJS和原生WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(origins)
                .setAllowedOrigins(origins) // 显式设置允许的源
                .withSockJS();
        
        // 也支持原生WebSocket连接
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns(origins)
                .setAllowedOrigins(origins); // 显式设置允许的源
    }
}
