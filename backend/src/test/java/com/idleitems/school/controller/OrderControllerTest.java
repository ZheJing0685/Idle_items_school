package com.idleitems.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idleitems.school.dto.order.CancelOrderRequest;
import com.idleitems.school.dto.order.OrderSummaryResponse;
import com.idleitems.school.service.OrderService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("OrderController 接口测试")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("测试获取买家订单列表 - 返回统一 DTO")
    void testGetBuyerOrdersReturnsOrderSummaryPage() throws Exception {
        OrderSummaryResponse response = buildSummary("PENDING_SHIPMENT", false);
        when(orderService.getBuyerOrderSummaries(eq(1L), eq(null), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/orders")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].orderStatus").value("PENDING_SHIPMENT"))
                .andExpect(jsonPath("$.data.content[0].itemCover").value("cover.jpg"))
                .andExpect(jsonPath("$.data.content[0].reviewed").value(false));
    }

    @Test
    @DisplayName("测试支付订单 - 返回支付后状态")
    void testPayOrderReturnsSummary() throws Exception {
        OrderSummaryResponse response = buildSummary("PENDING_SHIPMENT", false);
        when(orderService.payOrder(1L, 1L, "WECHAT_PAY")).thenReturn(null);
        when(orderService.toOrderSummary(null, 1L)).thenReturn(response);

        mockMvc.perform(post("/api/orders/1/pay")
                        .requestAttr("userId", 1L)
                        .param("paymentMethod", "WECHAT_PAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("支付成功"))
                .andExpect(jsonPath("$.data.orderStatus").value("PENDING_SHIPMENT"));
    }

    @Test
    @DisplayName("测试取消订单 - 空原因触发参数校验")
    void testCancelOrderValidation() throws Exception {
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("");

        mockMvc.perform(post("/api/orders/1/cancel")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("参数校验失败"))
                .andExpect(jsonPath("$.data.reason").value("取消原因不能为空"));
    }

    private OrderSummaryResponse buildSummary(String status, boolean reviewed) {
        OrderSummaryResponse response = new OrderSummaryResponse();
        response.setId(1L);
        response.setOrderNo("ORD123456");
        response.setItemId(10L);
        response.setItemTitle("MacBook");
        response.setItemCover("cover.jpg");
        response.setBuyerId(1L);
        response.setSellerId(2L);
        response.setPrice(BigDecimal.valueOf(1999));
        response.setOrderStatus(status);
        response.setCreatedAt(LocalDateTime.of(2026, 4, 22, 10, 0));
        response.setReviewed(reviewed);
        return response;
    }
}
