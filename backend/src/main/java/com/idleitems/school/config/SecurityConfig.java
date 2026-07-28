package com.idleitems.school.config;

import com.idleitems.school.security.filter.RateLimitFilter;
import com.idleitems.school.security.filter.XssFilter;
import com.idleitems.school.security.JwtTokenBlacklistService;
import com.idleitems.school.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final JwtTokenBlacklistService jwtTokenBlacklistService;
    private final XssFilter xssFilter;

    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Value("${rate-limit.default-limit:60}")
    private int defaultLimit;

    @Value("${rate-limit.default-window:60}")
    private int defaultWindow;

    @Value("${rate-limit.login-limit:5}")
    private int loginLimit;

    @Value("${rate-limit.login-window:60}")
    private int loginWindow;

    @Value("${rate-limit.auth-limit:20}")
    private int authLimit;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/doc.html", "/webjars/**", "/swagger-resources/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").authenticated()
                .requestMatchers("/api/auth/**", "/api/items/search", "/api/items/hot", "/api/items/{id}", "/api/categories/**", "/api/home/**", "/api/test/**").permitAll()
                .requestMatchers("/api/user/*/profile", "/api/user/*/items", "/api/user/*/reviews").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/ws/**", "/ws-native/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/items").permitAll()
                .requestMatchers("/api/favorites/**", "/api/items/user", "/api/items/upload", "/api/items/upload/**").authenticated()
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
            );

        http.addFilterBefore(xssFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(config::addAllowedOriginPattern);
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("X-Requested-With");
        config.addAllowedHeader("Accept");
        config.addAllowedHeader("Origin");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RedisTemplate<String, Object> redisTemplate, DefaultRedisScript<Long> rateLimitScript) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter(redisTemplate, rateLimitScript, defaultLimit, defaultWindow, loginLimit, loginWindow, authLimit));
        registration.addUrlPatterns("/api/*");
        registration.setName("rateLimitFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        return registration;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    private OncePerRequestFilter jwtAuthenticationFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                String token = jwtUtil.getTokenFromRequest(request);
                if (token != null) {
                    // 检查Token是否在黑名单中
                    if (jwtTokenBlacklistService.isBlacklisted(token)) {
                        String uri = request.getRequestURI();
                        if (!isPermitAll(uri)) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"code\":40101,\"message\":\"Token已失效，请重新登录\",\"data\":null}");
                            return;
                        }
                    } else if (jwtUtil.validateToken(token)) {
                        String userIdStr = jwtUtil.getUserIdFromToken(token);
                        Long userId = Long.parseLong(userIdStr);

                        long tokenVersion = jwtUtil.getTokenVersionFromToken(token);
                        long currentVersion = jwtTokenBlacklistService.getUserTokenVersion(userId);
                        if (tokenVersion < currentVersion) {
                            String uri = request.getRequestURI();
                            if (!isPermitAll(uri)) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"code\":40102,\"message\":\"Token已失效，请重新登录\",\"data\":null}");
                                return;
                            }
                        }

                        String username = jwtUtil.getUsernameFromToken(token);
                        String role = jwtUtil.getRoleFromToken(token);

                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
                        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        userIdStr, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        request.setAttribute("userId", userId);
                        request.setAttribute("username", username);
                        request.setAttribute("role", role);
                    } else {
                        // Token无效/过期，但如果是permitAll端点则不拦截
                        String uri = request.getRequestURI();
                        if (!isPermitAll(uri)) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"code\":40101,\"message\":\"token已过期或无效\",\"data\":null}");
                            return;
                        }
                    }
                }
                filterChain.doFilter(request, response);
            }

            private boolean isPermitAll(String uri) {
                return ApiPaths.isPublicPath(uri);
            }
        };
    }
}
