package com.idleitems.school.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.io.IOException;
import java.util.Collections;

@Slf4j
public class RateLimitFilter implements Filter {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;
    private final int defaultLimit;
    private final int defaultWindow;
    private final int loginLimit;
    private final int loginWindow;

    public RateLimitFilter(RedisTemplate<String, Object> redisTemplate,
                           DefaultRedisScript<Long> rateLimitScript,
                           int defaultLimit, int defaultWindow,
                           int loginLimit, int loginWindow) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = rateLimitScript;
        this.defaultLimit = defaultLimit;
        this.defaultWindow = defaultWindow;
        this.loginLimit = loginLimit;
        this.loginWindow = loginWindow;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String clientIP = getClientIP(httpRequest);

        int limit;
        int window;
        String key;

        if (uri.equals("/api/auth/login")) {
            // 登录接口：最严格限制
            limit = loginLimit;
            window = loginWindow;
            key = "rate_limit:login:" + clientIP;
        } else if (uri.startsWith("/api/auth/")) {
            // 认证相关接口（注册/刷新/忘记密码等）：共享一个限流桶
            limit = 20;
            window = defaultWindow;
            key = "rate_limit:auth:" + clientIP;
        } else {
            // 其他API接口：全局共享限流桶
            limit = defaultLimit;
            window = defaultWindow;
            key = "rate_limit:api:" + clientIP;
        }

        try {
            Long result = redisTemplate.execute(
                    rateLimitScript,
                    Collections.singletonList(key),
                    limit, window
            );

            if (result > 0) {
                chain.doFilter(request, response);
            } else {
                httpResponse.setStatus(429);
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"data\":null}");
                httpResponse.getWriter().flush();
            }
        } catch (Exception e) {
            // 降级处理：Redis不可用时放行请求
            log.warn("Redis限流服务不可用，降级放行: {}", e.getMessage());
            chain.doFilter(request, response);
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
