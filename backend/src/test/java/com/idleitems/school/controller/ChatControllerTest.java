package com.idleitems.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import static org.mockito.ArgumentMatchers.argThat;
import com.idleitems.school.module.chat.entity.Chat;
import com.idleitems.school.module.chat.entity.ChatMessage;
import com.idleitems.school.module.chat.service.ChatService;
import com.idleitems.school.module.chat.controller.ChatController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ChatController 接口测试")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    private Chat testChat;
    private ChatMessage testMessage;

    @BeforeEach
    void setUp() {
        testChat = new Chat();
        testChat.setId(1L);
        testChat.setBuyerId(1L);
        testChat.setSellerId(2L);
        testChat.setItemId(1L);
        testChat.setCreatedAt(LocalDateTime.now());

        testMessage = new ChatMessage();
        testMessage.setId(1L);
        testMessage.setChatId(1L);
        testMessage.setSenderId(1L);
        testMessage.setReceiverId(2L);
        testMessage.setContent("你好，请问物品还在吗？");
        testMessage.setMessageType(ChatMessage.MessageType.TEXT);
        testMessage.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("创建聊天会话 - 成功")
    void testCreateChatSuccess() throws Exception {
        when(chatService.createChat(1L, 2L, 1L)).thenReturn(testChat);

        mockMvc.perform(post("/api/chats")
                        .requestAttr("userId", 1L)
                        .param("sellerId", "2")
                        .param("itemId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("聊天会话创建成功"));
    }

    @Test
    @DisplayName("获取用户聊天列表 - 成功")
    void testGetUserChatsSuccess() throws Exception {
        when(chatService.getChatsByUserIdListWithUserInfo(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/chats")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取聊天消息 - 成功")
    void testGetMessagesSuccess() throws Exception {
        Page<ChatMessage> messagePage = new PageImpl<>(List.of(testMessage), PageRequest.of(0, 20), 1);
        when(chatService.getMessagesByChatId(eq(1L), eq(1L), any())).thenReturn(messagePage);
        doNothing().when(chatService).markMessagesAsReadAsync(1L, 1L);

        mockMvc.perform(get("/api/chats/1/messages")
                        .requestAttr("userId", 1L)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("发送消息 - 成功")
    void testSendMessageSuccess() throws Exception {
        when(chatService.sendMessage(eq(1L), eq(1L), eq(2L), eq("你好"), any())).thenReturn(testMessage);
        doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        mockMvc.perform(post("/api/chats/1/messages")
                        .requestAttr("userId", 1L)
                        .param("receiverId", "2")
                        .param("content", "你好"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("消息发送成功"));
    }

    @Test
    @DisplayName("发送消息 - 内容为空")
    void testSendMessageContentBlank() throws Exception {
        mockMvc.perform(post("/api/chats/1/messages")
                        .requestAttr("userId", 1L)
                        .param("receiverId", "2")
                        .param("content", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("发送消息 - 内容超长")
    void testSendMessageContentTooLong() throws Exception {
        String longContent = "a".repeat(2001);
        when(chatService.sendMessage(eq(1L), eq(1L), eq(2L), argThat(c -> c.length() > 2000), any()))
                .thenThrow(new BusinessException(ErrorCode.BAD_REQUEST, "消息内容不能超过2000个字符"));

        mockMvc.perform(post("/api/chats/1/messages")
                        .requestAttr("userId", 1L)
                        .param("receiverId", "2")
                        .param("content", longContent))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("撤回消息 - 成功")
    void testRecallMessageSuccess() throws Exception {
        when(chatService.recallMessage(1L, 1L)).thenReturn(testMessage);
        doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));
        when(chatService.getChatsByUserId(eq(1L), any())).thenReturn(new PageImpl<>(List.of(testChat), PageRequest.of(0, 20), 1));

        mockMvc.perform(post("/api/chats/1/messages/1/recall")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("消息已撤回"));
    }

    @Test
    @DisplayName("标记消息已读 - 成功")
    void testMarkAsReadSuccess() throws Exception {
        doNothing().when(chatService).markMessagesAsRead(1L, 1L);

        mockMvc.perform(post("/api/chats/1/read")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("已标记为已读"));
    }
}
