package com.idleitems.school.service;

import com.idleitems.school.dto.order.CancelOrderRequest;
import com.idleitems.school.dto.order.CreateOrderRequest;
import com.idleitems.school.dto.order.RefundRequest;
import com.idleitems.school.entity.Item;
import com.idleitems.school.entity.Order;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("OrderService 单元测试")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private OrderService orderService;

    private Item testItem;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testItem = new Item();
        testItem.setId(1L);
        testItem.setUserId(2L);
        testItem.setTitle("Test Item");
        testItem.setCoverImage("cover.jpg");
        testItem.setPrice(BigDecimal.valueOf(100));
        testItem.setStatus(Item.ItemStatus.ON_SALE);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD123456");
        testOrder.setBuyerId(1L);
        testOrder.setSellerId(2L);
        testOrder.setItemId(1L);
        testOrder.setPrice(BigDecimal.valueOf(100));
        testOrder.setOrderStatus(Order.OrderStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("测试创建订单 - 成功")
    void testCreateOrderSuccess() {
        when(itemRepository.findItemByIdWithLock(1L)).thenReturn(Optional.of(testItem));
        when(orderRepository.existsByBuyerIdAndItemIdAndOrderStatusIn(anyLong(), anyLong(), anyList())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItemId(1L);
        request.setBuyerName("张三");
        request.setBuyerPhone("13800138000");
        request.setBuyerAddress("北京市朝阳区");

        Order result = orderService.createOrder(1L, request);

        assertNotNull(result);
        assertEquals("张三", result.getBuyerName());
        assertEquals("13800138000", result.getBuyerPhone());
        assertEquals("cover.jpg", result.getItemImage());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("测试创建订单 - 物品不存在")
    void testCreateOrderItemNotFound() {
        when(itemRepository.findItemByIdWithLock(1L)).thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItemId(1L);

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(1L, request));
    }

    @Test
    @DisplayName("测试创建订单 - 物品已下架")
    void testCreateOrderItemOffShelf() {
        testItem.setStatus(Item.ItemStatus.OFF_SHELF);
        when(itemRepository.findItemByIdWithLock(1L)).thenReturn(Optional.of(testItem));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItemId(1L);

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(1L, request));
    }

    @Test
    @DisplayName("测试创建订单 - 不能购买自己的物品")
    void testCreateOrderCannotBuyOwnItem() {
        testItem.setUserId(1L);
        when(itemRepository.findItemByIdWithLock(1L)).thenReturn(Optional.of(testItem));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItemId(1L);

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(1L, request));
    }

    @Test
    @DisplayName("测试支付订单 - 成功")
    void testPayOrderSuccess() {
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findItemByIdWithLock(1L)).thenReturn(Optional.of(testItem));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.payOrder(1L, 1L, "WECHAT_PAY");

        assertNotNull(result);
        assertEquals(Order.OrderStatus.PENDING_SHIPMENT, result.getOrderStatus());
        assertEquals(Item.ItemStatus.SOLD, testItem.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("测试取消订单 - 成功")
    void testCancelOrderSuccess() {
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("不想要了");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.cancelOrder(1L, 1L, request);

        assertNotNull(result);
        assertEquals(Order.OrderStatus.CANCELLED, result.getOrderStatus());
    }

    @Test
    @DisplayName("测试发货订单 - 成功")
    void testShipOrderSuccess() {
        testOrder.setOrderStatus(Order.OrderStatus.PENDING_SHIPMENT);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.shipOrder(1L, 2L);

        assertNotNull(result);
        assertEquals(Order.OrderStatus.SHIPPED, result.getOrderStatus());
    }

    @Test
    @DisplayName("测试确认收货 - 成功")
    void testConfirmReceiveSuccess() {
        testOrder.setOrderStatus(Order.OrderStatus.SHIPPED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.confirmReceive(1L, 1L);

        assertNotNull(result);
        assertEquals(Order.OrderStatus.COMPLETED, result.getOrderStatus());
    }

    @Test
    @DisplayName("测试申请退款 - 成功")
    void testApplyRefundSuccess() {
        testOrder.setOrderStatus(Order.OrderStatus.PENDING_SHIPMENT);
        RefundRequest request = new RefundRequest();
        request.setReason("不想要了");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.applyRefund(1L, 1L, request);

        assertNotNull(result);
        assertEquals(Order.OrderStatus.REFUND_REQUESTED, result.getOrderStatus());
    }

    @Test
    @DisplayName("测试管理员取消订单 - 成功回滚物品状态")
    void testAdminCancelOrderSuccess() {
        testOrder.setOrderStatus(Order.OrderStatus.PENDING_SHIPMENT);
        testItem.setStatus(Item.ItemStatus.SOLD);

        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findItemByIdWithLock(1L)).thenReturn(Optional.of(testItem));
        when(orderRepository.existsByItemIdAndOrderStatusInAndIdNot(anyLong(), anyList(), anyLong())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.adminCancelOrder(1L, 99L, "管理员取消");

        assertNotNull(result);
        assertEquals(Order.OrderStatus.CANCELLED, result.getOrderStatus());
        assertEquals("管理员取消: 管理员取消", result.getCancelReason());
        assertEquals(Item.ItemStatus.ON_SALE, testItem.getStatus());
    }

    @Test
    @DisplayName("测试管理员取消订单 - 已完成订单不允许取消")
    void testAdminCancelCompletedOrderRejected() {
        testOrder.setOrderStatus(Order.OrderStatus.COMPLETED);
        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testOrder));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.adminCancelOrder(1L, 99L, "管理员取消")
        );

        assertEquals("当前订单状态不允许管理员取消", exception.getMessage());
    }

    @Test
    @DisplayName("测试审批退款 - 成功恢复物品状态")
    void testApproveRefundSuccess() {
        testOrder.setOrderStatus(Order.OrderStatus.REFUND_REQUESTED);
        testItem.setStatus(Item.ItemStatus.SOLD);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(orderRepository.existsByItemIdAndOrderStatusInAndIdNot(anyLong(), anyList(), anyLong())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.approveRefund(1L, 99L, "APPROVED");

        assertNotNull(result);
        assertEquals(Order.OrderStatus.REFUNDED, result.getOrderStatus());
        assertEquals(Item.ItemStatus.ON_SALE, testItem.getStatus());
        assertEquals(BigDecimal.valueOf(100), result.getRefundAmount());
    }

    @Test
    @DisplayName("测试获取订单 - 无权查看")
    void testGetOrderUnauthorized() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(IllegalArgumentException.class, () -> orderService.getOrderById(1L, 999L));
    }
}
