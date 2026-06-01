package com.idleitems.school.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "system_configs")
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "config_type", nullable = false)
    private Integer configType = 1;

    @Column(length = 200)
    private String description;

    @Column(name = "is_editable", nullable = false)
    private Boolean isEditable = true;

    @Column(name = "group_name", length = 50)
    private String groupName = "general";

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
