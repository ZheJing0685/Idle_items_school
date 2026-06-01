package com.idleitems.school.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderShippedEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;
    
    private final Long orderId;
    private final Long buyerId;
    private final String orderNo;
    private final String trackingNumber;
    private final String shippingCompany;

    public OrderShippedEvent(Object source, Long orderId, Long buyerId, String orderNo, String trackingNumber, String shippingCompany) {
        super(source);
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.orderNo = orderNo;
        this.trackingNumber = trackingNumber;
        this.shippingCompany = shippingCompany;
    }
}
