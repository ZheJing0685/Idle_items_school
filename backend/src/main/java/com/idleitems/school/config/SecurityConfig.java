package com.idleitems.school.config;

import com.idleitems.school.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh", "/api/auth/me", "/api/items/search", "/api/items/hot", "/api/items/{id}", "/api/items/upload", "/api/items/upload/chunk", "/api/items/upload/complete", "/api/items/upload/check", "/api/categories", "/api/categories/tree", "/api/test/**", "/uploads/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/items").permitAll()
                .requestMatchers("/api/favorites/**", "/api/items/user").authenticated()
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
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
                String token = extractToken(request);
                if (token != null) {
                    if (jwtUtil.validateToken(token)) {
                        String userIdStr = jwtUtil.getUserIdFromToken(token);
                        Long userId = Long.parseLong(userIdStr);
                        String username = jwtUtil.getUsernameFromToken(token);
                        String role = jwtUtil.getRoleFromToken(token);

                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
                        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        username, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        request.setAttribute("userId", userId);
                        request.setAttribute("username", username);
                        request.setAttribute("role", role);
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":40101,\"message\":\"token已过期或无效\",\"data\":null}");
                        return;
                    }
                }
                filterChain.doFilter(request, response);
            }

            private String extractToken(HttpServletRequest request) {
                String bearerToken = request.getHeader("Authorization");
                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    return bearerToken.substring(7);
                }
                return null;
            }
        };
    }
}