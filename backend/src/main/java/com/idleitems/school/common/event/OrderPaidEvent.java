package com.idleitems.school.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderPaidEvent extends ApplicationEvent {
    private final Long orderId;
    private final Long sellerId;
    private final String orderNo;

    public OrderPaidEvent(Object source, Long orderId, Long sellerId, String orderNo) {
        super(source);
        this.orderId = orderId;
        this.sellerId = sellerId;
        this.orderNo = orderNo;
    }
}
