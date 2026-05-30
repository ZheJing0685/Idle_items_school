package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.dto.CreateItemRequest;
import com.idleitems.school.dto.ItemDTO;
import com.idleitems.school.dto.ItemSummaryDTO;
import com.idleitems.school.dto.UpdateItemRequest;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.Order;
import com.idleitems.school.service.FileService;
import com.idleitems.school.service.ItemQueryService;
import com.idleitems.school.service.ItemCommandService;
import com.idleitems.school.service.ItemAdminService;
import com.idleitems.school.service.OrderBuyerService;
import com.idleitems.school.service.OrderSellerService;
import com.idleitems.school.service.OrderQueryService;
import com.idleitems.school.service.OrderRefundService;
import com.idleitems.school.service.OrderAdminService;
import com.idleitems.school.service.OrderTimeoutService;
import com.idleitems.school.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;

@Tag(name = "物品管理", description = "闲置物品发布、浏览、搜索等接口")
@Slf4j
@RestController
@Validated
@RequestMapping(ApiPaths.Item.BASE)
@RequiredArgsConstructor
public class ItemController {

    private final ItemQueryService itemQueryService;
    private final ItemCommandService itemCommandService;
    private final UserService userService;
    private final FileService fileService;
    private final OrderQueryService orderQueryService;
  
    @Operation(summary = "发布物品", description = "发布闲置物品，上传物品信息并等待审核")
    @PostMapping(ApiPaths.Item.CREATE_PATH)
    public Result<ItemDTO> createItem(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateItemRequest request) {
        Item savedItem = itemCommandService.createItem(userId, request);

        int sellerItemCount = itemQueryService.getSellerItemCount(savedItem.getUserId());
        userService.enrichItemWithSellerInfo(savedItem, sellerItemCount);

        return Result.success("发布成功，等待审核", ItemDTO.fromEntity(savedItem));
    }

    @Operation(summary = "上传图片", description = "上传物品图片，返回图片访问URL")
    @PostMapping(ApiPaths.Item.UPLOAD_PATH)
    public Result<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        Map<String, Object> result = fileService.uploadImage(file);
        return Result.success("上传成功", result);
    }

    @Operation(summary = "获取物品列表", description = "分页查询闲置物品列表，支持按分类、成色、交易方式等条件筛选")
    @GetMapping(ApiPaths.Item.LIST_PATH)
    public Result<Page<ItemSummaryDTO>> getItems(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "categoryId", required = false) String categoryIdStr,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "condition", required = false) String condition,
            @RequestParam(value = "deliveryMethod", required = false) String deliveryMethod) {
        Page<ItemSummaryDTO> items = itemQueryService.getItems(page, size, categoryIdStr, sortBy, condition, deliveryMethod);
        return Result.success(items);
    }

    @Operation(summary = "搜索物品", description = "根据关键字搜索闲置物品，支持分页和排序")
    @GetMapping(ApiPaths.Item.SEARCH_PATH)
    public Result<Page<ItemSummaryDTO>> searchItems(
            @RequestParam(value = "keyword") @NotBlank String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy) {
        Page<ItemSummaryDTO> items = itemQueryService.searchItems(keyword, page, size, sortBy);
        return Result.success(items);
    }

    @Operation(summary = "获取热门物品", description = "获取热门闲置物品列表（浏览量+时间衰减综合排序）")
    @GetMapping(ApiPaths.Item.HOT_PATH)
    public Result<List<ItemSummaryDTO>> getHotItems() {
        List<ItemSummaryDTO> items = itemQueryService.getHotItems();
        return Result.success(items);
    }

    @Operation(summary = "获取用户物品", description = "获取当前登录用户的闲置物品列表，可按状态筛选")
    @GetMapping(ApiPaths.Item.USER_ITEMS_PATH)
    public Result<Page<Item>> getUserItems(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "status", required = false) Item.ItemStatus status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Page<Item> items = itemQueryService.getUserItems(userId, status, page, size);
        return Result.success(items);
    }

    @Operation(summary = "获取物品详情", description = "根据物品ID获取闲置物品的详细信息")
    @GetMapping(ApiPaths.Item.DETAIL_PATH)
    public Result<Item> getItem(@PathVariable Long id) {
        Item item = itemQueryService.getItemById(id);
        return Result.success(item);
    }

    @Operation(summary = "更新物品", description = "更新闲置物品的信息，更新后需重新审核")
    @PutMapping(ApiPaths.Item.UPDATE_PATH)
    public Result<ItemDTO> updateItem(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request) {
        Item updatedItem = itemCommandService.updateItem(userId, id, request);

        int sellerItemCount = itemQueryService.getSellerItemCount(updatedItem.getUserId());
        userService.enrichItemWithSellerInfo(updatedItem, sellerItemCount);

        return Result.success("更新成功，等待审核", ItemDTO.fromEntity(updatedItem));
    }

    @Operation(summary = "下架物品", description = "将闲置物品下架，使其不再对外展示")
    @PostMapping(ApiPaths.Item.OFF_SHELF_PATH)
    public Result<Item> offShelfItem(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        Item updatedItem = itemCommandService.offShelfItem(userId, id);
        return Result.success("下架成功", updatedItem);
    }

    @Operation(summary = "上架物品", description = "将已下架的物品重新上架展示")
    @PostMapping("/{id}/on-shelf")
    public Result<Item> onShelfItem(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        Item updatedItem = itemCommandService.onShelfItem(userId, id);
        return Result.success("上架成功", updatedItem);
    }

    @Operation(summary = "获取物品订单", description = "获取指定闲置物品的所有订单记录")
    @GetMapping("/{id}/orders")
    public Result<List<Order>> getItemOrders(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        List<Order> orders = orderQueryService.getOrdersByItemId(id, userId);
        return Result.success(orders);
    }

    @Operation(summary = "删除物品", description = "删除自己的闲置物品（仅限未出售状态）")
    @DeleteMapping(ApiPaths.Item.UPDATE_PATH)
    public Result<Void> deleteItem(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        itemCommandService.deleteItemByUser(userId, id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取物品活跃订单", description = "获取指定闲置物品的活跃订单列表")
    @GetMapping("/{id}/active-orders")
    public Result<List<Order>> getItemActiveOrders(@PathVariable Long id) {
        List<Order> orders = orderQueryService.getActiveOrdersByItemId(id);
        return Result.success(orders);
    }
}
