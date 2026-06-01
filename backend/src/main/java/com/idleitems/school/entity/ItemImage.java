package com.idleitems.school.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "item_images")
public class ItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl;

    @Column(name = "is_cover")
    private Boolean isCover = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "format", length = 10)
    private String format;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}