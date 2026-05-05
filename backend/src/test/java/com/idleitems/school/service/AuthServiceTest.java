package com.idleitems.school.service;

import com.idleitems.school.dto.LoginRequest;
import com.idleitems.school.dto.RegisterRequest;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.security.JwtUtil;
import com.idleitems.school.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginSuccess() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setStatus(User.UserStatus.ACTIVE);
        user.setRole(User.Role.STUDENT);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyMap())).thenReturn("testToken");
        when(jwtUtil.generateToken(anyString())).thenReturn("testRefreshToken");

        Map<String, Object> result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("testToken", result.get("token"));
        assertEquals("testRefreshToken", result.get("refreshToken"));
        assertEquals(user, result.get("user"));
    }

    @Test
    void testLoginUserNotFound() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("password");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void testLoginInvalidPassword() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPhone("13800138000");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        User expectedUser = new User();
        expectedUser.setUsername("newuser");
        expectedUser.setPassword("encodedPassword");
        expectedUser.setEmail("newuser@example.com");
        expectedUser.setPhone("13800138000");
        expectedUser.setRole(User.Role.STUDENT);
        expectedUser.setStatus(User.UserStatus.ACTIVE);

        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        User result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals(User.Role.STUDENT, result.getRole());
        assertEquals(User.UserStatus.ACTIVE, result.getStatus());
    }

    @Test
    void testRegisterUsernameExists() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("existinguser");

        User existingUser = new User();
        existingUser.setUsername("existinguser");

        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("用户名已存在", exception.getMessage());
    }

    @Test
    void testRegisterEmailExists() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("existing@example.com");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

        User existingUser = new User();
        existingUser.setEmail("existing@example.com");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("邮箱已被注册", exception.getMessage());
    }
}
