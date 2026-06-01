package com.idleitems.school.module.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "dict_types")
public class DictType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_code", nullable = false, unique = true, length = 50)
    private String typeCode;

    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;

    @Column(length = 500)
    private String description;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @Column(nullable = false)
    private Boolean status = true;

    @OneToMany(mappedBy = "dictType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<DictItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}