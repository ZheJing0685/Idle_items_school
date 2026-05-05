package com.idleitems.school.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String clientIP = getClientIP(httpRequest);
        String key = "rate_limit:" + uri + ":" + clientIP;

        // 默认限流配置：每分钟60次请求
        int limit = 60;
        int window = 60;

        // 执行限流脚本
        Long result = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(key),
                limit, window
        );

        if (result == null || result > 0) {
            // 允许请求通过
            chain.doFilter(request, response);
        } else {
            // 限流
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"code\": 429, \"message\": \"请求过于频繁，请稍后再试\"}");
            httpResponse.getWriter().flush();
            return;
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}