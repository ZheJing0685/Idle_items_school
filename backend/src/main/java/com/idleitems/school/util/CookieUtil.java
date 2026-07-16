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

    private static boolean secure = false;

    /**
     * 设置 Cookie 的 Secure 标志
     * 生产环境通过 CookieSecureConfig 注入为 true
     */
    public static void setSecure(boolean secure) {
        CookieUtil.secure = secure;
    }

    private static ResponseCookie.ResponseCookieBuilder baseCookie(String name, String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .path(COOKIE_PATH)
                .sameSite("Lax");
    }

    /**
     * 设置 Access Token Cookie
     */
    public static void setAccessTokenCookie(HttpServletResponse response, String token, long maxAge) {
        ResponseCookie cookie = baseCookie(ACCESS_TOKEN_NAME, token)
                .maxAge(maxAge)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 设置 Refresh Token Cookie
     */
    public static void setRefreshTokenCookie(HttpServletResponse response, String token, long maxAge) {
        ResponseCookie cookie = baseCookie(REFRESH_TOKEN_NAME, token)
                .maxAge(maxAge)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 清除 Access Token Cookie
     */
    public static void clearAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = baseCookie(ACCESS_TOKEN_NAME, "")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 清除 Refresh Token Cookie
     */
    public static void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = baseCookie(REFRESH_TOKEN_NAME, "")
                .maxAge(0)
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
