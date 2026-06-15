package com.idleitems.school.module.category.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "sort", nullable = false)
    private Integer sort = 0;

    @Column(name = "status", nullable = false)
    private Boolean status = true;

    @Column(name = "level", nullable = false)
    private Integer level = 1;

    @Column(name = "icon", length = 255)
    private String icon;

    @Column(name = "keywords", length = 255)
    private String keywords;

    @Column(name = "carbon_saving_kg", precision = 10, scale = 2)
    private BigDecimal carbonSavingKg = BigDecimal.ZERO;

    @Column(name = "updated_by")
    private Long updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}