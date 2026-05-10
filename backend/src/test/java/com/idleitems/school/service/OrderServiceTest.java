package com.idleitems.school.service;

import com.idleitems.school.dto.order.CancelOrderRequest;
import com.idleitems.school.dto.order.CreateOrderRequest;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private Item testItem;

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

        testItem = new Item();
        testItem.setId(10L);
        testItem.setTitle("测试物品");
        testItem.setPrice(new BigDecimal("99.99"));
        testItem.setStatus(Item.ItemStatus.ON_SALE);
        testItem.setUserId(2L);
    }

    @Test
    void getOrderById_WhenOrderExists_ReturnsOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        Order result = orderService.getOrderById(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("ORD20240101120000ABC12345", result.getOrderNo());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getOrderById_WhenOrderNotExists_ThrowsException() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.getOrderById(999L, 1L);
        });
    }

    @Test
    void getOrderById_WhenNotBuyerOrSeller_ThrowsException() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.getOrderById(1L, 999L);
        });
    }

    @Test
    void cancelOrder_WhenValidRequest_CancelsOrder() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("不想买了");

        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        Order result = orderService.cancelOrder(1L, 1L, request);

        // Assert
        assertNotNull(result);
        assertEquals(Order.OrderStatus.CANCELLED, result.getOrderStatus());
        assertEquals("不想买了", result.getCancelReason());
        verify(orderRepository, times(1)).findByIdWithLock(1L);
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }

    @Test
    void cancelOrder_WhenNotBuyer_ThrowsException() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("不想买了");

        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.cancelOrder(1L, 999L, request);
        });
    }

    @Test
    void cancelOrder_WhenOrderNotPendingPayment_ThrowsException() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("不想买了");
        testOrder.setOrderStatus(Order.OrderStatus.PENDING_SHIPMENT);

        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.cancelOrder(1L, 1L, request);
        });
    }

    @Test
    void shipOrder_WhenValidRequest_ShipsOrder() {
        // Arrange
        testOrder.setOrderStatus(Order.OrderStatus.PENDING_SHIPMENT);
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        Order result = orderService.shipOrder(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(Order.OrderStatus.SHIPPED, result.getOrderStatus());
        assertNotNull(result.getShipTime());
        verify(orderRepository, times(1)).findByIdWithLock(1L);
    }

    @Test
    void confirmReceive_WhenValidRequest_CompletesOrder() {
        // Arrange
        testOrder.setOrderStatus(Order.OrderStatus.SHIPPED);
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        Order result = orderService.confirmReceive(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(Order.OrderStatus.COMPLETED, result.getOrderStatus());
        assertNotNull(result.getCompleteTime());
        verify(orderRepository, times(1)).findByIdWithLock(1L);
    }
}
