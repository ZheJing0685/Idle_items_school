package com.idleitems.school.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class OrderRefundEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    
    private final Long orderId;
    private final Long buyerId;
    private final Long sellerId;
    private final String orderNo;
    private final BigDecimal refundAmount;
    private final boolean approved;

    public OrderRefundEvent(Object source, Long orderId, Long buyerId, Long sellerId, String orderNo, BigDecimal refundAmount, boolean approved) {
        super(source);
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.orderNo = orderNo;
        this.refundAmount = refundAmount;
        this.approved = approved;
    }
}
