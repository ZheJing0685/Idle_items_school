package com.idleitems.school.service;

import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.module.order.service.OrderTimeoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTimeoutServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private OrderTimeoutService orderTimeoutService;

    private Order timeoutOrder;

    @BeforeEach
    void setUp() {
        timeoutOrder = new Order();
        timeoutOrder.setId(1L);
        timeoutOrder.setItemId(10L);
        timeoutOrder.setOrderNo("ORD001");
        timeoutOrder.setOrderStatus(Order.OrderStatus.PENDING_PAYMENT);
    }

    @Test
    void cancelTimeoutOrders_WhenTimeoutOrdersExist_CancelsOrders() {
        when(orderRepository.findTimeoutOrders(eq(Order.OrderStatus.PENDING_PAYMENT), any())).thenReturn(List.of(timeoutOrder));
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(timeoutOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.existsByItemIdAndOrderStatusInAndIdNot(eq(10L), anyList(), eq(1L))).thenReturn(false);
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());

        int result = orderTimeoutService.cancelTimeoutOrders(30);

        assertEquals(1, result);
        assertEquals(Order.OrderStatus.CANCELLED, timeoutOrder.getOrderStatus());
        assertEquals("Order timeout, auto cancelled", timeoutOrder.getCancelReason());
        verify(orderRepository, times(1)).findTimeoutOrders(eq(Order.OrderStatus.PENDING_PAYMENT), any());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void cancelTimeoutOrders_WhenNoTimeoutOrders_ReturnsZero() {
        when(orderRepository.findTimeoutOrders(eq(Order.OrderStatus.PENDING_PAYMENT), any())).thenReturn(Collections.emptyList());

        int result = orderTimeoutService.cancelTimeoutOrders(30);

        assertEquals(0, result);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelTimeoutOrders_WhenExceptionThrown_HandlesError() {
        when(orderRepository.findTimeoutOrders(eq(Order.OrderStatus.PENDING_PAYMENT), any())).thenReturn(List.of(timeoutOrder));
        when(orderRepository.findByIdWithLock(1L)).thenThrow(new RuntimeException("DB error"));

        int result = orderTimeoutService.cancelTimeoutOrders(30);

        assertEquals(0, result);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelSingleTimeoutOrder_WhenOrderExists_CancelsOrder() {
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(timeoutOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.existsByItemIdAndOrderStatusInAndIdNot(eq(10L), anyList(), eq(1L))).thenReturn(false);
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());

        orderTimeoutService.cancelSingleTimeoutOrder(1L);

        assertEquals(Order.OrderStatus.CANCELLED, timeoutOrder.getOrderStatus());
        assertEquals("Order timeout, auto cancelled", timeoutOrder.getCancelReason());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void cancelSingleTimeoutOrder_WhenOrderNotFound_ReturnsQuietly() {
        when(orderRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());

        orderTimeoutService.cancelSingleTimeoutOrder(999L);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelSingleTimeoutOrder_WhenOrderNotPendingPayment_ReturnsQuietly() {
        timeoutOrder.setOrderStatus(Order.OrderStatus.SHIPPED);
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(timeoutOrder));

        orderTimeoutService.cancelSingleTimeoutOrder(1L);

        verify(orderRepository, never()).save(any(Order.class));
    }
}
