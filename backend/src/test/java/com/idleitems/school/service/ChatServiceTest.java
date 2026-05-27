package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.dto.ChatDTO;
import com.idleitems.school.entity.Chat;
import com.idleitems.school.entity.ChatMessage;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.ChatMessageRepository;
import com.idleitems.school.repository.ChatRepository;
import com.idleitems.school.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService chatService;

    private Chat chat;
    private User buyer;
    private User seller;

    @BeforeEach
    void setUp() {
        chat = new Chat();
        chat.setId(1L);
        chat.setBuyerId(1L);
        chat.setSellerId(2L);
        chat.setItemId(100L);

        buyer = new User();
        buyer.setId(1L);
        buyer.setNickname("Buyer");
        buyer.setUsername("buyerUser");
        buyer.setAvatar("avatar1.jpg");

        seller = new User();
        seller.setId(2L);
        seller.setNickname("Seller");
        seller.setUsername("sellerUser");
        seller.setAvatar("avatar2.jpg");
    }

    @Test
    void createChat_ExistingChat_ReturnsExisting() {
        Page<Chat> page = new PageImpl<>(List.of(chat));
        when(chatRepository.findByBuyerIdOrSellerId(eq(1L), eq(2L), any(Pageable.class))).thenReturn(page);

        Chat result = chatService.createChat(1L, 2L, 100L);

        assertSame(chat, result);
        verify(chatRepository, never()).save(any());
    }

    @Test
    void createChat_NewChat_CreatesAndReturns() {
        when(chatRepository.findByBuyerIdOrSellerId(eq(1L), eq(2L), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(chatRepository.save(any(Chat.class))).thenReturn(chat);

        Chat result = chatService.createChat(1L, 2L, 100L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getItemId());
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void sendMessage_ValidSender_SavesMessage() {
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        ChatMessage saved = new ChatMessage();
        saved.setId(1L);
        saved.setChatId(1L);
        saved.setSenderId(1L);
        saved.setReceiverId(2L);
        saved.setContent("Hello");
        saved.setMessageType(ChatMessage.MessageType.TEXT);
        saved.setIsRead(false);
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(saved);

        ChatMessage result = chatService.sendMessage(1L, 1L, 2L, "Hello", ChatMessage.MessageType.TEXT);

        assertNotNull(result);
        assertEquals("Hello", result.getContent());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    void sendMessage_InvalidSender_ThrowsException() {
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> chatService.sendMessage(1L, 3L, 2L, "Hello", ChatMessage.MessageType.TEXT));

        assertEquals("无权发送消息", ex.getMessage());
        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    void getMessagesByChatId_ValidUser_ReturnsMessages() {
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        ChatMessage msg = new ChatMessage();
        msg.setId(1L);
        msg.setChatId(1L);
        msg.setSenderId(2L);
        msg.setReceiverId(1L);
        msg.setContent("Hello");
        msg.setIsRead(false);
        Page<ChatMessage> messagePage = new PageImpl<>(List.of(msg));
        when(chatMessageRepository.findByChatIdOrderByCreatedAtAsc(eq(1L), any(Pageable.class)))
                .thenReturn(messagePage);

        Page<ChatMessage> result = chatService.getMessagesByChatId(1L, 1L, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getMessagesByChatId_InvalidUser_ThrowsException() {
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> chatService.getMessagesByChatId(1L, 3L, Pageable.unpaged()));

        assertEquals("无权查看消息", ex.getMessage());
    }

    @Test
    void getMessagesByChatId_MarksMessagesAsRead() {
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        ChatMessage receivedMsg = new ChatMessage();
        receivedMsg.setId(1L);
        receivedMsg.setChatId(1L);
        receivedMsg.setSenderId(2L);
        receivedMsg.setReceiverId(1L);
        receivedMsg.setContent("Hello");
        receivedMsg.setIsRead(false);

        ChatMessage sentMsg = new ChatMessage();
        sentMsg.setId(2L);
        sentMsg.setChatId(1L);
        sentMsg.setSenderId(1L);
        sentMsg.setReceiverId(2L);
        sentMsg.setContent("Hi back");
        sentMsg.setIsRead(false);

        Page<ChatMessage> messagePage = new PageImpl<>(List.of(receivedMsg, sentMsg));
        when(chatMessageRepository.findByChatIdOrderByCreatedAtAsc(eq(1L), any(Pageable.class)))
                .thenReturn(messagePage);

        Page<ChatMessage> result = chatService.getMessagesByChatId(1L, 1L, Pageable.unpaged());

        assertTrue(result.getContent().get(0).getIsRead());
        assertNotNull(result.getContent().get(0).getReadAt());
        assertFalse(result.getContent().get(1).getIsRead());
        assertNull(result.getContent().get(1).getReadAt());
    }

    @Test
    void getChatsByUserIdListWithUserInfo_ReturnsDTOs() {
        when(chatRepository.findAllChatsByUserId(1L)).thenReturn(List.of(chat));
        when(userRepository.findAllById(anySet())).thenReturn(List.of(buyer, seller));

        ChatMessage lastMsg = new ChatMessage();
        lastMsg.setId(1L);
        lastMsg.setContent("最后一条消息");
        lastMsg.setSenderId(2L);
        lastMsg.setCreatedAt(LocalDateTime.now());
        when(chatMessageRepository.findByChatIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(lastMsg));

        List<ChatDTO> result = chatService.getChatsByUserIdListWithUserInfo(1L);

        assertEquals(1, result.size());
        ChatDTO dto = result.get(0);
        assertEquals("Buyer", dto.getBuyerNickname());
        assertEquals("buyerUser", dto.getBuyerUsername());
        assertEquals("avatar1.jpg", dto.getBuyerAvatar());
        assertEquals("Seller", dto.getSellerNickname());
        assertEquals("sellerUser", dto.getSellerUsername());
        assertEquals("avatar2.jpg", dto.getSellerAvatar());
        assertEquals("最后一条消息", dto.getLastMessage());
        assertEquals(2L, dto.getLastMessageSenderId());
        assertNotNull(dto.getLastMessageTime());
    }
}
