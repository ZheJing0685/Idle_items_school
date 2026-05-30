package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.dto.order.AdminOrderResponse;
import com.idleitems.school.dto.order.OrderSummaryResponse;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.Order;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private OrderQueryService orderQueryService;

    private Order testOrder;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD20240101120000ABC12345");
        testOrder.setBuyerId(1L);
        testOrder.setSellerId(2L);
        testOrder.setItemId(10L);
        testOrder.setItemTitle("测试物品");
        testOrder.setPrice(new BigDecimal("99.99"));
        testOrder.setOrderStatus(Order.OrderStatus.PENDING_PAYMENT);
        testOrder.setCreatedAt(LocalDateTime.now());

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getOrderById_WhenOrderExists_ReturnsOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        Order result = orderQueryService.getOrderById(1L, 1L);

        assertNotNull(result);
        assertEquals("ORD20240101120000ABC12345", result.getOrderNo());
    }

    @Test
    void getOrderById_WhenOrderNotExists_ThrowsException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            orderQueryService.getOrderById(999L, 1L);
        });
    }

    @Test
    void getOrderById_WhenNotBuyerOrSeller_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(BusinessException.class, () -> {
            orderQueryService.getOrderById(1L, 999L);
        });
    }

    @Test
    void getBuyerOrders_WithStatus_ReturnsFiltered() {
        Page<Order> page = new PageImpl<>(List.of(testOrder));
        when(orderRepository.findByBuyerIdAndOrderStatus(1L, Order.OrderStatus.PENDING_PAYMENT, pageable))
                .thenReturn(page);

        Page<Order> result = orderQueryService.getBuyerOrders(1L, Order.OrderStatus.PENDING_PAYMENT, pageable);

        assertEquals(1, result.getContent().size());
        verify(orderRepository).findByBuyerIdAndOrderStatus(1L, Order.OrderStatus.PENDING_PAYMENT, pageable);
    }

    @Test
    void getBuyerOrders_WithoutStatus_ReturnsAll() {
        Page<Order> page = new PageImpl<>(List.of(testOrder));
        when(orderRepository.findByBuyerId(1L, pageable)).thenReturn(page);

        Page<Order> result = orderQueryService.getBuyerOrders(1L, null, pageable);

        assertEquals(1, result.getContent().size());
        verify(orderRepository).findByBuyerId(1L, pageable);
    }

    @Test
    void getSellerOrders_WithStatus_ReturnsFiltered() {
        Page<Order> page = new PageImpl<>(List.of(testOrder));
        when(orderRepository.findBySellerIdAndOrderStatus(2L, Order.OrderStatus.PENDING_PAYMENT, pageable))
                .thenReturn(page);

        Page<Order> result = orderQueryService.getSellerOrders(2L, Order.OrderStatus.PENDING_PAYMENT, pageable);

        assertEquals(1, result.getContent().size());
        verify(orderRepository).findBySellerIdAndOrderStatus(2L, Order.OrderStatus.PENDING_PAYMENT, pageable);
    }

    @Test
    void getSellerOrders_WithoutStatus_ReturnsAll() {
        Page<Order> page = new PageImpl<>(List.of(testOrder));
        when(orderRepository.findBySellerId(2L, pageable)).thenReturn(page);

        Page<Order> result = orderQueryService.getSellerOrders(2L, null, pageable);

        assertEquals(1, result.getContent().size());
        verify(orderRepository).findBySellerId(2L, pageable);
    }

    @Test
    void getBuyerOrderSummaries_WithReviewedOrders_ReturnsSummaries() {
        Page<Order> page = new PageImpl<>(List.of(testOrder));
        when(orderRepository.findByBuyerIdAndOrderStatus(1L, Order.OrderStatus.PENDING_PAYMENT, pageable))
                .thenReturn(page);
        when(reviewRepository.findReviewedOrderIds(anyList(), eq(1L))).thenReturn(List.of());

        Page<OrderSummaryResponse> result = orderQueryService.getBuyerOrderSummaries(
                1L, Order.OrderStatus.PENDING_PAYMENT, pageable);

        assertEquals(1, result.getContent().size());
        assertFalse(result.getContent().get(0).isReviewed());
    }

    @Test
    void getBuyerOrderSummaries_WithReviewedMarked_ReturnsSummaries() {
        Page<Order> page = new PageImpl<>(List.of(testOrder));
        when(orderRepository.findByBuyerIdAndOrderStatus(1L, Order.OrderStatus.PENDING_PAYMENT, pageable))
                .thenReturn(page);
        when(reviewRepository.findReviewedOrderIds(anyList(), eq(1L))).thenReturn(List.of(1L));

        Page<OrderSummaryResponse> result = orderQueryService.getBuyerOrderSummaries(
                1L, Order.OrderStatus.PENDING_PAYMENT, pageable);

        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).isReviewed());
    }

    @Test
    void getSellerOrderSummaries_ReturnsSummaries() {
        Page<Order> page = new PageImpl<>(List.of(testOrder));
        when(orderRepository.findBySellerIdAndOrderStatus(2L, Order.OrderStatus.PENDING_PAYMENT, pageable))
                .thenReturn(page);

        Page<OrderSummaryResponse> result = orderQueryService.getSellerOrderSummaries(
                2L, Order.OrderStatus.PENDING_PAYMENT, pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getOrdersByItemId_ReturnsOrders() {
        Item item = new Item();
        item.setId(10L);
        item.setUserId(2L);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(orderRepository.findByItemIdAndSellerId(10L, 2L)).thenReturn(List.of(testOrder));

        List<Order> result = orderQueryService.getOrdersByItemId(10L, 2L);

        assertEquals(1, result.size());
        verify(orderRepository).findByItemIdAndSellerId(10L, 2L);
    }

    @Test
    void getActiveOrdersByItemId_ReturnsActiveOrders() {
        when(orderRepository.findByItemIdAndOrderStatusIn(eq(10L), anyList()))
                .thenReturn(List.of(testOrder));

        List<Order> result = orderQueryService.getActiveOrdersByItemId(10L);

        assertEquals(1, result.size());
    }

    @Test
    void getAdminOrderSummaries_ReturnsAdminOrders() {
        Page<Order> page = new PageImpl<>(List.of(testOrder));
        when(orderRepository.searchAdminOrders("test", Order.OrderStatus.PENDING_PAYMENT, "alipay", pageable))
                .thenReturn(page);

        Page<AdminOrderResponse> result = orderQueryService.getAdminOrderSummaries(
                "test", Order.OrderStatus.PENDING_PAYMENT, "alipay", pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getOrderSummary_WhenOrderExists_ReturnsSummary() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(reviewRepository.existsByOrderIdAndReviewerId(1L, 1L)).thenReturn(false);

        OrderSummaryResponse result = orderQueryService.getOrderSummary(1L, 1L);

        assertNotNull(result);
        assertEquals("ORD20240101120000ABC12345", result.getOrderNo());
    }

    @Test
    void getOrderSummary_WhenReviewed_ReturnsReviewed() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(reviewRepository.existsByOrderIdAndReviewerId(1L, 1L)).thenReturn(true);

        OrderSummaryResponse result = orderQueryService.getOrderSummary(1L, 1L);

        assertTrue(result.isReviewed());
    }

    @Test
    void getOrderSummary_WhenSeller_ReviewNotChecked() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        OrderSummaryResponse result = orderQueryService.getOrderSummary(1L, 2L);

        assertNotNull(result);
        assertFalse(result.isReviewed());
    }

    @Test
    void getAdminOrderSummary_WhenOrderExists_ReturnsSummary() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        AdminOrderResponse result = orderQueryService.getAdminOrderSummary(1L);

        assertNotNull(result);
        assertEquals("ORD20240101120000ABC12345", result.getOrderNo());
    }

    @Test
    void getAdminOrderSummary_WhenOrderNotExists_ThrowsException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            orderQueryService.getAdminOrderSummary(999L);
        });
    }

    @Test
    void getAdminOrderStats_ReturnsStats() {
        when(orderRepository.count()).thenReturn(100L);
        List<Object[]> groupedCounts = new ArrayList<>();
        groupedCounts.add(new Object[]{Order.OrderStatus.PENDING_PAYMENT, 10L});
        groupedCounts.add(new Object[]{Order.OrderStatus.COMPLETED, 50L});
        when(orderRepository.countByOrderStatusGrouped()).thenReturn(groupedCounts);
        when(orderRepository.sumCompletedOrderAmount()).thenReturn(new BigDecimal("5000.00"));

        Map<String, Object> result = orderQueryService.getAdminOrderStats();

        assertEquals(100L, result.get("total"));
        assertEquals(10L, result.get("pendingPayment"));
        assertEquals(50L, result.get("completed"));
        assertEquals(new BigDecimal("5000.00"), result.get("amount"));
    }

    @Test
    void getAdminOrderStats_WithMissingStatuses_ReturnsDefaults() {
        when(orderRepository.count()).thenReturn(50L);
        when(orderRepository.countByOrderStatusGrouped()).thenReturn(List.of());
        when(orderRepository.sumCompletedOrderAmount()).thenReturn(BigDecimal.ZERO);

        Map<String, Object> result = orderQueryService.getAdminOrderStats();

        assertEquals(50L, result.get("total"));
        assertEquals(0L, result.get("pendingPayment"));
        assertEquals(0L, result.get("shipped"));
        assertEquals(BigDecimal.ZERO, result.get("amount"));
    }
}
