package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.idleitems.school.dto.ItemDTO;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.User;
import com.idleitems.school.service.AdminLogService;
import com.idleitems.school.service.DictService;
import com.idleitems.school.service.ItemService;
import com.idleitems.school.service.UserService;
import com.idleitems.school.config.ApiPaths;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(ApiPaths.Admin.ITEMS)
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
@Tag(name = "管理员-物品管理", description = "管理员物品管理相关接口")
public class AdminItemController {

    private final AdminLogService adminLogService;
    private final DictService dictService;
    private final ItemService itemService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "获取物品列表", description = "分页查询所有物品，支持按状态筛选")
    public Result<Page<Item>> getItems(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) Item.ItemStatus status,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder) {
        if (size > 100) {
            size = 100;
        }
        Sort.Direction direction = Sort.Direction.DESC;
        if ("asc".equalsIgnoreCase(sortOrder)) {
            direction = Sort.Direction.ASC;
        }
        
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
        Page<Item> items = itemService.getAdminItems(pageable, status);

        java.util.Set<Long> userIds = items.getContent().stream()
                .map(Item::getUserId).collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, Integer> sellerCounts = itemService.getSellerItemCounts(new java.util.ArrayList<>(userIds));

        items.getContent().forEach(item -> {
            int count = sellerCounts.getOrDefault(item.getUserId(), 0);
            userService.enrichItemWithSellerInfo(item, count);
        });
        
        return Result.success(items);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取物品统计", description = "获取物品总数及各状态数量统计")
    public Result<Map<String, Long>> getItemStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", itemService.countItems());
        stats.put("onSale", itemService.countItemsByStatus(Item.ItemStatus.ON_SALE));
        stats.put("pending", itemService.countItemsByStatus(Item.ItemStatus.PENDING));
        stats.put("sold", itemService.countItemsByStatus(Item.ItemStatus.SOLD));
        
        return Result.success(stats);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "审核通过物品", description = "审核通过指定物品，将其状态设为在售")
    public Result<ItemDTO> approveItem(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        Item savedItem = itemService.approveItem(id);
        
        Map<String, Object> details = new HashMap<>();
        details.put("itemId", id);
        details.put("itemTitle", savedItem.getTitle());
        details.put("status", "ON_SALE");
        details.put("statusLabel", dictService.getDictLabel("ITEM_STATUS", "ON_SALE"));
        adminLogService.logOperation(adminId, "审核通过物品", "ITEM", id, details, request);
        
        return Result.success("物品审核通过", ItemDTO.fromEntity(savedItem));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "驳回物品", description = "驳回指定物品的审核申请")
    public Result<ItemDTO> rejectItem(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestParam String reason,
            HttpServletRequest request) {
        Item savedItem = itemService.rejectItem(id, reason);
        
        Map<String, Object> details = new HashMap<>();
        details.put("itemId", id);
        details.put("itemTitle", savedItem.getTitle());
        details.put("status", "REJECTED");
        details.put("statusLabel", dictService.getDictLabel("ITEM_STATUS", "REJECTED"));
        details.put("reason", reason);
        adminLogService.logOperation(adminId, "驳回物品", "ITEM", id, details, request);
        
        return Result.success("物品已驳回", ItemDTO.fromEntity(savedItem));
    }

    @PostMapping("/{id}/off-shelf")
    @Operation(summary = "强制下架物品", description = "强制下架指定物品")
    public Result<ItemDTO> forceOffShelfItem(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            HttpServletRequest request) {
        Item savedItem = itemService.forceOffShelfItem(id, reason);
        
        Map<String, Object> details = new HashMap<>();
        details.put("itemId", id);
        details.put("itemTitle", savedItem.getTitle());
        details.put("status", "OFF_SHELF");
        details.put("statusLabel", dictService.getDictLabel("ITEM_STATUS", "OFF_SHELF"));
        details.put("reason", reason);
        adminLogService.logOperation(adminId, "强制下架物品", "ITEM", id, details, request);
        
        return Result.success("物品已强制下架", ItemDTO.fromEntity(savedItem));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除物品", description = "根据ID删除指定物品")
    public Result<?> deleteItem(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        Item item = itemService.getItemById(id);
        
        if (itemService.existsOrderByItemId(id)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该物品存在关联订单，无法删除");
        }
        
        Map<String, Object> details = new HashMap<>();
        details.put("itemId", id);
        details.put("itemTitle", item.getTitle());
        adminLogService.logOperation(adminId, "删除物品", "ITEM", id, details, request);
        
        itemService.deleteItemById(id);
        
        return Result.success("物品已删除");
    }

    @GetMapping("/export")
    @Operation(summary = "导出物品", description = "导出物品列表为CSV文件")
    public void exportItems(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            HttpServletResponse response) throws IOException {
        Item.ItemStatus itemStatus = status != null ? Item.ItemStatus.valueOf(status) : null;

        List<Item> items = itemService.getItemsForExport(keyword, itemStatus, categoryId);
        if (items.size() > 5000) {
            items = items.subList(0, 5000);
        }

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition",
            "attachment;filename=items_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        StringBuilder csv = new StringBuilder();
        csv.append("ID,标题,价格,原价,状态,分类ID,卖家ID,浏览量,收藏量,发布时间\n");

        for (Item item : items) {
            csv.append(item.getId()).append(",")
               .append(escapeCsv(item.getTitle())).append(",")
               .append(item.getPrice() != null ? item.getPrice() : "").append(",")
               .append(item.getOriginalPrice() != null ? item.getOriginalPrice() : "").append(",")
               .append(escapeCsv(item.getStatus() != null ? item.getStatus().name() : "")).append(",")
               .append(item.getCategoryId() != null ? item.getCategoryId() : "").append(",")
               .append(item.getUserId()).append(",")
               .append(item.getViewCount()).append(",")
               .append(item.getFavoriteCount()).append(",")
               .append(item.getCreatedAt() != null ? item.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "").append("\n");
        }

        response.getOutputStream().write(csv.toString().getBytes("UTF-8"));
        response.getOutputStream().flush();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
