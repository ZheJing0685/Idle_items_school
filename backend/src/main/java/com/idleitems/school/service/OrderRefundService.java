package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.common.event.OrderRefundEvent;
import com.idleitems.school.dto.order.RefundRequest;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.Order;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderRefundService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Order applyRefund(Long orderId, Long userId, RefundRequest request) {
        log.info("Applying refund: orderId={}, userId={}", orderId, userId);

        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "No permission");
        }
        if (order.getOrderStatus() != Order.OrderStatus.PENDING_SHIPMENT &&
            order.getOrderStatus() != Order.OrderStatus.SHIPPED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Invalid order status for refund");
        }

        order.setOrderStatus(Order.OrderStatus.REFUND_REQUESTED);
        order.setRefundReason(request.getReason());

        Order savedOrder = orderRepository.save(order);
        log.info("Refund applied: {}", savedOrder.getOrderNo());
        return savedOrder;
    }

    @Transactional
    public Order approveRefund(Long orderId, Long adminId, String result) {
        log.info("Approving refund: orderId={}, adminId={}, result={}", orderId, adminId, result);

        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getOrderStatus() != Order.OrderStatus.REFUND_REQUESTED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Invalid order status");
        }

        if ("REJECTED".equals(result)) {
            log.info("Refund rejected: {}", order.getOrderNo());
            if (order.getShipTime() != null) {
                order.setOrderStatus(Order.OrderStatus.SHIPPED);
            } else {
                order.setOrderStatus(Order.OrderStatus.PENDING_SHIPMENT);
            }
            order.setRefundResult(result);
            order.setRefundAdminId(adminId);
            Order savedOrder = orderRepository.save(order);

            eventPublisher.publishEvent(new OrderRefundEvent(
                    this, savedOrder.getId(), order.getBuyerId(), order.getSellerId(),
                    savedOrder.getOrderNo(), null, false
            ));
            return savedOrder;
        }

        if (!"APPROVED".equals(result)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Invalid result");
        }

        order.setOrderStatus(Order.OrderStatus.REFUNDED);
        order.setRefundAmount(order.getPrice());
        order.setRefundTime(LocalDateTime.now());
        order.setRefundResult(result);
        order.setRefundAdminId(adminId);

        Item item = itemRepository.findById(order.getItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));
        restoreItemToSaleIfAvailable(item, order.getId());

        Order savedOrder = orderRepository.save(order);
        log.info("Refund approved: {}, amount: {}", savedOrder.getOrderNo(), order.getRefundAmount());

        eventPublisher.publishEvent(new OrderRefundEvent(
                this, savedOrder.getId(), order.getBuyerId(), order.getSellerId(),
                savedOrder.getOrderNo(), order.getRefundAmount(), true
        ));
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
