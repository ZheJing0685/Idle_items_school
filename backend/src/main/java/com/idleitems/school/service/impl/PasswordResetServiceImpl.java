package com.idleitems.school.service.impl;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.security.JwtTokenBlacklistService;
import com.idleitems.school.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenBlacklistService jwtTokenBlacklistService;

    private static final String RESET_CODE_PREFIX = "password_reset:";
    private static final long CODE_EXPIRE_MINUTES = 5;
    private static final int MAX_SEND_PER_HOUR = 3;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public void sendResetCode(String email) {
        boolean userExists = userRepository.findByEmail(email).isPresent();
        if (!userExists) {
            log.info("重置密码请求针对未注册邮箱: {}", email);
        }

        String countKey = RESET_CODE_PREFIX + "count:" + email;
        int count = getResetCount(countKey);
        if (count >= MAX_SEND_PER_HOUR) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "发送过于频繁，请稍后再试");
        }

        String code = generateCode();

        String codeKey = RESET_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        redisTemplate.opsForValue().increment(countKey);
        redisTemplate.expire(countKey, 1, TimeUnit.HOURS);

        log.info("密码重置验证码已生成，邮箱: {}", email);
    }

    @Override
    public boolean verifyCode(String email, String code) {
        String codeKey = RESET_CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            return false;
        }

        boolean matched = MessageDigest.isEqual(
                storedCode.getBytes(StandardCharsets.UTF_8),
                code.getBytes(StandardCharsets.UTF_8));

        // 验证成功后立即删除验证码，防止重复使用
        if (matched) {
            redisTemplate.delete(codeKey);
        }

        return matched;
    }

    @Override
    public void resetPassword(String email, String code, String newPassword) {
        String codeKey = RESET_CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if (storedCode == null || !MessageDigest.isEqual(
                storedCode.getBytes(StandardCharsets.UTF_8),
                code.getBytes(StandardCharsets.UTF_8))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码错误或已过期");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 删除验证码
        redisTemplate.delete(codeKey);
        // 删除发送频率计数器
        String countKey = RESET_CODE_PREFIX + "count:" + email;
        redisTemplate.delete(countKey);

        // 密码重置后使该用户的所有Token失效，强制重新登录
        jwtTokenBlacklistService.invalidateAllUserTokens(user.getId());

        log.info("密码重置成功，用户: {}，所有Token已失效", email);
    }

    private String generateCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private int getResetCount(String countKey) {
        try {
            String countStr = redisTemplate.opsForValue().get(countKey);
            if (countStr == null) return 0;
            return Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            log.warn("重置密码频率计数器数据损坏，已重置: key={}", countKey);
            redisTemplate.delete(countKey);
            return 0;
        }
    }
}
