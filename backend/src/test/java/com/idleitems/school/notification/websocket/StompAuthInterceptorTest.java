package com.idleitems.school.notification.websocket;

import com.idleitems.school.security.JwtTokenBlacklistService;
import com.idleitems.school.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StompAuthInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JwtTokenBlacklistService blacklistService;

    @InjectMocks
    private StompAuthInterceptor interceptor;

    private MessageChannel messageChannel;

    @BeforeEach
    void setUp() {
        messageChannel = mock(MessageChannel.class);
    }

    @Test
    void preSend_CONNECT_WithAuthorizationHeader_SetsAuthentication() {
        StompHeaderAccessor accessor = mock(StompHeaderAccessor.class);
        when(accessor.getCommand()).thenReturn(StompCommand.CONNECT);
        List<String> authHeader = Collections.singletonList("Bearer valid-token");
        when(accessor.getNativeHeader("Authorization")).thenReturn(authHeader);

        Message<?> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(mock(MessageHeaders.class));

        when(blacklistService.isBlacklisted("valid-token")).thenReturn(false);
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid-token")).thenReturn("1");
        when(jwtUtil.getRoleFromToken("valid-token")).thenReturn("USER");

        try (var mocked = mockStatic(MessageHeaderAccessor.class)) {
            mocked.when(() -> MessageHeaderAccessor.getAccessor(any(Message.class), eq(StompHeaderAccessor.class)))
                    .thenReturn(accessor);

            Message<?> result = interceptor.preSend(message, messageChannel);

            assertNotNull(result);
            verify(accessor, times(1)).setUser(any(UsernamePasswordAuthenticationToken.class));
            verify(blacklistService, times(1)).isBlacklisted("valid-token");
            verify(jwtUtil, times(1)).validateToken("valid-token");
            verify(jwtUtil, times(1)).getUserIdFromToken("valid-token");
            verify(jwtUtil, times(1)).getRoleFromToken("valid-token");
        }
    }

    @Test
    void preSend_CONNECT_WithSessionAttributeToken_SetsAuthentication() {
        StompHeaderAccessor accessor = mock(StompHeaderAccessor.class);
        when(accessor.getCommand()).thenReturn(StompCommand.CONNECT);
        Map<String, Object> sessionAttributes = new ConcurrentHashMap<>();
        sessionAttributes.put("access_token", "session-token");
        when(accessor.getSessionAttributes()).thenReturn(sessionAttributes);

        Message<?> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(mock(MessageHeaders.class));

        when(blacklistService.isBlacklisted("session-token")).thenReturn(false);
        when(jwtUtil.validateToken("session-token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("session-token")).thenReturn("2");
        when(jwtUtil.getRoleFromToken("session-token")).thenReturn("USER");

        try (var mocked = mockStatic(MessageHeaderAccessor.class)) {
            mocked.when(() -> MessageHeaderAccessor.getAccessor(any(Message.class), eq(StompHeaderAccessor.class)))
                    .thenReturn(accessor);

            Message<?> result = interceptor.preSend(message, messageChannel);

            assertNotNull(result);
            verify(accessor, times(1)).setUser(any(UsernamePasswordAuthenticationToken.class));
            verify(blacklistService, times(1)).isBlacklisted("session-token");
            verify(jwtUtil, times(1)).validateToken("session-token");
            verify(jwtUtil, times(1)).getUserIdFromToken("session-token");
        }
    }

    @Test
    void preSend_CONNECT_MissingToken_ThrowsSecurityException() {
        StompHeaderAccessor accessor = mock(StompHeaderAccessor.class);
        when(accessor.getCommand()).thenReturn(StompCommand.CONNECT);

        Message<?> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(mock(MessageHeaders.class));

        try (var mocked = mockStatic(MessageHeaderAccessor.class)) {
            mocked.when(() -> MessageHeaderAccessor.getAccessor(any(Message.class), eq(StompHeaderAccessor.class)))
                    .thenReturn(accessor);

            SecurityException ex = assertThrows(SecurityException.class,
                    () -> interceptor.preSend(message, messageChannel));
            assertTrue(ex.getMessage().contains("需要有效的认证信息"));
        }
    }

    @Test
    void preSend_CONNECT_BlacklistedToken_ThrowsSecurityException() {
        StompHeaderAccessor accessor = mock(StompHeaderAccessor.class);
        when(accessor.getCommand()).thenReturn(StompCommand.CONNECT);
        List<String> authHeader = Collections.singletonList("Bearer blacklisted-token");
        when(accessor.getNativeHeader("Authorization")).thenReturn(authHeader);

        Message<?> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(mock(MessageHeaders.class));

        when(blacklistService.isBlacklisted("blacklisted-token")).thenReturn(true);

        try (var mocked = mockStatic(MessageHeaderAccessor.class)) {
            mocked.when(() -> MessageHeaderAccessor.getAccessor(any(Message.class), eq(StompHeaderAccessor.class)))
                    .thenReturn(accessor);

            SecurityException ex = assertThrows(SecurityException.class,
                    () -> interceptor.preSend(message, messageChannel));
            assertTrue(ex.getMessage().contains("Token已失效"));
        }
    }

    @Test
    void preSend_CONNECT_InvalidToken_ThrowsSecurityException() {
        StompHeaderAccessor accessor = mock(StompHeaderAccessor.class);
        when(accessor.getCommand()).thenReturn(StompCommand.CONNECT);
        List<String> authHeader = Collections.singletonList("Bearer invalid-token");
        when(accessor.getNativeHeader("Authorization")).thenReturn(authHeader);

        Message<?> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(mock(MessageHeaders.class));

        when(blacklistService.isBlacklisted("invalid-token")).thenReturn(false);
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        try (var mocked = mockStatic(MessageHeaderAccessor.class)) {
            mocked.when(() -> MessageHeaderAccessor.getAccessor(any(Message.class), eq(StompHeaderAccessor.class)))
                    .thenReturn(accessor);

            SecurityException ex = assertThrows(SecurityException.class,
                    () -> interceptor.preSend(message, messageChannel));
            assertTrue(ex.getMessage().contains("无效的认证Token"));
        }
    }

    @Test
    void preSend_SUBSCRIBE_ValidToken_ReturnsMessage() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "1", null, Collections.singletonList(new SimpleGrantedAuthority("USER")));

        Map<String, String> sessions = new ConcurrentHashMap<>();
        sessions.put("1", "valid-token");
        ReflectionTestUtils.setField(interceptor, "authenticatedSessions", sessions);

        StompHeaderAccessor accessor = mock(StompHeaderAccessor.class);
        when(accessor.getCommand()).thenReturn(StompCommand.SUBSCRIBE);
        when(accessor.getUser()).thenReturn(auth);

        Message<?> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(mock(MessageHeaders.class));

        when(blacklistService.isBlacklisted("valid-token")).thenReturn(false);
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);

        try (var mocked = mockStatic(MessageHeaderAccessor.class)) {
            mocked.when(() -> MessageHeaderAccessor.getAccessor(any(Message.class), eq(StompHeaderAccessor.class)))
                    .thenReturn(accessor);

            Message<?> result = interceptor.preSend(message, messageChannel);

            assertNotNull(result);
        }
    }

    @Test
    void preSend_SUBSCRIBE_BlacklistedToken_ThrowsSecurityException() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "1", null, Collections.singletonList(new SimpleGrantedAuthority("USER")));

        Map<String, String> sessions = new ConcurrentHashMap<>();
        sessions.put("1", "expired-token");
        ReflectionTestUtils.setField(interceptor, "authenticatedSessions", sessions);

        StompHeaderAccessor accessor = mock(StompHeaderAccessor.class);
        when(accessor.getCommand()).thenReturn(StompCommand.SUBSCRIBE);
        when(accessor.getUser()).thenReturn(auth);

        Message<?> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(mock(MessageHeaders.class));

        when(blacklistService.isBlacklisted("expired-token")).thenReturn(true);

        try (var mocked = mockStatic(MessageHeaderAccessor.class)) {
            mocked.when(() -> MessageHeaderAccessor.getAccessor(any(Message.class), eq(StompHeaderAccessor.class)))
                    .thenReturn(accessor);

            SecurityException ex = assertThrows(SecurityException.class,
                    () -> interceptor.preSend(message, messageChannel));
            assertTrue(ex.getMessage().contains("Token已失效"));
        }
    }

    @Test
    void preSend_DISCONNECT_CleansUpSession() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "1", null, Collections.singletonList(new SimpleGrantedAuthority("USER")));

        Map<String, String> sessions = new ConcurrentHashMap<>();
        sessions.put("1", "valid-token");
        ReflectionTestUtils.setField(interceptor, "authenticatedSessions", sessions);

        StompHeaderAccessor accessor = mock(StompHeaderAccessor.class);
        when(accessor.getCommand()).thenReturn(StompCommand.DISCONNECT);
        when(accessor.getUser()).thenReturn(auth);

        Message<?> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(mock(MessageHeaders.class));

        try (var mocked = mockStatic(MessageHeaderAccessor.class)) {
            mocked.when(() -> MessageHeaderAccessor.getAccessor(any(Message.class), eq(StompHeaderAccessor.class)))
                    .thenReturn(accessor);

            Message<?> result = interceptor.preSend(message, messageChannel);

            assertNotNull(result);
        }
    }

    @Test
    void preSend_NullAccessor_ReturnsMessage() {
        Message<?> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(mock(MessageHeaders.class));

        try (var mocked = mockStatic(MessageHeaderAccessor.class)) {
            mocked.when(() -> MessageHeaderAccessor.getAccessor(any(Message.class), eq(StompHeaderAccessor.class)))
                    .thenReturn(null);

            Message<?> result = interceptor.preSend(message, messageChannel);

            assertSame(message, result);
        }
    }
}
