package com.idleitems.school.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.descriptor.java.StringJavaType;
import org.hibernate.type.descriptor.jdbc.JsonJdbcType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "items", indexes = {
    @Index(name = "idx_items_status", columnList = "status"),
    @Index(name = "idx_items_user_id", columnList = "user_id"),
    @Index(name = "idx_items_category_id", columnList = "category_id"),
    @Index(name = "idx_items_view_count", columnList = "view_count"),
    @Index(name = "idx_items_user_id_status", columnList = "user_id, status"),
    @Index(name = "idx_items_category_id_status", columnList = "category_id, status"),
    @Index(name = "idx_items_status_view_count", columnList = "status, view_count"),
    @Index(name = "idx_items_status_created_at", columnList = "status, created_at"),
    @Index(name = "idx_items_status_price", columnList = "status, price")
})
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "min_price", precision = 10, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "delivery_method", length = 50)
    private String deliveryMethod;

    @Column(name = "contact_type", length = 50)
    private String contactType;

    @Column(name = "is_bargain_allowed")
    private Boolean isBargainAllowed = true;

    @Column(length = 100)
    private String brand;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column(name = "warranty_info", length = 255)
    private String warrantyInfo;

    @Column(length = 255)
    private String tags;

    @Column(name = "contact_name", length = 50)
    private String contactName;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_info", length = 100)
    private String contactInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_condition", nullable = false)
    private ItemCondition condition = ItemCondition.GOOD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.PENDING;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "favorite_count")
    private Integer favoriteCount = 0;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @Column(length = 200)
    private String location;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Transient
    private String sellerNickname;

    @Transient
    private boolean sellerVerified;

    @Transient
    private Double sellerRating;

    @Transient
    private Integer sellerItemsCount;

    public enum ItemCondition {
        NEW, LIKE_NEW, GOOD, FAIR, POOR
    }

    public enum ItemStatus {
        DRAFT, PENDING, ON_SALE, SOLD, OFF_SHELF, REJECTED
    }
} 