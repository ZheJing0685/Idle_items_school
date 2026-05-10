package com.idleitems.school.dto;

import com.idleitems.school.entity.Item;
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
public class ItemDTO {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal minPrice;
    private Integer deliveryMethod;
    private Integer contactType;
    private Boolean isBargainAllowed;
    private String brand;
    private LocalDateTime purchaseDate;
    private String warrantyInfo;
    private String tags;
    private String contactName;
    private String contactPhone;
    private String contactInfo;
    private Item.ItemCondition condition;
    private Item.ItemStatus status;
    private Integer viewCount;
    private Integer favoriteCount;
    private String rejectReason;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String coverImage;
    private String sellerNickname;
    private Boolean sellerVerified;
    private Double sellerRating;
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
                .purchaseDate(item.getPurchaseDate())
                .warrantyInfo(item.getWarrantyInfo())
                .tags(item.getTags())
                .contactName(item.getContactName())
                .contactPhone(item.getContactPhone())
                .contactInfo(item.getContactInfo())
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
