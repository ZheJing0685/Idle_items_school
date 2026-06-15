package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.order.dto.CancelOrderRequest;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.module.order.service.OrderBuyerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderBuyerServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderBuyerService orderBuyerService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD20240101120000ABC12345");
        testOrder.setBuyerId(1L);
        testOrder.setSellerId(2L);
        testOrder.setItemId(10L);
        testOrder.setPrice(new BigDecimal("99.99"));
        testOrder.setOrderStatus(Order.OrderStatus.PENDING_PAYMENT);
        testOrder.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void cancelOrder_WhenValidRequest_CancelsOrder() {
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("不想买了");
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderBuyerService.cancelOrder(1L, 1L, request);

        assertNotNull(result);
        assertEquals(Order.OrderStatus.CANCELLED, result.getOrderStatus());
    }

    @Test
    void cancelOrder_WhenNotBuyer_ThrowsException() {
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("不想买了");
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(BusinessException.class, () -> {
            orderBuyerService.cancelOrder(1L, 999L, request);
        });
    }

    @Test
    void confirmReceive_WhenValidRequest_CompletesOrder() {
        testOrder.setOrderStatus(Order.OrderStatus.SHIPPED);
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderBuyerService.confirmReceive(1L, 1L);

        assertNotNull(result);
        assertEquals(Order.OrderStatus.COMPLETED, result.getOrderStatus());
    }
}
