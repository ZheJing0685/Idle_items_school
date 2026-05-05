package com.idleitems.school.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.dto.order.AdminOrderResponse;
import com.idleitems.school.dto.order.CancelOrderRequest;
import com.idleitems.school.entity.Order;
import com.idleitems.school.repository.CategoryRepository;
import com.idleitems.school.repository.ItemRepository;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.repository.VerificationRecordRepository;
import com.idleitems.school.service.AdminLogService;
import com.idleitems.school.service.OrderService;
import com.idleitems.school.util.CacheManager;
import com.idleitems.school.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminController 订单接口测试")
class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private VerificationRecordRepository verificationRecordRepository;

    @MockBean
    private AdminLogService adminLogService;

    @MockBean
    private CacheManager cacheManager;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("测试获取订单统计 - 返回统一统计结构")
    void testGetOrderStats() throws Exception {
        when(orderService.getAdminOrderStats()).thenReturn(Map.of(
                "total", 10L,
                "pendingPayment", 1L,
                "pendingShipment", 2L,
                "shipped", 3L,
                "completed", 4L,
                "cancelled", 0L,
                "refundRequested", 1L,
                "refunded", 0L,
                "amount", BigDecimal.valueOf(200)
        ));

        mockMvc.perform(get("/api/admin/orders/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(10))
                .andExpect(jsonPath("$.data.pendingShipment").value(2))
                .andExpect(jsonPath("$.data.amount").value(200));
    }

    @Test
    @DisplayName("测试获取订单列表 - 返回管理端 DTO")
    void testGetOrdersReturnsAdminOrderSummaryPage() throws Exception {
        when(orderService.getAdminOrderSummaries(eq("desk"), eq(Order.OrderStatus.SHIPPED), eq("OFFLINE"), any()))
                .thenReturn(new PageImpl<>(List.of(buildResponse("SHIPPED"))));

        mockMvc.perform(get("/api/admin/orders")
                        .param("keyword", "desk")
                        .param("status", "SHIPPED")
                        .param("paymentMethod", "OFFLINE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].orderStatus").value("SHIPPED"))
                .andExpect(jsonPath("$.data.content[0].itemCover").value("cover.jpg"))
                .andExpect(jsonPath("$.data.content[0].buyerName").value("张三"));
    }

    @Test
    @DisplayName("测试管理员取消订单 - 返回取消后 DTO")
    void testCancelOrder() throws Exception {
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("管理员介入取消");

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setOrderNo("ORD-1");
        savedOrder.setOrderStatus(Order.OrderStatus.CANCELLED);

        when(orderService.adminCancelOrder(1L, 99L, "管理员介入取消")).thenReturn(savedOrder);
        when(orderService.toAdminOrderSummary(savedOrder)).thenReturn(buildResponse("CANCELLED"));

        mockMvc.perform(put("/api/admin/orders/1/cancel")
                        .requestAttr("userId", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("订单已取消"))
                .andExpect(jsonPath("$.data.orderStatus").value("CANCELLED"));
    }

    @Test
    @DisplayName("测试审批退款 - 返回退款后 DTO")
    void testApproveRefund() throws Exception {
        Order refundedOrder = new Order();
        refundedOrder.setId(1L);
        refundedOrder.setOrderNo("ORD-1");
        refundedOrder.setOrderStatus(Order.OrderStatus.REFUNDED);

        when(orderService.approveRefund(1L, 99L, "APPROVED")).thenReturn(refundedOrder);
        when(orderService.toAdminOrderSummary(refundedOrder)).thenReturn(buildResponse("REFUNDED"));

        mockMvc.perform(put("/api/admin/orders/1/refund/approve")
                        .requestAttr("userId", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("退款已审批"))
                .andExpect(jsonPath("$.data.orderStatus").value("REFUNDED"));
    }

    private AdminOrderResponse buildResponse(String status) {
        AdminOrderResponse response = new AdminOrderResponse();
        response.setId(1L);
        response.setOrderNo("ORD-1");
        response.setItemId(11L);
        response.setItemTitle("Desk Lamp");
        response.setItemCover("cover.jpg");
        response.setBuyerId(2L);
        response.setBuyerName("张三");
        response.setBuyerPhone("13800138000");
        response.setBuyerAddress("教学楼 101");
        response.setSellerId(3L);
        response.setPrice(BigDecimal.valueOf(88));
        response.setPaymentMethod("OFFLINE");
        response.setOrderStatus(status);
        response.setCreatedAt(LocalDateTime.of(2026, 4, 23, 9, 0));
        return response;
    }
}
