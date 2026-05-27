package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.service.impl.PasswordResetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("encodedOldPassword");
    }

    @Test
    void testSendResetCode_ExistingEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(valueOperations.get("password_reset:count:test@example.com")).thenReturn(null);

        passwordResetService.sendResetCode("test@example.com");

        verify(valueOperations).set(eq("password_reset:test@example.com"), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(valueOperations).increment("password_reset:count:test@example.com");
    }

    @Test
    void testSendResetCode_NonExistingEmail_NoInfoLeak() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(valueOperations.get("password_reset:count:unknown@example.com")).thenReturn(null);

        passwordResetService.sendResetCode("unknown@example.com");

        verify(valueOperations).set(eq("password_reset:unknown@example.com"), anyString(), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    void testSendResetCode_RateLimited() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(valueOperations.get("password_reset:count:test@example.com")).thenReturn("3");

        assertThrows(BusinessException.class, () ->
            passwordResetService.sendResetCode("test@example.com"));

        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void testSendResetCode_CorruptedCounter() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(valueOperations.get("password_reset:count:test@example.com")).thenReturn("abc");

        passwordResetService.sendResetCode("test@example.com");

        verify(redisTemplate).delete("password_reset:count:test@example.com");
    }

    @Test
    void testVerifyCode_Correct() {
        when(valueOperations.get("password_reset:test@example.com")).thenReturn("123456");

        assertTrue(passwordResetService.verifyCode("test@example.com", "123456"));
    }

    @Test
    void testVerifyCode_Wrong() {
        when(valueOperations.get("password_reset:test@example.com")).thenReturn("123456");

        assertFalse(passwordResetService.verifyCode("test@example.com", "654321"));
    }

    @Test
    void testVerifyCode_Expired() {
        when(valueOperations.get("password_reset:test@example.com")).thenReturn(null);

        assertFalse(passwordResetService.verifyCode("test@example.com", "123456"));
    }

    @Test
    void testResetPassword_Success() {
        when(valueOperations.get("password_reset:test@example.com")).thenReturn("123456");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("NewP@ss1")).thenReturn("encodedNewPassword");

        passwordResetService.resetPassword("test@example.com", "123456", "NewP@ss1");

        verify(passwordEncoder).encode("NewP@ss1");
        verify(userRepository).save(testUser);
        verify(redisTemplate).delete("password_reset:test@example.com");
        verify(redisTemplate).delete("password_reset:count:test@example.com");
    }

    @Test
    void testResetPassword_WrongCode() {
        when(valueOperations.get("password_reset:test@example.com")).thenReturn("123456");

        assertThrows(BusinessException.class, () ->
            passwordResetService.resetPassword("test@example.com", "wrong", "NewP@ss1"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void testResetPassword_ExpiredCode() {
        when(valueOperations.get("password_reset:test@example.com")).thenReturn(null);

        assertThrows(BusinessException.class, () ->
            passwordResetService.resetPassword("test@example.com", "123456", "NewP@ss1"));

        verify(userRepository, never()).save(any());
    }
}
