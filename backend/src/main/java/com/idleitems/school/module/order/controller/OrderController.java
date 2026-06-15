package com.idleitems.school.module.order.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.common.annotation.Idempotent;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.module.order.dto.CancelOrderRequest;
import com.idleitems.school.module.order.dto.CreateOrderRequest;
import com.idleitems.school.module.order.dto.OrderSummaryResponse;
import com.idleitems.school.module.order.dto.RefundRequest;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.order.service.OrderBuyerService;
import com.idleitems.school.module.order.service.OrderSellerService;
import com.idleitems.school.module.order.service.OrderQueryService;
import com.idleitems.school.module.order.service.OrderRefundService;
import com.idleitems.school.module.order.service.OrderAdminService;
import com.idleitems.school.module.order.service.OrderTimeoutService;
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

    private final OrderBuyerService orderBuyerService;
    private final OrderSellerService orderSellerService;
    private final OrderQueryService orderQueryService;
    private final OrderRefundService orderRefundService;

    @Operation(summary = "创建订单", description = "买家创建新订单")
    @PostMapping
    @Idempotent(message = "订单正在处理中，请勿重复提交")
    public Result<OrderSummaryResponse> createOrder(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateOrderRequest request) {
        Order order = orderBuyerService.createOrder(userId, request);
        return Result.success("订单创建成功", orderQueryService.toOrderSummary(order, userId));
    }

    @Operation(summary = "获取买家订单列表", description = "分页查询买家订单，可按订单状态筛选")
    @GetMapping
    public Result<Page<OrderSummaryResponse>> getBuyerOrders(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "status", required = false) Order.OrderStatus status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(orderQueryService.getBuyerOrderSummaries(userId, status, pageable));
    }

    @Operation(summary = "获取卖家订单列表", description = "分页查询卖家订单，可按订单状态筛选")
    @GetMapping("/seller")
    public Result<Page<OrderSummaryResponse>> getSellerOrders(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "status", required = false) Order.OrderStatus status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(orderQueryService.getSellerOrderSummaries(userId, status, pageable));
    }

    @Operation(summary = "获取订单详情", description = "根据订单ID获取订单详细信息")
    @GetMapping("/{id}")
    public Result<OrderSummaryResponse> getOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        return Result.success(orderQueryService.getOrderSummary(id, userId));
    }

    @Operation(summary = "支付订单", description = "买家对指定订单进行支付")
    @PostMapping("/{id}/pay")
    @Idempotent(message = "支付正在处理中，请勿重复支付")
    public Result<OrderSummaryResponse> payOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @RequestParam(value = "paymentMethod", defaultValue = "OFFLINE") String paymentMethod) {
        Order order = orderBuyerService.payOrder(id, userId, paymentMethod);
        return Result.success("支付成功", orderQueryService.toOrderSummary(order, userId));
    }

    @Operation(summary = "取消订单", description = "取消指定订单并填写取消原因")
    @PostMapping("/{id}/cancel")
    public Result<OrderSummaryResponse> cancelOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody CancelOrderRequest request) {
        Order order = orderBuyerService.cancelOrder(id, userId, request);
        return Result.success("订单已取消", orderQueryService.toOrderSummary(order, userId));
    }

    @Operation(summary = "发货", description = "卖家对订单进行发货操作")
    @PostMapping("/{id}/ship")
    public Result<OrderSummaryResponse> shipOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        Order order = orderSellerService.shipOrder(id, userId);
        return Result.success("发货成功", orderQueryService.toOrderSummary(order, userId));
    }

    @Operation(summary = "更新物流信息", description = "卖家更新订单的物流单号和物流公司")
    @PostMapping("/{id}/shipping")
    public Result<OrderSummaryResponse> updateShippingInfo(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @RequestParam String trackingNumber,
            @RequestParam String shippingCompany) {
        Order order = orderSellerService.updateShippingInfo(id, userId, trackingNumber, shippingCompany);
        return Result.success("物流信息更新成功", orderQueryService.toOrderSummary(order, userId));
    }

    @Operation(summary = "确认收货", description = "买家确认收货完成交易")
    @PostMapping("/{id}/confirm-receive")
    public Result<OrderSummaryResponse> confirmReceive(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        Order order = orderBuyerService.confirmReceive(id, userId);
        return Result.success("确认收货成功", orderQueryService.toOrderSummary(order, userId));
    }

    @Operation(summary = "申请退款", description = "买家提交退款申请")
    @PostMapping("/{id}/refund")
    public Result<OrderSummaryResponse> applyRefund(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {
        Order order = orderRefundService.applyRefund(id, userId, request);
        return Result.success("退款申请已提交", orderQueryService.toOrderSummary(order, userId));
    }
}
