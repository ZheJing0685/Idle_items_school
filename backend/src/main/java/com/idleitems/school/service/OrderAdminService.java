package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.Order;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAdminService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Order adminCancelOrder(Long orderId, Long adminId, String reason) {
        log.info("Admin cancelling order: orderId={}, adminId={}, reason={}", orderId, adminId, reason);

        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getOrderStatus() == Order.OrderStatus.COMPLETED
                || order.getOrderStatus() == Order.OrderStatus.CANCELLED
                || order.getOrderStatus() == Order.OrderStatus.REFUNDED
                || order.getOrderStatus() == Order.OrderStatus.REFUND_REQUESTED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Cannot cancel order in current status");
        }

        boolean wasPaid = order.getPaymentTime() != null;
        if (wasPaid) {
            log.info("Admin cancelling paid order: {}, processing refund", order.getOrderNo());
            order.setRefundAmount(order.getPrice());
            order.setRefundTime(LocalDateTime.now());
        }

        if (order.getOrderStatus() == Order.OrderStatus.PENDING_SHIPMENT
                || order.getOrderStatus() == Order.OrderStatus.SHIPPED) {
            Item item = itemRepository.findItemByIdWithLock(order.getItemId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));
            restoreItemToSaleIfAvailable(item, order.getId());
        }

        order.setOrderStatus(Order.OrderStatus.CANCELLED);
        order.setCancelReason("Admin cancelled: " + reason);

        Order savedOrder = orderRepository.save(order);
        log.info("Admin order cancelled: {}, wasPaid: {}", savedOrder.getOrderNo(), wasPaid);
        return savedOrder;
    }

    private boolean hasOtherActiveOrdersForItem(Long itemId, Long excludeOrderId) {
        return orderRepository.existsByItemIdAndOrderStatusInAndIdNot(itemId, OrderQueryService.ACTIVE_STATUSES, excludeOrderId);
    }

    private void restoreItemToSaleIfAvailable(Item item, Long excludeOrderId) {
        if (!hasOtherActiveOrdersForItem(item.getId(), excludeOrderId)) {
            item.setStatus(Item.ItemStatus.ON_SALE);
            itemRepository.save(item);
            log.info("Item restored to sale: itemId={}", item.getId());
        }
    }
}
