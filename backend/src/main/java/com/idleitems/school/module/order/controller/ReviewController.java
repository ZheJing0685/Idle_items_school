package com.idleitems.school.module.order.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.module.order.dto.ReviewResponse;
import com.idleitems.school.module.order.dto.CreateReviewRequest;
import com.idleitems.school.module.order.entity.Review;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.module.order.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "评价管理", description = "交易评价相关接口")
@RestController
@RequestMapping(ApiPaths.Review.BASE)
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "创建评价", description = "用户对已完成订单进行评价")
    @PostMapping("/order/{orderId}")
    public Result<ReviewResponse> createReview(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long orderId,
            @Valid @RequestBody CreateReviewRequest request) {
        Review review = reviewService.createReview(userId, orderId, request);
        return Result.success("评价成功", ReviewResponse.from(review));
    }

    @Operation(summary = "获取用户评价", description = "分页查询指定用户的评价列表")
    @GetMapping("/user/{userId}")
    public Result<Page<ReviewResponse>> getUserReviews(
            @PathVariable Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(reviewService.getReviewsByUserId(userId, pageable).map(ReviewResponse::from));
    }

    @Operation(summary = "获取商品评价", description = "分页查询指定商品的评价列表")
    @GetMapping("/item/{itemId}")
    public Result<Page<ReviewResponse>> getItemReviews(
            @PathVariable Long itemId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(reviewService.getReviewsByItemId(itemId, pageable).map(ReviewResponse::from));
    }

    @Operation(summary = "获取用户评价统计", description = "获取指定用户的平均评分和评价数量")
    @GetMapping("/user/{userId}/stats")
    public Result<Map<String, Object>> getUserReviewStats(@PathVariable Long userId) {
        BigDecimal avgRating = reviewService.getAverageRatingByUserId(userId);
        Long count = reviewService.getReviewCountByUserId(userId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", avgRating);
        stats.put("reviewCount", count);
        return Result.success(stats);
    }

    @Operation(summary = "获取商品评价统计", description = "获取指定商品的平均评分和评价数量")
    @GetMapping("/item/{itemId}/stats")
    public Result<Map<String, Object>> getItemReviewStats(@PathVariable Long itemId) {
        BigDecimal avgRating = reviewService.getAverageRatingByItemId(itemId);
        Long count = reviewService.getReviewCountByItemId(itemId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", avgRating);
        stats.put("reviewCount", count);
        return Result.success(stats);
    }
}
