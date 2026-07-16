package com.idleitems.school.module.auth.dto;

import com.idleitems.school.common.constant.SecurityConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "重置密码请求参数")
public class ResetPasswordRequest {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码")
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Size(min = SecurityConstants.PASSWORD_MIN_LENGTH, max = SecurityConstants.PASSWORD_MAX_LENGTH, message = SecurityConstants.PASSWORD_MESSAGE)
    @Pattern(
        regexp = SecurityConstants.PASSWORD_PATTERN,
        message = SecurityConstants.PASSWORD_MESSAGE
    )
    @Schema(description = "新密码")
    private String newPassword;
}
