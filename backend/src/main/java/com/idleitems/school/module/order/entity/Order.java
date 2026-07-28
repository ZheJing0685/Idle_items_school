package com.idleitems.school.module.order.entity;

import com.idleitems.school.module.item.entity.Item;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_buyer_id", columnList = "buyer_id"),
    @Index(name = "idx_orders_seller_id", columnList = "seller_id"),
    @Index(name = "idx_orders_order_status", columnList = "order_status"),
    @Index(name = "idx_orders_buyer_id_status", columnList = "buyer_id, order_status"),
    @Index(name = "idx_orders_seller_id_status", columnList = "seller_id, order_status"),
    @Index(name = "idx_orders_order_no", columnList = "order_no")
})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", unique = true, nullable = false, length = 50)
    private String orderNo;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private Item item;

    @Column(name = "item_title", length = 200)
    private String itemTitle;

    @Column(name = "item_image")
    private String itemImage;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus = OrderStatus.PENDING_PAYMENT;

    @Column(name = "buyer_address", length = 500)
    private String buyerAddress;

    @Column(name = "buyer_phone", length = 20)
    private String buyerPhone;

    @Column(name = "buyer_name", length = 50)
    private String buyerName;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "ship_time")
    private LocalDateTime shipTime;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "shipping_company", length = 100)
    private String shippingCompany;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "refund_reason")
    private String refundReason;

    @Column(name = "refund_time")
    private LocalDateTime refundTime;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "cancel_time")
    private LocalDateTime cancelTime;

    @Column(name = "refund_result", length = 50)
    private String refundResult;

    @Column(name = "refund_admin_id")
    private Long refundAdminId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum OrderStatus {
        PENDING_PAYMENT,
        PENDING_SHIPMENT,
        SHIPPED,
        COMPLETED,
        CANCELLED,
        REFUND_REQUESTED,
        REFUNDED
    }
}
