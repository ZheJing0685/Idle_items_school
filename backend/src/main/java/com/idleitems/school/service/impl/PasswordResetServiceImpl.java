package com.idleitems.school.service.impl;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String RESET_CODE_PREFIX = "password_reset:";
    private static final long CODE_EXPIRE_MINUTES = 5;
    private static final int MAX_SEND_PER_HOUR = 3;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public void sendResetCode(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "该邮箱未注册"));

        String countKey = RESET_CODE_PREFIX + "count:" + email;
        String countStr = redisTemplate.opsForValue().get(countKey);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
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
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码已过期");
        }

        return storedCode.equals(code);
    }

    @Override
    public void resetPassword(String email, String code, String newPassword) {
        if (!verifyCode(email, code)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码错误");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        String codeKey = RESET_CODE_PREFIX + email;
        redisTemplate.delete(codeKey);
        String countKey = RESET_CODE_PREFIX + "count:" + email;
        redisTemplate.delete(countKey);

        log.info("密码重置成功，用户: {}", email);
    }

    private String generateCode() {
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}
