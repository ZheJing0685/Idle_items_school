package com.idleitems.school.module.order.service;

import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTimeoutService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    public int cancelTimeoutOrders(int timeoutMinutes) {
        log.info("Checking timeout orders, timeout: {} minutes", timeoutMinutes);

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<Order> timeoutOrders = orderRepository.findTimeoutOrders(
                Order.OrderStatus.PENDING_PAYMENT, threshold
        );

        int cancelledCount = 0;
        for (Order order : timeoutOrders) {
            try {
                cancelSingleTimeoutOrder(order.getId());
                cancelledCount++;
            } catch (Exception e) {
                log.error("Failed to cancel timeout order: orderId={}, error={}", order.getId(), e.getMessage());
            }
        }

        log.info("Timeout orders cancelled: {}", cancelledCount);
        return cancelledCount;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelSingleTimeoutOrder(Long orderId) {
        Order lockedOrder = orderRepository.findByIdWithLock(orderId).orElse(null);
        if (lockedOrder == null || lockedOrder.getOrderStatus() != Order.OrderStatus.PENDING_PAYMENT) {
            return;
        }

        lockedOrder.setOrderStatus(Order.OrderStatus.CANCELLED);
        lockedOrder.setCancelReason("Order timeout, auto cancelled");
        orderRepository.save(lockedOrder);

        // 恢复物品状态为在售（如果没有其他活跃订单）
        restoreItemToSale(lockedOrder.getItemId(), lockedOrder.getId());

        log.info("Timeout order cancelled: {}", lockedOrder.getOrderNo());
    }

    private void restoreItemToSale(Long itemId, Long excludeOrderId) {
        boolean hasOtherActiveOrders = orderRepository.existsByItemIdAndOrderStatusInAndIdNot(
                itemId, OrderQueryService.ACTIVE_STATUSES, excludeOrderId);
        if (!hasOtherActiveOrders) {
            Item item = itemRepository.findById(itemId).orElse(null);
            if (item != null && item.getStatus() == Item.ItemStatus.SOLD) {
                item.setStatus(Item.ItemStatus.ON_SALE);
                itemRepository.save(item);
                log.info("Item restored to sale after timeout cancel: itemId={}", itemId);
            }
        }
    }
}
