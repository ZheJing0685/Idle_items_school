package com.idleitems.school.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "创建纠纷请求参数")
public class CreateDisputeRequest {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "纠纷类型: 1=退款, 2=退货退款, 3=换货, 4=其他")
    private Integer disputeType;

    @NotBlank(message = "纠纷原因不能为空")
    @Size(max = 100, message = "纠纷原因不能超过100个字符")
    private String reason;

    @Size(max = 1000, message = "纠纷描述不能超过1000个字符")
    private String description;

    private String evidenceImages;

    @Size(max = 200, message = "期望结果不能超过200个字符")
    private String expectResult;

    private BigDecimal expectRefundAmount;
}
