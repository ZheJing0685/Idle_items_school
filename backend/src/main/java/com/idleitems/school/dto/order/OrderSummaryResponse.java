package com.idleitems.school.dto.order;

import com.idleitems.school.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderSummaryResponse {

    private Long id;
    private String orderNo;
    private Long itemId;
    private String itemTitle;
    private String itemCover;
    private Long buyerId;
    private Long sellerId;
    private BigDecimal price;
    private String orderStatus;
    private LocalDateTime createdAt;
    private boolean reviewed;

    public static OrderSummaryResponse from(Order order, boolean reviewed) {
        OrderSummaryResponse response = new OrderSummaryResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setItemId(order.getItemId());
        response.setItemTitle(order.getItemTitle());
        response.setItemCover(order.getItemImage());
        response.setBuyerId(order.getBuyerId());
        response.setSellerId(order.getSellerId());
        response.setPrice(order.getPrice());
        response.setOrderStatus(order.getOrderStatus().name());
        response.setCreatedAt(order.getCreatedAt());
        response.setReviewed(reviewed);
        return response;
    }
}
