package com.idleitems.school.integration;

import com.idleitems.school.BaseIntegrationTest;
import com.idleitems.school.module.auth.dto.LoginRequest;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 物品模块集成测试
 */
@DisplayName("物品模块集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ItemIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_USERNAME = "item_test_user";
    private static final String TEST_PASSWORD = "TestPassword@123";
    private static String authToken;
    private static Long testUserId;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        itemRepository.deleteAll();
        userRepository.findByUsername(TEST_USERNAME).ifPresent(user -> userRepository.deleteById(user.getId()));
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("创建测试用户并登录")
    void testCreateUserAndLogin() throws Exception {
        // 创建测试用户
        User user = new User();
        user.setUsername(TEST_USERNAME);
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setEmail("item_test@example.com");
        user.setPhone("13800138003");
        user.setNickname("物品测试用户");
        user.setRole(User.Role.STUDENT);
        user.setStatus(User.UserStatus.ACTIVE);
        User saved = userRepository.save(user);
        testUserId = saved.getId();

        // 登录获取token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        authToken = extractToken(response);
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("发布物品 - 成功")
    void testCreateItemSuccess() throws Exception {
        testCreateUserAndLogin();

        Item item = new Item();
        item.setTitle("测试物品 - 全新iPhone 15");
        item.setDescription("这是一部全新的iPhone 15，未拆封，因为买多了想出售");
        item.setPrice(new BigDecimal("5999.00"));
        item.setOriginalPrice(new BigDecimal("6999.00"));
        item.setCondition(Item.ItemCondition.NEW);
        item.setDeliveryMethod("面交");
        item.setLocation("北京大学");
        item.setCategoryId(1L);

        String response = mockMvc.perform(post("/api/items")
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("测试物品 - 全新iPhone 15"))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("获取物品列表 - 成功")
    void testGetItemsSuccess() throws Exception {
        // 先创建物品
        createTestItem();

        mockMvc.perform(get("/api/items")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("搜索物品 - 成功")
    void testSearchItemsSuccess() throws Exception {
        // 先创建物品
        createTestItem();

        mockMvc.perform(get("/api/items/search")
                        .param("keyword", "iPhone")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("搜索物品 - 关键字为空")
    void testSearchItemsEmptyKeyword() throws Exception {
        mockMvc.perform(get("/api/items/search")
                        .param("keyword", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("获取物品详情 - 成功")
    void testGetItemDetailSuccess() throws Exception {
        // 先创建物品
        Long itemId = createTestItem();

        mockMvc.perform(get("/api/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(itemId))
                .andExpect(jsonPath("$.data.title").exists());
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("获取物品详情 - 物品不存在")
    void testGetItemDetailNotFound() throws Exception {
        mockMvc.perform(get("/api/items/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    @DisplayName("获取用户物品列表 - 成功")
    void testGetUserItemsSuccess() throws Exception {
        // 先创建物品
        createTestItem();

        mockMvc.perform(get("/api/items/user")
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", testUserId)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @org.junit.jupiter.api.Order(9)
    @DisplayName("下架物品 - 成功")
    void testOffShelfItemSuccess() throws Exception {
        // 先创建物品
        Long itemId = createTestItem();

        mockMvc.perform(post("/api/items/" + itemId + "/off-shelf")
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("下架成功"));
    }

    @Test
    @org.junit.jupiter.api.Order(10)
    @DisplayName("删除物品 - 成功")
    void testDeleteItemSuccess() throws Exception {
        // 先创建物品
        Long itemId = createTestItem();

        mockMvc.perform(delete("/api/items/" + itemId)
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));
    }

    @Test
    @org.junit.jupiter.api.Order(11)
    @DisplayName("完整物品流程测试")
    void testFullItemFlow() throws Exception {
        testCreateUserAndLogin();

        // 1. 发布物品
        Item item = new Item();
        item.setTitle("流程测试物品");
        item.setDescription("这是一个流程测试物品的描述");
        item.setPrice(new BigDecimal("100.00"));
        item.setCondition(Item.ItemCondition.GOOD);
        item.setDeliveryMethod("面交");
        item.setLocation("北京大学");
        item.setCategoryId(1L);

        String createResponse = mockMvc.perform(post("/api/items")
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(item)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long itemId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        // 2. 获取物品详情
        mockMvc.perform(get("/api/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("流程测试物品"));

        // 3. 下架物品
        mockMvc.perform(post("/api/items/" + itemId + "/off-shelf")
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", testUserId))
                .andExpect(status().isOk());

        // 4. 删除物品
        mockMvc.perform(delete("/api/items/" + itemId)
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", testUserId))
                .andExpect(status().isOk());
    }

    // ========== 辅助方法 ==========

    private Long createTestItem() throws Exception {
        if (authToken == null) {
            testCreateUserAndLogin();
        }

        Item item = new Item();
        item.setTitle("测试物品 - iPhone 15");
        item.setDescription("这是一个测试物品的描述，用于集成测试");
        item.setPrice(new BigDecimal("5999.00"));
        item.setCondition(Item.ItemCondition.NEW);
        item.setDeliveryMethod("面交");
        item.setLocation("北京大学");
        item.setCategoryId(1L);

        String response = mockMvc.perform(post("/api/items")
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(item)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private String extractToken(String response) throws Exception {
        return objectMapper.readTree(response)
                .path("data")
                .path("token")
                .asText();
    }
}
