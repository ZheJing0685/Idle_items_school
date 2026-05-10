package com.idleitems.school.task;

import com.idleitems.school.service.ConfigService;
import com.idleitems.school.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderService orderService;
    private final ConfigService configService;

    private static final String CONFIG_TIMEOUT_MINUTES = "order_timeout_minutes";
    private static final int DEFAULT_TIMEOUT_MINUTES = 30;

    @Scheduled(fixedRateString = "${order.timeout.check-interval:300000}")
    public void checkTimeoutOrders() {
        log.info("开始执行订单超时检查任务");
        try {
            Integer timeoutMinutes = configService.getConfigInt(CONFIG_TIMEOUT_MINUTES);
            int actualTimeoutMinutes = timeoutMinutes != null ? timeoutMinutes : DEFAULT_TIMEOUT_MINUTES;
            
            int cancelledCount = orderService.cancelTimeoutOrders(actualTimeoutMinutes);
            if (cancelledCount > 0) {
                log.info("订单超时检查任务完成，取消 {} 个超时订单", cancelledCount);
            } else {
                log.info("订单超时检查任务完成，没有需要取消的订单");
            }
        } catch (Exception e) {
            log.error("订单超时检查任务执行失败: {}", e.getMessage(), e);
        }
    }
}