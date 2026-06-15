package com.idleitems.school.common.event;

import com.idleitems.school.module.carbon.service.CarbonService;
import com.idleitems.school.module.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CarbonService carbonService;

    @InjectMocks
    private OrderEventListener orderEventListener;

    private static final Long ORDER_ID = 1001L;
    private static final Long BUYER_ID = 2001L;
    private static final Long SELLER_ID = 3001L;
    private static final String ORDER_NO = "20250101001";
    private static final String ITEM_TITLE = "测试商品";
    private static final String REASON = "买家取消";
    private static final BigDecimal REFUND_AMOUNT = new BigDecimal("99.99");

    private OrderCreatedEvent createdEvent;
    private OrderPaidEvent paidEvent;
    private OrderShippedEvent shippedEvent;
    private OrderCancelledEvent cancelledEvent;
    private OrderCompletedEvent completedEvent;
    private OrderRefundEvent refundApprovedEvent;
    private OrderRefundEvent refundRejectedEvent;

    @BeforeEach
    void setUp() {
        createdEvent = new OrderCreatedEvent(this, ORDER_ID, BUYER_ID, SELLER_ID, ORDER_NO, ITEM_TITLE);
        paidEvent = new OrderPaidEvent(this, ORDER_ID, SELLER_ID, ORDER_NO);
        shippedEvent = new OrderShippedEvent(this, ORDER_ID, BUYER_ID, ORDER_NO, "SF123456", "顺丰速运");
        cancelledEvent = new OrderCancelledEvent(this, ORDER_ID, SELLER_ID, ORDER_NO, REASON);
        completedEvent = new OrderCompletedEvent(this, ORDER_ID, SELLER_ID, BUYER_ID, ORDER_NO);
        refundApprovedEvent = new OrderRefundEvent(this, ORDER_ID, BUYER_ID, SELLER_ID, ORDER_NO, REFUND_AMOUNT, true);
        refundRejectedEvent = new OrderRefundEvent(this, ORDER_ID, BUYER_ID, SELLER_ID, ORDER_NO, REFUND_AMOUNT, false);
    }

    @Test
    void handleOrderCreated_CallsNotificationService() {
        orderEventListener.onOrderCreated(createdEvent);

        verify(notificationService, times(1)).createOrderNotification(
                eq(SELLER_ID), anyString(), contains(ORDER_NO), eq(ORDER_ID)
        );
    }

    @Test
    void handleOrderCreated_WhenNotificationFails_DoesNotPropagate() {
        doThrow(new RuntimeException("Notification failed"))
                .when(notificationService).createOrderNotification(anyLong(), anyString(), anyString(), anyLong());

        orderEventListener.onOrderCreated(createdEvent);

        verify(notificationService, times(1)).createOrderNotification(anyLong(), anyString(), anyString(), anyLong());
    }

    @Test
    void handleOrderPaid_CallsNotificationService() {
        orderEventListener.onOrderPaid(paidEvent);

        verify(notificationService, times(1)).createOrderNotification(
                eq(SELLER_ID), anyString(), contains(ORDER_NO), eq(ORDER_ID)
        );
    }

    @Test
    void handleOrderPaid_WhenNotificationFails_DoesNotPropagate() {
        doThrow(new RuntimeException("Payment notification failed"))
                .when(notificationService).createOrderNotification(anyLong(), anyString(), anyString(), anyLong());

        orderEventListener.onOrderPaid(paidEvent);

        verify(notificationService, times(1)).createOrderNotification(anyLong(), anyString(), anyString(), anyLong());
    }

    @Test
    void handleOrderShipped_CallsNotificationService() {
        orderEventListener.onOrderShipped(shippedEvent);

        verify(notificationService, times(1)).createOrderNotification(
                eq(BUYER_ID), anyString(), argThat(s -> s.contains(ORDER_NO) && s.contains("SF123456")), eq(ORDER_ID)
        );
    }

    @Test
    void handleOrderShipped_WithoutTrackingNumber_OmitsTrackingInfo() {
        OrderShippedEvent eventWithoutTracking = new OrderShippedEvent(
                this, ORDER_ID, BUYER_ID, ORDER_NO, null, null
        );

        orderEventListener.onOrderShipped(eventWithoutTracking);

        verify(notificationService, times(1)).createOrderNotification(
                eq(BUYER_ID), anyString(), contains(ORDER_NO), eq(ORDER_ID)
        );
    }

    @Test
    void handleOrderShipped_WhenNotificationFails_DoesNotPropagate() {
        doThrow(new RuntimeException("Shipping notification failed"))
                .when(notificationService).createOrderNotification(anyLong(), anyString(), anyString(), anyLong());

        orderEventListener.onOrderShipped(shippedEvent);

        verify(notificationService, times(1)).createOrderNotification(anyLong(), anyString(), anyString(), anyLong());
    }

    @Test
    void handleOrderCancelled_CallsNotificationService() {
        orderEventListener.onOrderCancelled(cancelledEvent);

        verify(notificationService, times(1)).createOrderNotification(
                eq(SELLER_ID), anyString(), argThat(s -> s.contains(ORDER_NO) && s.contains(REASON)), eq(ORDER_ID)
        );
    }

    @Test
    void handleOrderCancelled_WhenNotificationFails_DoesNotPropagate() {
        doThrow(new RuntimeException("Cancellation notification failed"))
                .when(notificationService).createOrderNotification(anyLong(), anyString(), anyString(), anyLong());

        orderEventListener.onOrderCancelled(cancelledEvent);

        verify(notificationService, times(1)).createOrderNotification(anyLong(), anyString(), anyString(), anyLong());
    }

    @Test
    void handleOrderCompleted_CallsNotificationAndCarbonService() {
        orderEventListener.onOrderCompleted(completedEvent);

        verify(notificationService, times(1)).createOrderNotification(
                eq(SELLER_ID), anyString(), contains(ORDER_NO), eq(ORDER_ID)
        );
        verify(carbonService, times(1)).recordCarbonSaving(completedEvent);
    }

    @Test
    void handleOrderCompleted_WhenNotificationFails_StillCallsCarbonService() {
        doThrow(new RuntimeException("Completion notification failed"))
                .when(notificationService).createOrderNotification(anyLong(), anyString(), anyString(), anyLong());

        orderEventListener.onOrderCompleted(completedEvent);

        verify(notificationService, times(1)).createOrderNotification(anyLong(), anyString(), anyString(), anyLong());
        verify(carbonService, never()).recordCarbonSaving(any());
    }

    @Test
    void handleOrderCompleted_WhenCarbonServiceThrows_DoesNotPropagate() {
        doThrow(new RuntimeException("Carbon service failed"))
                .when(carbonService).recordCarbonSaving(completedEvent);

        orderEventListener.onOrderCompleted(completedEvent);

        verify(carbonService, times(1)).recordCarbonSaving(completedEvent);
    }

    @Test
    void handleOrderRefund_WhenApproved_SendsNotificationsToBoth() {
        orderEventListener.onOrderRefund(refundApprovedEvent);

        verify(notificationService, times(2)).createOrderNotification(anyLong(), anyString(), anyString(), eq(ORDER_ID));
        verify(notificationService).createOrderNotification(eq(BUYER_ID), contains("已通过"), anyString(), eq(ORDER_ID));
        verify(notificationService).createOrderNotification(eq(SELLER_ID), contains("已处理"), anyString(), eq(ORDER_ID));
    }

    @Test
    void handleOrderRefund_WhenRejected_SendsRejectionToBuyer() {
        orderEventListener.onOrderRefund(refundRejectedEvent);

        verify(notificationService, times(1)).createOrderNotification(anyLong(), anyString(), anyString(), eq(ORDER_ID));
        verify(notificationService).createOrderNotification(eq(BUYER_ID), contains("被拒绝"), anyString(), eq(ORDER_ID));
        verify(notificationService, never()).createOrderNotification(eq(SELLER_ID), anyString(), anyString(), eq(ORDER_ID));
    }

    @Test
    void handleOrderRefund_WhenNotificationFails_DoesNotPropagate() {
        doThrow(new RuntimeException("Refund notification failed"))
                .when(notificationService).createOrderNotification(anyLong(), anyString(), anyString(), anyLong());

        orderEventListener.onOrderRefund(refundApprovedEvent);

        verify(notificationService, atLeastOnce()).createOrderNotification(anyLong(), anyString(), anyString(), anyLong());
    }
}
