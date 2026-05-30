package com.idleitems.school.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderCancelledEvent extends ApplicationEvent {
    private final Long orderId;
    private final Long sellerId;
    private final String orderNo;
    private final String reason;

    public OrderCancelledEvent(Object source, Long orderId, Long sellerId, String orderNo, String reason) {
        super(source);
        this.orderId = orderId;
        this.sellerId = sellerId;
        this.orderNo = orderNo;
        this.reason = reason;
    }
}
