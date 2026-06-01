package com.idleitems.school.module.order.dto;

import com.idleitems.school.module.order.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "管理员订单信息响应")
public class AdminOrderResponse {

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
    @Schema(description = "买家姓名")
    private String buyerName;
    @Schema(description = "买家电话")
    private String buyerPhone;
    @Schema(description = "买家地址")
    private String buyerAddress;
    @Schema(description = "卖家ID")
    private Long sellerId;
    @Schema(description = "价格")
    private BigDecimal price;
    @Schema(description = "支付方式")
    private String paymentMethod;
    @Schema(description = "订单状态")
    private String orderStatus;
    @Schema(description = "支付时间")
    private LocalDateTime paymentTime;
    @Schema(description = "发货时间")
    private LocalDateTime shipTime;
    @Schema(description = "完成时间")
    private LocalDateTime completeTime;
    @Schema(description = "取消原因")
    private String cancelReason;
    @Schema(description = "退款原因")
    private String refundReason;
    @Schema(description = "退款时间")
    private LocalDateTime refundTime;
    @Schema(description = "退款金额")
    private BigDecimal refundAmount;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
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
