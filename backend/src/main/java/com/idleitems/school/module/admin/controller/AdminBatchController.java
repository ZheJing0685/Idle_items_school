package com.idleitems.school.module.admin.controller;

import com.idleitems.school.common.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.module.admin.dto.BatchApproveItemsRequest;
import com.idleitems.school.module.admin.dto.BatchCancelOrdersRequest;
import com.idleitems.school.module.admin.dto.BatchDeleteUsersRequest;
import com.idleitems.school.module.admin.dto.BatchOffShelfItemsRequest;
import com.idleitems.school.module.admin.dto.BatchRejectItemsRequest;
import com.idleitems.school.module.admin.dto.BatchUpdateUserStatusRequest;
import com.idleitems.school.module.admin.service.AdminBatchService;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.admin.service.AdminLogService;
import com.idleitems.school.module.system.service.DictService;
import com.idleitems.school.config.ApiPaths;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(ApiPaths.Admin.BATCH)
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
public class AdminBatchController {

    private final AdminBatchService adminBatchService;
    private final AdminLogService adminLogService;
    private final DictService dictService;

    @PostMapping("/items/approve")
    public Result<Void> batchApproveItems(
            @RequestAttribute("userId") Long adminId,
            @Valid @RequestBody BatchApproveItemsRequest request,
            HttpServletRequest requestCtx) {
        List<Long> itemIds = request.getItemIds();
        adminBatchService.batchApproveItems(itemIds);

        Map<String, Object> details = new HashMap<>();
        details.put("count", itemIds.size());
        details.put("action", "approve");
        adminLogService.logOperation(adminId, "批量审核通过物品", "ITEM", null, details, requestCtx);

        return Result.success("批量审核通过成功", null);
    }

    @PostMapping("/items/reject")
    public Result<Void> batchRejectItems(
            @RequestAttribute("userId") Long adminId,
            @Valid @RequestBody BatchRejectItemsRequest request,
            HttpServletRequest requestCtx) {
        List<Long> itemIds = request.getItemIds();
        String reason = request.getReason();
        adminBatchService.batchRejectItems(itemIds, reason);

        Map<String, Object> details = new HashMap<>();
        details.put("count", itemIds.size());
        details.put("action", "reject");
        details.put("reason", reason);
        adminLogService.logOperation(adminId, "批量驳回物品", "ITEM", null, details, requestCtx);

        return Result.success("批量驳回成功", null);
    }

    @PostMapping("/items/off-shelf")
    public Result<Void> batchOffShelfItems(
            @RequestAttribute("userId") Long adminId,
            @Valid @RequestBody BatchOffShelfItemsRequest request,
            HttpServletRequest requestCtx) {
        List<Long> itemIds = request.getItemIds();
        String reason = request.getReason();
        adminBatchService.batchOffShelfItems(itemIds, reason);

        Map<String, Object> details = new HashMap<>();
        details.put("count", itemIds.size());
        details.put("action", "off_shelf");
        details.put("reason", reason);
        adminLogService.logOperation(adminId, "批量下架物品", "ITEM", null, details, requestCtx);

        return Result.success("批量下架成功", null);
    }

    @PostMapping("/users/status")
    public Result<Void> batchUpdateUserStatus(
            @RequestAttribute("userId") Long adminId,
            @Valid @RequestBody BatchUpdateUserStatusRequest request,
            HttpServletRequest requestCtx) {
        List<Long> userIds = request.getUserIds();
        User.UserStatus status;
        try {
            status = User.UserStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的状态值: " + request.getStatus());
        }
        adminBatchService.batchUpdateUserStatus(userIds, status);

        Map<String, Object> details = new HashMap<>();
        details.put("count", userIds.size());
        details.put("status", status);
        adminLogService.logOperation(adminId, "批量更新用户状态", "USER", null, details, requestCtx);

        return Result.success("批量更新用户状态成功", null);
    }

    @PostMapping("/orders/cancel")
    public Result<Void> batchCancelOrders(
            @RequestAttribute("userId") Long adminId,
            @Valid @RequestBody BatchCancelOrdersRequest request,
            HttpServletRequest requestCtx) {
        List<Long> orderIds = request.getOrderIds();
        String reason = request.getReason();
        adminBatchService.batchCancelOrders(orderIds, reason, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("count", orderIds.size());
        details.put("action", "cancel");
        details.put("reason", reason);
        adminLogService.logOperation(adminId, "批量取消订单", "ORDER", null, details, requestCtx);

        return Result.success("批量取消订单成功", null);
    }

    @PostMapping("/users/delete")
    public Result<Void> batchDeleteUsers(
            @RequestAttribute("userId") Long adminId,
            @Valid @RequestBody BatchDeleteUsersRequest request,
            HttpServletRequest requestCtx) {
        List<Long> userIds = request.getUserIds();
        try {
            adminBatchService.batchDeleteUsers(userIds);

            Map<String, Object> details = new HashMap<>();
            details.put("count", userIds.size());
            adminLogService.logOperation(adminId, "批量删除用户", "USER", null, details, requestCtx);

            return Result.success("批量删除成功", null);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
