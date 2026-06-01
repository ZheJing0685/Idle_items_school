package com.idleitems.school.module.category.dto;

import com.idleitems.school.module.category.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分类信息")
public class CategoryDTO {
    @Schema(description = "分类ID")
    private Long id;
    @Schema(description = "分类名称")
    private String name;
    @Schema(description = "分类描述")
    private String description;
    @Schema(description = "父分类ID")
    private Long parentId;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "是否启用")
    private Boolean status;
    @Schema(description = "层级")
    private Integer level;
    @Schema(description = "图标")
    private String icon;
    @Schema(description = "图片")
    private String image;
    @Schema(description = "关键词")
    private String keywords;
    @Schema(description = "Meta描述")
    private String metaDescription;
    @Schema(description = "路径")
    private String path;
    @Schema(description = "背景颜色")
    private String backgroundColor;
    @Schema(description = "创建人")
    private Long createdBy;
    @Schema(description = "更新人")
    private Long updatedBy;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    @Schema(description = "物品数量")
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

    public static CategoryDTO fromEntity(Category category, Integer itemCount) {
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
                .itemCount(itemCount)
                .build();
    }
}
