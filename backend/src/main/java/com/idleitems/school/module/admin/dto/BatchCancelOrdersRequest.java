package com.idleitems.school.module.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class BatchCancelOrdersRequest {

    @NotEmpty(message = "订单ID列表不能为空")
    @Size(max = 100, message = "一次操作最多100个订单")
    private List<Long> orderIds;

    @Size(max = 500, message = "取消原因最多500字符")
    private String reason;
}
