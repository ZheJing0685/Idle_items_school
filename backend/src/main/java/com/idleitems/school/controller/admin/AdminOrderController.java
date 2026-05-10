package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.dto.order.AdminOrderResponse;
import com.idleitems.school.dto.order.CancelOrderRequest;
import com.idleitems.school.entity.Order;
import com.idleitems.school.entity.User;
import com.idleitems.school.service.AdminLogService;
import com.idleitems.school.service.DictService;
import com.idleitems.school.service.OrderService;
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
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
public class AdminOrderController {

    private final OrderService orderService;
    private final AdminLogService adminLogService;
    private final DictService dictService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getOrderStats() {
        return Result.success(orderService.getAdminOrderStats());
    }

    @GetMapping
    public Result<Page<AdminOrderResponse>> getOrders(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) Order.OrderStatus status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(orderService.getAdminOrderSummaries(keyword, status, paymentMethod, pageable));
    }

    @GetMapping("/{id}")
    public Result<AdminOrderResponse> getOrder(@PathVariable Long id) {
        return Result.success(orderService.getAdminOrderSummary(id));
    }

    @PutMapping("/{id}/cancel")
    public Result<AdminOrderResponse> cancelOrder(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @Valid @RequestBody CancelOrderRequest request,
            HttpServletRequest httpRequest) {
        Order order = orderService.adminCancelOrder(id, adminId, request.getReason());

        Map<String, Object> details = new HashMap<>();
        details.put("orderId", id);
        details.put("orderNo", order.getOrderNo());
        details.put("status", "CANCELLED");
        details.put("statusLabel", dictService.getDictLabel("ORDER_STATUS", "CANCELLED"));
        details.put("reason", request.getReason());
        adminLogService.logOperation(adminId, "管理员取消订单", "ORDER", id, details, httpRequest);
        
        return Result.success("订单已取消", orderService.toAdminOrderSummary(order));
    }

    @PutMapping("/{id}/refund/approve")
    public Result<AdminOrderResponse> approveRefund(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        Order savedOrder = orderService.approveRefund(id, adminId, "APPROVED");
        
        Map<String, Object> details = new HashMap<>();
        details.put("orderId", savedOrder.getId());
        details.put("orderNo", savedOrder.getOrderNo());
        details.put("status", "REFUNDED");
        details.put("statusLabel", dictService.getDictLabel("ORDER_STATUS", "REFUNDED"));
        adminLogService.logOperation(adminId, "审批退款", "ORDER", id, details, request);
        
        return Result.success("退款已审批", orderService.toAdminOrderSummary(savedOrder));
    }
}
