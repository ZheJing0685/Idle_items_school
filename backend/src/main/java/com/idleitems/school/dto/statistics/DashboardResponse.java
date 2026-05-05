package com.idleitems.school.dto.statistics;

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
public class DashboardResponse {
    private Long totalOrders;
    private BigDecimal totalAmount;
    private Long pendingOrders;
    private Long completedOrders;
    private List<OrderTrendItem> orderTrend;
    private Map<String, Long> orderStatusDistribution;
    private List<RecentOrder> recentOrders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderTrendItem {
        private String date;
        private Long count;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrder {
        private Long id;
        private String orderNo;
        private String buyerName;
        private String sellerName;
        private BigDecimal amount;
        private String status;
        private LocalDateTime createdAt;
    }
}
