package com.idleitems.school.dto;

import com.idleitems.school.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Integer sort;
    private Boolean status;
    private Integer level;
    private String icon;
    private String image;
    private String keywords;
    private String metaDescription;
    private String path;
    private String backgroundColor;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer itemCount;

    public static CategoryDTO fromEntity(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .sort(category.getSort())
                .status(category.getStatus())
                .level(category.getLevel())
                .icon(category.getIcon())
                .image(category.getImage())
                .keywords(category.getKeywords())
                .metaDescription(category.getMetaDescription())
                .path(category.getPath())
                .backgroundColor(category.getBackgroundColor())
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdatedBy())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
