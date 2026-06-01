package com.idleitems.school.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "验证重置码请求参数")
public class VerifyCodeRequest {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码为6位数字")
    @Schema(description = "验证码")
    private String code;
}
