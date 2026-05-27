package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.dto.order.AdminOrderResponse;
import com.idleitems.school.dto.order.CancelOrderRequest;
import com.idleitems.school.dto.order.CreateOrderRequest;
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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;

    static final List<Order.OrderStatus> ACTIVE_STATUSES = List.of(
            Order.OrderStatus.PENDING_PAYMENT,
            Order.OrderStatus.PENDING_SHIPMENT,
            Order.OrderStatus.SHIPPED,
            Order.OrderStatus.REFUND_REQUESTED
    );

    @Transactional
    public Order createOrder(Long buyerId, CreateOrderRequest request) {
        log.info("创建订单，用户ID: {}, 物品ID: {}", buyerId, request.getItemId());

        Item item = itemRepository.findItemByIdWithLock(request.getItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        if (item.getStatus() != Item.ItemStatus.ON_SALE) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "物品已下架或已售出");
        }

        if (item.getUserId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "不能购买自己的物品");
        }

        if (orderRepository.existsByBuyerIdAndItemIdAndOrderStatusIn(buyerId, request.getItemId(), ACTIVE_STATUSES)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "您已购买过该物品的订单");
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

        // 发送通知给卖家
        notificationService.createOrderNotification(
                item.getUserId(),
                "您有新的订单",
                "订单号 " + savedOrder.getOrderNo() + "，商品：" + item.getTitle(),
                savedOrder.getId()
        );

        return savedOrder;
    }

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
        return orderRepository.findByItemIdAndOrderStatusIn(itemId, ACTIVE_STATUSES);
    }

    public Page<AdminOrderResponse> getAdminOrderSummaries(
            String keyword, Order.OrderStatus status, String paymentMethod, Pageable pageable) {
        return orderRepository.searchAdminOrders(keyword, status, paymentMethod, pageable)
                .map(AdminOrderResponse::from);
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

    public OrderSummaryResponse getOrderSummary(Long orderId, Long userId) {
        Order order = getOrderById(orderId, userId);
        return toOrderSummary(order, userId);
    }

    @Transactional
    public Order payOrder(Long orderId, Long userId, String paymentMethod) {
        log.info("支付订单，订单ID: {}, 用户ID: {}, 支付方式: {}", orderId, userId, paymentMethod);

        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作此订单");
        }

        if (order.getOrderStatus() != Order.OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "订单状态不正确，无法支付");
        }

        Item item = itemRepository.findItemByIdWithLock(order.getItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        int updated = itemRepository.markItemAsSold(order.getItemId());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "物品已下架或已售出，无法支付");
        }

        order.setOrderStatus(Order.OrderStatus.PENDING_SHIPMENT);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentTime(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("订单支付成功，订单号: {}, 物品ID: {}", savedOrder.getOrderNo(), item.getId());

        // 发送通知给卖家
        notificationService.createOrderNotification(
                order.getSellerId(),
                "订单已支付",
                "订单号 " + savedOrder.getOrderNo() + " 已支付，请尽快发货",
                savedOrder.getId()
        );

        return savedOrder;
    }

    @Transactional
    public Order cancelOrder(Long orderId, Long userId, CancelOrderRequest request) {
        log.info("取消订单，订单ID: {}, 用户ID: {}", orderId, userId);

        // 使用悲观锁防止并发操作
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作此订单");
        }

        if (order.getOrderStatus() != Order.OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "只有待支付的订单才能取消");
        }

        order.setOrderStatus(Order.OrderStatus.CANCELLED);
        order.setCancelReason(request.getReason());
        order.setCancelTime(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("订单取消成功，订单号: {}", savedOrder.getOrderNo());

        // 发送通知给卖家
        notificationService.createOrderNotification(
                order.getSellerId(),
                "订单已取消",
                "订单号 " + savedOrder.getOrderNo() + " 已取消，原因：" + request.getReason(),
                savedOrder.getId()
        );

        return savedOrder;
    }

    @Transactional
    public Order shipOrder(Long orderId, Long userId) {
        log.info("发货订单，订单ID: {}, 用户ID: {}", orderId, userId);

        // 使用悲观锁防止并发操作
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作此订单");
        }

        if (order.getOrderStatus() != Order.OrderStatus.PENDING_SHIPMENT) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "只有待发货的订单才能发货");
        }

        order.setOrderStatus(Order.OrderStatus.SHIPPED);
        order.setShipTime(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("订单发货成功，订单号: {}", savedOrder.getOrderNo());

        // 发送通知给买家
        notificationService.createOrderNotification(
                order.getBuyerId(),
                "订单已发货",
                "订单号 " + savedOrder.getOrderNo() + " 已发货，请注意查收",
                savedOrder.getId()
        );

        return savedOrder;
    }

    @Transactional
    public Order updateShippingInfo(Long orderId, Long userId, String trackingNumber, String shippingCompany) {
        log.info("更新物流信息，订单ID: {}, 用户ID: {}, 快递单号: {}, 快递公司: {}",
                orderId, userId, trackingNumber, shippingCompany);

        // 使用悲观锁防止并发操作
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作此订单");
        }

        if (order.getOrderStatus() != Order.OrderStatus.PENDING_SHIPMENT
                && order.getOrderStatus() != Order.OrderStatus.SHIPPED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "当前订单状态不允许更新物流信息");
        }

        order.setTrackingNumber(trackingNumber);
        order.setShippingCompany(shippingCompany);
        order.setShipTime(LocalDateTime.now());
        order.setOrderStatus(Order.OrderStatus.SHIPPED);

        Order savedOrder = orderRepository.save(order);
        log.info("物流信息更新成功，订单号: {}, 快递单号: {}", savedOrder.getOrderNo(), trackingNumber);

        // 发送通知给买家
        notificationService.createOrderNotification(
                order.getBuyerId(),
                "订单已发货",
                "订单号 " + savedOrder.getOrderNo() + " 已发货，快递公司: " + shippingCompany + "，单号: " + trackingNumber,
                savedOrder.getId()
        );

        return savedOrder;
    }

    @Transactional
    public Order confirmReceive(Long orderId, Long userId) {
        log.info("确认收货，订单ID: {}, 用户ID: {}", orderId, userId);

        // 使用悲观锁防止并发操作
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作此订单");
        }

        if (order.getOrderStatus() != Order.OrderStatus.SHIPPED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "只有已发货的订单才能确认收货");
        }

        order.setOrderStatus(Order.OrderStatus.COMPLETED);
        order.setCompleteTime(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("确认收货成功，订单号: {}", savedOrder.getOrderNo());

        // 发送通知给卖家
        notificationService.createOrderNotification(
                order.getSellerId(),
                "订单已确认收货",
                "订单号 " + savedOrder.getOrderNo() + " 已确认收货，交易完成",
                savedOrder.getId()
        );

        return savedOrder;
    }

    @Transactional
    public Order applyRefund(Long orderId, Long userId, RefundRequest request) {
        log.info("申请退款，订单ID: {}, 用户ID: {}", orderId, userId);

        // 使用悲观锁防止并发操作
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作此订单");
        }

        if (order.getOrderStatus() != Order.OrderStatus.PENDING_SHIPMENT &&
            order.getOrderStatus() != Order.OrderStatus.SHIPPED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "当前状态无法申请退款");
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

        // 使用悲观锁防止并发操作
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getOrderStatus() != Order.OrderStatus.REFUND_REQUESTED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "订单状态不正确");
        }

        if ("REJECTED".equals(result)) {
            log.info("退款申请被拒绝，订单号: {}", order.getOrderNo());
            // 根据原状态回退
            if (order.getShipTime() != null) {
                order.setOrderStatus(Order.OrderStatus.SHIPPED);
            } else {
                order.setOrderStatus(Order.OrderStatus.PENDING_SHIPMENT);
            }
            order.setRefundResult(result);
            order.setRefundAdminId(adminId);
            Order savedOrder = orderRepository.save(order);

            // 通知买家退款被拒绝
            notificationService.createOrderNotification(
                    order.getBuyerId(),
                    "退款申请被拒绝",
                    "订单号 " + savedOrder.getOrderNo() + " 的退款申请已被拒绝，如有疑问请联系管理员",
                    savedOrder.getId()
            );

            return savedOrder;
        }

        if (!"APPROVED".equals(result)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无效的审批结果");
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
        log.info("退款审批通过，订单号: {}, 退款金额: {}", savedOrder.getOrderNo(), order.getRefundAmount());

        // 通知买家退款已通过
        notificationService.createOrderNotification(
                order.getBuyerId(),
                "退款申请已通过",
                "订单号 " + savedOrder.getOrderNo() + " 的退款申请已通过，退款金额: ¥" + order.getRefundAmount() + "，预计1-3个工作日到账",
                savedOrder.getId()
        );

        // 通知卖家
        notificationService.createOrderNotification(
                order.getSellerId(),
                "订单退款已处理",
                "订单号 " + savedOrder.getOrderNo() + " 的退款申请已通过，物品已恢复上架",
                savedOrder.getId()
        );

        return savedOrder;
    }

    private boolean hasOtherActiveOrdersForItem(Long itemId, Long excludeOrderId) {
        return orderRepository.existsByItemIdAndOrderStatusInAndIdNot(itemId, ACTIVE_STATUSES, excludeOrderId);
    }

    /**
     * 如果没有其他活跃订单占用该物品，则将其重新上架
     */
    private void restoreItemToSaleIfAvailable(Item item, Long excludeOrderId) {
        if (!hasOtherActiveOrdersForItem(item.getId(), excludeOrderId)) {
            item.setStatus(Item.ItemStatus.ON_SALE);
            itemRepository.save(item);
            log.info("物品已重新上架，物品ID: {}", item.getId());
        } else {
            log.info("物品被其他订单占用，保持下架状态，物品ID: {}", item.getId());
        }
    }

    @Transactional
    public Order adminCancelOrder(Long orderId, Long adminId, String reason) {
        log.info("管理员取消订单，订单ID: {}, 管理员ID: {}, 原因: {}", orderId, adminId, reason);

        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getOrderStatus() == Order.OrderStatus.COMPLETED
                || order.getOrderStatus() == Order.OrderStatus.CANCELLED
                || order.getOrderStatus() == Order.OrderStatus.REFUNDED
                || order.getOrderStatus() == Order.OrderStatus.REFUND_REQUESTED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "当前订单状态不允许管理员取消");
        }

        boolean wasPaid = order.getPaymentTime() != null;
        if (wasPaid) {
            log.info("管理员取消已支付订单，订单号: {}, 处理退款", order.getOrderNo());
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
        order.setCancelReason("管理员取消: " + reason);

        Order savedOrder = orderRepository.save(order);
        log.info("管理员取消订单成功，订单号: {}, 是否已支付: {}", savedOrder.getOrderNo(), wasPaid);

        return savedOrder;
    }

    /**
     * 生成订单号：ORD + 时间戳(yyyyMMddHHmmss) + 随机数(8位) + 随机字母(4位)
     * 格式: ORD20260527170216_A3K9B2M7
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String RANDOM_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // 8位随机数字
        StringBuilder randomDigits = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            randomDigits.append(ThreadLocalRandom.current().nextInt(10));
        }

        // 4位随机字母数字（排除易混淆字符 0/O/1/I/L）
        StringBuilder randomChars = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            randomChars.append(RANDOM_CHARS.charAt(SECURE_RANDOM.nextInt(RANDOM_CHARS.length())));
        }

        return "ORD" + timestamp + randomDigits + randomChars;
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

    public OrderSummaryResponse toOrderSummary(Order order, Long userId) {
        boolean reviewed = order.getBuyerId().equals(userId)
                && reviewRepository.existsByOrderIdAndReviewerId(order.getId(), userId);
        return OrderSummaryResponse.from(order, reviewed);
    }
}
