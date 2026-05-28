package com.idleitems.school.integration;

import com.idleitems.school.BaseIntegrationTest;
import com.idleitems.school.dto.CreateItemRequest;
import com.idleitems.school.dto.LoginRequest;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.UserRepository;
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
    private static Long testItemId;

    @BeforeAll
    static void setUpClass() {
        // 测试类开始前的准备工作
    }

    @BeforeEach
    void setUp() {
        // 清理测试数据
        itemRepository.deleteAll();
        userRepository.deleteByUsername(TEST_USERNAME);
    }

    @Test
    @Order(1)
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
        userRepository.save(user);

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
    @Order(2)
    @DisplayName("发布物品 - 成功")
    void testCreateItemSuccess() throws Exception {
        CreateItemRequest request = new CreateItemRequest();
        request.setTitle("测试物品 - 全新iPhone 15");
        request.setDescription("这是一部全新的iPhone 15，未拆封，因为买多了想出售");
        request.setPrice(new BigDecimal("5999.00"));
        request.setOriginalPrice(new BigDecimal("6999.00"));
        request.setCondition("全新");
        request.setDeliveryMethod("面交");
        request.setLocation("北京大学");
        request.setCategoryId(1L);

        String response = mockMvc.perform(post("/api/items")
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("发布成功，等待审核"))
                .andExpect(jsonPath("$.data.title").value("测试物品 - 全新iPhone 15"))
                .andExpect(jsonPath("$.data.price").value(5999.00))
                .andReturn()
                .getResponse()
                .getContentAsString();

        testItemId = extractItemId(response);
    }

    @Test
    @Order(3)
    @DisplayName("发布物品 - 参数校验失败（标题为空）")
    void testCreateItemValidationTitleBlank() throws Exception {
        CreateItemRequest request = new CreateItemRequest();
        request.setTitle("");
        request.setDescription("测试描述内容");
        request.setPrice(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/items")
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @Order(4)
    @DisplayName("发布物品 - 参数校验失败（价格为负数）")
    void testCreateItemValidationNegativePrice() throws Exception {
        CreateItemRequest request = new CreateItemRequest();
        request.setTitle("测试物品");
        request.setDescription("测试描述内容");
        request.setPrice(new BigDecimal("-100.00"));

        mockMvc.perform(post("/api/items")
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"));
    }

    @Test
    @Order(5)
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
    @Order(6)
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
    @Order(7)
    @DisplayName("搜索物品 - 关键字为空")
    void testSearchItemsEmptyKeyword() throws Exception {
        mockMvc.perform(get("/api/items/search")
                        .param("keyword", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(8)
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
    @Order(9)
    @DisplayName("获取物品详情 - 物品不存在")
    void testGetItemDetailNotFound() throws Exception {
        mockMvc.perform(get("/api/items/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("物品不存在"));
    }

    @Test
    @Order(10)
    @DisplayName("获取用户物品列表 - 成功")
    void testGetUserItemsSuccess() throws Exception {
        // 先创建物品
        createTestItem();

        mockMvc.perform(get("/api/items/user")
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", getUserId())
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @Order(11)
    @DisplayName("更新物品 - 成功")
    void testUpdateItemSuccess() throws Exception {
        // 先创建物品
        Long itemId = createTestItem();

        CreateItemRequest updateRequest = new CreateItemRequest();
        updateRequest.setTitle("更新后的物品标题");
        updateRequest.setDescription("更新后的描述内容，这是一个很长的描述");
        updateRequest.setPrice(new BigDecimal("4999.00"));
        updateRequest.setCondition("九成新");
        updateRequest.setDeliveryMethod("邮寄");
        updateRequest.setLocation("清华大学");

        mockMvc.perform(put("/api/items/" + itemId)
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新成功，等待审核"));
    }

    @Test
    @Order(12)
    @DisplayName("下架物品 - 成功")
    void testOffShelfItemSuccess() throws Exception {
        // 先创建物品
        Long itemId = createTestItem();

        mockMvc.perform(post("/api/items/" + itemId + "/off-shelf")
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("下架成功"));
    }

    @Test
    @Order(13)
    @DisplayName("删除物品 - 成功")
    void testDeleteItemSuccess() throws Exception {
        // 先创建物品
        Long itemId = createTestItem();

        mockMvc.perform(delete("/api/items/" + itemId)
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));
    }

    @Test
    @Order(14)
    @DisplayName("完整物品流程测试")
    void testFullItemFlow() throws Exception {
        // 1. 发布物品
        CreateItemRequest createRequest = new CreateItemRequest();
        createRequest.setTitle("流程测试物品");
        createRequest.setDescription("这是一个流程测试物品的描述");
        createRequest.setPrice(new BigDecimal("100.00"));
        createRequest.setCondition("九成新");
        createRequest.setDeliveryMethod("面交");
        createRequest.setLocation("北京大学");
        createRequest.setCategoryId(1L);

        String createResponse = mockMvc.perform(post("/api/items")
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long itemId = extractItemId(createResponse);

        // 2. 获取物品详情
        mockMvc.perform(get("/api/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("流程测试物品"));

        // 3. 更新物品
        CreateItemRequest updateRequest = new CreateItemRequest();
        updateRequest.setTitle("更新后的流程测试物品");
        updateRequest.setDescription("更新后的描述");
        updateRequest.setPrice(new BigDecimal("200.00"));
        updateRequest.setCondition("全新");
        updateRequest.setDeliveryMethod("邮寄");
        updateRequest.setLocation("清华大学");

        mockMvc.perform(put("/api/items/" + itemId)
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateRequest)))
                .andExpect(status().isOk());

        // 4. 下架物品
        mockMvc.perform(post("/api/items/" + itemId + "/off-shelf")
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", getUserId()))
                .andExpect(status().isOk());

        // 5. 删除物品
        mockMvc.perform(delete("/api/items/" + itemId)
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", getUserId()))
                .andExpect(status().isOk());
    }

    // ========== 辅助方法 ==========

    private Long getUserId() {
        return userRepository.findByUsername(TEST_USERNAME)
                .map(User::getId)
                .orElse(1L);
    }

    private Long createTestItem() throws Exception {
        CreateItemRequest request = new CreateItemRequest();
        request.setTitle("测试物品 - iPhone 15");
        request.setDescription("这是一个测试物品的描述，用于集成测试");
        request.setPrice(new BigDecimal("5999.00"));
        request.setCondition("全新");
        request.setDeliveryMethod("面交");
        request.setLocation("北京大学");
        request.setCategoryId(1L);

        String response = mockMvc.perform(post("/api/items")
                        .header("Authorization", bearerToken(authToken))
                        .requestAttr("userId", getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractItemId(response);
    }

    private String extractToken(String response) throws Exception {
        return objectMapper.readTree(response)
                .path("data")
                .path("token")
                .asText();
    }

    private Long extractItemId(String response) throws Exception {
        return objectMapper.readTree(response)
                .path("data")
                .path("id")
                .asLong();
    }
}
