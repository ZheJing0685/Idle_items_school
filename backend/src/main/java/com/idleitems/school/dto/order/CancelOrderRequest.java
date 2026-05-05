package com.idleitems.school.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelOrderRequest {

    @NotBlank(message = "取消原因不能为空")
    private String reason;
}
