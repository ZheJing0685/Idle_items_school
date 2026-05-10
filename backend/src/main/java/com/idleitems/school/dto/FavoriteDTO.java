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
public class FavoriteDTO {
    private Long id;
    private Long userId;
    private Long itemId;
    private LocalDateTime createdAt;
    
    // Item信息
    private String title;
    private BigDecimal price;
    private String coverImage;
    private String status;
    private String sellerName;
    private Long sellerId;
}
