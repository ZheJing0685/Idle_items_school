package com.idleitems.school.module.item.dto;

import com.idleitems.school.module.item.entity.Item;
import com.idleitems.school.util.DataMaskUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "物品信息响应体")
public class ItemDTO {
    @Schema(description = "物品ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "分类ID")
    private Long categoryId;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "价格")
    private BigDecimal price;
    @Schema(description = "原价")
    private BigDecimal originalPrice;
    @Schema(description = "最低价")
    private BigDecimal minPrice;
    @Schema(description = "配送方式")
    private String deliveryMethod;
    @Schema(description = "联系方式类型")
    private String contactType;
    @Schema(description = "是否允许议价")
    private Boolean isBargainAllowed;
    @Schema(description = "品牌")
    private String brand;
    @Schema(description = "保修信息")
    private String warrantyInfo;
    @Schema(description = "标签")
    private String tags;
    @Schema(description = "联系人姓名")
    private String contactName;
    @Schema(description = "联系人电话")
    private String contactPhone;
    @Schema(description = "联系信息")
    private String contactInfo;
    @Schema(description = "物品成色", example = "GOOD")
    private Item.ItemCondition condition;
    @Schema(description = "物品状态", example = "ON_SALE")
    private Item.ItemStatus status;
    @Schema(description = "浏览次数")
    private Integer viewCount;
    @Schema(description = "收藏次数")
    private Integer favoriteCount;
    @Schema(description = "拒绝原因")
    private String rejectReason;
    @Schema(description = "位置")
    private String location;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    @Schema(description = "封面图片URL")
    private String coverImage;
    @Schema(description = "卖家昵称")
    private String sellerNickname;
    @Schema(description = "卖家是否实名认证")
    private Boolean sellerVerified;
    @Schema(description = "卖家评分")
    private Double sellerRating;
    @Schema(description = "卖家物品数量")
    private Integer sellerItemsCount;

    public static ItemDTO fromEntity(Item item) {
        if (item == null) {
            return null;
        }
        return ItemDTO.builder()
                .id(item.getId())
                .userId(item.getUserId())
                .categoryId(item.getCategoryId())
                .title(item.getTitle())
                .description(item.getDescription())
                .price(item.getPrice())
                .originalPrice(item.getOriginalPrice())
                .minPrice(item.getMinPrice())
                .deliveryMethod(item.getDeliveryMethod())
                .contactType(item.getContactType())
                .isBargainAllowed(item.getIsBargainAllowed())
                .brand(item.getBrand())
                .warrantyInfo(item.getWarrantyInfo())
                .tags(item.getTags())
                .contactName(item.getContactName())
                .contactPhone(DataMaskUtil.maskPhone(item.getContactPhone()))
                .contactInfo(DataMaskUtil.maskContactInfo(item.getContactInfo()))
                .condition(item.getCondition())
                .status(item.getStatus())
                .viewCount(item.getViewCount())
                .favoriteCount(item.getFavoriteCount())
                .rejectReason(item.getRejectReason())
                .location(item.getLocation())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .coverImage(item.getCoverImage())
                .sellerNickname(item.getSellerNickname())
                .sellerVerified(item.isSellerVerified())
                .sellerRating(item.getSellerRating())
                .sellerItemsCount(item.getSellerItemsCount())
                .build();
    }
}
