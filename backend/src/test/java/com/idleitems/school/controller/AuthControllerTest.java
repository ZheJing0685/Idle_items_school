package com.idleitems.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.module.auth.dto.LoginRequest;
import com.idleitems.school.module.auth.dto.RegisterRequest;
import com.idleitems.school.module.auth.dto.ForgotPasswordRequest;
import com.idleitems.school.module.auth.dto.VerifyCodeRequest;
import com.idleitems.school.module.auth.dto.ResetPasswordRequest;
import com.idleitems.school.module.auth.dto.ChangePasswordRequest;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.auth.service.AuthService;
import com.idleitems.school.module.auth.service.PasswordResetService;
import com.idleitems.school.module.auth.controller.AuthController;
import com.idleitems.school.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController 接口测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("Password@123");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPhone("13800138000");
    }

    @Test
    @DisplayName("测试登录成功")
    void testLoginSuccess() throws Exception {
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("accessToken", "jwt-access-token");
        tokenData.put("refreshToken", "jwt-refresh-token");
        when(authService.login(any(LoginRequest.class))).thenReturn(tokenData);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.accessToken").value("jwt-access-token"));
    }

    @Test
    @DisplayName("测试登录失败 - 用户名或密码错误")
    void testLoginFailureUserNotFound() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("测试登录参数校验 - 用户名为空")
    void testLoginValidationUsernameBlank() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setUsername("");
        invalidRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("测试登录参数校验 - 密码为空")
    void testLoginValidationPasswordBlank() throws Exception {
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setUsername("testuser");
        invalidRequest.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("测试注册成功")
    void testRegisterSuccess() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("newuser");
        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("accessToken", "jwt-access-token");
        tokenData.put("refreshToken", "jwt-refresh-token");
        when(authService.login(any(LoginRequest.class))).thenReturn(tokenData);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data.accessToken").value("jwt-access-token"));
    }

    @Test
    @DisplayName("测试注册失败 - 用户已存在")
    void testRegisterFailureUserExists() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "用户名已存在"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    @DisplayName("测试注册参数校验 - 密码格式不正确")
    void testRegisterValidationWeakPassword() throws Exception {
        RegisterRequest weakPasswordRequest = new RegisterRequest();
        weakPasswordRequest.setUsername("newuser");
        weakPasswordRequest.setPassword("12345678");
        weakPasswordRequest.setEmail("new@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(weakPasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("测试注册参数校验 - 邮箱格式不正确")
    void testRegisterValidationInvalidEmail() throws Exception {
        RegisterRequest invalidEmailRequest = new RegisterRequest();
        invalidEmailRequest.setUsername("newuser");
        invalidEmailRequest.setPassword("Password@123");
        invalidEmailRequest.setEmail("not-an-email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @DisplayName("测试忘记密码 - 发送验证码成功")
    void testForgotPasswordSuccess() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");
        doNothing().when(passwordResetService).sendResetCode("test@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("验证码已发送到您的邮箱"));
    }

    @Test
    @DisplayName("测试验证验证码成功")
    void testVerifyCodeSuccess() throws Exception {
        VerifyCodeRequest request = new VerifyCodeRequest();
        request.setEmail("test@example.com");
        request.setCode("123456");
        when(passwordResetService.verifyCode("test@example.com", "123456")).thenReturn(true);

        mockMvc.perform(post("/api/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("验证码验证成功"));
    }

    @Test
    @DisplayName("测试验证验证码失败")
    void testVerifyCodeFailure() throws Exception {
        VerifyCodeRequest request = new VerifyCodeRequest();
        request.setEmail("test@example.com");
        request.setCode("000000");
        when(passwordResetService.verifyCode("test@example.com", "000000")).thenReturn(false);

        mockMvc.perform(post("/api/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("验证码错误或已过期"));
    }

    @Test
    @DisplayName("测试重置密码成功")
    void testResetPasswordSuccess() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setCode("12345678");
        request.setNewPassword("NewPassword@123");
        doNothing().when(passwordResetService).resetPassword("test@example.com", "12345678", "NewPassword@123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("密码重置成功，请重新登录"));
    }

    @Test
    @DisplayName("测试修改密码成功")
    void testChangePasswordSuccess() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("OldPassword@123");
        request.setNewPassword("NewPassword@123");
        doNothing().when(authService).changePassword(1L, "OldPassword@123", "NewPassword@123");

        mockMvc.perform(post("/api/auth/change-password")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("密码修改成功，请重新登录"));
    }

    @Test
    @DisplayName("测试登出成功")
    void testLogoutSuccess() throws Exception {
        doNothing().when(authService).logout("valid-token");

        mockMvc.perform(post("/api/auth/logout")
                        .requestAttr("userId", 1L)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登出成功"));
    }

    @Test
    @DisplayName("测试登出无Token")
    void testLogoutWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登出成功"));
    }
}
