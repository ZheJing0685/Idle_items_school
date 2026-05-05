package com.idleitems.school.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "image_analysis")
public class ImageAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "analysis_result", columnDefinition = "JSON")
    private Object analysisResult;

    @Column(name = "item_type", length = 100)
    private String itemType;

    @Column(length = 100)
    private String brand;

    @Column(length = 50)
    private String color;

    @Column(precision = 5, scale = 2)
    private BigDecimal confidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Status {
        PENDING, SUCCESS, FAILED
    }
}