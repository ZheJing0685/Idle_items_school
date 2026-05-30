package com.idleitems.school.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderCreatedEvent extends ApplicationEvent {
    private final Long orderId;
    private final Long buyerId;
    private final Long sellerId;
    private final String orderNo;
    private final String itemTitle;

    public OrderCreatedEvent(Object source, Long orderId, Long buyerId, Long sellerId, String orderNo, String itemTitle) {
        super(source);
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.orderNo = orderNo;
        this.itemTitle = itemTitle;
    }
}
