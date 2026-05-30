package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.common.event.OrderCancelledEvent;
import com.idleitems.school.common.event.OrderCompletedEvent;
import com.idleitems.school.common.event.OrderCreatedEvent;
import com.idleitems.school.common.event.OrderPaidEvent;
import com.idleitems.school.dto.order.CancelOrderRequest;
import com.idleitems.school.dto.order.CreateOrderRequest;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.Order;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.util.OrderNoGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderBuyerService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Order createOrder(Long buyerId, CreateOrderRequest request) {
        log.info("Creating order, userId: {}, itemId: {}", buyerId, request.getItemId());

        Item item = itemRepository.findItemByIdWithLock(request.getItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        if (item.getStatus() != Item.ItemStatus.ON_SALE) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Item not available");
        }
        if (item.getUserId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Cannot buy own item");
        }
        if (orderRepository.existsByBuyerIdAndItemIdAndOrderStatusIn(buyerId, request.getItemId(), OrderQueryService.ACTIVE_STATUSES)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Already ordered this item");
        }

        Order order = new Order();
        order.setOrderNo(OrderNoGenerator.generate());
        order.setBuyerId(buyerId);
        order.setSellerId(item.getUserId());
        order.setItemId(item.getId());
        order.setItemTitle(item.getTitle());
        order.setItemImage(item.getCoverImage() != null ? item.getCoverImage() : "");
        order.setPrice(item.getPrice());
        order.setOrderStatus(Order.OrderStatus.PENDING_PAYMENT);
        order.setBuyerName(request.getBuyerName());
        order.setBuyerPhone(request.getBuyerPhone());
        order.setBuyerAddress(request.getBuyerAddress());
        order.setPaymentMethod(request.getPaymentMethod());

        Order savedOrder = orderRepository.save(order);
        log.info("Order created: {}", savedOrder.getOrderNo());

        eventPublisher.publishEvent(new OrderCreatedEvent(
                this, savedOrder.getId(), buyerId, item.getUserId(),
                savedOrder.getOrderNo(), item.getTitle()
        ));
        return savedOrder;
    }

    @Transactional
    public Order payOrder(Long orderId, Long userId, String paymentMethod) {
        log.info("Paying order: orderId={}, userId={}", orderId, userId);

        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "No permission");
        }
        if (order.getOrderStatus() != Order.OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Invalid order status");
        }

        int updated = itemRepository.markItemAsSold(order.getItemId());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Item already sold");
        }

        order.setOrderStatus(Order.OrderStatus.PENDING_SHIPMENT);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentTime(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("Order paid: {}", savedOrder.getOrderNo());

        eventPublisher.publishEvent(new OrderPaidEvent(
                this, savedOrder.getId(), order.getSellerId(), savedOrder.getOrderNo()
        ));
        return savedOrder;
    }

    @Transactional
    public Order cancelOrder(Long orderId, Long userId, CancelOrderRequest request) {
        log.info("Cancelling order: orderId={}, userId={}", orderId, userId);

        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "No permission");
        }
        if (order.getOrderStatus() != Order.OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Only pending payment orders can be cancelled");
        }

        order.setOrderStatus(Order.OrderStatus.CANCELLED);
        order.setCancelReason(request.getReason());
        order.setCancelTime(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("Order cancelled: {}", savedOrder.getOrderNo());

        eventPublisher.publishEvent(new OrderCancelledEvent(
                this, savedOrder.getId(), order.getSellerId(), savedOrder.getOrderNo(), request.getReason()
        ));
        return savedOrder;
    }

    @Transactional
    public Order confirmReceive(Long orderId, Long userId) {
        log.info("Confirming receive: orderId={}, userId={}", orderId, userId);

        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "No permission");
        }
        if (order.getOrderStatus() != Order.OrderStatus.SHIPPED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Only shipped orders can be confirmed");
        }

        order.setOrderStatus(Order.OrderStatus.COMPLETED);
        order.setCompleteTime(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("Order completed: {}", savedOrder.getOrderNo());

        eventPublisher.publishEvent(new OrderCompletedEvent(
                this, savedOrder.getId(), order.getSellerId(), order.getBuyerId(), savedOrder.getOrderNo()
        ));
        return savedOrder;
    }
}
