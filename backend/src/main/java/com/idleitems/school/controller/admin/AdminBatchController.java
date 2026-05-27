package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.entity.Item;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.idleitems.school.entity.Order;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.service.AdminLogService;
import com.idleitems.school.service.DictService;
import com.idleitems.school.service.OrderService;
import com.idleitems.school.service.UserService;
import com.idleitems.school.config.ApiPaths;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(ApiPaths.Admin.BATCH)
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
@Tag(name = "管理员-批量操作", description = "管理员批量操作相关接口")
public class AdminBatchController {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final AdminLogService adminLogService;
    private final DictService dictService;
    private final UserService userService;

    @PostMapping("/items/approve")
    @Operation(summary = "批量审核通过物品", description = "批量审核通过指定ID列表的物品")
    @Transactional
    public Result<Void> batchApproveItems(
            @RequestAttribute("userId") Long adminId,
            @RequestBody List<Long> itemIds,
            HttpServletRequest request) {
        for (Long id : itemIds) {
            Item item = itemRepository.findById(id.longValue())
                    .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
            item.setStatus(Item.ItemStatus.ON_SALE);
            itemRepository.save(item);
            
            Map<String, Object> details = new HashMap<>();
            details.put("itemId", id);
            details.put("itemTitle", item.getTitle());
            details.put("status", "ON_SALE");
            details.put("statusLabel", dictService.getDictLabel("ITEM_STATUS", "ON_SALE"));
            adminLogService.logOperation(adminId, "批量审核通过物品", "ITEM", id, details, request);
        }
        return Result.success("批量审核通过成功", null);
    }

    @PostMapping("/items/reject")
    @Operation(summary = "批量驳回物品", description = "批量驳回指定ID列表的物品")
    @Transactional
    public Result<Void> batchRejectItems(
            @RequestAttribute("userId") Long adminId,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        List<Number> itemNumberIds = (List<Number>) requestBody.get("itemIds");
        String reason = (String) requestBody.get("reason");
        
        List<Long> itemIds = itemNumberIds.stream()
                .map(Number::longValue)
                .toList();
        
        for (Long id : itemIds) {
            Item item = itemRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
            item.setStatus(Item.ItemStatus.REJECTED);
            item.setRejectReason(reason);
            itemRepository.save(item);
            
            Map<String, Object> details = new HashMap<>();
            details.put("itemId", id);
            details.put("itemTitle", item.getTitle());
            details.put("status", "REJECTED");
            details.put("statusLabel", dictService.getDictLabel("ITEM_STATUS", "REJECTED"));
            details.put("reason", reason);
            adminLogService.logOperation(adminId, "批量驳回物品", "ITEM", id, details, request);
        }
        return Result.success("批量驳回成功", null);
    }

    @PostMapping("/items/off-shelf")
    @Operation(summary = "批量下架物品", description = "批量强制下架指定ID列表的物品")
    @Transactional
    public Result<Void> batchOffShelfItems(
            @RequestAttribute("userId") Long adminId,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        List<Number> itemNumberIds = (List<Number>) requestBody.get("itemIds");
        String reason = (String) requestBody.get("reason");
        
        List<Long> itemIds = itemNumberIds.stream()
                .map(Number::longValue)
                .toList();
        
        for (Long id : itemIds) {
            Item item = itemRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
            item.setStatus(Item.ItemStatus.OFF_SHELF);
            if (reason != null) {
                item.setRejectReason(reason);
            }
            itemRepository.save(item);
            
            Map<String, Object> details = new HashMap<>();
            details.put("itemId", id);
            details.put("itemTitle", item.getTitle());
            details.put("status", "OFF_SHELF");
            details.put("statusLabel", dictService.getDictLabel("ITEM_STATUS", "OFF_SHELF"));
            details.put("reason", reason);
            adminLogService.logOperation(adminId, "批量下架物品", "ITEM", id, details, request);
        }
        return Result.success("批量下架成功", null);
    }

    @PostMapping("/users/status")
    @Operation(summary = "批量更新用户状态", description = "批量更新指定ID列表的用户状态")
    @Transactional
    public Result<Void> batchUpdateUserStatus(
            @RequestAttribute("userId") Long adminId,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        List<Number> userNumberIds = (List<Number>) requestBody.get("userIds");
        User.UserStatus status = User.UserStatus.valueOf((String) requestBody.get("status"));
        
        List<Long> userIds = userNumberIds.stream()
                .map(Number::longValue)
                .toList();
        
        for (Long id : userIds) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
            User.UserStatus oldStatus = user.getStatus();
            user.setStatus(status);
            userRepository.save(user);
            
            Map<String, Object> details = new HashMap<>();
            details.put("userId", id);
            details.put("username", user.getUsername());
            details.put("oldStatus", oldStatus);
            details.put("newStatus", status);
            adminLogService.logOperation(adminId, "批量更新用户状态", "USER", id, details, request);
        }
        return Result.success("批量更新用户状态成功", null);
    }

    @PostMapping("/orders/cancel")
    @Operation(summary = "批量取消订单", description = "批量取消指定ID列表的订单")
    @Transactional
    public Result<Void> batchCancelOrders(
            @RequestAttribute("userId") Long adminId,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        List<Number> orderIdNumbers = (List<Number>) requestBody.get("orderIds");
        String reason = (String) requestBody.get("reason");

        if (orderIdNumbers == null || orderIdNumbers.isEmpty()) {
            throw new IllegalArgumentException("订单ID不能为空");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("取消原因不能为空");
        }

        List<Long> orderIds = orderIdNumbers.stream()
                .map(Number::longValue)
                .toList();

        for (Long id : orderIds) {
            Order order = orderService.adminCancelOrder(id, adminId, reason);
            
            Map<String, Object> details = new HashMap<>();
            details.put("orderId", id);
            details.put("orderNo", order.getOrderNo());
            details.put("status", "CANCELLED");
            details.put("statusLabel", dictService.getDictLabel("ORDER_STATUS", "CANCELLED"));
            details.put("reason", reason);
            adminLogService.logOperation(adminId, "批量取消订单", "ORDER", id, details, request);
        }
        return Result.success("批量取消订单成功", null);
    }

    @PostMapping("/users/delete")
    @Operation(summary = "批量删除用户", description = "批量删除指定ID列表的用户")
    @Transactional
    public Result<Void> batchDeleteUsers(
            @RequestAttribute("userId") Long adminId,
            @RequestBody List<Long> userIds,
            HttpServletRequest request) {
        try {
            userService.deleteUsers(userIds);
            
            Map<String, Object> details = new HashMap<>();
            details.put("userIds", userIds);
            details.put("count", userIds.size());
            adminLogService.logOperation(adminId, "批量删除用户", "USER", null, details, request);
            
            return Result.success("批量删除成功", null);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
