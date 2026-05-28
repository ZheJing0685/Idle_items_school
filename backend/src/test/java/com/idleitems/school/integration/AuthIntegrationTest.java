package com.idleitems.school.integration;

import com.idleitems.school.BaseIntegrationTest;
import com.idleitems.school.dto.LoginRequest;
import com.idleitems.school.dto.RegisterRequest;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证模块集成测试
 * 测试完整的认证流程：注册 -> 登录 -> 获取用户信息 -> 修改密码 -> 登出
 */
@DisplayName("认证模块集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_USERNAME = "testuser_integration";
    private static final String TEST_PASSWORD = "TestPassword@123";
    private static final String TEST_EMAIL = "test_integration@example.com";
    private static String authToken;
    private static String refreshToken;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        userRepository.deleteByUsername(TEST_USERNAME);
    }

    @Test
    @Order(1)
    @DisplayName("用户注册 - 成功")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(TEST_USERNAME);
        request.setPassword(TEST_PASSWORD);
        request.setEmail(TEST_EMAIL);
        request.setPhone("13800138001");
        request.setNickname("集成测试用户");

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.username").value(TEST_USERNAME))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 保存token供后续测试使用
        authToken = extractToken(response);
        refreshToken = extractRefreshToken(response);
    }

    @Test
    @Order(2)
    @DisplayName("用户注册 - 用户名已存在")
    void testRegisterDuplicateUsername() throws Exception {
        // 先创建用户
        createTestUser();

        RegisterRequest request = new RegisterRequest();
        request.setUsername(TEST_USERNAME);
        request.setPassword(TEST_PASSWORD);
        request.setEmail("another@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    @Order(3)
    @DisplayName("用户登录 - 成功")
    void testLoginSuccess() throws Exception {
        // 先创建用户
        createTestUser();

        LoginRequest request = new LoginRequest();
        request.setUsername(TEST_USERNAME);
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.username").value(TEST_USERNAME));
    }

    @Test
    @Order(4)
    @DisplayName("用户登录 - 密码错误")
    void testLoginWrongPassword() throws Exception {
        // 先创建用户
        createTestUser();

        LoginRequest request = new LoginRequest();
        request.setUsername(TEST_USERNAME);
        request.setPassword("WrongPassword@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @Order(5)
    @DisplayName("用户登录 - 用户不存在")
    void testLoginUserNotFound() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent_user");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @Order(6)
    @DisplayName("获取当前用户信息 - 成功")
    void testGetCurrentUserSuccess() throws Exception {
        // 先登录获取token
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.data.email").value(TEST_EMAIL));
    }

    @Test
    @Order(7)
    @DisplayName("获取当前用户信息 - 未登录")
    void testGetCurrentUserUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    @DisplayName("刷新Token - 成功")
    void testRefreshTokenSuccess() throws Exception {
        // 先登录获取token
        loginAndGetToken();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    @Order(9)
    @DisplayName("修改密码 - 成功")
    void testChangePasswordSuccess() throws Exception {
        // 先登录获取token
        String token = loginAndGetToken();

        String newPassword = "NewPassword@456";
        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\": \"" + TEST_PASSWORD + "\", \"newPassword\": \"" + newPassword + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("密码修改成功，请重新登录"));
    }

    @Test
    @Order(10)
    @DisplayName("修改密码 - 旧密码错误")
    void testChangePasswordWrongOldPassword() throws Exception {
        // 先登录获取token
        String token = loginAndGetToken();

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\": \"WrongOldPassword@123\", \"newPassword\": \"NewPassword@456\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("旧密码错误"));
    }

    @Test
    @Order(11)
    @DisplayName("用户登出 - 成功")
    void testLogoutSuccess() throws Exception {
        // 先登录获取token
        String token = loginAndGetToken();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登出成功"));
    }

    @Test
    @Order(12)
    @DisplayName("完整认证流程测试")
    void testFullAuthFlow() throws Exception {
        // 1. 注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("flow_test_user");
        registerRequest.setPassword("FlowTest@123");
        registerRequest.setEmail("flow_test@example.com");
        registerRequest.setPhone("13800138002");

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = extractToken(registerResponse);

        // 2. 获取用户信息
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("flow_test_user"));

        // 3. 登出
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk());

        // 4. 登出后token应失效
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isUnauthorized());

        // 清理
        userRepository.deleteByUsername("flow_test_user");
    }

    // ========== 辅助方法 ==========

    private void createTestUser() {
        if (userRepository.findByUsername(TEST_USERNAME).isEmpty()) {
            User user = new User();
            user.setUsername(TEST_USERNAME);
            user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
            user.setEmail(TEST_EMAIL);
            user.setPhone("13800138001");
            user.setNickname("集成测试用户");
            user.setRole(User.Role.STUDENT);
            user.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(user);
        }
    }

    private String loginAndGetToken() throws Exception {
        createTestUser();

        LoginRequest request = new LoginRequest();
        request.setUsername(TEST_USERNAME);
        request.setPassword(TEST_PASSWORD);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractToken(response);
    }

    private String extractToken(String response) throws Exception {
        return objectMapper.readTree(response)
                .path("data")
                .path("token")
                .asText();
    }

    private String extractRefreshToken(String response) throws Exception {
        return objectMapper.readTree(response)
                .path("data")
                .path("refreshToken")
                .asText();
    }
}
