package com.idleitems.school.service.impl;

import com.idleitems.school.dto.LoginRequest;
import com.idleitems.school.dto.RegisterRequest;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.security.JwtTokenBlacklistService;
import com.idleitems.school.security.JwtUtil;
import com.idleitems.school.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtTokenBlacklistService jwtTokenBlacklistService;

    @Override
    public Map<String, Object> login(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new IllegalStateException("账号已被禁用");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());

        String token = jwtUtil.generateToken(user.getId().toString(), claims);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId().toString());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("user", user);

        return result;
    }

    @Override
    public User register(RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setRole(User.Role.STUDENT);
        user.setStatus(User.UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    @Override
    public User getCurrentUser(String userId) {
        return userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    @Override
    public Map<String, Object> refreshToken(String refreshToken) {
        try {
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                throw new IllegalArgumentException("refresh token无效或已过期");
            }

            String userId = jwtUtil.getSubject(refreshToken);
            User user = getCurrentUser(userId);

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("username", user.getUsername());
            claims.put("role", user.getRole());

            String newToken = jwtUtil.generateToken(userId, claims);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("token", newToken);
            result.put("refreshToken", newRefreshToken);

            return result;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("刷新token失败");
        }
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public String getUserIdFromToken(String token) {
        return jwtUtil.getSubject(token);
    }
    
    @Override
    public void logout(String token) {
        try {
            // 获取Token的过期时间
            Date expiration = jwtUtil.getExpirationDate(token);
            long ttl = expiration.getTime() - System.currentTimeMillis();
            
            // 将Token加入黑名单
            jwtTokenBlacklistService.addToBlacklist(token, ttl);
            log.info("用户登出成功，Token已失效");
        } catch (Exception e) {
            log.error("用户登出处理失败: {}", e.getMessage());
            // 即使获取过期时间失败，也立即将Token加入黑名单
            jwtTokenBlacklistService.addToBlacklist(token, 0);
        }
    }
    
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("旧密码错误");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // 使该用户的所有Token失效
        jwtTokenBlacklistService.invalidateAllUserTokens(userId);
        log.info("用户{}密码修改成功，所有Token已失效", userId);
    }
}
