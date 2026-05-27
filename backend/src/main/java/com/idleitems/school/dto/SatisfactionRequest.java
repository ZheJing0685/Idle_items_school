package com.idleitems.school.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "满意度评价请求参数")
public class SatisfactionRequest {
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    @Schema(description = "满意度评分（1-5）")
    private Integer score;

    @Schema(description = "评价备注")
    private String remark;
}
