package com.idleitems.school.integration;

import com.idleitems.school.BaseIntegrationTest;
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
 * 聊天模块集成测试
 */
@DisplayName("聊天模块集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ChatIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static Long buyerId;
    private static Long sellerId;
    private static String buyerToken;
    private static String sellerToken;

    @Test
    @Order(1)
    @DisplayName("创建测试用户")
    void createTestUsers() throws Exception {
        // 创建买家
        User buyer = new User();
        buyer.setUsername("chat_buyer");
        buyer.setPassword(passwordEncoder.encode("TestPassword@123"));
        buyer.setEmail("buyer@chat.com");
        buyer.setPhone("13800138000");
        buyer.setNickname("聊天买家");
        buyer.setRole(User.Role.STUDENT);
        buyer.setStatus(User.UserStatus.ACTIVE);
        User savedBuyer = userRepository.save(buyer);
        buyerId = savedBuyer.getId();

        // 创建卖家
        User seller = new User();
        seller.setUsername("chat_seller");
        seller.setPassword(passwordEncoder.encode("TestPassword@123"));
        seller.setEmail("seller@chat.com");
        seller.setPhone("13800138001");
        seller.setNickname("聊天卖家");
        seller.setRole(User.Role.STUDENT);
        seller.setStatus(User.UserStatus.ACTIVE);
        User savedSeller = userRepository.save(seller);
        sellerId = savedSeller.getId();

        // 登录获取token
        buyerToken = loginAndGetToken("chat_buyer", "TestPassword@123");
        sellerToken = loginAndGetToken("chat_seller", "TestPassword@123");
    }

    @Test
    @Order(2)
    @DisplayName("创建聊天会话")
    void testCreateChat() throws Exception {
        mockMvc.perform(post("/api/chats")
                        .header("Authorization", bearerToken(buyerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sellerId\": " + sellerId + ", \"itemId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    @Order(3)
    @DisplayName("发送消息")
    void testSendMessage() throws Exception {
        // 先创建聊天
        String chatResponse = mockMvc.perform(post("/api/chats")
                        .header("Authorization", bearerToken(buyerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sellerId\": " + sellerId + ", \"itemId\": 1}"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long chatId = objectMapper.readTree(chatResponse).path("data").path("id").asLong();

        // 发送消息
        mockMvc.perform(post("/api/chats/" + chatId + "/messages")
                        .header("Authorization", bearerToken(buyerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\": \"你好，请问这个物品还在吗？\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").value("你好，请问这个物品还在吗？"));
    }

    @Test
    @Order(4)
    @DisplayName("获取聊天消息")
    void testGetMessages() throws Exception {
        // 先创建聊天并发送消息
        String chatResponse = mockMvc.perform(post("/api/chats")
                        .header("Authorization", bearerToken(buyerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sellerId\": " + sellerId + ", \"itemId\": 1}"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long chatId = objectMapper.readTree(chatResponse).path("data").path("id").asLong();

        mockMvc.perform(post("/api/chats/" + chatId + "/messages")
                        .header("Authorization", bearerToken(buyerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\": \"测试消息\"}"));

        // 获取消息列表
        mockMvc.perform(get("/api/chats/" + chatId + "/messages")
                        .header("Authorization", bearerToken(buyerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").path("token").asText();
    }
}
