package com.idleitems.school.module.order.dto;

import com.idleitems.school.module.order.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "评价响应")
public class ReviewResponse {
    private Long id;
    private Long orderId;
    private Long reviewerId;
    private Long reviewedUserId;
    private Long itemId;
    private Integer rating;
    private String content;
    private String images;
    private Boolean isAnonymous;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewResponse from(Review review) {
        if (review == null) return null;
        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrderId())
                .reviewerId(review.getIsAnonymous() != null && review.getIsAnonymous() ? null : review.getReviewerId())
                .reviewedUserId(review.getReviewedUserId())
                .itemId(review.getItemId())
                .rating(review.getRating())
                .content(review.getContent())
                .images(review.getImages())
                .isAnonymous(review.getIsAnonymous())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
