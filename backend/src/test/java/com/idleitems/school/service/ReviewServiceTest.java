package com.idleitems.school.service;

import com.idleitems.school.dto.order.CreateReviewRequest;
import com.idleitems.school.entity.Order;
import com.idleitems.school.entity.Review;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("ReviewService 单元测试")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Order completedOrder;
    private CreateReviewRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        completedOrder = new Order();
        completedOrder.setId(1L);
        completedOrder.setBuyerId(1L);
        completedOrder.setSellerId(2L);
        completedOrder.setItemId(3L);
        completedOrder.setOrderStatus(Order.OrderStatus.COMPLETED);

        request = new CreateReviewRequest();
        request.setRating(5);
        request.setContent("很好");
        request.setImages("img-1.jpg");
        request.setIsAnonymous(false);
    }

    @Test
    @DisplayName("测试创建评价 - 成功")
    void testCreateReviewSuccess() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(completedOrder));
        when(reviewRepository.existsByOrderIdAndReviewerId(1L, 1L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(10L);
            return review;
        });

        Review result = reviewService.createReview(1L, 1L, request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(2L, result.getReviewedUserId());
        assertEquals(3L, result.getItemId());
        assertEquals(5, result.getRating());
    }

    @Test
    @DisplayName("测试创建评价 - 仅已完成订单可评价")
    void testCreateReviewRequiresCompletedOrder() {
        completedOrder.setOrderStatus(Order.OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(completedOrder));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.createReview(1L, 1L, request)
        );

        assertEquals("只能在订单完成后评价", exception.getMessage());
    }

    @Test
    @DisplayName("测试创建评价 - 禁止重复评价")
    void testCreateReviewRejectsDuplicate() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(completedOrder));
        when(reviewRepository.existsByOrderIdAndReviewerId(1L, 1L)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.createReview(1L, 1L, request)
        );

        assertEquals("您已评价过此订单", exception.getMessage());
    }
}
