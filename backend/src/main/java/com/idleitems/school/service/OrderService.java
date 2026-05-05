package com.idleitems.school.service;

import com.idleitems.school.dto.order.CancelOrderRequest;
import com.idleitems.school.dto.order.CreateOrderRequest;
import com.idleitems.school.dto.order.AdminOrderResponse;
import com.idleitems.school.dto.order.OrderSummaryResponse;
import com.idleitems.school.dto.order.RefundRequest;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.Order;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public Order createOrder(Long buyerId, CreateOrderRequest request) {
        log.info("创建订单，用户ID: {}, 物品ID: {}", buyerId, request.getItemId());

        Item item = itemRepository.findItemByIdWithLock(request.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

        if (item.getStatus() != Item.ItemStatus.ON_SALE) {
            throw new IllegalArgumentException("物品已下架或已售出");
        }

        if (item.getUserId().equals(buyerId)) {
            throw new IllegalArgumentException("不能购买自己的物品");
        }

        List<Order.OrderStatus> activeStatuses = List.of(
                Order.OrderStatus.PENDING_PAYMENT,
                Order.OrderStatus.PENDING_SHIPMENT,
                Order.OrderStatus.SHIPPED,
                Order.OrderStatus.REFUND_REQUESTED
        );
        if (orderRepository.existsByBuyerIdAndItemIdAndOrderStatusIn(buyerId, request.getItemId(), activeStatuses)) {
            throw new IllegalArgumentException("您已购买过该物品的订单");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
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
        log.info("订单创建成功，订单号: {}, 物品ID: {}", savedOrder.getOrderNo(), item.getId());

        return savedOrder;
    }

    public Order getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new IllegalArgumentException("无权查看此订单");
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

    public Page<AdminOrderResponse> getAdminOrderSummaries(
            String keyword,
            Order.OrderStatus status,
            String paymentMethod,
            Pageable pageable
    ) {
        return orderRepository.searchAdminOrders(keyword, status, paymentMethod, pageable)
                .map(AdminOrderResponse::from);
    }

    public AdminOrderResponse getAdminOrderSummary(Long orderId) {
        Order order = orderRepository.findById(orderId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        return toAdminOrderSummary(order);
    }

    public AdminOrderResponse toAdminOrderSummary(Order order) {
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

    public OrderSummaryResponse getOrderSummary(Long orderId, Long userId) {
        Order order = getOrderById(orderId, userId);
        return toOrderSummary(order, userId);
    }

    public OrderSummaryResponse toOrderSummary(Order order, Long userId) {
        boolean reviewed = order.getBuyerId().equals(userId)
                && reviewRepository.existsByOrderIdAndReviewerId(order.getId(), userId);
        return OrderSummaryResponse.from(order, reviewed);
    }

    @Transactional
    public Order payOrder(Long orderId, Long userId, String paymentMethod) {
        log.info("支付订单，订单ID: {}, 用户ID: {}, 支付方式: {}", orderId, userId, paymentMethod);

        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (!order.getBuyerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此订单");
        }

        if (order.getOrderStatus() != Order.OrderStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException("订单状态不正确，无法支付");
        }

        Item item = itemRepository.findItemByIdWithLock(order.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

        if (item.getStatus() != Item.ItemStatus.ON_SALE) {
            throw new IllegalArgumentException("物品已下架或已售出，无法支付");
        }

        order.setOrderStatus(Order.OrderStatus.PENDING_SHIPMENT);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentTime(LocalDateTime.now());

        item.setStatus(Item.ItemStatus.SOLD);
        itemRepository.save(item);

        Order savedOrder = orderRepository.save(order);
        log.info("订单支付成功，订单号: {}, 物品ID: {}", savedOrder.getOrderNo(), item.getId());

        return savedOrder;
    }

    @Transactional
    public Order cancelOrder(Long orderId, Long userId, CancelOrderRequest request) {
        log.info("取消订单，订单ID: {}, 用户ID: {}", orderId, userId);

        Order order = orderRepository.findById(orderId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (!order.getBuyerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此订单");
        }

        if (order.getOrderStatus() != Order.OrderStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException("只有待支付的订单才能取消");
        }

        order.setOrderStatus(Order.OrderStatus.CANCELLED);
        order.setCancelReason(request.getReason());

        Order savedOrder = orderRepository.save(order);
        log.info("订单取消成功，订单号: {}", savedOrder.getOrderNo());

        return savedOrder;
    }

    @Transactional
    public Order shipOrder(Long orderId, Long userId) {
        log.info("发货订单，订单ID: {}, 用户ID: {}", orderId, userId);

        Order order = orderRepository.findById(orderId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (!order.getSellerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此订单");
        }

        if (order.getOrderStatus() != Order.OrderStatus.PENDING_SHIPMENT) {
            throw new IllegalArgumentException("只有待发货的订单才能发货");
        }

        order.setOrderStatus(Order.OrderStatus.SHIPPED);
        order.setShipTime(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("订单发货成功，订单号: {}", savedOrder.getOrderNo());

        return savedOrder;
    }

    @Transactional
    public Order updateShippingInfo(Long orderId, Long userId, String trackingNumber, String shippingCompany) {
        log.info("更新物流信息，订单ID: {}, 用户ID: {}, 快递单号: {}, 快递公司: {}",
                orderId, userId, trackingNumber, shippingCompany);

        Order order = orderRepository.findById(orderId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (!order.getSellerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此订单");
        }

        if (order.getOrderStatus() != Order.OrderStatus.PENDING_SHIPMENT
                && order.getOrderStatus() != Order.OrderStatus.SHIPPED) {
            throw new IllegalArgumentException("当前订单状态不允许更新物流信息");
        }

        order.setTrackingNumber(trackingNumber);
        order.setShippingCompany(shippingCompany);
        order.setShipTime(LocalDateTime.now());
        order.setOrderStatus(Order.OrderStatus.SHIPPED);

        Order savedOrder = orderRepository.save(order);
        log.info("物流信息更新成功，订单号: {}, 快递单号: {}", savedOrder.getOrderNo(), trackingNumber);

        return savedOrder;
    }

    @Transactional
    public Order confirmReceive(Long orderId, Long userId) {
        log.info("确认收货，订单ID: {}, 用户ID: {}", orderId, userId);

        Order order = orderRepository.findById(orderId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (!order.getBuyerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此订单");
        }

        if (order.getOrderStatus() != Order.OrderStatus.SHIPPED) {
            throw new IllegalArgumentException("只有已发货的订单才能确认收货");
        }

        order.setOrderStatus(Order.OrderStatus.COMPLETED);
        order.setCompleteTime(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("确认收货成功，订单号: {}", savedOrder.getOrderNo());

        return savedOrder;
    }

    @Transactional
    public Order applyRefund(Long orderId, Long userId, RefundRequest request) {
        log.info("申请退款，订单ID: {}, 用户ID: {}", orderId, userId);

        Order order = orderRepository.findById(orderId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (!order.getBuyerId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此订单");
        }

        if (order.getOrderStatus() != Order.OrderStatus.PENDING_SHIPMENT &&
            order.getOrderStatus() != Order.OrderStatus.SHIPPED) {
            throw new IllegalArgumentException("当前状态无法申请退款");
        }

        order.setOrderStatus(Order.OrderStatus.REFUND_REQUESTED);
        order.setRefundReason(request.getReason());

        Order savedOrder = orderRepository.save(order);
        log.info("退款申请成功，订单号: {}", savedOrder.getOrderNo());

        return savedOrder;
    }

    @Transactional
    public Order approveRefund(Long orderId, Long adminId, String result) {
        log.info("审批退款，订单ID: {}, 管理员ID: {}, 结果: {}", orderId, adminId, result);

        Order order = orderRepository.findById(orderId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (order.getOrderStatus() != Order.OrderStatus.REFUND_REQUESTED) {
            throw new IllegalArgumentException("订单状态不正确");
        }

        if ("REJECTED".equals(result)) {
            log.info("退款申请被拒绝，订单号: {}", order.getOrderNo());
            throw new IllegalArgumentException("退款申请被拒绝");
        }

        if (!"APPROVED".equals(result)) {
            throw new IllegalArgumentException("无效的审批结果");
        }

        order.setOrderStatus(Order.OrderStatus.REFUNDED);
        order.setRefundAmount(order.getPrice());
        order.setRefundTime(LocalDateTime.now());

        Item item = itemRepository.findById(order.getItemId().longValue())
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

        if (!hasOtherActiveOrdersForItem(order.getItemId(), order.getId())) {
            item.setStatus(Item.ItemStatus.ON_SALE);
            itemRepository.save(item);
            log.info("物品已重新上架，物品ID: {}", item.getId());
        } else {
            log.info("物品被其他订单占用，保持下架状态，物品ID: {}", item.getId());
        }

        Order savedOrder = orderRepository.save(order);
        log.info("退款完成，订单号: {}", savedOrder.getOrderNo());

        return savedOrder;
    }

    private boolean hasOtherActiveOrdersForItem(Long itemId, Long excludeOrderId) {
        List<Order.OrderStatus> activeStatuses = List.of(
                Order.OrderStatus.PENDING_PAYMENT,
                Order.OrderStatus.PENDING_SHIPMENT,
                Order.OrderStatus.SHIPPED,
                Order.OrderStatus.REFUND_REQUESTED
        );
        return orderRepository.existsByItemIdAndOrderStatusInAndIdNot(itemId, activeStatuses, excludeOrderId);
    }

    @Transactional
    public Order adminCancelOrder(Long orderId, Long adminId, String reason) {
        log.info("管理员取消订单，订单ID: {}, 管理员ID: {}, 原因: {}", orderId, adminId, reason);

        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (order.getOrderStatus() == Order.OrderStatus.COMPLETED
                || order.getOrderStatus() == Order.OrderStatus.CANCELLED
                || order.getOrderStatus() == Order.OrderStatus.REFUNDED
                || order.getOrderStatus() == Order.OrderStatus.REFUND_REQUESTED) {
            throw new IllegalArgumentException("当前订单状态不允许管理员取消");
        }

        boolean wasPaid = order.getPaymentTime() != null;
        if (wasPaid) {
            log.info("管理员取消已支付订单，订单号: {}, 需要处理退款", order.getOrderNo());
        }

        if (order.getOrderStatus() == Order.OrderStatus.PENDING_SHIPMENT
                || order.getOrderStatus() == Order.OrderStatus.SHIPPED) {
            Item item = itemRepository.findItemByIdWithLock(order.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

            if (!hasOtherActiveOrdersForItem(order.getItemId(), order.getId())) {
                item.setStatus(Item.ItemStatus.ON_SALE);
                itemRepository.save(item);
                log.info("物品已重新上架，物品ID: {}", item.getId());
            } else {
                log.info("物品被其他订单占用，保持下架状态，物品ID: {}", item.getId());
            }
        }

        order.setOrderStatus(Order.OrderStatus.CANCELLED);
        order.setCancelReason("管理员取消: " + reason);

        Order savedOrder = orderRepository.save(order);
        log.info("管理员取消订单成功，订单号: {}, 是否已支付: {}", savedOrder.getOrderNo(), wasPaid);

        return savedOrder;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD" + timestamp + uuid;
    }

    public int cancelTimeoutOrders(int timeoutMinutes) {
        log.info("开始取消超时订单，超时时间: {} 分钟", timeoutMinutes);

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<Order> timeoutOrders = orderRepository.findTimeoutOrders(
                Order.OrderStatus.PENDING_PAYMENT,
                threshold
        );

        int cancelledCount = 0;
        for (Order order : timeoutOrders) {
            try {
                cancelSingleTimeoutOrder(order.getId());
                cancelledCount++;
            } catch (Exception e) {
                log.error("取消超时订单失败，订单ID: {}, 错误: {}", order.getId(), e.getMessage());
            }
        }

        log.info("超时订单取消完成，共取消 {} 个订单", cancelledCount);
        return cancelledCount;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelSingleTimeoutOrder(Long orderId) {
        Order lockedOrder = orderRepository.findByIdWithLock(orderId)
                .orElse(null);
        if (lockedOrder == null || lockedOrder.getOrderStatus() != Order.OrderStatus.PENDING_PAYMENT) {
            return;
        }

        lockedOrder.setOrderStatus(Order.OrderStatus.CANCELLED);
        lockedOrder.setCancelReason("订单超时未支付，系统自动取消");
        orderRepository.save(lockedOrder);

        log.info("超时订单已取消，订单号: {}", lockedOrder.getOrderNo());
    }
}
