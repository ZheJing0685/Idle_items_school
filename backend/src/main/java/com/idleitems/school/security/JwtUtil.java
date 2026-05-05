package com.idleitems.school.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    private javax.crypto.SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateToken(String subject) {
        return generateToken(subject, null);
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SignatureException e) {
            log.error("无效的JWT签名: {}", e.getMessage());
            throw new JwtException("无效的token");
        } catch (MalformedJwtException e) {
            log.error("无效的JWT令牌: {}", e.getMessage());
            throw new JwtException("无效的token");
        } catch (ExpiredJwtException e) {
            log.error("JWT令牌已过期: {}", e.getMessage());
            throw new JwtException("token已过期");
        } catch (UnsupportedJwtException e) {
            log.error("JWT令牌不受支持: {}", e.getMessage());
            throw new JwtException("无效的token");
        } catch (IllegalArgumentException e) {
            log.error("JWT令牌参数错误: {}", e.getMessage());
            throw new JwtException("无效的token");
        }
    }

    public String getSubject(String token) {
        return parseToken(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Date getExpirationDate(String token) {
        return parseToken(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        return getExpirationDate(token).before(new Date());
    }

    public String getUserIdFromToken(String token) {
        return parseToken(token).getSubject();
    }

    public String getUsernameFromToken(String token) {
        return parseToken(token).get("username", String.class);
    }

    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }
}
