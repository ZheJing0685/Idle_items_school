package com.idleitems.school.module.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "关联推荐物品信息")
public class RelatedItemDTO {
    @Schema(description = "物品ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "封面图片URL")
    private String coverImage;

    @Schema(description = "成色")
    private String condition;

    @Schema(description = "卖家昵称")
    private String sellerNickname;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
