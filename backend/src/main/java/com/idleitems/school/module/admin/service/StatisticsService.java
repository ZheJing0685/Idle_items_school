package com.idleitems.school.module.admin.service;

import com.idleitems.school.module.admin.dto.DashboardResponse;
import com.idleitems.school.module.category.entity.Category;
import com.idleitems.school.module.category.repository.CategoryRepository;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    public DashboardResponse getDashboard(String timeRange, LocalDate startDate, LocalDate endDate) {
        LocalDateTime[] dateRange = calculateDateRange(timeRange, startDate, endDate);
        LocalDateTime start = dateRange[0];
        LocalDateTime end = dateRange[1];

        List<Order> ordersInRange = orderRepository.findByCreatedAtBetween(start, end);
        long totalOrders = orderRepository.count();

        Map<String, Long> statusCountMap = new HashMap<>();
        List<Object[]> groupedCounts = orderRepository.countByOrderStatusGrouped();
        for (Object[] row : groupedCounts) {
            if (row[0] == null || row[1] == null) {
                log.warn("跳过无效的订单状态分组数据: status={}, count={}", row[0], row[1]);
                continue;
            }
            Order.OrderStatus status = (Order.OrderStatus) row[0];
            Long count = (Long) row[1];
            statusCountMap.put(status.name(), count);
        }

        long pendingOrders = statusCountMap.getOrDefault("PENDING_PAYMENT", 0L);
        long completedOrders = statusCountMap.getOrDefault("COMPLETED", 0L);
        BigDecimal totalAmount = orderRepository.sumCompletedOrderAmount();
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }

        List<DashboardResponse.OrderTrendItem> orderTrend = generateOrderTrend(start, end);

        Map<String, Long> statusDistribution = new HashMap<>();
        statusDistribution.put("pending_payment", statusCountMap.getOrDefault("PENDING_PAYMENT", 0L));
        statusDistribution.put("pending_shipment", statusCountMap.getOrDefault("PENDING_SHIPMENT", 0L));
        statusDistribution.put("shipped", statusCountMap.getOrDefault("SHIPPED", 0L));
        statusDistribution.put("completed", statusCountMap.getOrDefault("COMPLETED", 0L));
        statusDistribution.put("cancelled", statusCountMap.getOrDefault("CANCELLED", 0L));
        statusDistribution.put("refund_requested", statusCountMap.getOrDefault("REFUND_REQUESTED", 0L));
        statusDistribution.put("refunded", statusCountMap.getOrDefault("REFUNDED", 0L));

        List<Order> recentOrders = ordersInRange.stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .limit(10)
                .collect(Collectors.toList());

        Set<Long> recentOrderUserIds = recentOrders.stream()
                .flatMap(o -> Stream.of(o.getBuyerId(), o.getSellerId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> recentUserMap = userRepository.findAllById(recentOrderUserIds)
                .stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<DashboardResponse.RecentOrder> recentOrderItems = recentOrders.stream()
                .map(order -> {
                    User buyer = recentUserMap.get(order.getBuyerId());
                    User seller = recentUserMap.get(order.getSellerId());
                    String buyerName = buyer != null
                            ? (buyer.getNickname() != null ? buyer.getNickname() : buyer.getUsername())
                            : "未知";
                    String sellerName = seller != null
                            ? (seller.getNickname() != null ? seller.getNickname() : seller.getUsername())
                            : "未知";
                    return DashboardResponse.RecentOrder.builder()
                            .id(order.getId())
                            .orderNo(order.getOrderNo())
                            .buyerName(buyerName)
                            .sellerName(sellerName)
                            .amount(order.getPrice())
                            .status(order.getOrderStatus().name())
                            .createdAt(order.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalOrders(totalOrders)
                .totalAmount(totalAmount)
                .pendingOrders(pendingOrders)
                .completedOrders(completedOrders)
                .orderTrend(orderTrend)
                .orderStatusDistribution(statusDistribution)
                .recentOrders(recentOrderItems)
                .build();
    }

    public Map<String, Object> getOverview() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findByStatus(User.UserStatus.ACTIVE, PageRequest.of(0, 1)).getTotalElements();
        long totalItems = itemRepository.count();
        long onSaleItems = itemRepository.findByStatus(Item.ItemStatus.ON_SALE, PageRequest.of(0, 1)).getTotalElements();
        long totalOrders = orderRepository.count();
        long completedOrders = orderRepository.countByOrderStatus(Order.OrderStatus.COMPLETED);
        BigDecimal totalAmount = orderRepository.sumCompletedOrderAmount();

        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("totalItems", totalItems);
        stats.put("onSaleItems", onSaleItems);
        stats.put("totalOrders", totalOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);

        return stats;
    }

    public Map<String, Object> getMonthlyStatistics() {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> monthlyData = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDateTime start = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = start.plusMonths(1);
            Long orderCount = orderRepository.countCompletedOrdersByDateRange(start, end);
            BigDecimal amount = orderRepository.sumCompletedOrderAmountByDateRange(start, end);
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", start.getMonthValue());
            monthData.put("year", start.getYear());
            monthData.put("orderCount", orderCount);
            monthData.put("amount", amount != null ? amount : BigDecimal.ZERO);
            monthlyData.add(monthData);
        }

        stats.put("monthlyData", monthlyData);
        return stats;
    }

    public List<Map<String, Object>> getCategoryStatistics() {
        List<Category> categories = categoryRepository.findAll();

        List<Long> categoryIds = categories.stream()
                .map(Category::getId)
                .collect(Collectors.toList());
        List<Object[]> groupedCounts = itemRepository.countByCategoryIdsGrouped(categoryIds);
        Map<Long, Long> countMap = groupedCounts.stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        List<Map<String, Object>> categoryStats = new ArrayList<>();
        for (Category category : categories) {
            Long count = countMap.getOrDefault(category.getId(), 0L);
            Map<String, Object> catStat = new HashMap<>();
            catStat.put("categoryId", category.getId());
            catStat.put("categoryName", category.getName());
            catStat.put("count", count);
            categoryStats.add(catStat);
        }

        return categoryStats;
    }

    public List<Item> getHotItems() {
        return itemRepository.findTop10ByStatusOrderByViewCountDesc(Item.ItemStatus.ON_SALE);
    }

    private LocalDateTime[] calculateDateRange(String timeRange, LocalDate startDate, LocalDate endDate) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;

        switch (timeRange) {
            case "week":
                start = end.minusWeeks(1).withHour(0).withMinute(0).withSecond(0);
                break;
            case "month":
                start = end.minusMonths(1).withHour(0).withMinute(0).withSecond(0);
                break;
            case "custom":
                start = startDate.atStartOfDay();
                end = endDate.atTime(LocalTime.MAX);
                break;
            case "today":
            default:
                start = end.withHour(0).withMinute(0).withSecond(0);
                break;
        }

        return new LocalDateTime[]{start, end};
    }

    private List<DashboardResponse.OrderTrendItem> generateOrderTrend(LocalDateTime start, LocalDateTime end) {
        List<DashboardResponse.OrderTrendItem> trend = new ArrayList<>();
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
        int maxDays = (int) Math.min(daysBetween, 30);

        List<Object[]> groupedData = orderRepository.countOrdersAndAmountGroupedByDate(start, end);
        
        Map<String, Object[]> dateDataMap = new HashMap<>();
        for (Object[] row : groupedData) {
            String dateStr = row[0].toString();
            dateDataMap.put(dateStr, row);
        }

        for (int i = maxDays; i >= 0; i--) {
            LocalDate date = end.minusDays(i).toLocalDate();
            String dateStr = date.toString();
            
            long count = 0;
            BigDecimal amount = BigDecimal.ZERO;
            
            Object[] data = dateDataMap.get(dateStr);
            if (data != null) {
                count = (Long) data[1];
                amount = (BigDecimal) data[2];
            }

            trend.add(DashboardResponse.OrderTrendItem.builder()
                    .date(dateStr)
                    .count(count)
                    .amount(amount)
                    .build());
        }

        return trend;
    }
}
