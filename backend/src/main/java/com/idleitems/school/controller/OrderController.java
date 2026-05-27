package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.dto.order.CancelOrderRequest;
import com.idleitems.school.dto.order.CreateOrderRequest;
import com.idleitems.school.dto.order.OrderSummaryResponse;
import com.idleitems.school.dto.order.RefundRequest;
import com.idleitems.school.entity.Order;
import com.idleitems.school.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单管理", description = "订单创建、支付、发货、收货等接口")
@RestController
@RequestMapping(ApiPaths.Order.BASE)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单", description = "买家创建新订单")
    @PostMapping
    public Result<OrderSummaryResponse> createOrder(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(userId, request);
        return Result.success("订单创建成功", orderService.toOrderSummary(order, userId));
    }

    @Operation(summary = "获取买家订单列表", description = "分页查询买家订单，可按订单状态筛选")
    @GetMapping
    public Result<Page<OrderSummaryResponse>> getBuyerOrders(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "status", required = false) Order.OrderStatus status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(orderService.getBuyerOrderSummaries(userId, status, pageable));
    }

    @Operation(summary = "获取卖家订单列表", description = "分页查询卖家订单，可按订单状态筛选")
    @GetMapping("/seller")
    public Result<Page<OrderSummaryResponse>> getSellerOrders(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "status", required = false) Order.OrderStatus status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(orderService.getSellerOrderSummaries(userId, status, pageable));
    }

    @Operation(summary = "获取订单详情", description = "根据订单ID获取订单详细信息")
    @GetMapping("/{id}")
    public Result<OrderSummaryResponse> getOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        return Result.success(orderService.getOrderSummary(id, userId));
    }

    @Operation(summary = "支付订单", description = "买家对指定订单进行支付")
    @PostMapping("/{id}/pay")
    public Result<OrderSummaryResponse> payOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @RequestParam(value = "paymentMethod", defaultValue = "OFFLINE") String paymentMethod) {
        Order order = orderService.payOrder(id, userId, paymentMethod);
        return Result.success("支付成功", orderService.toOrderSummary(order, userId));
    }

    @Operation(summary = "取消订单", description = "取消指定订单并填写取消原因")
    @PostMapping("/{id}/cancel")
    public Result<OrderSummaryResponse> cancelOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody CancelOrderRequest request) {
        Order order = orderService.cancelOrder(id, userId, request);
        return Result.success("订单已取消", orderService.toOrderSummary(order, userId));
    }

    @Operation(summary = "发货", description = "卖家对订单进行发货操作")
    @PostMapping("/{id}/ship")
    public Result<OrderSummaryResponse> shipOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        Order order = orderService.shipOrder(id, userId);
        return Result.success("发货成功", orderService.toOrderSummary(order, userId));
    }

    @Operation(summary = "更新物流信息", description = "卖家更新订单的物流单号和物流公司")
    @PostMapping("/{id}/shipping")
    public Result<OrderSummaryResponse> updateShippingInfo(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @RequestParam String trackingNumber,
            @RequestParam String shippingCompany) {
        Order order = orderService.updateShippingInfo(id, userId, trackingNumber, shippingCompany);
        return Result.success("物流信息更新成功", orderService.toOrderSummary(order, userId));
    }

    @Operation(summary = "确认收货", description = "买家确认收货完成交易")
    @PostMapping("/{id}/confirm-receive")
    public Result<OrderSummaryResponse> confirmReceive(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        Order order = orderService.confirmReceive(id, userId);
        return Result.success("确认收货成功", orderService.toOrderSummary(order, userId));
    }

    @Operation(summary = "申请退款", description = "买家提交退款申请")
    @PostMapping("/{id}/refund")
    public Result<OrderSummaryResponse> applyRefund(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {
        Order order = orderService.applyRefund(id, userId, request);
        return Result.success("退款申请已提交", orderService.toOrderSummary(order, userId));
    }
}
