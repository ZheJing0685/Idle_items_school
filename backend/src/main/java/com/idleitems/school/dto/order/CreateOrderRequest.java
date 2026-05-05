package com.idleitems.school.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateOrderRequest {

    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    @NotBlank(message = "收货人姓名不能为空")
    private String buyerName;

    @NotBlank(message = "收货人电话不能为空")
    private String buyerPhone;

    @NotBlank(message = "收货地址不能为空")
    private String buyerAddress;

    private String paymentMethod = "OFFLINE";
}
