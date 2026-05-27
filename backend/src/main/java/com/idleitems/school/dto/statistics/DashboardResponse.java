package com.idleitems.school.dto.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "仪表盘统计数据响应")
public class DashboardResponse {
    @Schema(description = "订单总数")
    private Long totalOrders;
    @Schema(description = "总金额")
    private BigDecimal totalAmount;
    @Schema(description = "待处理订单数")
    private Long pendingOrders;
    @Schema(description = "已完成订单数")
    private Long completedOrders;
    @Schema(description = "订单趋势")
    private List<OrderTrendItem> orderTrend;
    @Schema(description = "订单状态分布")
    private Map<String, Long> orderStatusDistribution;
    @Schema(description = "最近订单列表")
    private List<RecentOrder> recentOrders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "订单趋势项")
    public static class OrderTrendItem {
        @Schema(description = "日期")
        private String date;
        @Schema(description = "数量")
        private Long count;
        @Schema(description = "金额")
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "最近订单")
    public static class RecentOrder {
        @Schema(description = "订单ID")
        private Long id;
        @Schema(description = "订单号")
        private String orderNo;
        @Schema(description = "买家名称")
        private String buyerName;
        @Schema(description = "卖家名称")
        private String sellerName;
        @Schema(description = "金额")
        private BigDecimal amount;
        @Schema(description = "状态")
        private String status;
        @Schema(description = "创建时间")
        private LocalDateTime createdAt;
    }
}
