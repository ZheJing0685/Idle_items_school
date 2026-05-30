package com.idleitems.school.common.event;

import com.idleitems.school.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final NotificationService notificationService;

    @Async("notificationExecutor")
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        try {
            notificationService.createOrderNotification(
                event.getSellerId(),
                "\u60a8\u6709\u65b0\u7684\u8ba2\u5355",
                "\u8ba2\u5355\u53f7 " + event.getOrderNo() + "\uff0c\u5546\u54c1\uff1a" + event.getItemTitle(),
                event.getOrderId()
            );
            log.debug("Order created notification sent: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send order created notification: orderId={}", event.getOrderId(), e);
        }
    }

    @Async("notificationExecutor")
    @EventListener
    public void onOrderPaid(OrderPaidEvent event) {
        try {
            notificationService.createOrderNotification(
                event.getSellerId(),
                "\u8ba2\u5355\u5df2\u652f\u4ed8",
                "\u8ba2\u5355\u53f7 " + event.getOrderNo() + " \u5df2\u652f\u4ed8\uff0c\u8bf7\u5c3d\u5feb\u53d1\u8d27",
                event.getOrderId()
            );
            log.debug("Order paid notification sent: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send order paid notification: orderId={}", event.getOrderId(), e);
        }
    }

    @Async("notificationExecutor")
    @EventListener
    public void onOrderShipped(OrderShippedEvent event) {
        try {
            String message = "\u8ba2\u5355\u53f7 " + event.getOrderNo() + " \u5df2\u53d1\u8d27";
            if (event.getTrackingNumber() != null) {
                message += "\uff0c\u5feb\u9012\u516c\u53f8: " + event.getShippingCompany() + "\uff0c\u5355\u53f7: " + event.getTrackingNumber();
            }
            notificationService.createOrderNotification(
                event.getBuyerId(),
                "\u8ba2\u5355\u5df2\u53d1\u8d27",
                message,
                event.getOrderId()
            );
            log.debug("Order shipped notification sent: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send order shipped notification: orderId={}", event.getOrderId(), e);
        }
    }

    @Async("notificationExecutor")
    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        try {
            notificationService.createOrderNotification(
                event.getSellerId(),
                "\u8ba2\u5355\u5df2\u53d6\u6d88",
                "\u8ba2\u5355\u53f7 " + event.getOrderNo() + " \u5df2\u53d6\u6d88\uff0c\u539f\u56e0\uff1a" + event.getReason(),
                event.getOrderId()
            );
            log.debug("Order cancelled notification sent: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send order cancelled notification: orderId={}", event.getOrderId(), e);
        }
    }

    @Async("notificationExecutor")
    @EventListener
    public void onOrderCompleted(OrderCompletedEvent event) {
        try {
            notificationService.createOrderNotification(
                event.getSellerId(),
                "\u8ba2\u5355\u5df2\u786e\u8ba4\u6536\u8d27",
                "\u8ba2\u5355\u53f7 " + event.getOrderNo() + " \u5df2\u786e\u8ba4\u6536\u8d27\uff0c\u4ea4\u6613\u5b8c\u6210",
                event.getOrderId()
            );
            log.debug("Order completed notification sent: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send order completed notification: orderId={}", event.getOrderId(), e);
        }
    }

    @Async("notificationExecutor")
    @EventListener
    public void onOrderRefund(OrderRefundEvent event) {
        try {
            if (event.isApproved()) {
                notificationService.createOrderNotification(
                    event.getBuyerId(),
                    "\u9000\u6b3e\u7533\u8bf7\u5df2\u901a\u8fc7",
                    "\u8ba2\u5355\u53f7 " + event.getOrderNo() + " \u7684\u9000\u6b3e\u7533\u8bf7\u5df2\u901a\u8fc7\uff0c\u9000\u6b3e\u91d1\u989d: \u00a5" + event.getRefundAmount() + "\uff0c\u9884\u8ba11-3\u4e2a\u5de5\u4f5c\u65e5\u5230\u8d26",
                    event.getOrderId()
                );
                notificationService.createOrderNotification(
                    event.getSellerId(),
                    "\u8ba2\u5355\u9000\u6b3e\u5df2\u5904\u7406",
                    "\u8ba2\u5355\u53f7 " + event.getOrderNo() + " \u7684\u9000\u6b3e\u7533\u8bf7\u5df2\u901a\u8fc7\uff0c\u7269\u54c1\u5df2\u6062\u590d\u4e0a\u67b6",
                    event.getOrderId()
                );
            } else {
                notificationService.createOrderNotification(
                    event.getBuyerId(),
                    "\u9000\u6b3e\u7533\u8bf7\u88ab\u62d2\u7edd",
                    "\u8ba2\u5355\u53f7 " + event.getOrderNo() + " \u7684\u9000\u6b3e\u7533\u8bf7\u5df2\u88ab\u62d2\u7edd\uff0c\u5982\u6709\u7591\u95ee\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458",
                    event.getOrderId()
                );
            }
            log.debug("Order refund notification sent: orderId={}, approved={}", event.getOrderId(), event.isApproved());
        } catch (Exception e) {
            log.error("Failed to send order refund notification: orderId={}", event.getOrderId(), e);
        }
    }
}
