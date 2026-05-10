package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.dto.ItemDTO;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.User;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.ReviewRepository;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.service.AdminLogService;
import com.idleitems.school.service.DictService;
import com.idleitems.school.service.ItemService;
import com.idleitems.school.util.CacheManager;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/items")
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
public class AdminItemController {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final AdminLogService adminLogService;
    private final DictService dictService;
    private final CacheManager cacheManager;
    private final ItemService itemService;

    @GetMapping
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
        
        items.getContent().forEach(item -> {
            userRepository.findById(item.getUserId()).ifPresent(user -> {
                item.setSellerNickname(user.getNickname() != null && !user.getNickname().isEmpty() ? user.getNickname() : user.getUsername());
                item.setSellerVerified(user.getVerified() != null ? user.getVerified() : false);
                BigDecimal averageRating = reviewRepository.getAverageRatingByUserId(item.getUserId());
                item.setSellerRating(averageRating != null ? averageRating.doubleValue() : 0.0);
                item.setSellerItemsCount(itemRepository.countByUserId(item.getUserId()).intValue());
            });
        });
        
        return Result.success(items);
    }

    @GetMapping("/stats")
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

    @PutMapping("/{id}/approve")
    public Result<ItemDTO> approveItem(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        Item item = itemRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
        item.setStatus(Item.ItemStatus.ON_SALE);
        Item savedItem = itemRepository.save(item);
        
        Map<String, Object> details = new HashMap<>();
        details.put("itemId", id);
        details.put("itemTitle", item.getTitle());
        details.put("status", "ON_SALE");
        details.put("statusLabel", dictService.getDictLabel("ITEM_STATUS", "ON_SALE"));
        adminLogService.logOperation(adminId, "审核通过物品", "ITEM", id, details, request);
        
        return Result.success("物品审核通过", ItemDTO.fromEntity(savedItem));
    }

    @PutMapping("/{id}/reject")
    public Result<ItemDTO> rejectItem(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestParam String reason,
            HttpServletRequest request) {
        Item item = itemRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
        item.setStatus(Item.ItemStatus.REJECTED);
        item.setRejectReason(reason);
        Item savedItem = itemRepository.save(item);
        
        Map<String, Object> details = new HashMap<>();
        details.put("itemId", id);
        details.put("itemTitle", item.getTitle());
        details.put("status", "REJECTED");
        details.put("statusLabel", dictService.getDictLabel("ITEM_STATUS", "REJECTED"));
        details.put("reason", reason);
        adminLogService.logOperation(adminId, "驳回物品", "ITEM", id, details, request);
        
        return Result.success("物品已驳回", ItemDTO.fromEntity(savedItem));
    }

    @PutMapping("/{id}/off-shelf")
    public Result<ItemDTO> forceOffShelfItem(
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
        
        Map<String, Object> details = new HashMap<>();
        details.put("itemId", id);
        details.put("itemTitle", item.getTitle());
        details.put("status", "OFF_SHELF");
        details.put("statusLabel", dictService.getDictLabel("ITEM_STATUS", "OFF_SHELF"));
        details.put("reason", reason);
        adminLogService.logOperation(adminId, "强制下架物品", "ITEM", id, details, request);
        
        return Result.success("物品已强制下架", ItemDTO.fromEntity(savedItem));
    }
    
    @DeleteMapping("/{id}")
    public Result<?> deleteItem(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        Item item = itemRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
        
        if (orderRepository.existsByItemId(id)) {
            throw new IllegalArgumentException("该物品存在关联订单，无法删除");
        }
        
        Map<String, Object> details = new HashMap<>();
        details.put("itemId", id);
        details.put("itemTitle", item.getTitle());
        adminLogService.logOperation(adminId, "删除物品", "ITEM", id, details, request);
        
        itemRepository.delete(item);
        
        cacheManager.delete(CacheManager.getItemKey(id));
        cacheManager.deletePattern("item:list:*");
        cacheManager.deletePattern("item:hot");
        
        return Result.success("物品已删除");
    }

    @GetMapping("/export")
    public void exportItems(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            HttpServletResponse response) throws IOException {
        Item.ItemStatus itemStatus = status != null ? Item.ItemStatus.valueOf(status) : null;
        
        List<Item> items = itemService.getItemsForExport(keyword, itemStatus, categoryId);
        
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", 
            "attachment;filename=items_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,标题,价格,原价,状态,分类ID,卖家ID,浏览量,收藏量,发布时间\n");
        
        for (Item item : items) {
            csv.append(item.getId()).append(",")
               .append(item.getTitle()).append(",")
               .append(item.getPrice() != null ? item.getPrice() : "").append(",")
               .append(item.getOriginalPrice() != null ? item.getOriginalPrice() : "").append(",")
               .append(item.getStatus()).append(",")
               .append(item.getCategoryId() != null ? item.getCategoryId() : "").append(",")
               .append(item.getUserId()).append(",")
               .append(item.getViewCount()).append(",")
               .append(item.getFavoriteCount()).append(",")
               .append(item.getCreatedAt() != null ? item.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "").append("\n");
        }
        
        response.getOutputStream().write(csv.toString().getBytes("UTF-8"));
        response.getOutputStream().flush();
    }
}
