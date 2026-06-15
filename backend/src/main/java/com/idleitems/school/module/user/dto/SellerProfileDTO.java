package com.idleitems.school.module.user.dto;

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
@Schema(description = "卖家店铺信息")
public class SellerProfileDTO {
    @Schema(description = "卖家用户ID")
    private Long id;

    @Schema(description = "卖家昵称")
    private String nickname;

    @Schema(description = "卖家头像")
    private String avatar;

    @Schema(description = "学校名称")
    private String schoolName;
    @Schema(description = "学院/系")
    private String department;
    @Schema(description = "专业")
    private String major;
    @Schema(description = "年级")
    private String grade;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "是否实名认证")
    private Boolean verified;

    @Schema(description = "信用分")
    private Integer creditScore;

    @Schema(description = "注册时间")
    private LocalDateTime memberSince;

    @Schema(description = "在售商品数")
    private Long totalItems;

    @Schema(description = "已售件数")
    private Long soldItems;

    @Schema(description = "完成交易数")
    private Long completedDeals;

    @Schema(description = "平均评分")
    private Double rating;

    @Schema(description = "评价数")
    private Long reviewCount;
}
