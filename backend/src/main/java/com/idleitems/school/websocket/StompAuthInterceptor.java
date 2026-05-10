package com.idleitems.school.websocket;

import com.idleitems.school.security.JwtTokenBlacklistService;
import com.idleitems.school.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtTokenBlacklistService blacklistService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = getAuthHeader(accessor);

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                log.warn("STOMP CONNECT缺少有效的Authorization头");
                throw new SecurityException("STOMP连接需要有效的Bearer Token");
            }

            String token = authHeader.substring(BEARER_PREFIX.length());

            if (blacklistService.isBlacklisted(token)) {
                log.warn("STOMP CONNECT使用了已失效的Token");
                throw new SecurityException("Token已失效，请重新登录");
            }

            if (!jwtUtil.validateToken(token)) {
                log.warn("STOMP CONNECT使用了无效的Token");
                throw new SecurityException("无效的认证Token");
            }

            String userIdStr = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority(role != null ? role : "USER")
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userIdStr, null, authorities);
            accessor.setUser(authentication);

            log.debug("STOMP WebSocket认证成功，用户ID: {}", userIdStr);
        }

        return message;
    }

    private String getAuthHeader(StompHeaderAccessor accessor) {
        List<String> authValues = accessor.getNativeHeader(AUTHORIZATION_HEADER);
        if (authValues != null && !authValues.isEmpty()) {
            return authValues.get(0);
        }

        return null;
    }
}
