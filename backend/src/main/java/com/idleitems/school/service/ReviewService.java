package com.idleitems.school.service;

import com.idleitems.school.dto.order.CreateReviewRequest;
import com.idleitems.school.entity.Order;
import com.idleitems.school.entity.Review;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Review createReview(Long reviewerId, Long orderId, CreateReviewRequest request) {
        log.info("创建评价，用户ID: {}, 订单ID: {}", reviewerId, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

        if (order.getOrderStatus() != Order.OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("只能在订单完成后评价");
        }

        if (!order.getBuyerId().equals(reviewerId)) {
            throw new IllegalArgumentException("只有买家才能评价");
        }

        if (reviewRepository.existsByOrderIdAndReviewerId(orderId, reviewerId)) {
            throw new IllegalArgumentException("您已评价过此订单");
        }

        Review review = new Review();
        review.setOrderId(orderId);
        review.setReviewerId(reviewerId);
        review.setReviewedUserId(order.getSellerId());
        review.setItemId(order.getItemId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setImages(request.getImages());
        review.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);

        Review savedReview = reviewRepository.save(review);
        log.info("评价创建成功，评价ID: {}", savedReview.getId());

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
