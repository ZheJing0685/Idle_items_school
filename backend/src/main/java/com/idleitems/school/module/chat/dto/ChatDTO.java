package com.idleitems.school.module.chat.dto;

import com.idleitems.school.module.chat.entity.Chat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天会话信息")
public class ChatDTO {
    @Schema(description = "聊天ID")
    private Long id;
    @Schema(description = "物品ID")
    private Long itemId;
    @Schema(description = "买家ID")
    private Long buyerId;
    @Schema(description = "卖家ID")
    private Long sellerId;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "买家昵称")
    private String buyerNickname;
    @Schema(description = "买家用户名")
    private String buyerUsername;
    @Schema(description = "买家头像URL")
    private String buyerAvatar;

    @Schema(description = "卖家昵称")
    private String sellerNickname;
    @Schema(description = "卖家用户名")
    private String sellerUsername;
    @Schema(description = "卖家头像URL")
    private String sellerAvatar;

    @Schema(description = "最后一条消息内容")
    private String lastMessage;
    @Schema(description = "最后一条消息发送者ID")
    private Long lastMessageSenderId;
    @Schema(description = "最后一条消息时间")
    private Long lastMessageTime;

    public static ChatDTO fromEntity(Chat chat, 
                                      String buyerNickname, String buyerUsername, String buyerAvatar,
                                      String sellerNickname, String sellerUsername, String sellerAvatar,
                                      String lastMessage, Long lastMessageSenderId, Long lastMessageTime) {
        return ChatDTO.builder()
                .id(chat.getId())
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
