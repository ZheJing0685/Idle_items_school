package com.idleitems.school.task;

import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.module.system.service.ConfigService;
import com.idleitems.school.module.notification.service.NotificationService;
import com.idleitems.school.module.order.service.OrderTimeoutService;
import com.idleitems.school.shared.task.AutoConfirmReceiveTask;
import com.idleitems.school.shared.task.OrderTimeoutTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTaskTest {

    @Mock
    private OrderTimeoutService orderTimeoutService;

    @Mock
    private ConfigService configService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderTimeoutTask orderTimeoutTask;

    @InjectMocks
    private AutoConfirmReceiveTask autoConfirmReceiveTask;

    private Order createTestOrder(Long id, String orderNo) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNo(orderNo);
        order.setBuyerId(1L);
        order.setSellerId(2L);
        order.setOrderStatus(Order.OrderStatus.SHIPPED);
        order.setShipTime(LocalDateTime.now().minusDays(10));
        return order;
    }

    @Test
    void testCheckTimeoutOrders_NoTimeoutOrders() {
        when(configService.getConfigInt("order_timeout_minutes")).thenReturn(null);
        when(orderTimeoutService.cancelTimeoutOrders(30)).thenReturn(0);

        orderTimeoutTask.checkTimeoutOrders();

        verify(orderTimeoutService, times(1)).cancelTimeoutOrders(30);
    }

    @Test
    void testCheckTimeoutOrders_HasTimeoutOrders() {
        when(configService.getConfigInt("order_timeout_minutes")).thenReturn(45);
        when(orderTimeoutService.cancelTimeoutOrders(45)).thenReturn(5);

        orderTimeoutTask.checkTimeoutOrders();

        verify(orderTimeoutService, times(1)).cancelTimeoutOrders(45);
    }

    @Test
    void testCheckTimeoutOrders_ExceptionHandled() {
        when(configService.getConfigInt("order_timeout_minutes")).thenThrow(new RuntimeException("Redis连接失败"));

        orderTimeoutTask.checkTimeoutOrders();

        verify(orderTimeoutService, never()).cancelTimeoutOrders(anyInt());
    }

    @Test
    void testAutoConfirmReceived_NoOrders() {
        when(orderRepository.findByOrderStatusAndShipTimeBefore(
                eq(Order.OrderStatus.SHIPPED), any())).thenReturn(List.of());

        autoConfirmReceiveTask.autoConfirmReceived();

        verify(orderRepository, never()).save(any());
        verify(notificationService, never()).createOrderNotification(any(), anyString(), anyString(), anyLong());
    }

    @Test
    void testAutoConfirmReceived_HasOrders() {
        Order order = createTestOrder(1L, "ORD001");
        when(orderRepository.findByOrderStatusAndShipTimeBefore(
                eq(Order.OrderStatus.SHIPPED), any())).thenReturn(List.of(order));
        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));

        autoConfirmReceiveTask.autoConfirmReceived();

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(notificationService, times(2)).createOrderNotification(any(), anyString(), anyString(), anyLong());
    }

    @Test
    void testAutoConfirmReceived_ExceptionInQuery() {
        when(orderRepository.findByOrderStatusAndShipTimeBefore(
                eq(Order.OrderStatus.SHIPPED), any()))
                .thenThrow(new RuntimeException("数据库连接失败"));

        autoConfirmReceiveTask.autoConfirmReceived();

        verify(orderRepository, never()).saveAll(any());
    }
}
