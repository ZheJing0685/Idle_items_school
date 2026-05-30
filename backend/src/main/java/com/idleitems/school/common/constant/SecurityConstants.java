package com.idleitems.school.common.constant;

/**
 * 安全相关常量
 * 统一管理JWT、认证、限流等安全配置
 */
public final class SecurityConstants {

    private SecurityConstants() {}

    // ==================== JWT Claims ====================
    public static final String CLAIM_TOKEN_VERSION = "tv";
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_ROLE = "role";

    // ==================== HTTP头 ====================
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_BEARER_PREFIX = "Bearer ";
    public static final String HEADER_X_REAL_IP = "X-Real-IP";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    // ==================== 登录安全 ====================
    public static final int MAX_LOGIN_FAILURES = 5;
    public static final int LOCK_DURATION_MINUTES = 15;
    public static final int FAIL_COUNT_EXPIRE_MINUTES = 30;

    // ==================== 密码重置 ====================
    public static final int MAX_RESET_SEND_PER_HOUR = 3;
    public static final int RESET_CODE_EXPIRE_MINUTES = 5;
    public static final int RESET_CODE_LENGTH = 8;

    // ==================== Token过期时间（毫秒）====================
    public static final long DEFAULT_TOKEN_EXPIRATION = 3600000L;      // 1小时
    public static final long DEFAULT_REFRESH_EXPIRATION = 604800000L;  // 7天

    // ==================== 角色标识 ====================
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_STUDENT = "STUDENT";
}
