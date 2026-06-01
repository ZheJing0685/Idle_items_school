package com.idleitems.school.module.dispute.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "回复纠纷请求参数")
public class ReplyDisputeRequest {
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 1000, message = "回复内容不能超过1000个字符")
    private String content;
}
