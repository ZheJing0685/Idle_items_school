package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.dto.ItemSummaryDTO;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.Order;
import com.idleitems.school.service.FileService;
import com.idleitems.school.service.ItemService;
import com.idleitems.school.service.OrderService;
import com.idleitems.school.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(ApiPaths.Item.BASE)
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final UserService userService;
    private final FileService fileService;
    private final OrderService orderService;
  
    @PostMapping(ApiPaths.Item.CREATE_PATH)
    public Result<Item> createItem(@RequestAttribute("userId") Long userId, @RequestBody Map<String, Object> request) throws Exception {
        Item savedItem = itemService.createItem(userId, request);

        int sellerItemCount = itemService.getSellerItemCount(savedItem.getUserId());
        userService.enrichItemWithSellerInfo(savedItem, sellerItemCount);

        return Result.success("发布成功，等待审核", savedItem);
    }

    @PostMapping(ApiPaths.Item.UPLOAD_PATH)
    public Result<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        Map<String, Object> result = fileService.uploadImage(file);
        return Result.success("上传成功", result);
    }

    @GetMapping(ApiPaths.Item.LIST_PATH)
    public Result<Page<ItemSummaryDTO>> getItems(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "categoryId", required = false) String categoryIdStr,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "condition", required = false) String condition,
            @RequestParam(value = "deliveryMethod", required = false) Integer deliveryMethod) {
        Page<ItemSummaryDTO> items = itemService.getItems(page, size, categoryIdStr, sortBy, condition, deliveryMethod);
        return Result.success(items);
    }

    @GetMapping(ApiPaths.Item.SEARCH_PATH)
    public Result<Page<Item>> searchItems(
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy) {
        Page<Item> items = itemService.searchItems(keyword, page, size, sortBy);
        return Result.success(items);
    }

    @GetMapping(ApiPaths.Item.HOT_PATH)
    public Result<List<Item>> getHotItems() {
        List<Item> items = itemService.getHotItems();
        return Result.success(items);
    }

    @GetMapping(ApiPaths.Item.USER_ITEMS_PATH)
    public Result<Page<Item>> getUserItems(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "status", required = false) Item.ItemStatus status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Page<Item> items = itemService.getUserItems(userId, status, page, size);
        return Result.success(items);
    }

    @GetMapping(ApiPaths.Item.DETAIL_PATH)
    public Result<Item> getItem(@PathVariable Long id) {
        Item item = itemService.getItemById(id);
        return Result.success(item);
    }

    @PutMapping(ApiPaths.Item.UPDATE_PATH)
    public Result<Item> updateItem(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) throws Exception {
        Item updatedItem = itemService.updateItem(userId, id, request);

        int sellerItemCount = itemService.getSellerItemCount(updatedItem.getUserId());
        userService.enrichItemWithSellerInfo(updatedItem, sellerItemCount);

        return Result.success("更新成功，等待审核", updatedItem);
    }

    @PutMapping(ApiPaths.Item.OFF_SHELF_PATH)
    public Result<Item> offShelfItem(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        Item updatedItem = itemService.offShelfItem(userId, id);
        return Result.success("下架成功", updatedItem);
    }

    @PutMapping("/{id}/on-shelf")
    public Result<Item> onShelfItem(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        Item updatedItem = itemService.onShelfItem(userId, id);
        return Result.success("上架成功", updatedItem);
    }

    @GetMapping("/{id}/orders")
    public Result<List<Order>> getItemOrders(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        List<Order> orders = orderService.getOrdersByItemId(id, userId);
        return Result.success(orders);
    }

    @GetMapping("/{id}/active-orders")
    public Result<List<Order>> getItemActiveOrders(@PathVariable Long id) {
        List<Order> orders = orderService.getActiveOrdersByItemId(id);
        return Result.success(orders);
    }
}
