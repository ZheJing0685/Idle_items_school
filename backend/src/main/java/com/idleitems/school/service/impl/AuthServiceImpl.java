package com.idleitems.school.service.impl;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.dto.LoginRequest;
import com.idleitems.school.dto.RegisterRequest;
import com.idleitems.school.dto.UserDTO;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.security.JwtTokenBlacklistService;
import com.idleitems.school.security.JwtUtil;
import com.idleitems.school.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtTokenBlacklistService jwtTokenBlacklistService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final String LOGIN_LOCK_PREFIX = "login:lock:";
    private static final int MAX_LOGIN_FAILURES = 5;
    private static final int LOCK_DURATION_MINUTES = 15;
    private static final int FAIL_COUNT_EXPIRE_MINUTES = 30;

    @Override
    public Map<String, Object> login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();

        // 检查账号是否被锁定
        if (isAccountLocked(username)) {
            long remainingTtl = getRemainingLockTime(username);
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "账号已锁定，请" + remainingTtl + "分钟后重试");
        }

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            recordLoginFailure(username);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            recordLoginFailure(username);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "账号已被禁用");
        }

        // 登录成功，清除失败记录
        clearLoginFailure(username);

        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() == null ? 1 : user.getLoginCount() + 1);
        userRepository.save(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());

        String token = jwtUtil.generateToken(user.getId().toString(), claims);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId().toString());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("user", UserDTO.fromEntity(user));

        return result;
    }

    /**
     * 记录登录失败
     */
    private void recordLoginFailure(String username) {
        String failKey = LOGIN_FAIL_PREFIX + username;
        String countStr = redisTemplate.opsForValue().get(failKey);
        int count = (countStr != null) ? Integer.parseInt(countStr) + 1 : 1;
        redisTemplate.opsForValue().set(failKey, String.valueOf(count), FAIL_COUNT_EXPIRE_MINUTES, TimeUnit.MINUTES);

        if (count >= MAX_LOGIN_FAILURES) {
            // 锁定账号
            String lockKey = LOGIN_LOCK_PREFIX + username;
            redisTemplate.opsForValue().set(lockKey, "1", LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            log.warn("账号{}连续登录失败{}次，已锁定{}分钟", username, count, LOCK_DURATION_MINUTES);
        } else {
            log.info("账号{}登录失败，当前失败次数: {}/{}", username, count, MAX_LOGIN_FAILURES);
        }
    }

    /**
     * 清除登录失败记录
     */
    private void clearLoginFailure(String username) {
        String failKey = LOGIN_FAIL_PREFIX + username;
        String lockKey = LOGIN_LOCK_PREFIX + username;
        redisTemplate.delete(failKey);
        redisTemplate.delete(lockKey);
    }

    /**
     * 检查账号是否被锁定
     */
    private boolean isAccountLocked(String username) {
        String lockKey = LOGIN_LOCK_PREFIX + username;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    /**
     * 获取剩余锁定时间（分钟）
     */
    private long getRemainingLockTime(String username) {
        String lockKey = LOGIN_LOCK_PREFIX + username;
        Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
        return (ttl != null && ttl > 0) ? ttl : LOCK_DURATION_MINUTES;
    }

    @Override
    public User register(RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }

        if (registerRequest.getEmail() != null && userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已被注册");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setNickname(registerRequest.getNickname());
        user.setRole(User.Role.STUDENT);
        user.setStatus(User.UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    @Override
    public User getCurrentUser(String userId) {
        return userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
    }

    @Override
    public Map<String, Object> refreshToken(String refreshToken) {
        if (refreshToken == null || !jwtUtil.validateRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "refresh token无效或已过期");
        }

        try {
            String userId = jwtUtil.getSubject(refreshToken);
            User user = getCurrentUser(userId);

            // 验证用户状态仍然有效
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "账号已被禁用");
            }

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
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("刷新Token失败", e);
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "刷新token失败");
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
            Date expiration = jwtUtil.getExpirationDate(token);
            long ttl = expiration.getTime() - System.currentTimeMillis();
            jwtTokenBlacklistService.addToBlacklist(token, ttl);
            log.info("用户登出成功，Token已失效");
        } catch (Exception e) {
            log.error("用户登出处理失败: {}", e.getMessage());
            jwtTokenBlacklistService.addToBlacklist(token, 0);
        }
    }
    
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD, "旧密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        jwtTokenBlacklistService.invalidateAllUserTokens(userId);
        log.info("用户{}密码修改成功，所有Token已失效", userId);
    }
}
