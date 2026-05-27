package com.idleitems.school.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建评价请求参数")
public class CreateReviewRequest {

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    @Schema(description = "评分（1-5）")
    private Integer rating;

    @Size(max = 500, message = "评价内容不能超过500个字符")
    @Schema(description = "评价内容（最多500字）")
    private String content;

    @Schema(description = "图片URL，多个用逗号分隔（最多9张）")
    private String images;

    @Schema(description = "是否匿名评价")
    private Boolean isAnonymous = false;
}
