package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.dto.order.AdminOrderResponse;
import com.idleitems.school.dto.order.CancelOrderRequest;
import com.idleitems.school.entity.AdminLog;
import com.idleitems.school.entity.Category;
import com.idleitems.school.entity.CategoryChangeLog;
import com.idleitems.school.entity.CategoryFeedback;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.Order;
import com.idleitems.school.entity.User;
import com.idleitems.school.entity.VerificationRecord;
import com.idleitems.school.repository.CategoryRepository;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.repository.VerificationRecordRepository;
import com.idleitems.school.service.AdminLogService;
import com.idleitems.school.service.CategoryService;
import com.idleitems.school.service.OrderService;
import com.idleitems.school.util.CacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
public class AdminController {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final VerificationRecordRepository verificationRecordRepository;
    private final AdminLogService adminLogService;
    private final CacheManager cacheManager;
    private final OrderService orderService;
    private final CategoryService categoryService;

    @GetMapping("/users")
    public Result<Page<User>> getUsers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "role", required = false) User.Role role,
            @RequestParam(value = "status", required = false) User.UserStatus userStatus,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {
        try {
            log.info("getUsers called - page: {}, size: {}, sortBy: {}, sortOrder: {}", page, size, sortBy, sortOrder);
            
            Sort.Direction direction = Sort.Direction.DESC;
            if ("asc".equalsIgnoreCase(sortOrder)) {
                direction = Sort.Direction.ASC;
            }
            
            String sortField;
            switch (sortBy) {
                case "id":
                    sortField = "id";
                    break;
                case "username":
                    sortField = "username";
                    break;
                case "createdAt":
                default:
                    sortField = "createdAt";
            }
            
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortField));
            log.info("Pageable created: {}", pageable);
            
            Page<User> users;
            if (role != null && userStatus != null) {
                users = userRepository.findByRoleAndStatus(role, userStatus, pageable);
            } else if (role != null) {
                users = userRepository.findByRole(role, pageable);
            } else if (userStatus != null) {
                users = userRepository.findByStatus(userStatus, pageable);
            } else {
                users = userRepository.findAll(pageable);
            }
            
            log.info("Users found: {}", users.getTotalElements());
            return Result.success(users);
        } catch (Exception e) {
            log.error("Error in getUsers", e);
            throw e;
        }
    }

    @GetMapping("/users/stats")
    public Result<Map<String, Object>> getUserStats() {
        try {
            long total = userRepository.count();
            long active = userRepository.countByStatus(User.UserStatus.ACTIVE);
            long verified = userRepository.countByVerified(true);
            
            // 计算本周新增用户数
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            long newThisWeek = userRepository.countByCreatedAtAfter(oneWeekAgo);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", total);
            stats.put("active", active);
            stats.put("verified", verified);
            stats.put("newThisWeek", newThisWeek);
            
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/users/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/users/{id}/status")
    public Result<User> updateUserStatus(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestParam User.UserStatus status,
            HttpServletRequest request) {
        User user = userRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setStatus(status);
        User savedUser = userRepository.save(user);
        savedUser.setPassword(null);
        
        // 记录操作日志
        Map<String, Object> details = new HashMap<>();
        details.put("userId", id);
        details.put("oldStatus", user.getStatus());
        details.put("newStatus", status);
        adminLogService.logOperation(adminId, "更新用户状态", "USER", id, details, request);
        
        return Result.success("用户状态已更新", savedUser);
    }

    @GetMapping("/items")
    public Result<Page<Item>> getItems(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) Item.ItemStatus status,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {
        Sort.Direction direction = Sort.Direction.DESC;
        if ("asc".equalsIgnoreCase(sortOrder)) {
            direction = Sort.Direction.ASC;
        }
        
        // 支持的排序字段
        String sortField;
        switch (sortBy) {
            case "id":
                sortField = "id";
                break;
            case "title":
                sortField = "title";
                break;
            case "price":
                sortField = "price";
                break;
            case "createdAt":
            default:
                sortField = "createdAt";
        }
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortField));
        Page<Item> items;
        if (status != null) {
            items = itemRepository.findByStatus(status, pageable);
        } else {
            items = itemRepository.findAll(pageable);
        }
        
        // 为每个物品添加卖家信息并处理图片数据
        items.getContent().forEach(item -> {
            // 从用户表查询卖家信息
            userRepository.findById(item.getUserId()).ifPresent(user -> {
                item.setSellerNickname(user.getNickname() != null && !user.getNickname().isEmpty() ? user.getNickname() : user.getUsername());
                item.setSellerVerified(user.getVerified() != null ? user.getVerified() : false);
                // 计算卖家评分（这里简化处理，实际应该从评价表查询）
                item.setSellerRating(5.0);
                // 计算卖家发布物品数量
                item.setSellerItemsCount(itemRepository.countByUserId(item.getUserId()).intValue());
            });
                
            // 图片数据已经是List<String>类型，不需要处理
        });
        
        return Result.success(items);
    }

    @GetMapping("/items/stats")
    public Result<Map<String, Object>> getItemStats() {
        long total = itemRepository.count();
        long onSale = itemRepository.findByStatus(Item.ItemStatus.ON_SALE, PageRequest.of(0, 1)).getTotalElements();
        long pending = itemRepository.findByStatus(Item.ItemStatus.PENDING, PageRequest.of(0, 1)).getTotalElements();
        long sold = itemRepository.findByStatus(Item.ItemStatus.SOLD, PageRequest.of(0, 1)).getTotalElements();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("onSale", onSale);
        stats.put("sold", sold);
        
        return Result.success(stats);
    }

    @PutMapping("/items/{id}/approve")
    public Result<Item> approveItem(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        Item item = itemRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
        item.setStatus(Item.ItemStatus.ON_SALE);
        Item savedItem = itemRepository.save(item);
        
        // 记录操作日志
        Map<String, Object> details = new HashMap<>();
        details.put("itemId", id);
        details.put("itemTitle", item.getTitle());
        details.put("status", "ON_SALE");
        adminLogService.logOperation(adminId, "审核通过物品", "ITEM", id, details, request);
        
        return Result.success("物品审核通过", savedItem);
    }

    @PutMapping("/items/{id}/reject")
    public Result<Item> rejectItem(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestParam String reason,
            HttpServletRequest request) {
        Item item = itemRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
        item.setStatus(Item.ItemStatus.REJECTED);
        item.setRejectReason(reason);
        Item savedItem = itemRepository.save(item);
        
        // 记录操作日志
        Map<String, Object> details = new HashMap<>();
        details.put("itemId", id);
        details.put("itemTitle", item.getTitle());
        details.put("status", "REJECTED");
        details.put("reason", reason);
        adminLogService.logOperation(adminId, "驳回物品", "ITEM", id, details, request);
        
        return Result.success("物品已驳回", savedItem);
    }

    @PutMapping("/items/{id}/off-shelf")
    public Result<Item> forceOffShelfItem(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            HttpServletRequest request) {
        Item item = itemRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
        item.setStatus(Item.ItemStatus.OFF_SHELF);
        if (reason != null) {
            item.setRejectReason(reason);
        }
        Item savedItem = itemRepository.save(item);
        
        // 记录操作日志
        Map<String, Object> details = new HashMap<>();
        details.put("itemId", id);
        details.put("itemTitle", item.getTitle());
        details.put("status", "OFF_SHELF");
        details.put("reason", reason);
        adminLogService.logOperation(adminId, "强制下架物品", "ITEM", id, details, request);
        
        return Result.success("物品已强制下架", savedItem);
    }
    
    @DeleteMapping("/items/{id}")
    public Result<?> deleteItem(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        Item item = itemRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
        
        // 记录操作日志
        Map<String, Object> details = new HashMap<>();
        details.put("itemId", id);
        details.put("itemTitle", item.getTitle());
        adminLogService.logOperation(adminId, "删除物品", "ITEM", id, details, request);
        
        // 删除物品
        itemRepository.delete(item);
        
        // 清除相关缓存
        cacheManager.delete(CacheManager.getItemKey(id));
        cacheManager.deletePattern("item:list:*");
        cacheManager.deletePattern("item:hot");
        
        return Result.success("物品已删除");
    }

    @GetMapping("/orders/stats")
    public Result<Map<String, Object>> getOrderStats() {
        return Result.success(orderService.getAdminOrderStats());
    }

    @GetMapping("/orders")
    public Result<Page<AdminOrderResponse>> getOrders(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) Order.OrderStatus status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(orderService.getAdminOrderSummaries(keyword, status, paymentMethod, pageable));
    }

    @GetMapping("/orders/{id}")
    public Result<AdminOrderResponse> getOrder(@PathVariable Long id) {
        return Result.success(orderService.getAdminOrderSummary(id));
    }

    @PutMapping("/orders/{id}/cancel")
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
        details.put("reason", request.getReason());
        adminLogService.logOperation(adminId, "管理员取消订单", "ORDER", id, details, httpRequest);

        return Result.success("订单已取消", orderService.toAdminOrderSummary(order));
    }

    @PutMapping("/orders/{id}/refund/approve")
    public Result<AdminOrderResponse> approveRefund(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        Order savedOrder = orderService.approveRefund(id, adminId, "APPROVED");
        
        // 记录操作日志
        Map<String, Object> details = new HashMap<>();
        details.put("orderId", savedOrder.getId());
        details.put("orderNo", savedOrder.getOrderNo());
        details.put("status", "REFUNDED");
        adminLogService.logOperation(adminId, "审批退款", "ORDER", id, details, request);
        
        return Result.success("退款已审批", orderService.toAdminOrderSummary(savedOrder));
    }

    // 批量操作接口
    @PutMapping("/items/batch/approve")
    public Result<Void> batchApproveItems(
            @RequestAttribute("userId") Long adminId,
            @RequestBody List<Long> itemIds,
            HttpServletRequest request) {
        for (Long id : itemIds) {
            Item item = itemRepository.findById(id.longValue())
                    .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
            item.setStatus(Item.ItemStatus.ON_SALE);
            itemRepository.save(item);
            
            // 记录操作日志
            Map<String, Object> details = new HashMap<>();
            details.put("itemId", id);
            details.put("itemTitle", item.getTitle());
            details.put("status", "ON_SALE");
            adminLogService.logOperation(adminId, "批量审核通过物品", "ITEM", id, details, request);
        }
        return Result.success("批量审核通过成功", null);
    }

    @PutMapping("/items/batch/reject")
    public Result<Void> batchRejectItems(
            @RequestAttribute("userId") Long adminId,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        List<Long> itemIds = (List<Long>) requestBody.get("itemIds");
        String reason = (String) requestBody.get("reason");
        
        for (Long id : itemIds) {
            Item item = itemRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
            item.setStatus(Item.ItemStatus.REJECTED);
            item.setRejectReason(reason);
            itemRepository.save(item);
            
            // 记录操作日志
            Map<String, Object> details = new HashMap<>();
            details.put("itemId", id);
            details.put("itemTitle", item.getTitle());
            details.put("status", "REJECTED");
            details.put("reason", reason);
            adminLogService.logOperation(adminId, "批量驳回物品", "ITEM", id, details, request);
        }
        return Result.success("批量驳回成功", null);
    }

    @PutMapping("/items/batch/off-shelf")
    public Result<Void> batchOffShelfItems(
            @RequestAttribute("userId") Long adminId,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        List<Long> itemIds = (List<Long>) requestBody.get("itemIds");
        String reason = (String) requestBody.get("reason");
        
        for (Long id : itemIds) {
            Item item = itemRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
            item.setStatus(Item.ItemStatus.OFF_SHELF);
            if (reason != null) {
                item.setRejectReason(reason);
            }
            itemRepository.save(item);
            
            // 记录操作日志
            Map<String, Object> details = new HashMap<>();
            details.put("itemId", id);
            details.put("itemTitle", item.getTitle());
            details.put("status", "OFF_SHELF");
            details.put("reason", reason);
            adminLogService.logOperation(adminId, "批量下架物品", "ITEM", id, details, request);
        }
        return Result.success("批量下架成功", null);
    }

    @PutMapping("/users/batch/status")
    public Result<Void> batchUpdateUserStatus(
            @RequestAttribute("userId") Long adminId,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        List<Long> userIds = (List<Long>) requestBody.get("userIds");
        User.UserStatus status = User.UserStatus.valueOf((String) requestBody.get("status"));
        
        for (Long id : userIds) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
            User.UserStatus oldStatus = user.getStatus();
            user.setStatus(status);
            userRepository.save(user);
            
            // 记录操作日志
            Map<String, Object> details = new HashMap<>();
            details.put("userId", id);
            details.put("username", user.getUsername());
            details.put("oldStatus", oldStatus);
            details.put("newStatus", status);
            adminLogService.logOperation(adminId, "批量更新用户状态", "USER", id, details, request);
        }
        return Result.success("批量更新用户状态成功", null);
    }

    @PutMapping("/orders/batch/cancel")
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
            
            // 记录操作日志
            Map<String, Object> details = new HashMap<>();
            details.put("orderId", id);
            details.put("orderNo", order.getOrderNo());
            details.put("status", "CANCELLED");
            details.put("reason", reason);
            adminLogService.logOperation(adminId, "批量取消订单", "ORDER", id, details, request);
        }
        return Result.success("批量取消订单成功", null);
    }

    @GetMapping("/verifications")
    public Result<Page<Map<String, Object>>> getVerifications(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) VerificationRecord.Status status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<VerificationRecord> records;
        if (status != null) {
            records = verificationRecordRepository.findByStatus(status, pageable);
        } else {
            records = verificationRecordRepository.findAll(pageable);
        }

        Page<Map<String, Object>> result = records.map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("userId", record.getUserId());
            map.put("realName", record.getRealName());
            map.put("idNumber", record.getIdCard());
            map.put("studentId", record.getStudentId());
            map.put("school", record.getSchool());
            map.put("studentCard", record.getStudentCard());
            map.put("idCardFront", record.getIdCardFront());
            map.put("idCardBack", record.getIdCardBack());
            map.put("teacherId", record.getTeacherId());
            map.put("teacherCard", record.getTeacherCard());
            map.put("type", record.getType());
            map.put("verificationType", record.getType() == null ? null : record.getType().ordinal() + 1);
            map.put("status", record.getStatus());
            map.put("rejectReason", record.getRejectReason());
            map.put("createdAt", record.getCreatedAt());
            map.put("updatedAt", record.getUpdatedAt());

            userRepository.findById(record.getUserId()).ifPresent(user -> {
                map.put("username", user.getUsername());
            });

            return map;
        });

        return Result.success(result);
    }

    @GetMapping("/verifications/stats")
    public Result<Map<String, Object>> getVerificationStats() {
        try {
            long total = verificationRecordRepository.count();
            long pending = verificationRecordRepository.countByStatus(VerificationRecord.Status.PENDING);
            long approved = verificationRecordRepository.countByStatus(VerificationRecord.Status.APPROVED);
            long rejected = verificationRecordRepository.countByStatus(VerificationRecord.Status.REJECTED);
            
            // 计算本周新增认证数
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            long newThisWeek = verificationRecordRepository.countByCreatedAtAfter(oneWeekAgo);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", total);
            stats.put("pending", pending);
            stats.put("approved", approved);
            stats.put("rejected", rejected);
            stats.put("newThisWeek", newThisWeek);
            
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/verifications/{id}/approve")
    public Result<VerificationRecord> approveVerification(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        VerificationRecord record = verificationRecordRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("认证记录不存在"));
        record.setStatus(VerificationRecord.Status.APPROVED);
        record.setReviewerId(adminId);
        record.setReviewedAt(LocalDateTime.now());
        VerificationRecord saved = verificationRecordRepository.save(record);

        Map<String, Object> details = new HashMap<>();
        details.put("recordId", id);
        details.put("userId", record.getUserId());
        details.put("realName", record.getRealName());
        adminLogService.logOperation(adminId, "通过实名认证", "VERIFICATION", id, details, request);

        return Result.success("认证已通过", saved);
    }

    @PutMapping("/verifications/{id}/reject")
    public Result<VerificationRecord> rejectVerification(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestParam String reason,
            HttpServletRequest request) {
        VerificationRecord record = verificationRecordRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("认证记录不存在"));
        record.setStatus(VerificationRecord.Status.REJECTED);
        record.setRejectReason(reason);
        record.setReviewerId(adminId);
        record.setReviewedAt(LocalDateTime.now());
        VerificationRecord saved = verificationRecordRepository.save(record);

        Map<String, Object> details = new HashMap<>();
        details.put("recordId", id);
        details.put("userId", record.getUserId());
        details.put("realName", record.getRealName());
        details.put("reason", reason);
        adminLogService.logOperation(adminId, "拒绝实名认证", "VERIFICATION", id, details, request);

        return Result.success("认证已拒绝", saved);
    }

    @GetMapping("/categories")
    public Result<Page<Map<String, Object>>> getCategories(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) Boolean status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "sort"));
        Page<Category> categories;
        if (status != null) {
            categories = categoryRepository.findByStatus(status, pageable);
        } else {
            categories = categoryRepository.findAll(pageable);
        }

        Page<Map<String, Object>> result = categories.map(category -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", category.getId());
            map.put("name", category.getName());
            map.put("description", category.getDescription());
            map.put("parentId", category.getParentId());
            map.put("level", category.getLevel());
            map.put("sort", category.getSort());
            map.put("status", category.getStatus());
            map.put("createdAt", category.getCreatedAt());
            map.put("updatedAt", category.getUpdatedAt());
            List<Long> categoryIds = new ArrayList<>();
            categoryIds.add(category.getId());
            collectChildCategories(category.getId(), categoryIds);
            map.put("itemCount", itemRepository.countByCategoryIds(categoryIds));
            return map;
        });
        return Result.success(result);
    }

    @GetMapping("/categories/stats")
    public Result<Map<String, Object>> getCategoryStats() {
        return Result.success(categoryService.getCategoryStats());
    }

    private void collectChildCategories(Long categoryId, List<Long> categoryIds) {
        List<Category> children = categoryRepository.findByParentId(categoryId);
        for (Category child : children) {
            categoryIds.add(child.getId());
            collectChildCategories(child.getId(), categoryIds);
        }
    }

    @PostMapping("/categories")
    public Result<Category> createCategory(
            @RequestAttribute("userId") Long adminId,
            @RequestBody Category category,
            HttpServletRequest request) {
        Category saved = categoryService.createCategory(category, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", saved.getId());
        details.put("categoryName", saved.getName());
        adminLogService.logOperation(adminId, "创建分类", "CATEGORY", saved.getId(), details, request);

        return Result.success("分类创建成功", saved);
    }

    @PutMapping("/categories/{id}")
    public Result<Category> updateCategory(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestBody Category category,
            HttpServletRequest request) {
        Category saved = categoryService.updateCategory(id, category, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", id);
        details.put("categoryName", saved.getName());
        adminLogService.logOperation(adminId, "更新分类", "CATEGORY", id, details, request);

        return Result.success("分类更新成功", saved);
    }

    @PutMapping("/categories/{id}/move-up")
    public Result<Void> moveCategoryUp(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        Category category = categoryRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("分类不存在"));

        Integer currentSort = category.getSort();
        if (currentSort == null) {
            currentSort = 0;
        }

        Page<Category> nextPage = categoryRepository.findBySortGreaterThan(currentSort, PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "sort")));
        if (nextPage.hasContent()) {
            Category nextCategory = nextPage.getContent().get(0);
            Integer nextSort = nextCategory.getSort();
            nextCategory.setSort(currentSort);
            category.setSort(nextSort != null ? nextSort : currentSort + 1);
            categoryRepository.save(nextCategory);
            categoryRepository.save(category);
        } else {
            category.setSort(currentSort + 1);
            categoryRepository.save(category);
        }

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", id);
        details.put("action", "move-up");
        adminLogService.logOperation(adminId, "分类排序上移", "CATEGORY", id, details, request);

        return Result.success("排序已更新", null);
    }

    @PutMapping("/categories/{id}/move-down")
    public Result<Void> moveCategoryDown(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        Category category = categoryRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("分类不存在"));

        Integer currentSort = category.getSort();
        if (currentSort == null) {
            currentSort = 0;
        }

        Page<Category> prevPage = categoryRepository.findBySortLessThan(currentSort, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "sort")));
        if (prevPage.hasContent()) {
            Category prevCategory = prevPage.getContent().get(0);
            Integer prevSort = prevCategory.getSort();
            prevCategory.setSort(currentSort);
            category.setSort(prevSort != null ? prevSort : currentSort - 1);
            categoryRepository.save(prevCategory);
            categoryRepository.save(category);
        } else {
            if (currentSort > 0) {
                category.setSort(currentSort - 1);
                categoryRepository.save(category);
            }
        }

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", id);
        details.put("action", "move-down");
        adminLogService.logOperation(adminId, "分类排序下移", "CATEGORY", id, details, request);

        return Result.success("排序已更新", null);
    }

    @PutMapping("/categories/{id}/status")
    public Result<Category> updateCategoryStatus(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        Boolean status = (Boolean) requestBody.get("status");
        if (status == null) {
            throw new IllegalArgumentException("状态参数不能为空");
        }

        Category saved = categoryService.toggleCategoryStatus(id, status, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", id);
        details.put("newStatus", status);
        adminLogService.logOperation(adminId, "更新分类状态", "CATEGORY", id, details, request);

        return Result.success("状态更新成功", saved);
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        categoryService.deleteCategory(id, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("categoryId", id);
        adminLogService.logOperation(adminId, "删除分类", "CATEGORY", id, details, request);

        return Result.success("分类删除成功", null);
    }

    @GetMapping("/categories/feedback")
    public Result<Page<CategoryFeedback>> getCategoryFeedbacks(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(categoryService.getAllFeedbacks(status, pageable));
    }

    @PutMapping("/categories/feedback/{id}/review")
    public Result<CategoryFeedback> reviewFeedback(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        String action = (String) requestBody.get("action");
        String reply = (String) requestBody.get("reply");
        CategoryFeedback feedback = categoryService.reviewFeedback(id, action, reply, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("feedbackId", id);
        details.put("action", action);
        adminLogService.logOperation(adminId, "审核分类反馈", "CATEGORY_FEEDBACK", id, details, request);

        return Result.success("反馈审核成功", feedback);
    }

    @GetMapping("/categories/change-logs")
    public Result<Page<CategoryChangeLog>> getCategoryChangeLogs(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(categoryService.getCategoryChangeLogs(categoryId, pageable));
    }

    @GetMapping("/categories/export")
    public ResponseEntity<byte[]> exportCategories() {
        String csv = categoryService.exportCategories();
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=categories.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(bytes);
    }

    @PostMapping("/categories/import")
    public Result<Map<String, Object>> importCategories(
            @RequestAttribute("userId") Long adminId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        Map<String, Object> result = categoryService.importCategories(file, adminId);

        Map<String, Object> details = new HashMap<>();
        details.put("successCount", result.get("successCount"));
        details.put("failCount", result.get("failCount"));
        adminLogService.logOperation(adminId, "批量导入分类", "CATEGORY", null, details, request);

        return Result.success("导入完成", result);
    }

    @RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        userRepository.deleteById(id.longValue());

        Map<String, Object> details = new HashMap<>();
        details.put("userId", id);
        adminLogService.logOperation(adminId, "删除用户", "USER", id, details, request);

        return Result.success("用户已删除", null);
    }

    @GetMapping("/logs")
    public Result<Page<AdminLog>> getAdminLogs(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "adminId", required = false) Long adminId) {
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdminLog> logs;
        
        if (adminId != null) {
            logs = adminLogService.getAdminLogsByAdminId(adminId, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            logs = adminLogService.searchAdminLogs(keyword, pageable);
        } else {
            logs = adminLogService.getAdminLogs(pageable);
        }
        
        return Result.success(logs);
    }

    @GetMapping("/logs/{id}")
    public Result<AdminLog> getAdminLog(@PathVariable Long id) {
        AdminLog log = adminLogService.getAdminLogById(id);
        return Result.success(log);
    }
    
}
