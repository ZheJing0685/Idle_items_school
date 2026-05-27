package com.idleitems.school.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "创建订单请求参数")
public class CreateOrderRequest {

    @NotNull(message = "物品ID不能为空")
    @Schema(description = "物品ID")
    private Long itemId;

    @NotBlank(message = "收货人姓名不能为空")
    @Schema(description = "收货人姓名")
    private String buyerName;

    @NotBlank(message = "收货人电话不能为空")
    @Schema(description = "收货人电话")
    private String buyerPhone;

    @NotBlank(message = "收货地址不能为空")
    @Schema(description = "收货地址")
    private String buyerAddress;

    @Schema(description = "支付方式", example = "OFFLINE")
    private String paymentMethod = "OFFLINE";
}
