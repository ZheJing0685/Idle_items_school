package com.idleitems.school.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JWT Token黑名单服务
 * 用于管理已失效的Token，支持用户登出、密码修改、管理员禁用等场景
 *
 * 安全改进：
 * - 使用SHA-256替代MD5进行Token哈希
 * - 使用SCAN替代KEYS命令，避免Redis阻塞
 * - 使用用户级Token版本号方案，高效使用户所有Token失效
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_TOKEN_VERSION_PREFIX = "token:user_version:";

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
            redisTemplate.opsForValue().set(key, "1", 1, TimeUnit.MINUTES);
            log.info("Token已立即加入黑名单");
        }
    }

    /**
     * 将指定用户的Token加入黑名单（同时记录用户级信息用于批量失效）
     *
     * @param token         Token
     * @param userId        用户ID
     * @param expirationMs  Token剩余有效时间
     */
    public void addToBlacklistForUser(String token, Long userId, long expirationMs) {
        String hash = getTokenHash(token);
        String key = BLACKLIST_PREFIX + hash;
        String userKey = BLACKLIST_PREFIX + "user:" + userId + ":" + hash;

        long ttlMs = expirationMs > 0 ? expirationMs : 60_000L;
        redisTemplate.opsForValue().set(key, "1", ttlMs, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set(userKey, "1", ttlMs, TimeUnit.MILLISECONDS);
        log.info("Token已为用户{}加入黑名单", userId);
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
     * 使用用户级Token版本号方案：递增版本号后，旧版本Token的JWT claims中
     * 携带的版本号与Redis中的当前版本号不匹配即视为失效。
     *
     * 同时使用SCAN清理该用户的黑名单key，避免KEYS命令阻塞Redis。
     *
     * @param userId 用户ID
     */
    public void invalidateAllUserTokens(Long userId) {
        // 方案1：递增用户Token版本号（主方案，O(1)操作）
        String versionKey = USER_TOKEN_VERSION_PREFIX + userId;
        Long newVersion = redisTemplate.opsForValue().increment(versionKey);
        if (newVersion != null) {
            // 设置过期时间为7天（与refresh token最大有效期一致）
            redisTemplate.expire(versionKey, 7, TimeUnit.DAYS);
        }

        // 方案2：使用SCAN清理该用户的黑名单key（辅助清理，避免内存泄漏）
        String userPattern = BLACKLIST_PREFIX + "user:" + userId + ":*";
        List<String> keysToDelete = scanKeys(userPattern);
        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }

        log.info("已使用户{}的所有Token失效，新版本号: {}", userId, newVersion);
    }

    /**
     * 获取用户的Token版本号
     * Token中携带的版本号需与此值匹配才有效
     *
     * @param userId 用户ID
     * @return 当前Token版本号，不存在时返回0
     */
    public long getUserTokenVersion(Long userId) {
        String versionKey = USER_TOKEN_VERSION_PREFIX + userId;
        String value = redisTemplate.opsForValue().get(versionKey);
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 使用SCAN命令迭代匹配的key，替代KEYS命令
     * SCAN是增量式迭代，不会阻塞Redis
     *
     * @param pattern key匹配模式
     * @return 匹配的key列表
     */
    private List<String> scanKeys(String pattern) {
        List<String> keys = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        } catch (Exception e) {
            log.warn("SCAN操作异常，pattern={}, error={}", pattern, e.getMessage());
        }
        return keys;
    }

    /**
     * 计算Token的SHA-256哈希值
     * 使用SHA-256替代MD5，提供更强的抗碰撞性
     *
     * @param token Token
     * @return 十六进制哈希值
     */
    private String getTokenHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256是JDK标准算法，不会出现此异常
            log.error("SHA-256算法不可用，降级使用默认哈希", e);
            return String.valueOf(token.hashCode());
        }
    }
}
