package com.idleitems.school.module.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class BatchUpdateUserStatusRequest {

    @NotEmpty(message = "用户ID列表不能为空")
    @Size(max = 100, message = "一次操作最多100个用户")
    private List<Long> userIds;

    @NotNull(message = "用户状态不能为空")
    private String status;
}
