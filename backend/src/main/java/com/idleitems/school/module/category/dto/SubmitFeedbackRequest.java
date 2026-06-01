package com.idleitems.school.module.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "提交分类反馈请求参数")
public class SubmitFeedbackRequest {

    @NotBlank(message = "反馈类型不能为空")
    @Schema(description = "反馈类型")
    private String feedbackType;

    @Schema(description = "关联分类ID")
    private Long categoryId;

    @NotBlank(message = "反馈描述不能为空")
    @Schema(description = "反馈描述")
    private String description;
}
