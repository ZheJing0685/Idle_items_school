package com.idleitems.school.module.carbon.service;

import com.idleitems.school.common.event.OrderCompletedEvent;
import com.idleitems.school.module.carbon.entity.CarbonRecord;
import com.idleitems.school.module.carbon.repository.CarbonRecordRepository;
import com.idleitems.school.module.category.entity.Category;
import com.idleitems.school.module.category.repository.CategoryRepository;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarbonService {

    private final CarbonRecordRepository carbonRecordRepository;
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;

    /**
     * 树年吸碳量常量（kg/年）
     */
    private static final BigDecimal TREE_ANNUAL_ABSORPTION = new BigDecimal("18");

    /**
     * 异步记录减碳量 — 订单完成时调用
     */
    @Async("carbonExecutor")
    @Transactional
    public void recordCarbonSaving(OrderCompletedEvent event) {
        try {
            // 查询订单获取 itemId
            Optional<Order> orderOpt = orderRepository.findById(event.getOrderId());
            if (orderOpt.isEmpty()) {
                log.warn("Order not found for carbon record: orderId={}", event.getOrderId());
                return;
            }
            Order order = orderOpt.get();

            // 根据 itemId 查询物品获取 categoryId
            Long categoryId = null;
            Optional<Item> itemOpt = itemRepository.findById(order.getItemId());
            if (itemOpt.isPresent()) {
                categoryId = itemOpt.get().getCategoryId();
            } else {
                log.warn("Item not found for carbon record: itemId={}", order.getItemId());
            }

            // 根据 categoryId 查询分类获取 carbonSavingKg
            BigDecimal carbonSavingKg = BigDecimal.ZERO;
            if (categoryId != null) {
                Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
                if (categoryOpt.isPresent()) {
                    Category category = categoryOpt.get();
                    carbonSavingKg = category.getCarbonSavingKg() != null
                            ? category.getCarbonSavingKg()
                            : BigDecimal.ZERO;
                }
            }

            // 保存碳减排记录
            CarbonRecord record = new CarbonRecord();
            record.setOrderId(event.getOrderId());
            record.setItemId(order.getItemId());
            record.setBuyerId(event.getBuyerId());
            record.setSellerId(event.getSellerId());
            record.setCategoryId(categoryId);
            record.setCarbonSavingKg(carbonSavingKg);

            carbonRecordRepository.save(record);
            log.info("Carbon record saved: orderId={}, carbonSavingKg={}", event.getOrderId(), carbonSavingKg);
        } catch (Exception e) {
            // 异步记录，异常不影响主流程
            log.error("Failed to record carbon saving for orderId={}", event.getOrderId(), e);
        }
    }

    /**
     * 获取本月统计
     */
    public MonthlyStats getMonthlyStats() {
        LocalDate now = LocalDate.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = now.atTime(LocalTime.MAX);

        BigDecimal monthlySavingKg = carbonRecordRepository.findTotalByCreatedAtBetween(monthStart, monthEnd);
        Long transactionCount = carbonRecordRepository.countByCreatedAtBetween(monthStart, monthEnd);
        Long participantCount = carbonRecordRepository.countDistinctBuyerIdByCreatedAtBetween(monthStart, monthEnd);

        // treeEquivalent = monthlySavingKg / 18
        long treeEquivalent = 0;
        if (monthlySavingKg.compareTo(BigDecimal.ZERO) > 0) {
            treeEquivalent = monthlySavingKg.divide(TREE_ANNUAL_ABSORPTION, 0, RoundingMode.DOWN).longValue();
        }

        return new MonthlyStats(monthlySavingKg, treeEquivalent, transactionCount, participantCount);
    }

    /**
     * 获取累计总减碳量
     */
    public BigDecimal getTotalSavingKg() {
        return carbonRecordRepository.findTotalAllTime();
    }

    /**
     * 本月统计 DTO
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class MonthlyStats {
        private BigDecimal monthlySavingKg;
        private long treeEquivalent;
        private long transactionCount;
        private long participantCount;
    }
}
