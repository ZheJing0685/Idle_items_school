package com.idleitems.school.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "category_change_logs")
public class CategoryChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActionType action;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ActionType {
        CREATE, UPDATE, DELETE, STATUS_CHANGE
    }
}
