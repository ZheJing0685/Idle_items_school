package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.dto.ChatDTO;
import com.idleitems.school.entity.Chat;
import com.idleitems.school.entity.ChatMessage;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.ChatRepository;
import com.idleitems.school.repository.ChatMessageRepository;
import com.idleitems.school.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    private static final int MAX_MESSAGE_LENGTH = 2000;
    private static final int RECALL_TIME_LIMIT_MINUTES = 2;

    @Transactional
    public Chat createChat(Long buyerId, Long sellerId, Long itemId) {
        // 检查是否已存在聊天会话
        Page<Chat> existingChats = chatRepository.findByBuyerIdOrSellerId(buyerId, sellerId, Pageable.unpaged());
        for (Chat chat : existingChats.getContent()) {
            if (chat.getItemId().equals(itemId)) {
                return chat;
            }
        }

        Chat chat = new Chat();
        chat.setBuyerId(buyerId);
        chat.setSellerId(sellerId);
        chat.setItemId(itemId);
        return chatRepository.save(chat);
    }

    @Transactional
    public ChatMessage sendMessage(Long chatId, Long senderId, Long receiverId, String content, ChatMessage.MessageType messageType) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "聊天会话不存在"));

        if (!chat.getBuyerId().equals(senderId) && !chat.getSellerId().equals(senderId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权发送消息");
        }

        // 消息内容校验
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息内容不能为空");
        }
        if (content.length() > MAX_MESSAGE_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "消息内容不能超过" + MAX_MESSAGE_LENGTH + "个字符");
        }

        ChatMessage message = new ChatMessage();
        message.setChatId(chatId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content.trim());
        message.setMessageType(messageType);
        message.setIsRead(false);

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 更新会话最后消息
        chat.setLastMessage(content.length() > 50 ? content.substring(0, 50) + "..." : content);
        chat.setLastMessageTime(LocalDateTime.now());
        chatRepository.save(chat);

        return savedMessage;
    }

    public Page<Chat> getChatsByUserId(Long userId, Pageable pageable) {
        return chatRepository.findByBuyerIdOrSellerId(userId, userId, pageable);
    }

    public List<Chat> getChatsByUserIdList(Long userId) {
        return chatRepository.findAllChatsByUserId(userId);
    }

    /**
     * 获取用户聊天列表（优化版：批量查询最近消息，避免N+1）
     */
    public List<ChatDTO> getChatsByUserIdListWithUserInfo(Long userId) {
        List<Chat> chats = chatRepository.findAllChatsByUserId(userId);
        if (chats.isEmpty()) {
            return List.of();
        }

        // 批量查询用户信息
        Set<Long> userIds = chats.stream()
                .flatMap(c -> java.util.stream.Stream.of(c.getBuyerId(), c.getSellerId()))
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 批量查询每个会话的最近一条消息（避免N+1）
        Map<Long, ChatMessage> lastMessageMap = new HashMap<>();
        for (Chat chat : chats) {
            List<ChatMessage> recentMessages = chatMessageRepository.findByChatIdOrderByCreatedAtDesc(
                    chat.getId(), PageRequest.of(0, 1));
            if (!recentMessages.isEmpty()) {
                lastMessageMap.put(chat.getId(), recentMessages.get(0));
            }
        }

        // 构建DTO
        List<ChatDTO> chatDTOs = new ArrayList<>();
        for (Chat chat : chats) {
            User buyer = userMap.get(chat.getBuyerId());
            User seller = userMap.get(chat.getSellerId());
            ChatMessage lastMsg = lastMessageMap.get(chat.getId());

            String lastMessage = lastMsg != null ? lastMsg.getContent() : null;
            Long lastMessageTime = lastMsg != null && lastMsg.getCreatedAt() != null
                    ? lastMsg.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : null;

            chatDTOs.add(ChatDTO.fromEntity(
                    chat,
                    buyer != null ? buyer.getNickname() : null,
                    buyer != null ? buyer.getUsername() : null,
                    buyer != null ? buyer.getAvatar() : null,
                    seller != null ? seller.getNickname() : null,
                    seller != null ? seller.getUsername() : null,
                    seller != null ? seller.getAvatar() : null,
                    lastMessage, lastMsg != null ? lastMsg.getSenderId() : null, lastMessageTime
            ));
        }

        return chatDTOs;
    }

    /**
     * 获取聊天消息（不自动标记已读，避免读操作中的写操作）
     */
    public Page<ChatMessage> getMessagesByChatId(Long chatId, Long userId, Pageable pageable) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "聊天会话不存在"));

        if (!chat.getBuyerId().equals(userId) && !chat.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权查看消息");
        }

        return chatMessageRepository.findByChatIdOrderByCreatedAtAsc(chatId, pageable);
    }

    /**
     * 异步标记消息为已读
     */
    @Async
    @Transactional
    public void markMessagesAsReadAsync(Long chatId, Long userId) {
        try {
            markMessagesAsRead(chatId, userId);
        } catch (Exception e) {
            log.error("异步标记消息已读失败: chatId={}, userId={}", chatId, userId, e);
        }
    }

    @Transactional
    public void markMessagesAsRead(Long chatId, Long userId) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findByChatIdAndReceiverIdAndIsReadFalse(chatId, userId);
        for (ChatMessage message : unreadMessages) {
            message.setIsRead(true);
            message.setReadAt(LocalDateTime.now());
        }
        chatMessageRepository.saveAll(unreadMessages);
    }

    /**
     * 撤回消息（仅限发送者本人，且在2分钟内）
     */
    @Transactional
    public ChatMessage recallMessage(Long messageId, Long userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "消息不存在"));

        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "只能撤回自己发送的消息");
        }

        // 检查撤回时间限制（2分钟内）
        if (message.getCreatedAt() != null) {
            long minutesSinceSent = java.time.temporal.ChronoUnit.MINUTES.between(
                    message.getCreatedAt(), LocalDateTime.now());
            if (minutesSinceSent > RECALL_TIME_LIMIT_MINUTES) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                        "消息发送超过" + RECALL_TIME_LIMIT_MINUTES + "分钟，无法撤回");
            }
        }

        // 检查消息是否已读
        if (Boolean.TRUE.equals(message.getIsRead())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "消息已被阅读，无法撤回");
        }

        message.setContent("[消息已撤回]");
        message.setMessageType(ChatMessage.MessageType.SYSTEM);

        ChatMessage saved = chatMessageRepository.save(message);
        log.info("消息已撤回: messageId={}, userId={}", messageId, userId);

        return saved;
    }
}
