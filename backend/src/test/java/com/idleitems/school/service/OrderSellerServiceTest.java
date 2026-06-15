package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.common.event.OrderShippedEvent;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.module.order.service.OrderSellerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSellerServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderSellerService orderSellerService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD001");
        testOrder.setBuyerId(1L);
        testOrder.setSellerId(2L);
        testOrder.setItemId(10L);
        testOrder.setPrice(new BigDecimal("100.00"));
        testOrder.setOrderStatus(Order.OrderStatus.PENDING_SHIPMENT);
    }

    @Test
    void shipOrder_WhenValidRequest_ShipsOrder() {
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderSellerService.shipOrder(1L, 2L);

        assertNotNull(result);
        assertEquals(Order.OrderStatus.SHIPPED, result.getOrderStatus());
        assertNotNull(result.getShipTime());
        verify(orderRepository, times(1)).findByIdWithLock(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(eventPublisher, times(1)).publishEvent(any(OrderShippedEvent.class));
    }

    @Test
    void shipOrder_WhenOrderNotFound_ThrowsException() {
        when(orderRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderSellerService.shipOrder(999L, 2L));
        assertEquals(ErrorCode.ORDER_NOT_FOUND, ex.getErrorCode());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shipOrder_WhenWrongSeller_ThrowsException() {
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderSellerService.shipOrder(1L, 999L));
        assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shipOrder_WhenInvalidStatus_ThrowsException() {
        testOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderSellerService.shipOrder(1L, 2L));
        assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
