package com.idleitems.school.service;

import com.idleitems.school.entity.Chat;
import com.idleitems.school.entity.ChatMessage;
import com.idleitems.school.repository.ChatRepository;
import com.idleitems.school.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public Chat createChat(Long buyerId, Long sellerId, Long itemId) {
        // 检查是否已存在聊天会话
        Page<Chat> existingChats = chatRepository.findByBuyerIdOrSellerId(buyerId, sellerId, Pageable.unpaged());
        for (Chat chat : existingChats.getContent()) {
            if (chat.getItemId().equals(itemId)) {
                return chat; // 已存在聊天会话，直接返回
            }
        }

        // 创建新的聊天会话
        Chat chat = new Chat();
        chat.setBuyerId(buyerId);
        chat.setSellerId(sellerId);
        chat.setItemId(itemId);
        return chatRepository.save(chat);
    }

    @Transactional
    public ChatMessage sendMessage(Long chatId, Long senderId, Long receiverId, String content, ChatMessage.MessageType messageType) {
        Chat chat = chatRepository.findById(chatId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("聊天会话不存在"));

        // 验证发送者是否是聊天会话的参与者
        if (!chat.getBuyerId().equals(senderId) && !chat.getSellerId().equals(senderId)) {
            throw new IllegalArgumentException("无权发送消息");
        }

        ChatMessage message = new ChatMessage();
        message.setChatId(chatId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setIsRead(false);

        return chatMessageRepository.save(message);
    }

    public Page<Chat> getChatsByUserId(Long userId, Pageable pageable) {
        return chatRepository.findByBuyerIdOrSellerId(userId, userId, pageable);
    }

    public Page<ChatMessage> getMessagesByChatId(Long chatId, Long userId, Pageable pageable) {
        Chat chat = chatRepository.findById(chatId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("聊天会话不存在"));

        // 验证用户是否是聊天会话的参与者
        if (!chat.getBuyerId().equals(userId) && !chat.getSellerId().equals(userId)) {
            throw new IllegalArgumentException("无权查看消息");
        }

        // 标记当前页消息为已读
        Page<ChatMessage> messages = chatMessageRepository.findByChatIdOrderByCreatedAtAsc(chatId, pageable);
        for (ChatMessage message : messages.getContent()) {
            if (message.getReceiverId().equals(userId) && !message.getIsRead()) {
                message.setIsRead(true);
                message.setReadAt(LocalDateTime.now());
            }
        }

        return messages;
    }

    @Transactional
    public void markMessagesAsRead(Long chatId, Long userId) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findByChatIdAndReceiverIdAndIsReadFalse(chatId, userId);
        for (ChatMessage message : unreadMessages) {
            message.setIsRead(true);
            message.setReadAt(LocalDateTime.now());
        }
    }
}
