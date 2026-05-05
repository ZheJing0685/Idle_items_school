package com.idleitems.school.dto.order;

import com.idleitems.school.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminOrderResponse {

    private Long id;
    private String orderNo;
    private Long itemId;
    private String itemTitle;
    private String itemCover;
    private Long buyerId;
    private String buyerName;
    private String buyerPhone;
    private String buyerAddress;
    private Long sellerId;
    private BigDecimal price;
    private String paymentMethod;
    private String orderStatus;
    private LocalDateTime paymentTime;
    private LocalDateTime shipTime;
    private LocalDateTime completeTime;
    private String cancelReason;
    private String refundReason;
    private LocalDateTime refundTime;
    private BigDecimal refundAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminOrderResponse from(Order order) {
        AdminOrderResponse response = new AdminOrderResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setItemId(order.getItemId());
        response.setItemTitle(order.getItemTitle());
        response.setItemCover(order.getItemImage());
        response.setBuyerId(order.getBuyerId());
        response.setBuyerName(order.getBuyerName());
        response.setBuyerPhone(order.getBuyerPhone());
        response.setBuyerAddress(order.getBuyerAddress());
        response.setSellerId(order.getSellerId());
        response.setPrice(order.getPrice());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setOrderStatus(order.getOrderStatus().name());
        response.setPaymentTime(order.getPaymentTime());
        response.setShipTime(order.getShipTime());
        response.setCompleteTime(order.getCompleteTime());
        response.setCancelReason(order.getCancelReason());
        response.setRefundReason(order.getRefundReason());
        response.setRefundTime(order.getRefundTime());
        response.setRefundAmount(order.getRefundAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        return response;
    }
}
