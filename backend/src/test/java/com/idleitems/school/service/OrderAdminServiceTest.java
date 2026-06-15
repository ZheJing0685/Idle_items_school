package com.idleitems.school.service;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.module.item.repository.ItemRepository;
import com.idleitems.school.module.order.entity.Order;
import com.idleitems.school.module.order.repository.OrderRepository;
import com.idleitems.school.module.order.service.OrderAdminService;
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
class OrderAdminServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private OrderAdminService orderAdminService;

    private Order testOrder;
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

        testItem = new Item();
        testItem.setId(10L);
        testItem.setStatus(Item.ItemStatus.SOLD);
    }

    @Test
    void adminCancelOrder_WhenPendingPayment_CancelsOrder() {
        testOrder.setOrderStatus(Order.OrderStatus.PENDING_PAYMENT);
        testOrder.setPaymentTime(null);
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderAdminService.adminCancelOrder(1L, 3L, "Violation");

        assertEquals(Order.OrderStatus.CANCELLED, result.getOrderStatus());
        assertTrue(result.getCancelReason().contains("Admin cancelled"));
        assertNull(result.getRefundAmount());
        verify(orderRepository, times(1)).findByIdWithLock(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void adminCancelOrder_WhenPendingShipment_RestoresItem() {
        testOrder.setOrderStatus(Order.OrderStatus.PENDING_SHIPMENT);
        testOrder.setPaymentTime(LocalDateTime.now());
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findItemByIdWithLock(10L)).thenReturn(Optional.of(testItem));
        when(orderRepository.existsByItemIdAndOrderStatusInAndIdNot(eq(10L), anyList(), eq(1L))).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderAdminService.adminCancelOrder(1L, 3L, "Violation");

        assertEquals(Order.OrderStatus.CANCELLED, result.getOrderStatus());
        assertEquals(new BigDecimal("100.00"), result.getRefundAmount());
        assertNotNull(result.getRefundTime());
        assertEquals(Item.ItemStatus.ON_SALE, testItem.getStatus());
        verify(itemRepository, times(1)).save(testItem);
    }

    @Test
    void adminCancelOrder_WhenInvalidStatus_ThrowsException() {
        testOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderAdminService.adminCancelOrder(1L, 3L, "Violation"));
        assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, ex.getErrorCode());
    }

    @Test
    void adminCancelOrder_WhenOrderNotFound_ThrowsException() {
        when(orderRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderAdminService.adminCancelOrder(999L, 3L, "Violation"));
        assertEquals(ErrorCode.ORDER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void adminCancelOrder_WhenShippedAndItemNotFound_ThrowsException() {
        testOrder.setOrderStatus(Order.OrderStatus.SHIPPED);
        testOrder.setPaymentTime(LocalDateTime.now());
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findItemByIdWithLock(10L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderAdminService.adminCancelOrder(1L, 3L, "Violation"));
        assertEquals(ErrorCode.ITEM_NOT_FOUND, ex.getErrorCode());
    }
}
