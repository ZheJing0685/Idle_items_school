package com.idleitems.school.module.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "退款请求参数")
public class RefundRequest {

    @NotBlank(message = "退款原因不能为空")
    @Schema(description = "退款原因")
    private String reason;
}
