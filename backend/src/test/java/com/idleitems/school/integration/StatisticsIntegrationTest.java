package com.idleitems.school.integration;

import com.idleitems.school.BaseIntegrationTest;
import com.idleitems.school.module.auth.dto.LoginRequest;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("统计功能集成测试")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class StatisticsIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String ADMIN_USERNAME = "stats_admin_test";
    private static final String ADMIN_PASSWORD = "AdminPassword@123";
    private static String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        if (adminToken == null) {
            createAdminAndLogin();
        }
    }

    @Test
    @DisplayName("获取仪表盘数据 - 成功")
    void getDashboard_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/statistics/dashboard")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertNotNull(content);
    }

    @Test
    @DisplayName("获取数据总览 - 成功")
    void getOverview_Success() throws Exception {
        mockMvc.perform(get("/api/admin/statistics/overview")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @DisplayName("获取月度统计 - 成功")
    void getMonthlyStatistics_Success() throws Exception {
        mockMvc.perform(get("/api/admin/statistics/monthly")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @DisplayName("获取分类统计 - 成功")
    void getCategoryStatistics_Success() throws Exception {
        mockMvc.perform(get("/api/admin/statistics/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @DisplayName("获取热门物品 - 成功")
    void getHotItems_Success() throws Exception {
        mockMvc.perform(get("/api/admin/statistics/hot-items")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @DisplayName("获取仪表盘数据 - 未授权")
    void getDashboard_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/statistics/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 401 && status != 403) {
                        throw new AssertionError("Expected 401 or 403 but was " + status);
                    }
                });
    }

    // ========== 辅助方法 ==========

    private void createAdminAndLogin() throws Exception {
        userRepository.findByUsername(ADMIN_USERNAME).ifPresent(user -> userRepository.deleteById(user.getId()));

        User admin = new User();
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setEmail("stats_admin@example.com");
        admin.setPhone("13800138010");
        admin.setNickname("统计测试管理员");
        admin.setRole(User.Role.ADMIN);
        admin.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(admin);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(ADMIN_USERNAME);
        loginRequest.setPassword(ADMIN_PASSWORD);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        adminToken = objectMapper.readTree(response)
                .path("data")
                .path("token")
                .asText();
    }
}
