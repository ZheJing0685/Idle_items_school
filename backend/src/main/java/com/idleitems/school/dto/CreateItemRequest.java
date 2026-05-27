package com.idleitems.school.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "物品发布请求参数")
public class CreateItemRequest {

    @NotBlank(message = "标题不能为空")
    @Size(min = 3, max = 60, message = "标题长度3-60个字符")
    @Schema(description = "物品标题")
    private String title;

    @NotBlank(message = "描述不能为空")
    @Size(min = 10, max = 500, message = "描述长度10-500个字符")
    @Schema(description = "物品描述")
    private String description;

    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须大于0")
    @Schema(description = "出售价格")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "最低接受价格")
    private BigDecimal minPrice;

    @NotNull(message = "分类不能为空")
    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "物品成色", example = "GOOD")
    private String condition;

    @Schema(description = "配送方式")
    private String deliveryMethod;

    @Schema(description = "联系方式类型")
    private String contactType;

    @Schema(description = "是否允许议价")
    private Boolean isBargainAllowed;

    @Schema(description = "交易地点")
    private String location;

    @Schema(description = "品牌")
    private String brand;

    @Schema(description = "保修信息")
    private String warrantyInfo;

    @Schema(description = "标签，逗号分隔")
    private String tags;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "联系信息（微信号/QQ号）")
    private String contactInfo;

    @Schema(description = "图片URL列表")
    private List<String> images;

    @Schema(description = "封面图URL")
    private String coverImage;
}
