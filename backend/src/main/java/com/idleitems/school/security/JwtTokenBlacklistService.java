package com.idleitems.school.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.concurrent.TimeUnit;

/**
 * JWT Token黑名单服务
 * 用于管理已失效的Token，支持用户登出、密码修改、管理员禁用等场景
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * 将Token加入黑名单
     *
     * @param token         需要失效的Token
     * @param expirationMs  Token的剩余有效时间（毫秒）
     */
    public void addToBlacklist(String token, long expirationMs) {
        String key = BLACKLIST_PREFIX + getTokenHash(token);
        if (expirationMs > 0) {
            redisTemplate.opsForValue().set(key, "1", expirationMs, TimeUnit.MILLISECONDS);
            log.info("Token已加入黑名单，将在{}毫秒后自动过期", expirationMs);
        } else {
            // 立即失效，设置一个短时间的过期时间
            redisTemplate.opsForValue().set(key, "1", 1, TimeUnit.MINUTES);
            log.info("Token已立即加入黑名单");
        }
    }

    /**
     * 检查Token是否在黑名单中
     *
     * @param token 需要检查的Token
     * @return true表示Token已被加入黑名单（已失效）
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + getTokenHash(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 使指定用户的所有Token失效
     * 用于密码修改、管理员禁用等场景
     *
     * @param userId 用户ID
     */
    public void invalidateAllUserTokens(Long userId) {
        String pattern = BLACKLIST_PREFIX + "user:" + userId + ":*";
        var keys = redisTemplate.keys(pattern);
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("已使用户{}的所有Token失效", userId);
        }
    }

    /**
     * 将指定用户的Token加入黑名单
     *
     * @param token         Token
     * @param userId        用户ID
     * @param expirationMs  Token剩余有效时间
     */
    public void addToBlacklistForUser(String token, Long userId, long expirationMs) {
        String key = BLACKLIST_PREFIX + getTokenHash(token);
        String userKey = BLACKLIST_PREFIX + "user:" + userId + ":" + getTokenHash(token);
        
        if (expirationMs > 0) {
            redisTemplate.opsForValue().set(key, "1", expirationMs, TimeUnit.MILLISECONDS);
            redisTemplate.opsForValue().set(userKey, "1", expirationMs, TimeUnit.MILLISECONDS);
        } else {
            redisTemplate.opsForValue().set(key, "1", 1, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(userKey, "1", 1, TimeUnit.MINUTES);
        }
        log.info("Token已为用户{}加入黑名单", userId);
    }

    /**
     * 计算Token的MD5哈希值
     *
     * @param token Token
     * @return 哈希值
     */
    private String getTokenHash(String token) {
        return DigestUtils.md5DigestAsHex(token.getBytes());
    }
}
