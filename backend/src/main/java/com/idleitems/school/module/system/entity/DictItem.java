package com.idleitems.school.module.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "dict_items")
public class DictItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_code", referencedColumnName = "type_code", insertable = false, updatable = false)
    private DictType dictType;

    @Column(name = "type_code", nullable = false, length = 50)
    private String typeCode;

    @Column(name = "item_value", nullable = false, length = 100)
    private String itemValue;

    @Column(name = "item_label", nullable = false, length = 200)
    private String itemLabel;

    @Column(name = "item_label_en", length = 200)
    private String itemLabelEn;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean status = true;

    @Column(name = "css_class", length = 100)
    private String cssClass;

    @Column(name = "extra_data", length = 500)
    private String extraData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}