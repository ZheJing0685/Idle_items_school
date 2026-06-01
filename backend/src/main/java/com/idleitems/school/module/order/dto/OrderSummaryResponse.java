package com.idleitems.school.module.order.dto;

import com.idleitems.school.module.order.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "订单概要响应")
public class OrderSummaryResponse {

    @Schema(description = "订单ID")
    private Long id;
    @Schema(description = "订单号")
    private String orderNo;
    @Schema(description = "物品ID")
    private Long itemId;
    @Schema(description = "物品标题")
    private String itemTitle;
    @Schema(description = "物品封面图片URL")
    private String itemCover;
    @Schema(description = "买家ID")
    private Long buyerId;
    @Schema(description = "卖家ID")
    private Long sellerId;
    @Schema(description = "价格")
    private BigDecimal price;
    @Schema(description = "订单状态")
    private String orderStatus;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "是否已评价")
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
