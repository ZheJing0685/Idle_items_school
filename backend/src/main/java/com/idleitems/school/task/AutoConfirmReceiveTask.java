package com.idleitems.school.task;

import com.idleitems.school.entity.Order;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 自动确认收货定时任务
 * 每天凌晨2点执行，自动确认发货超过7天的订单
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoConfirmReceiveTask {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    private static final int AUTO_CONFIRM_DAYS = 7;

    /**
     * 自动确认收货任务
     * 每天凌晨2点执行: 0 0 2 * * ?
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoConfirmReceived() {
        log.info("开始执行自动确认收货任务");
        
        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(AUTO_CONFIRM_DAYS);
            
            // 查询发货超过7天的订单
            List<Order> shippedOrders = orderRepository.findByOrderStatusAndShipTimeBefore(
                    Order.OrderStatus.SHIPPED, threshold);
            
            log.info("找到{}个需要自动确认收货的订单", shippedOrders.size());
            
            int successCount = 0;
            for (Order order : shippedOrders) {
                try {
                    // 自动确认收货
                    order.setOrderStatus(Order.OrderStatus.COMPLETED);
                    order.setCompleteTime(LocalDateTime.now());
                    orderRepository.save(order);
                    
                    // 发送通知给买卖双方
                    String message = "订单号 " + order.getOrderNo() + " 已自动确认收货";
                    
                    // 通知买家
                    notificationService.createOrderNotification(
                            order.getBuyerId(),
                            "订单已自动确认收货",
                            message + "，交易完成",
                            order.getId()
                    );
                    
                    // 通知卖家
                    notificationService.createOrderNotification(
                            order.getSellerId(),
                            "订单已自动确认收货",
                            message + "，交易完成",
                            order.getId()
                    );
                    
                    successCount++;
                    log.info("订单{}自动确认收货成功", order.getOrderNo());
                } catch (Exception e) {
                    log.error("订单{}自动确认收货失败: {}", order.getOrderNo(), e.getMessage());
                }
            }
            
            log.info("自动确认收货任务完成，成功处理{}个订单", successCount);
        } catch (Exception e) {
            log.error("自动确认收货任务执行失败: {}", e.getMessage(), e);
        }
    }
}
