package com.idleitems.school.dto;

import com.idleitems.school.entity.Chat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatDTO {
    private Long id;
    private Long orderId;
    private Long itemId;
    private Long buyerId;
    private Long sellerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 买家信息
    private String buyerNickname;
    private String buyerUsername;
    private String buyerAvatar;

    // 卖家信息
    private String sellerNickname;
    private String sellerUsername;
    private String sellerAvatar;

    // 最后一条消息
    private String lastMessage;
    private Long lastMessageSenderId;
    private Long lastMessageTime;

    public static ChatDTO fromEntity(Chat chat, 
                                      String buyerNickname, String buyerUsername, String buyerAvatar,
                                      String sellerNickname, String sellerUsername, String sellerAvatar,
                                      String lastMessage, Long lastMessageSenderId, Long lastMessageTime) {
        return ChatDTO.builder()
                .id(chat.getId())
                .orderId(chat.getOrderId())
                .itemId(chat.getItemId())
                .buyerId(chat.getBuyerId())
                .sellerId(chat.getSellerId())
                .createdAt(chat.getCreatedAt())
                .updatedAt(chat.getUpdatedAt())
                .buyerNickname(buyerNickname)
                .buyerUsername(buyerUsername)
                .buyerAvatar(buyerAvatar)
                .sellerNickname(sellerNickname)
                .sellerUsername(sellerUsername)
                .sellerAvatar(sellerAvatar)
                .lastMessage(lastMessage)
                .lastMessageSenderId(lastMessageSenderId)
                .lastMessageTime(lastMessageTime)
                .build();
    }
}
