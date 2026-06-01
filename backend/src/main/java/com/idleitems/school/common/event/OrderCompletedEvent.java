package com.idleitems.school.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderCompletedEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    
    private final Long orderId;
    private final Long sellerId;
    private final Long buyerId;
    private final String orderNo;

    public OrderCompletedEvent(Object source, Long orderId, Long sellerId, Long buyerId, String orderNo) {
        super(source);
        this.orderId = orderId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.orderNo = orderNo;
    }
}
