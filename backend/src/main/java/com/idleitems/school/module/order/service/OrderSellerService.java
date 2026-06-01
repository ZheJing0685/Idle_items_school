package com.idleitems.school.module.order.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.common.event.OrderShippedEvent;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSellerService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Order shipOrder(Long orderId, Long userId) {
        log.info("Shipping order: orderId={}, userId={}", orderId, userId);

        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "No permission");
        }
        if (order.getOrderStatus() != Order.OrderStatus.PENDING_SHIPMENT) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Only pending shipment orders can be shipped");
        }

        order.setOrderStatus(Order.OrderStatus.SHIPPED);
        order.setShipTime(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("Order shipped: {}", savedOrder.getOrderNo());

        eventPublisher.publishEvent(new OrderShippedEvent(
                this, savedOrder.getId(), order.getBuyerId(), savedOrder.getOrderNo(), null, null
        ));
        return savedOrder;
    }

    @Transactional
    public Order updateShippingInfo(Long orderId, Long userId, String trackingNumber, String shippingCompany) {
        log.info("Updating shipping info: orderId={}, trackingNumber={}", orderId, trackingNumber);

        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "No permission");
        }
        if (order.getOrderStatus() != Order.OrderStatus.PENDING_SHIPMENT
                && order.getOrderStatus() != Order.OrderStatus.SHIPPED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "Invalid order status");
        }

        order.setTrackingNumber(trackingNumber);
        order.setShippingCompany(shippingCompany);
        order.setShipTime(LocalDateTime.now());
        order.setOrderStatus(Order.OrderStatus.SHIPPED);

        Order savedOrder = orderRepository.save(order);
        log.info("Shipping info updated: {}", savedOrder.getOrderNo());

        eventPublisher.publishEvent(new OrderShippedEvent(
                this, savedOrder.getId(), order.getBuyerId(), savedOrder.getOrderNo(),
                trackingNumber, shippingCompany
        ));
        return savedOrder;
    }
}
