package com.idleitems.school.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

/**
 * HttpOnly Cookie 工具类
 * 统一管理 Token Cookie 的创建和清除
 */
public class CookieUtil {

    private static final String ACCESS_TOKEN_NAME = "access_token";
    private static final String REFRESH_TOKEN_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/";

    /**
     * 设置 Access Token Cookie
     */
    public static void setAccessTokenCookie(HttpServletResponse response, String token, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_NAME, token)
                .httpOnly(true)
                .secure(false) // 开发环境用 false，生产环境改为 true
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 设置 Refresh Token Cookie
     */
    public static void setRefreshTokenCookie(HttpServletResponse response, String token, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_NAME, token)
                .httpOnly(true)
                .secure(false)
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 清除 Access Token Cookie
     */
    public static void clearAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path(COOKIE_PATH)
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 清除 Refresh Token Cookie
     */
    public static void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path(COOKIE_PATH)
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 清除所有 Token Cookie
     */
    public static void clearAllTokens(HttpServletResponse response) {
        clearAccessTokenCookie(response);
        clearRefreshTokenCookie(response);
    }
}
