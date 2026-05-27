package com.idleitems.school.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "收藏信息")
public class FavoriteDTO {
    @Schema(description = "收藏ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "物品ID")
    private Long itemId;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "物品标题")
    private String title;
    @Schema(description = "物品价格")
    private BigDecimal price;
    @Schema(description = "物品封面图片URL")
    private String coverImage;
    @Schema(description = "物品状态")
    private String status;
    @Schema(description = "卖家名称")
    private String sellerName;
    @Schema(description = "卖家ID")
    private Long sellerId;
}
