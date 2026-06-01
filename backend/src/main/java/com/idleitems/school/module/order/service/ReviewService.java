package com.idleitems.school.module.order.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.module.order.dto.CreateReviewRequest;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.order.entity.Review;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.module.order.repository.ReviewRepository;
import com.idleitems.school.util.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    private static final int MAX_CONTENT_LENGTH = 500;
    private static final int MAX_IMAGES_COUNT = 9;

    @Transactional
    public Review createReview(Long reviewerId, Long orderId, CreateReviewRequest request) {
        log.info("创建评价，用户ID: {}, 订单ID: {}", reviewerId, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getOrderStatus() != Order.OrderStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "只能在订单完成后评价");
        }

        if (!order.getBuyerId().equals(reviewerId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "只有买家才能评价");
        }

        // 防刷评：同一订单只能评价一次（数据库唯一约束 + 业务层双重检查）
        if (reviewRepository.existsByOrderIdAndReviewerId(orderId, reviewerId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "您已评价过此订单，不可重复评价");
        }

        // 评分范围校验（DTO已有@Min(1)@Max(5)，此处做双重保险）
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评分必须在1-5之间");
        }

        // 评价内容校验
        String content = request.getContent();
        if (content != null && !content.isBlank()) {
            // 长度限制
            if (content.length() > MAX_CONTENT_LENGTH) {
                throw new BusinessException(ErrorCode.BAD_REQUEST,
                        "评价内容不能超过" + MAX_CONTENT_LENGTH + "个字符");
            }

            // 敏感词过滤
            List<String> sensitiveWords = SensitiveWordFilter.findSensitiveWords(content);
            if (!sensitiveWords.isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST,
                        SensitiveWordFilter.getWarningMessage(sensitiveWords));
            }
        }

        // 图片数量校验
        if (request.getImages() != null && !request.getImages().isBlank()) {
            String[] imageArray = request.getImages().split(",");
            if (imageArray.length > MAX_IMAGES_COUNT) {
                throw new BusinessException(ErrorCode.BAD_REQUEST,
                        "评价图片最多" + MAX_IMAGES_COUNT + "张");
            }
        }

        Review review = new Review();
        review.setOrderId(orderId);
        review.setReviewerId(reviewerId);
        review.setReviewedUserId(order.getSellerId());
        review.setItemId(order.getItemId());
        review.setRating(request.getRating());
        review.setContent(content);
        review.setImages(request.getImages());
        review.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);

        Review savedReview = reviewRepository.save(review);
        log.info("评价创建成功，评价ID: {}, 订单ID: {}, 评分: {}", savedReview.getId(), orderId, request.getRating());

        return savedReview;
    }

    public Page<Review> getReviewsByUserId(Long userId, Pageable pageable) {
        return reviewRepository.findByReviewedUserId(userId, pageable);
    }

    public Page<Review> getReviewsByItemId(Long itemId, Pageable pageable) {
        return reviewRepository.findByItemId(itemId, pageable);
    }

    public Page<Review> getReviewsByOrderId(Long orderId, Pageable pageable) {
        return reviewRepository.findByOrderId(orderId, pageable);
    }

    public BigDecimal getAverageRatingByUserId(Long userId) {
        BigDecimal avg = reviewRepository.getAverageRatingByUserId(userId);
        return avg != null ? avg : BigDecimal.ZERO;
    }

    public Long getReviewCountByUserId(Long userId) {
        return reviewRepository.countByReviewedUserId(userId);
    }

    public BigDecimal getAverageRatingByItemId(Long itemId) {
        BigDecimal avg = reviewRepository.getAverageRatingByItemId(itemId);
        return avg != null ? avg : BigDecimal.ZERO;
    }

    public Long getReviewCountByItemId(Long itemId) {
        return reviewRepository.countByItemId(itemId);
    }
}
