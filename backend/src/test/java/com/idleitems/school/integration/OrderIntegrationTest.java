package com.idleitems.school.integration;

import com.idleitems.school.BaseIntegrationTest;
import com.idleitems.school.module.auth.dto.LoginRequest;
import com.idleitems.school.module.order.dto.CreateOrderRequest;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.order.repository.OrderRepository;
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
 * 订单模块集成测试
 */
@DisplayName("订单模块集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String BUYER_USERNAME = "order_buyer_test";
    private static final String SELLER_USERNAME = "order_seller_test";
    private static final String TEST_PASSWORD = "TestPassword@123";

    private static String buyerToken;
    private static String sellerToken;
    private static Long buyerId;
    private static Long sellerId;
    private static Long testItemId;
    private static Long testOrderId;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        orderRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.findByUsername(BUYER_USERNAME).ifPresent(user -> userRepository.deleteById(user.getId()));
        userRepository.findByUsername(SELLER_USERNAME).ifPresent(user -> userRepository.deleteById(user.getId()));
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("创建测试用户并登录")
    void testCreateUsersAndLogin() throws Exception {
        // 创建买家
        User buyer = new User();
        buyer.setUsername(BUYER_USERNAME);
        buyer.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        buyer.setEmail("buyer_test@example.com");
        buyer.setPhone("13800138004");
        buyer.setNickname("买家测试用户");
        buyer.setRole(User.Role.STUDENT);
        buyer.setStatus(User.UserStatus.ACTIVE);
        User savedBuyer = userRepository.save(buyer);
        buyerId = savedBuyer.getId();

        // 创建卖家
        User seller = new User();
        seller.setUsername(SELLER_USERNAME);
        seller.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        seller.setEmail("seller_test@example.com");
        seller.setPhone("13800138005");
        seller.setNickname("卖家测试用户");
        seller.setRole(User.Role.STUDENT);
        seller.setStatus(User.UserStatus.ACTIVE);
        User savedSeller = userRepository.save(seller);
        sellerId = savedSeller.getId();

        // 买家登录
        LoginRequest buyerLogin = new LoginRequest();
        buyerLogin.setUsername(BUYER_USERNAME);
        buyerLogin.setPassword(TEST_PASSWORD);

        String buyerResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(buyerLogin)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        buyerToken = extractToken(buyerResponse);

        // 卖家登录
        LoginRequest sellerLogin = new LoginRequest();
        sellerLogin.setUsername(SELLER_USERNAME);
        sellerLogin.setPassword(TEST_PASSWORD);

        String sellerResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(sellerLogin)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        sellerToken = extractToken(sellerResponse);
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("创建测试物品")
    void testCreateTestItem() throws Exception {
        // 先创建用户
        testCreateUsersAndLogin();

        // 卖家发布物品
        Item item = new Item();
        item.setUserId(sellerId);
        item.setTitle("订单测试物品");
        item.setDescription("这是一个用于订单测试的物品");
        item.setPrice(new BigDecimal("100.00"));
        item.setOriginalPrice(new BigDecimal("150.00"));
        item.setCondition(Item.ItemCondition.GOOD);
        item.setDeliveryMethod("面交");
        item.setLocation("北京大学");
        item.setStatus(Item.ItemStatus.ON_SALE);
        item.setCategoryId(1L);

        Item savedItem = itemRepository.save(item);
        testItemId = savedItem.getId();
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("创建订单 - 成功")
    void testCreateOrderSuccess() throws Exception {
        // 先创建物品
        testCreateTestItem();

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItemId(testItemId);
        request.setBuyerName("测试买家");
        request.setBuyerPhone("13800138000");
        request.setBuyerAddress("北京市海淀区");
        request.setPaymentMethod("OFFLINE");

        String response = mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearerToken(buyerToken))
                        .requestAttr("userId", buyerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").exists())
                .andExpect(jsonPath("$.data.orderStatus").value("PENDING_PAYMENT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        testOrderId = objectMapper.readTree(response).path("data").path("id").asLong();
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("创建订单 - 物品不存在")
    void testCreateOrderItemNotFound() throws Exception {
        testCreateUsersAndLogin();

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItemId(99999L);
        request.setBuyerName("测试买家");
        request.setBuyerPhone("13800138000");
        request.setBuyerAddress("北京市海淀区");
        request.setPaymentMethod("OFFLINE");

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearerToken(buyerToken))
                        .requestAttr("userId", buyerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("获取买家订单列表 - 成功")
    void testGetBuyerOrdersSuccess() throws Exception {
        // 先创建订单
        testCreateOrderSuccess();

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", bearerToken(buyerToken))
                        .requestAttr("userId", buyerId)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("获取卖家订单列表 - 成功")
    void testGetSellerOrdersSuccess() throws Exception {
        // 先创建订单
        testCreateOrderSuccess();

        mockMvc.perform(get("/api/orders/seller")
                        .header("Authorization", bearerToken(sellerToken))
                        .requestAttr("userId", sellerId)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("获取订单详情 - 成功")
    void testGetOrderDetailSuccess() throws Exception {
        // 先创建订单
        testCreateOrderSuccess();

        mockMvc.perform(get("/api/orders/" + testOrderId)
                        .header("Authorization", bearerToken(buyerToken))
                        .requestAttr("userId", buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testOrderId));
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    @DisplayName("支付订单 - 成功")
    void testPayOrderSuccess() throws Exception {
        // 先创建订单
        testCreateOrderSuccess();

        mockMvc.perform(post("/api/orders/" + testOrderId + "/pay")
                        .header("Authorization", bearerToken(buyerToken))
                        .requestAttr("userId", buyerId)
                        .param("paymentMethod", "OFFLINE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("支付成功"));
    }

    @Test
    @org.junit.jupiter.api.Order(9)
    @DisplayName("取消订单 - 成功")
    void testCancelOrderSuccess() throws Exception {
        // 先创建订单
        testCreateOrderSuccess();

        mockMvc.perform(post("/api/orders/" + testOrderId + "/cancel")
                        .header("Authorization", bearerToken(buyerToken))
                        .requestAttr("userId", buyerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"不想要了\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("订单已取消"));
    }

    @Test
    @org.junit.jupiter.api.Order(10)
    @DisplayName("发货 - 成功")
    void testShipOrderSuccess() throws Exception {
        // 先创建订单并支付
        testPayOrderSuccess();

        mockMvc.perform(post("/api/orders/" + testOrderId + "/ship")
                        .header("Authorization", bearerToken(sellerToken))
                        .requestAttr("userId", sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("发货成功"));
    }

    @Test
    @org.junit.jupiter.api.Order(11)
    @DisplayName("确认收货 - 成功")
    void testConfirmReceiveSuccess() throws Exception {
        // 先创建订单、支付、发货
        testShipOrderSuccess();

        mockMvc.perform(post("/api/orders/" + testOrderId + "/confirm-receive")
                        .header("Authorization", bearerToken(buyerToken))
                        .requestAttr("userId", buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("确认收货成功"));
    }

    @Test
    @org.junit.jupiter.api.Order(12)
    @DisplayName("完整订单流程测试")
    void testFullOrderFlow() throws Exception {
        // 1. 创建测试物品
        testCreateTestItem();

        // 2. 买家创建订单
        CreateOrderRequest createRequest = new CreateOrderRequest();
        createRequest.setItemId(testItemId);
        createRequest.setBuyerName("流程测试买家");
        createRequest.setBuyerPhone("13800138000");
        createRequest.setBuyerAddress("北京市海淀区");
        createRequest.setPaymentMethod("OFFLINE");

        String createResponse = mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearerToken(buyerToken))
                        .requestAttr("userId", buyerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long orderId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        // 3. 买家支付订单
        mockMvc.perform(post("/api/orders/" + orderId + "/pay")
                        .header("Authorization", bearerToken(buyerToken))
                        .requestAttr("userId", buyerId)
                        .param("paymentMethod", "OFFLINE"))
                .andExpect(status().isOk());

        // 4. 卖家发货
        mockMvc.perform(post("/api/orders/" + orderId + "/ship")
                        .header("Authorization", bearerToken(sellerToken))
                        .requestAttr("userId", sellerId))
                .andExpect(status().isOk());

        // 5. 买家确认收货
        mockMvc.perform(post("/api/orders/" + orderId + "/confirm-receive")
                        .header("Authorization", bearerToken(buyerToken))
                        .requestAttr("userId", buyerId))
                .andExpect(status().isOk());

        // 6. 验证订单状态
        mockMvc.perform(get("/api/orders/" + orderId)
                        .header("Authorization", bearerToken(buyerToken))
                        .requestAttr("userId", buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderStatus").value("COMPLETED"));
    }

    @Test
    @org.junit.jupiter.api.Order(13)
    @DisplayName("订单状态流转测试 - 不能支付已取消的订单")
    void testCannotPayCancelledOrder() throws Exception {
        // 先创建订单
        testCreateOrderSuccess();

        // 取消订单
        mockMvc.perform(post("/api/orders/" + testOrderId + "/cancel")
                        .header("Authorization", bearerToken(buyerToken))
                        .requestAttr("userId", buyerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"不想要了\"}"))
                .andExpect(status().isOk());

        // 尝试支付已取消的订单
        mockMvc.perform(post("/api/orders/" + testOrderId + "/pay")
                        .header("Authorization", bearerToken(buyerToken))
                        .requestAttr("userId", buyerId)
                        .param("paymentMethod", "OFFLINE"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    // ========== 辅助方法 ==========

    private String extractToken(String response) throws Exception {
        return objectMapper.readTree(response)
                .path("data")
                .path("token")
                .asText();
    }
}
