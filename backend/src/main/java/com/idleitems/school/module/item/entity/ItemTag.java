package com.idleitems.school.module.item.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "item_tags", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"item_id", "tag_name"})
})
public class ItemTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}