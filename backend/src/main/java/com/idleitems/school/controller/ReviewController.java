package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.dto.order.CreateReviewRequest;
import com.idleitems.school.entity.Review;
import com.idleitems.school.service.ReviewService;
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

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/order/{orderId}")
    public Result<Review> createReview(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long orderId,
            @Valid @RequestBody CreateReviewRequest request) {
        Review review = reviewService.createReview(userId, orderId, request);
        return Result.success("评价成功", review);
    }

    @GetMapping("/user/{userId}")
    public Result<Page<Review>> getUserReviews(
            @PathVariable Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewService.getReviewsByUserId(userId, pageable);
        return Result.success(reviews);
    }

    @GetMapping("/item/{itemId}")
    public Result<Page<Review>> getItemReviews(
            @PathVariable Long itemId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewService.getReviewsByItemId(itemId, pageable);
        return Result.success(reviews);
    }

    @GetMapping("/user/{userId}/stats")
    public Result<Map<String, Object>> getUserReviewStats(@PathVariable Long userId) {
        BigDecimal avgRating = reviewService.getAverageRatingByUserId(userId);
        Long count = reviewService.getReviewCountByUserId(userId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", avgRating);
        stats.put("reviewCount", count);
        return Result.success(stats);
    }

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
