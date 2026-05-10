package com.idleitems.school.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_user_id", columnList = "user_id"),
    @Index(name = "idx_notifications_user_read_created", columnList = "user_id, is_read, created_at DESC")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "notification_type", nullable = false)
    private Integer notificationType;  // 1-系统通知，2-订单通知，3-商品通知，4-互动通知

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "related_type", length = 50)
    private String relatedType;  // ORDER/ITEM/REVIEW/DISPUTE

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_time")
    private LocalDateTime readTime;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 通知类型枚举
    public enum NotificationType {
        SYSTEM(1, "系统通知"),
        ORDER(2, "订单通知"),
        ITEM(3, "商品通知"),
        INTERACTION(4, "互动通知");

        private final int code;
        private final String description;

        NotificationType(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static NotificationType fromCode(int code) {
            for (NotificationType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            return SYSTEM;
        }
    }
}
