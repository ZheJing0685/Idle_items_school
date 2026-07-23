package com.idleitems.school.module.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class BatchOffShelfItemsRequest {

    @NotEmpty(message = "物品ID列表不能为空")
    @Size(max = 100, message = "一次操作最多100个物品")
    private List<Long> itemIds;

    @Size(max = 500, message = "下架原因最多500字符")
    private String reason;
}
