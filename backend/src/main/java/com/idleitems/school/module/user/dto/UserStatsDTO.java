package com.idleitems.school.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户统计数据")
public class UserStatsDTO {
    @Schema(description = "物品总数")
    private Long totalItems;
    @Schema(description = "已售数量")
    private Long soldItems;
    @Schema(description = "完成交易数")
    private Long completedDeals;
    @Schema(description = "评分")
    private Double rating;
}
