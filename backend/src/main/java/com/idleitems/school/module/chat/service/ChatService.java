package com.idleitems.school.module.chat.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.module.chat.dto.ChatDTO;
import com.idleitems.school.module.chat.entity.Chat;
import com.idleitems.school.module.chat.entity.ChatMessage;
import com.idleitems.school.module.notification.entity.Notification;
import com.idleitems.school.module.notification.service.NotificationService;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.chat.repository.ChatRepository;
import com.idleitems.school.module.chat.repository.ChatMessageRepository;
import com.idleitems.school.module.user.repository.UserRepository;
import com.idleitems.school.security.filter.XssFilter;
import com.idleitems.school.util.SensitiveWordFilter;
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
    private final NotificationService notificationService;

    private static final int MAX_MESSAGE_LENGTH = 2000;
    private static final int RECALL_TIME_LIMIT_MINUTES = 2;

    @Transactional
    public Chat createChat(Long buyerId, Long sellerId, Long itemId) {
        // 使用精确查询替代全量遍历
        Optional<Chat> existing = chatRepository.findByBuyerIdAndSellerIdAndItemId(buyerId, sellerId, itemId);
        if (existing.isPresent()) {
            return existing.get();
        }

        try {
            Chat chat = new Chat();
            chat.setBuyerId(buyerId);
            chat.setSellerId(sellerId);
            chat.setItemId(itemId);
            return chatRepository.save(chat);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 并发创建时唯一约束冲突，重新查询已存在的会话
            return chatRepository.findByBuyerIdAndSellerIdAndItemId(buyerId, sellerId, itemId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CONFLICT, "创建会话失败"));
        }
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

        String safeContent;
        String lastMessagePreview;

        if (messageType == ChatMessage.MessageType.IMAGE || messageType == ChatMessage.MessageType.VIDEO) {
            // 文件消息：跳过敏感词和 XSS 过滤，content 是 URL
            if (content.length() > 2000) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "文件路径过长");
            }
            safeContent = content.trim();
            lastMessagePreview = messageType == ChatMessage.MessageType.IMAGE ? "[图片]" : "[视频]";
        } else {
            // 文本消息：正常校验和过滤
            if (content.length() > MAX_MESSAGE_LENGTH) {
                throw new BusinessException(ErrorCode.BAD_REQUEST,
                        "消息内容不能超过" + MAX_MESSAGE_LENGTH + "个字符");
            }

            List<String> sensitiveWords = SensitiveWordFilter.findSensitiveWords(content);
            if (!sensitiveWords.isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, SensitiveWordFilter.getWarningMessage(sensitiveWords));
            }

            safeContent = XssFilter.filterXss(content.trim());
            lastMessagePreview = safeContent.length() > 50 ? safeContent.substring(0, 50) + "..." : safeContent;
        }

        ChatMessage message = new ChatMessage();
        message.setChatId(chatId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(safeContent);
        message.setMessageType(messageType);
        message.setIsRead(false);

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 更新会话最后消息
        chat.setLastMessage(lastMessagePreview);
        chat.setLastMessageTime(LocalDateTime.now());
        chatRepository.save(chat);

        try {
            String senderNickname = userRepository.findById(senderId)
                    .map(User::getNickname)
                    .orElse("用户");
            notificationService.createNotification(
                    receiverId,
                    Notification.NotificationType.INTERACTION.getCode(),
                    senderNickname,
                    lastMessagePreview,
                    chatId,
                    "CHAT"
            );
        } catch (Exception e) {
            log.warn("发送聊天通知失败: chatId={}, error={}", chatId, e.getMessage());
        }

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

        // 批量查询每个会话的最近一条消息（单次查询替代 N+1）
        List<Long> chatIds = chats.stream().map(Chat::getId).collect(Collectors.toList());
        Map<Long, ChatMessage> lastMessageMap = chatMessageRepository.findLastMessagesByChatIds(chatIds)
                .stream()
                .collect(Collectors.toMap(ChatMessage::getChatId, m -> m, (a, b) -> a));

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

        return chatMessageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable);
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
