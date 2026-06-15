package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.module.auth.dto.LoginRequest;
import com.idleitems.school.module.auth.dto.RegisterRequest;
import com.idleitems.school.module.auth.service.impl.AuthServiceImpl;
import com.idleitems.school.module.user.dto.UserDTO;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.user.repository.UserRepository;
import com.idleitems.school.security.JwtTokenBlacklistService;
import com.idleitems.school.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JwtTokenBlacklistService jwtTokenBlacklistService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setNickname("测试用户");
        testUser.setRole(User.Role.STUDENT);
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setLoginCount(0);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");
    }

    @Test
    void login_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyMap())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refreshToken");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        Map<String, Object> result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("accessToken", result.get("token"));
        assertEquals("refreshToken", result.get("refreshToken"));
        assertTrue(result.get("user") instanceof UserDTO);
        assertEquals(1, testUser.getLoginCount());
        assertNotNull(testUser.getLastLoginTime());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password", "encodedPassword");
        verify(jwtUtil).generateToken(eq("1"), anyMap());
        verify(jwtUtil).generateRefreshToken("1");
        verify(redisTemplate).delete("login:fail:testuser");
        verify(redisTemplate).delete("login:lock:testuser");
    }

    @Test
    void login_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            authService.login(new LoginRequest() {{ setUsername("nonexistent"); setPassword("pwd"); }});
        });

        assertEquals("用户名或密码错误", ex.getMessage());
        verify(valueOperations).increment(anyString());
    }

    @Test
    void login_WrongPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        loginRequest.setPassword("wrongpassword");
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(loginRequest));

        assertEquals("用户名或密码错误", ex.getMessage());
        verify(valueOperations).increment(anyString());
    }

    @Test
    void login_UserDisabled() {
        testUser.setStatus(User.UserStatus.DISABLED);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(loginRequest));

        assertEquals("账号已被禁用", ex.getMessage());
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void login_AccountLocked() {
        when(redisTemplate.hasKey("login:lock:testuser")).thenReturn(true);
        when(redisTemplate.getExpire("login:lock:testuser", TimeUnit.MINUTES)).thenReturn(10L);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(loginRequest));

        assertTrue(ex.getMessage().contains("账号已锁定"));
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void login_AccountLocked_RedisExceptionFallback() {
        when(redisTemplate.hasKey("login:lock:testuser")).thenThrow(new RuntimeException("Redis down"));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyMap())).thenReturn("token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("rtoken");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        Map<String, Object> result = authService.login(loginRequest);

        assertNotNull(result.get("token"));
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void login_AccountLocked_GetRemainingLockTimeRedisException() {
        when(redisTemplate.hasKey("login:lock:testuser")).thenReturn(true);
        when(redisTemplate.getExpire("login:lock:testuser", TimeUnit.MINUTES)).thenThrow(new RuntimeException("Redis down"));

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(loginRequest));

        assertTrue(ex.getMessage().contains("账号已锁定"));
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("Password1@");
        request.setEmail("new@test.com");
        request.setPhone("13900000001");
        request.setNickname("新用户");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password1@")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.register(request);

        assertEquals("newuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals("new@test.com", result.getEmail());
        assertEquals("13900000001", result.getPhone());
        assertEquals("新用户", result.getNickname());
        assertEquals(User.Role.STUDENT, result.getRole());
        assertEquals(User.UserStatus.ACTIVE, result.getStatus());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");
        request.setPassword("Password1@");
        request.setEmail("e@t.com");

        when(userRepository.findByUsername("existing")).thenReturn(Optional.of(new User()));

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(request));
        assertEquals("用户名已存在", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_EmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("Password1@");
        request.setEmail("taken@test.com");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("taken@test.com")).thenReturn(Optional.of(new User()));

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(request));
        assertEquals("邮箱已被注册", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_NullEmailSkipsEmailCheck() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("Password1@");
        request.setEmail(null);

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password1@")).thenReturn("enc");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.register(request);

        assertEquals("newuser", result.getUsername());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void getCurrentUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = authService.getCurrentUser("1");

        assertEquals("testuser", result.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void getCurrentUser_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.getCurrentUser("99"));
        assertEquals("用户不存在", ex.getMessage());
        verify(userRepository).findById(99L);
    }

    @Test
    void refreshToken_Success() {
        when(jwtUtil.validateRefreshToken("validRefreshToken")).thenReturn(true);
        when(jwtUtil.getSubject("validRefreshToken")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(jwtUtil.getExpirationDate("validRefreshToken")).thenReturn(new Date(System.currentTimeMillis() + 3600000));
        doNothing().when(jwtTokenBlacklistService).addToBlacklist(anyString(), anyLong());
        when(jwtUtil.generateToken(anyString(), anyMap())).thenReturn("newAccessToken");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("newRefreshToken");

        Map<String, Object> result = authService.refreshToken("validRefreshToken");

        assertEquals("newAccessToken", result.get("token"));
        assertEquals("newRefreshToken", result.get("refreshToken"));
        verify(jwtTokenBlacklistService).addToBlacklist(eq("validRefreshToken"), anyLong());
        verify(jwtUtil).generateToken(eq("1"), anyMap());
    }

    @Test
    void refreshToken_NullToken() {
        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refreshToken(null));
        assertTrue(ex.getMessage().contains("refresh token无效"));
        verify(jwtUtil, never()).validateRefreshToken(any());
    }

    @Test
    void refreshToken_InvalidToken() {
        when(jwtUtil.validateRefreshToken("badToken")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refreshToken("badToken"));
        assertTrue(ex.getMessage().contains("refresh token无效"));
    }

    @Test
    void refreshToken_UserDisabled() {
        testUser.setStatus(User.UserStatus.DISABLED);
        when(jwtUtil.validateRefreshToken("token")).thenReturn(true);
        when(jwtUtil.getSubject("token")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refreshToken("token"));
        assertEquals("账号已被禁用", ex.getMessage());
    }

    @Test
    void refreshToken_BlacklistExceptionFallback() {
        when(jwtUtil.validateRefreshToken("token")).thenReturn(true);
        when(jwtUtil.getSubject("token")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(jwtUtil.getExpirationDate("token")).thenReturn(new Date(System.currentTimeMillis() + 3600000));
        doThrow(new RuntimeException("Blacklist error")).when(jwtTokenBlacklistService).addToBlacklist(anyString(), anyLong());
        when(jwtUtil.generateToken(anyString(), anyMap())).thenReturn("newAccessToken");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("newRefreshToken");

        Map<String, Object> result = authService.refreshToken("token");

        assertEquals("newAccessToken", result.get("token"));
        verify(jwtUtil).generateToken(eq("1"), anyMap());
    }

    @Test
    void refreshToken_ParseException() {
        when(jwtUtil.validateRefreshToken("token")).thenReturn(true);
        when(jwtUtil.getSubject("token")).thenThrow(new RuntimeException("parse error"));

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refreshToken("token"));
        assertTrue(ex.getMessage().contains("刷新token失败"));
    }

    @Test
    void validateToken_True() {
        when(jwtUtil.validateToken("validToken")).thenReturn(true);
        assertTrue(authService.validateToken("validToken"));
        verify(jwtUtil).validateToken("validToken");
    }

    @Test
    void validateToken_False() {
        when(jwtUtil.validateToken("expiredToken")).thenReturn(false);
        assertFalse(authService.validateToken("expiredToken"));
        verify(jwtUtil).validateToken("expiredToken");
    }

    @Test
    void getUserIdFromToken_Success() {
        when(jwtUtil.getSubject("token")).thenReturn("42");
        String userId = authService.getUserIdFromToken("token");
        assertEquals("42", userId);
        verify(jwtUtil).getSubject("token");
    }

    @Test
    void logout_Success() {
        when(jwtUtil.getExpirationDate("token")).thenReturn(new Date(System.currentTimeMillis() + 3600000));
        doNothing().when(jwtTokenBlacklistService).addToBlacklist(anyString(), anyLong());

        authService.logout("token");

        verify(jwtUtil).getExpirationDate("token");
        verify(jwtTokenBlacklistService).addToBlacklist(eq("token"), anyLong());
    }

    @Test
    void logout_ExceptionFallback() {
        when(jwtUtil.getExpirationDate("token")).thenThrow(new RuntimeException("JWT error"));

        authService.logout("token");

        verify(jwtTokenBlacklistService).addToBlacklist("token", 0);
    }

    @Test
    void changePassword_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newEncoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(jwtTokenBlacklistService).invalidateAllUserTokens(1L);

        authService.changePassword(1L, "oldPass", "newPass");

        verify(passwordEncoder).matches("oldPass", "encodedPassword");
        verify(passwordEncoder).encode("newPass");
        assertEquals("newEncoded", testUser.getPassword());
        verify(userRepository).save(testUser);
        verify(jwtTokenBlacklistService).invalidateAllUserTokens(1L);
    }

    @Test
    void changePassword_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.changePassword(99L, "old", "new"));
        assertEquals("用户不存在", ex.getMessage());
        verify(userRepository).findById(99L);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void changePassword_WrongOldPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongOld", "encodedPassword")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.changePassword(1L, "wrongOld", "newPass"));
        assertEquals("旧密码错误", ex.getMessage());
        verify(passwordEncoder).matches("wrongOld", "encodedPassword");
        verify(userRepository, never()).save(any());
        verify(jwtTokenBlacklistService, never()).invalidateAllUserTokens(anyLong());
    }

    @Test
    void login_RecordLoginFailureIncrements() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(valueOperations.increment("login:fail:testuser")).thenReturn(1L);

        assertThrows(BusinessException.class, () -> authService.login(loginRequest));

        verify(valueOperations).increment("login:fail:testuser");
        verify(redisTemplate).expire("login:fail:testuser", 30L, TimeUnit.MINUTES);
    }

    @Test
    void login_RecordLoginFailureReachesThreshold() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(valueOperations.increment("login:fail:testuser")).thenReturn(5L);

        assertThrows(BusinessException.class, () -> authService.login(loginRequest));

        verify(valueOperations).increment("login:fail:testuser");
        verify(valueOperations).set("login:lock:testuser", "1", 15L, TimeUnit.MINUTES);
    }

    @Test
    void login_RecordLoginFailureRedisException() {
        when(valueOperations.increment("login:fail:testuser")).thenThrow(new RuntimeException("Redis down"));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_ClearLoginFailureOnSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyMap())).thenReturn("token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("rtoken");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        authService.login(loginRequest);

        verify(redisTemplate).delete("login:fail:testuser");
        verify(redisTemplate).delete("login:lock:testuser");
    }

    @Test
    void login_ClearLoginFailureRedisException() {
        doThrow(new RuntimeException("Redis down")).when(redisTemplate).delete(anyString());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyMap())).thenReturn("token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("rtoken");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        Map<String, Object> result = authService.login(loginRequest);
        assertNotNull(result.get("token"));
    }

    @Test
    void login_NullLoginCount() {
        testUser.setLoginCount(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyMap())).thenReturn("token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("rtoken");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        authService.login(loginRequest);

        assertEquals(1, testUser.getLoginCount());
    }

    @Test
    void refreshToken_UserNotFound() {
        when(jwtUtil.validateRefreshToken("token")).thenReturn(true);
        when(jwtUtil.getSubject("token")).thenReturn("99");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refreshToken("token"));
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    void logout_BlacklistException() {
        when(jwtUtil.getExpirationDate("token")).thenReturn(new Date(System.currentTimeMillis() + 1000));
        doThrow(new RuntimeException("addToBlacklist failed"))
            .doNothing()
            .when(jwtTokenBlacklistService).addToBlacklist(anyString(), anyLong());

        authService.logout("token");

        verify(jwtTokenBlacklistService).addToBlacklist("token", 0);
    }

    @Test
    void refreshToken_GetExpirationExceptionFallback() {
        when(jwtUtil.validateRefreshToken("token")).thenReturn(true);
        when(jwtUtil.getSubject("token")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(jwtUtil.getExpirationDate("token")).thenThrow(new RuntimeException("Exp extraction failed"));

        when(jwtUtil.generateToken(anyString(), anyMap())).thenReturn("newAccessToken");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("newRefreshToken");

        Map<String, Object> result = authService.refreshToken("token");

        assertEquals("newAccessToken", result.get("token"));
        assertEquals("newRefreshToken", result.get("refreshToken"));
        verify(jwtTokenBlacklistService, never()).addToBlacklist(anyString(), anyLong());
    }
}
