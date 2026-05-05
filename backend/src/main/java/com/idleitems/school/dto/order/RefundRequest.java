package com.idleitems.school.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefundRequest {

    @NotBlank(message = "退款原因不能为空")
    private String reason;
}
