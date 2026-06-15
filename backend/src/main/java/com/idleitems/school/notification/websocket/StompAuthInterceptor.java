package com.idleitems.school.notification.websocket;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtTokenBlacklistService blacklistService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String SESSION_TOKEN_KEY = "ws_token";

    // 跟踪已认证的用户会话：userId -> token
    private final Map<String, String> authenticatedSessions = new ConcurrentHashMap<>();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            return handleConnect(accessor, message);
        }

        // 对 SUBSCRIBE/SEND 等命令验证 Token 是否仍然有效
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) ||
            StompCommand.SEND.equals(accessor.getCommand())) {
            Principal user = accessor.getUser();
            if (user instanceof UsernamePasswordAuthenticationToken authToken) {
                String userId = (String) authToken.getPrincipal();
                String token = authenticatedSessions.get(userId);
                if (token != null && (blacklistService.isBlacklisted(token) || !jwtUtil.validateToken(token))) {
                    log.warn("WebSocket消息被拒绝，Token已失效: userId={}", userId);
                    throw new SecurityException("Token已失效，请重新登录");
                }
            }
        }

        // DISCONNECT 时清理会话记录
        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            Principal user = accessor.getUser();
            if (user instanceof UsernamePasswordAuthenticationToken authToken) {
                String userId = (String) authToken.getPrincipal();
                authenticatedSessions.remove(userId);
                log.debug("WebSocket会话已清理: userId={}", userId);
            }
        }

        return message;
    }

    private Message<?> handleConnect(StompHeaderAccessor accessor, Message<?> message) {
        String token = null;

        // 1. 优先从 Authorization 头读取
        String authHeader = getAuthHeader(accessor);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            token = authHeader.substring(BEARER_PREFIX.length());
        }

        // 2. 如果 Authorization 头没有，从 WebSocket 会话属性中读取 cookie token
        if (token == null) {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                token = (String) sessionAttributes.get("access_token");
                if (token != null) {
                    log.debug("从WebSocket会话属性中获取到access_token");
                }
            }
        }

        if (token == null) {
            log.warn("STOMP CONNECT缺少有效的认证信息（Authorization头或Cookie）");
            throw new SecurityException("STOMP连接需要有效的认证信息");
        }

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

        // 记录已认证的会话，用于后续消息验证
        authenticatedSessions.put(userIdStr, token);

        log.debug("STOMP WebSocket认证成功，用户ID: {}", userIdStr);
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
