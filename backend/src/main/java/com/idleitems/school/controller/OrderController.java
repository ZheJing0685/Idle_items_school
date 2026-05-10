package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.dto.order.CancelOrderRequest;
import com.idleitems.school.dto.order.CreateOrderRequest;
import com.idleitems.school.dto.order.OrderSummaryResponse;
import com.idleitems.school.dto.order.RefundRequest;
import com.idleitems.school.entity.Order;
import com.idleitems.school.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.Order.BASE)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrderSummaryResponse> createOrder(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(userId, request);
        return Result.success("订单创建成功", orderService.toOrderSummary(order, userId));
    }

    @GetMapping
    public Result<Page<OrderSummaryResponse>> getBuyerOrders(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "status", required = false) Order.OrderStatus status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(orderService.getBuyerOrderSummaries(userId, status, pageable));
    }

    @GetMapping("/seller")
    public Result<Page<OrderSummaryResponse>> getSellerOrders(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "status", required = false) Order.OrderStatus status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(orderService.getSellerOrderSummaries(userId, status, pageable));
    }

    @GetMapping("/{id}")
    public Result<OrderSummaryResponse> getOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        return Result.success(orderService.getOrderSummary(id, userId));
    }

    @PostMapping("/{id}/pay")
    public Result<OrderSummaryResponse> payOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @RequestParam(value = "paymentMethod", defaultValue = "OFFLINE") String paymentMethod) {
        Order order = orderService.payOrder(id, userId, paymentMethod);
        return Result.success("支付成功", orderService.toOrderSummary(order, userId));
    }

    @PostMapping("/{id}/cancel")
    public Result<OrderSummaryResponse> cancelOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody CancelOrderRequest request) {
        Order order = orderService.cancelOrder(id, userId, request);
        return Result.success("订单已取消", orderService.toOrderSummary(order, userId));
    }

    @PostMapping("/{id}/ship")
    public Result<OrderSummaryResponse> shipOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        Order order = orderService.shipOrder(id, userId);
        return Result.success("发货成功", orderService.toOrderSummary(order, userId));
    }

    @PostMapping("/{id}/shipping")
    public Result<OrderSummaryResponse> updateShippingInfo(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @RequestParam String trackingNumber,
            @RequestParam String shippingCompany) {
        Order order = orderService.updateShippingInfo(id, userId, trackingNumber, shippingCompany);
        return Result.success("物流信息更新成功", orderService.toOrderSummary(order, userId));
    }

    @PostMapping("/{id}/confirm-receive")
    public Result<OrderSummaryResponse> confirmReceive(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        Order order = orderService.confirmReceive(id, userId);
        return Result.success("确认收货成功", orderService.toOrderSummary(order, userId));
    }

    @PostMapping("/{id}/refund")
    public Result<OrderSummaryResponse> applyRefund(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {
        Order order = orderService.applyRefund(id, userId, request);
        return Result.success("退款申请已提交", orderService.toOrderSummary(order, userId));
    }
}
