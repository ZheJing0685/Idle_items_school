package com.idleitems.school.task;

import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 自动确认收货定时任务
 * 每天凌晨2点执行，自动确认发货超过7天的订单
 * 使用逐单事务，确保单个订单失败不影响其他订单
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoConfirmReceiveTask {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    private static final int AUTO_CONFIRM_DAYS = 7;

    @Scheduled(cron = "0 0 2 * * ?")
    public void autoConfirmReceived() {
        log.info("开始执行自动确认收货任务");

        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(AUTO_CONFIRM_DAYS);

            List<Order> shippedOrders = orderRepository.findByOrderStatusAndShipTimeBefore(
                    Order.OrderStatus.SHIPPED, threshold);

            if (shippedOrders.isEmpty()) {
                log.info("没有需要自动确认收货的订单");
                return;
            }

            log.info("找到{}个需要自动确认收货的订单", shippedOrders.size());

            int successCount = 0;
            int failCount = 0;

            for (Order order : shippedOrders) {
                try {
                    processSingleOrder(order);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("订单{}自动确认收货处理失败: {}", order.getOrderNo(), e.getMessage());
                }
            }

            log.info("自动确认收货任务完成: 成功={}, 失败={}", successCount, failCount);
        } catch (Exception e) {
            log.error("自动确认收货任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理单个订单的自动确认收货（独立事务）
     */
    @Transactional
    public void processSingleOrder(Order order) {
        // 重新查询确保状态最新
        Order currentOrder = orderRepository.findById(order.getId()).orElse(null);
        if (currentOrder == null || currentOrder.getOrderStatus() != Order.OrderStatus.SHIPPED) {
            log.debug("订单{}状态已变更，跳过", order.getOrderNo());
            return;
        }

        currentOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        currentOrder.setCompleteTime(LocalDateTime.now());
        orderRepository.save(currentOrder);

        // 发送通知（通知失败不影响订单状态）
        try {
            String message = "订单号 " + currentOrder.getOrderNo() + " 已自动确认收货";

            notificationService.createOrderNotification(
                    currentOrder.getBuyerId(), "订单已自动确认收货",
                    message + "，交易完成", currentOrder.getId());

            notificationService.createOrderNotification(
                    currentOrder.getSellerId(), "订单已自动确认收货",
                    message + "，交易完成", currentOrder.getId());
        } catch (Exception e) {
            log.warn("订单{}自动确认收货通知发送失败（不影响订单状态）: {}", currentOrder.getOrderNo(), e.getMessage());
        }

        log.info("订单{}自动确认收货成功", currentOrder.getOrderNo());
    }
}
