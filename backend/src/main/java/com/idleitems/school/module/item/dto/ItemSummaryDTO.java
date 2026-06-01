package com.idleitems.school.module.item.dto;

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
@Schema(description = "物品概要信息")
public class ItemSummaryDTO {
    @Schema(description = "物品ID")
    private Long id;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "价格")
    private BigDecimal price;
    @Schema(description = "原价")
    private BigDecimal originalPrice;
    @Schema(description = "封面图片URL")
    private String coverImage;
    @Schema(description = "浏览次数")
    private Integer viewCount;
    @Schema(description = "收藏次数")
    private Integer favoriteCount;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "是否允许议价")
    private Boolean isBargainAllowed;
    @Schema(description = "成色")
    private String condition;
    @Schema(description = "卖家昵称")
    private String sellerNickname;
    @Schema(description = "卖家是否实名认证")
    private Boolean sellerVerified;
    @Schema(description = "卖家物品数量")
    private Integer sellerItemsCount;
    @Schema(description = "卖家评分")
    private Double sellerRating;
}
