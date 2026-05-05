package com.idleitems.school.dto;

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
public class ItemSummaryDTO {
    private Long id;
    private String title;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String coverImage;
    private Integer viewCount;
    private Integer favoriteCount;
    private LocalDateTime createdAt;
    private Boolean isBargainAllowed;
    private String condition;
    private String sellerNickname;
    private Boolean sellerVerified;
    private Integer sellerItemsCount;
    private Double sellerRating;
}
