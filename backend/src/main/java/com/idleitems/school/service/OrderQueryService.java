package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.dto.order.AdminOrderResponse;
import com.idleitems.school.dto.order.OrderSummaryResponse;
import com.idleitems.school.entity.Order;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    public Order getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权查看此订单");
        }

        return order;
    }

    public Page<Order> getBuyerOrders(Long buyerId, Order.OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByBuyerIdAndOrderStatus(buyerId, status, pageable);
        }
        return orderRepository.findByBuyerId(buyerId, pageable);
    }

    public Page<Order> getSellerOrders(Long sellerId, Order.OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findBySellerIdAndOrderStatus(sellerId, status, pageable);
        }
        return orderRepository.findBySellerId(sellerId, pageable);
    }

    public Page<OrderSummaryResponse> getBuyerOrderSummaries(Long buyerId, Order.OrderStatus status, Pageable pageable) {
        Page<Order> orders = getBuyerOrders(buyerId, status, pageable);
        List<Long> orderIds = orders.getContent().stream()
                .map(Order::getId)
                .toList();
        Set<Long> reviewedOrderIds = new HashSet<>();
        if (!orderIds.isEmpty()) {
            reviewedOrderIds.addAll(reviewRepository.findReviewedOrderIds(orderIds, buyerId));
        }
        return orders.map(order -> OrderSummaryResponse.from(order, reviewedOrderIds.contains(order.getId())));
    }

    public Page<OrderSummaryResponse> getSellerOrderSummaries(Long sellerId, Order.OrderStatus status, Pageable pageable) {
        return getSellerOrders(sellerId, status, pageable)
                .map(order -> OrderSummaryResponse.from(order, false));
    }

    public List<Order> getOrdersByItemId(Long itemId, Long sellerId) {
        return orderRepository.findByItemIdAndSellerId(itemId, sellerId);
    }

    public List<Order> getActiveOrdersByItemId(Long itemId) {
        return orderRepository.findByItemIdAndOrderStatusIn(itemId, OrderService.ACTIVE_STATUSES);
    }

    public Page<AdminOrderResponse> getAdminOrderSummaries(
            String keyword, Order.OrderStatus status, String paymentMethod, Pageable pageable) {
        return orderRepository.searchAdminOrders(keyword, status, paymentMethod, pageable)
                .map(AdminOrderResponse::from);
    }

    public OrderSummaryResponse getOrderSummary(Long orderId, Long userId) {
        Order order = getOrderById(orderId, userId);
        boolean reviewed = order.getBuyerId().equals(userId)
                && reviewRepository.existsByOrderIdAndReviewerId(order.getId(), userId);
        return OrderSummaryResponse.from(order, reviewed);
    }

    public AdminOrderResponse getAdminOrderSummary(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return AdminOrderResponse.from(order);
    }

    public Map<String, Object> getAdminOrderStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", orderRepository.count());

        Map<String, Long> statusCountMap = new HashMap<>();
        List<Object[]> groupedCounts = orderRepository.countByOrderStatusGrouped();
        for (Object[] row : groupedCounts) {
            Order.OrderStatus status = (Order.OrderStatus) row[0];
            Long count = (Long) row[1];
            statusCountMap.put(status.name(), count);
        }

        stats.put("pendingPayment", statusCountMap.getOrDefault("PENDING_PAYMENT", 0L));
        stats.put("pendingShipment", statusCountMap.getOrDefault("PENDING_SHIPMENT", 0L));
        stats.put("shipped", statusCountMap.getOrDefault("SHIPPED", 0L));
        stats.put("completed", statusCountMap.getOrDefault("COMPLETED", 0L));
        stats.put("cancelled", statusCountMap.getOrDefault("CANCELLED", 0L));
        stats.put("refundRequested", statusCountMap.getOrDefault("REFUND_REQUESTED", 0L));
        stats.put("refunded", statusCountMap.getOrDefault("REFUNDED", 0L));
        stats.put("amount", orderRepository.sumCompletedOrderAmount());
        return stats;
    }
}
