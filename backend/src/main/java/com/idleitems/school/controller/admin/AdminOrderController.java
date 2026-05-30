package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.idleitems.school.dto.order.AdminOrderResponse;
import com.idleitems.school.dto.order.CancelOrderRequest;
import com.idleitems.school.entity.Order;
import com.idleitems.school.entity.User;
import com.idleitems.school.service.AdminLogService;
import com.idleitems.school.service.DictService;
import com.idleitems.school.service.OrderBuyerService;
import com.idleitems.school.service.OrderSellerService;
import com.idleitems.school.service.OrderQueryService;
import com.idleitems.school.service.OrderRefundService;
import com.idleitems.school.service.OrderAdminService;
import com.idleitems.school.service.OrderTimeoutService;
import com.idleitems.school.config.ApiPaths;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(ApiPaths.Admin.ORDERS)
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
@Tag(name = "管理员-订单管理", description = "管理员订单管理相关接口")
public class AdminOrderController {

    private final OrderAdminService orderAdminService;
    private final OrderRefundService orderRefundService;
    private final OrderQueryService orderQueryService;
    private final AdminLogService adminLogService;
    private final DictService dictService;

    @GetMapping("/stats")
    @Operation(summary = "获取订单统计", description = "获取订单总数、金额及各状态统计信息")
    public Result<Map<String, Object>> getOrderStats() {
        return Result.success(orderQueryService.getAdminOrderStats());
    }

    @GetMapping
    @Operation(summary = "获取订单列表", description = "分页查询所有订单，支持按状态和关键字筛选")
    public Result<Page<AdminOrderResponse>> getOrders(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) Order.OrderStatus status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(orderQueryService.getAdminOrderSummaries(keyword, status, paymentMethod, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情", description = "根据ID获取指定订单的详细信息")
    public Result<AdminOrderResponse> getOrder(@PathVariable Long id) {
        return Result.success(orderQueryService.getAdminOrderSummary(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "管理员取消指定订单")
    public Result<AdminOrderResponse> cancelOrder(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @Valid @RequestBody CancelOrderRequest request,
            HttpServletRequest httpRequest) {
        Order order = orderAdminService.adminCancelOrder(id, adminId, request.getReason());

        Map<String, Object> details = new HashMap<>();
        details.put("orderId", id);
        details.put("orderNo", order.getOrderNo());
        details.put("status", "CANCELLED");
        details.put("statusLabel", dictService.getDictLabel("ORDER_STATUS", "CANCELLED"));
        details.put("reason", request.getReason());
        adminLogService.logOperation(adminId, "管理员取消订单", "ORDER", id, details, httpRequest);

        return Result.success("订单已取消", AdminOrderResponse.from(order));
    }

    @PostMapping("/{id}/refund/approve")
    @Operation(summary = "审批退款", description = "审批通过指定订单的退款申请")
    public Result<AdminOrderResponse> approveRefund(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        Order savedOrder = orderRefundService.approveRefund(id, adminId, "APPROVED");

        Map<String, Object> details = new HashMap<>();
        details.put("orderId", savedOrder.getId());
        details.put("orderNo", savedOrder.getOrderNo());
        details.put("status", "REFUNDED");
        details.put("statusLabel", dictService.getDictLabel("ORDER_STATUS", "REFUNDED"));
        adminLogService.logOperation(adminId, "审批退款", "ORDER", id, details, request);

        return Result.success("退款已审批", AdminOrderResponse.from(savedOrder));
    }
}
