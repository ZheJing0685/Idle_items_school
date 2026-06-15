package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.common.event.OrderRefundEvent;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.order.dto.RefundRequest;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.module.order.service.OrderRefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class OrderRefundServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderRefundService orderRefundService;

    private Order testOrder;
    private RefundRequest refundRequest;
    private Item testItem;

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

        refundRequest = new RefundRequest();
        refundRequest.setReason("Not satisfied");

        testItem = new Item();
        testItem.setId(10L);
        testItem.setCategoryId(5L);
        testItem.setStatus(Item.ItemStatus.SOLD);
    }

    @Test
    void applyRefund_WhenValidRequest_AppliesRefund() {
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderRefundService.applyRefund(1L, 1L, refundRequest);

        assertNotNull(result);
        assertEquals(Order.OrderStatus.REFUND_REQUESTED, result.getOrderStatus());
        assertEquals("Not satisfied", result.getRefundReason());
        verify(orderRepository, times(1)).findByIdWithLock(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void applyRefund_WhenOrderNotFound_ThrowsException() {
        when(orderRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderRefundService.applyRefund(999L, 1L, refundRequest));
        assertEquals(ErrorCode.ORDER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void applyRefund_WhenWrongBuyer_ThrowsException() {
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderRefundService.applyRefund(1L, 999L, refundRequest));
        assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
    }

    @Test
    void applyRefund_WhenInvalidStatus_ThrowsException() {
        testOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderRefundService.applyRefund(1L, 1L, refundRequest));
        assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
    }

    @Test
    void approveRefund_WhenApproved_ApprovesRefund() {
        testOrder.setOrderStatus(Order.OrderStatus.REFUND_REQUESTED);
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(testItem));
        when(orderRepository.existsByItemIdAndOrderStatusInAndIdNot(eq(10L), anyList(), eq(1L))).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderRefundService.approveRefund(1L, 3L, "APPROVED");

        assertEquals(Order.OrderStatus.REFUNDED, result.getOrderStatus());
        assertEquals("APPROVED", result.getRefundResult());
        assertEquals(3L, result.getRefundAdminId());
        assertEquals(new BigDecimal("100.00"), result.getRefundAmount());
        assertNotNull(result.getRefundTime());
        assertEquals(Item.ItemStatus.ON_SALE, testItem.getStatus());
        verify(itemRepository, times(1)).save(testItem);
        verify(eventPublisher, times(1)).publishEvent(any(OrderRefundEvent.class));
    }

    @Test
    void approveRefund_WhenRejectedAndShipped_RejectsRefund() {
        testOrder.setOrderStatus(Order.OrderStatus.REFUND_REQUESTED);
        testOrder.setShipTime(java.time.LocalDateTime.now());
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderRefundService.approveRefund(1L, 3L, "REJECTED");

        assertEquals(Order.OrderStatus.SHIPPED, result.getOrderStatus());
        assertEquals("REJECTED", result.getRefundResult());
        assertEquals(3L, result.getRefundAdminId());
        ArgumentCaptor<OrderRefundEvent> eventCaptor = ArgumentCaptor.forClass(OrderRefundEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertFalse(eventCaptor.getValue().isApproved());
        assertNull(eventCaptor.getValue().getRefundAmount());
    }

    @Test
    void approveRefund_WhenRejectedAndNotShipped_RejectsRefund() {
        testOrder.setOrderStatus(Order.OrderStatus.REFUND_REQUESTED);
        testOrder.setShipTime(null);
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderRefundService.approveRefund(1L, 3L, "REJECTED");

        assertEquals(Order.OrderStatus.PENDING_SHIPMENT, result.getOrderStatus());
        assertEquals("REJECTED", result.getRefundResult());
    }

    @Test
    void approveRefund_WhenOrderNotFound_ThrowsException() {
        when(orderRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                orderRefundService.approveRefund(999L, 3L, "APPROVED"));
    }

    @Test
    void approveRefund_WhenInvalidStatus_ThrowsException() {
        testOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(BusinessException.class, () ->
                orderRefundService.approveRefund(1L, 3L, "APPROVED"));
    }

    @Test
    void approveRefund_WhenInvalidResult_ThrowsException() {
        testOrder.setOrderStatus(Order.OrderStatus.REFUND_REQUESTED);
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(BusinessException.class, () ->
                orderRefundService.approveRefund(1L, 3L, "INVALID"));
    }
}
