package com.idleitems.school.module.user.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.module.item.dto.ItemSummaryDTO;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.service.ItemQueryService;
import com.idleitems.school.module.order.dto.ReviewResponse;
import com.idleitems.school.module.order.service.ReviewService;
import com.idleitems.school.module.user.dto.SellerProfileDTO;
import com.idleitems.school.module.user.dto.UpdateProfileRequest;
import com.idleitems.school.module.user.dto.UserDTO;
import com.idleitems.school.module.user.dto.UserStatsDTO;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "用户个人信息管理 + 卖家公开信息")
@RestController
@RequestMapping(ApiPaths.User.BASE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ItemQueryService itemQueryService;
    private final ReviewService reviewService;

    @Operation(summary = "获取用户信息", description = "获取当前登录用户的个人资料信息")
    @GetMapping(ApiPaths.User.PROFILE_PATH)
    public Result<UserDTO> getProfile(@RequestAttribute("userId") Long userId) {
        User user = userService.getUserById(userId);
        return Result.success(UserDTO.fromEntityWithoutMask(user));
    }

    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人资料")
    @PutMapping(ApiPaths.User.UPDATE_PATH)
    public Result<UserDTO> updateProfile(
            @RequestAttribute("userId") Long userId,
            @RequestBody UpdateProfileRequest request) {
        User updatedUser = userService.updateUser(userId, request);
        return Result.success("更新成功", UserDTO.fromEntity(updatedUser));
    }

    @Operation(summary = "获取用户统计", description = "获取当前用户的物品数量、收藏数量等统计数据")
    @GetMapping(ApiPaths.User.STATS_PATH)
    public Result<UserStatsDTO> getUserStats(@RequestAttribute("userId") Long userId) {
        UserStatsDTO stats = userService.getUserStats(userId);
        return Result.success(stats);
    }

    // ========== 卖家公开端点（无需登录） ==========

    @Operation(summary = "获取卖家店铺信息", description = "公开接口，获取卖家的店铺信息、统计和评分")
    @GetMapping(ApiPaths.User.SELLER_PROFILE_PATH)
    public Result<SellerProfileDTO> getSellerProfile(@PathVariable Long id) {
        SellerProfileDTO profile = userService.getSellerProfile(id);
        return Result.success(profile);
    }

    @Operation(summary = "获取卖家在售商品", description = "公开接口，分页获取卖家的在售商品列表")
    @GetMapping(ApiPaths.User.SELLER_ITEMS_PATH)
    public Result<Page<ItemSummaryDTO>> getSellerItems(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<Item> itemPage = itemQueryService.getUserItems(id, Item.ItemStatus.ON_SALE, page, size);
        Page<ItemSummaryDTO> result = itemQueryService.convertToSummaryPage(itemPage);
        return Result.success(result);
    }

    @Operation(summary = "获取卖家评价", description = "公开接口，分页获取卖家收到的评价列表")
    @GetMapping(ApiPaths.User.SELLER_REVIEWS_PATH)
    public Result<Page<ReviewResponse>> getSellerReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(reviewService.getReviewsByUserId(id, pageable).map(ReviewResponse::from));
    }
}
