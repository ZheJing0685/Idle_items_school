package com.idleitems.school.module.auth.dto;

import com.idleitems.school.common.constant.SecurityConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "修改密码请求")
public class ChangePasswordRequest {
    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "旧密码")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = SecurityConstants.PASSWORD_MIN_LENGTH, max = SecurityConstants.PASSWORD_MAX_LENGTH, message = SecurityConstants.PASSWORD_MESSAGE)
    @Pattern(
        regexp = SecurityConstants.PASSWORD_PATTERN,
        message = SecurityConstants.PASSWORD_MESSAGE
    )
    @Schema(description = "新密码")
    private String newPassword;
}
