package com.idleitems.school.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private String base64Secret;

    private static final long EXPIRATION_MS = 3600000L;
    private static final long REFRESH_EXPIRATION_MS = 604800000L;

    @BeforeEach
    void setUp() {
        byte[] keyBytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            keyBytes[i] = (byte) i;
        }
        base64Secret = Base64.getEncoder().encodeToString(keyBytes);

        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", base64Secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION_MS);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", REFRESH_EXPIRATION_MS);
        jwtUtil.validateSecretKey();
    }

    @Test
    void generateToken_WithSubject_ReturnsValidToken() {
        String token = jwtUtil.generateToken("user123");
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void generateToken_WithSubjectAndClaims_ReturnsTokenWithClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        claims.put("username", "testuser");

        String token = jwtUtil.generateToken("user123", claims);

        assertNotNull(token);
        assertEquals("user123", jwtUtil.getUserIdFromToken(token));
        assertEquals("ADMIN", jwtUtil.getRoleFromToken(token));
        assertEquals("testuser", jwtUtil.getUsernameFromToken(token));
    }

    @Test
    void generateRefreshToken_ReturnsValidToken() {
        String token = jwtUtil.generateRefreshToken("user123");
        assertNotNull(token);
        assertTrue(jwtUtil.validateRefreshToken(token));
    }

    @Test
    void getUserIdFromToken_ReturnsSubject() {
        String token = jwtUtil.generateToken("user456");
        assertEquals("user456", jwtUtil.getUserIdFromToken(token));
    }

    @Test
    void getRoleFromToken_ReturnsRole() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "SELLER");
        String token = jwtUtil.generateToken("user789", claims);

        assertEquals("SELLER", jwtUtil.getRoleFromToken(token));
    }

    @Test
    void getRoleFromToken_WhenNoRole_ReturnsNull() {
        String token = jwtUtil.generateToken("user789");
        assertNull(jwtUtil.getRoleFromToken(token));
    }

    @Test
    void getUsernameFromToken_ReturnsUsername() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", "johndoe");
        String token = jwtUtil.generateToken("user1", claims);

        assertEquals("johndoe", jwtUtil.getUsernameFromToken(token));
    }

    @Test
    void getSubject_ReturnsSubject() {
        String token = jwtUtil.generateToken("sub123");
        assertEquals("sub123", jwtUtil.getSubject(token));
    }

    @Test
    void validateToken_WithValidToken_ReturnsTrue() {
        String token = jwtUtil.generateToken("user123");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_WithExpiredToken_ReturnsFalse() {
        Date past = new Date(System.currentTimeMillis() - 100000);
        String expiredToken = Jwts.builder()
                .subject("user123")
                .issuedAt(new Date(System.currentTimeMillis() - 200000))
                .expiration(past)
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret)))
                .compact();

        assertFalse(jwtUtil.validateToken(expiredToken));
    }

    @Test
    void validateToken_WithTamperedToken_ReturnsFalse() {
        String token = jwtUtil.generateToken("user123");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertFalse(jwtUtil.validateToken(tampered));
    }

    @Test
    void validateToken_WithNullToken_ReturnsFalse() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void validateToken_WithMalformedToken_ReturnsFalse() {
        assertFalse(jwtUtil.validateToken("not.a.token"));
    }

    @Test
    void validateRefreshToken_WithValidToken_ReturnsTrue() {
        String token = jwtUtil.generateRefreshToken("user123");
        assertTrue(jwtUtil.validateRefreshToken(token));
    }

    @Test
    void getAccessTokenMaxAge_ReturnsExpirationInSeconds() {
        assertEquals(EXPIRATION_MS / 1000, jwtUtil.getAccessTokenMaxAge());
    }

    @Test
    void getRefreshTokenMaxAge_ReturnsRefreshExpirationInSeconds() {
        assertEquals(REFRESH_EXPIRATION_MS / 1000, jwtUtil.getRefreshTokenMaxAge());
    }

    @Test
    void getExpirationDate_ReturnsExpiration() {
        String token = jwtUtil.generateToken("user123");
        Date expiration = jwtUtil.getExpirationDate(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date(System.currentTimeMillis() - 1000)));
    }

    @Test
    void isTokenExpired_WithValidToken_ReturnsFalse() {
        String token = jwtUtil.generateToken("user123");
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void getTokenFromRequest_WithAuthorizationHeader_ReturnsToken() {
        String token = jwtUtil.generateToken("user123");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        String result = jwtUtil.getTokenFromRequest(request);
        assertEquals(token, result);
    }

    @Test
    void getTokenFromRequest_WithNoBearerPrefix_ReturnsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        String result = jwtUtil.getTokenFromRequest(request);
        assertNull(result);
    }

    @Test
    void getTokenFromRequest_WithCookie_ReturnsToken() {
        String token = jwtUtil.generateToken("user123");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("access_token", token));

        String result = jwtUtil.getTokenFromRequest(request);
        assertEquals(token, result);
    }

    @Test
    void getTokenFromRequest_WithNoAuth_ReturnsNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertNull(jwtUtil.getTokenFromRequest(request));
    }

    @Test
    void getTokenFromRequest_WithWrongCookieName_ReturnsNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("other_cookie", "value"));
        assertNull(jwtUtil.getTokenFromRequest(request));
    }

    @Test
    void parseToken_WithTamperedToken_ThrowsJwtException() {
        String token = jwtUtil.generateToken("user123");
        String tampered = token.substring(0, token.length() - 4) + "abcd";

        assertThrows(JwtException.class, () -> jwtUtil.parseToken(tampered));
    }

    @Test
    void parseToken_WithMalformedToken_ThrowsJwtException() {
        assertThrows(JwtException.class, () -> jwtUtil.parseToken("invalid.token.here"));
    }

    @Test
    void parseToken_WithExpiredToken_ThrowsJwtException() {
        Date past = new Date(System.currentTimeMillis() - 100000);
        String expiredToken = Jwts.builder()
                .subject("user123")
                .expiration(past)
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret)))
                .compact();

        assertThrows(JwtException.class, () -> jwtUtil.parseToken(expiredToken));
    }

    @Test
    void validateSecretKey_WithShortKey_ThrowsIllegalArgumentException() {
        byte[] shortKey = new byte[16];
        for (int i = 0; i < 16; i++) {
            shortKey[i] = (byte) i;
        }
        String shortBase64 = Base64.getEncoder().encodeToString(shortKey);

        JwtUtil util = new JwtUtil();
        ReflectionTestUtils.setField(util, "secretKey", shortBase64);

        assertThrows(IllegalArgumentException.class, util::validateSecretKey);
    }
}
