package com.idleitems.school.config;

import com.idleitems.school.security.JwtUtil;
import com.idleitems.school.notification.websocket.StompAuthInterceptor;
import com.idleitems.school.notification.websocket.WebSocketHandshakeInterceptor;
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
    
    @Value("${websocket.broker-type:simple}")
    private String brokerType;
    
    @Value("${websocket.relay-host:localhost}")
    private String relayHost;
    
    @Value("${websocket.relay-port:61613}")
    private int relayPort;
    
    @Value("${websocket.relay-login:guest}")
    private String relayLogin;
    
    @Value("${websocket.relay-passcode:guest}")
    private String relayPasscode;
    
    private final JwtUtil jwtUtil;
    private final StompAuthInterceptor stompAuthInterceptor;
    private final WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;

    @Bean
    public TaskScheduler webSocketTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("WebSocket-");
        scheduler.setPoolSize(4);
        return scheduler;
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        if ("relay".equalsIgnoreCase(brokerType)) {
            config.enableStompBrokerRelay("/topic", "/queue")
                    .setRelayHost(relayHost)
                    .setRelayPort(relayPort)
                    .setClientLogin(relayLogin)
                    .setClientPasscode(relayPasscode)
                    .setSystemLogin(relayLogin)
                    .setSystemPasscode(relayPasscode);
        } else {
            config.enableSimpleBroker("/topic", "/queue")
                    .setHeartbeatValue(new long[]{4000, 4000})
                    .setTaskScheduler(webSocketTaskScheduler());
        }
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
                .addInterceptors(webSocketHandshakeInterceptor)
                .withSockJS();
        
        // 也支持原生WebSocket连接
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns(origins)
                .setAllowedOrigins(origins) // 显式设置允许的源
                .addInterceptors(webSocketHandshakeInterceptor);
    }
}
