package com.idleitems.school.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "取消订单请求参数")
public class CancelOrderRequest {

    @NotBlank(message = "取消原因不能为空")
    @Schema(description = "取消原因")
    private String reason;
}
