package com.idleitems.school.module.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
@Schema(description = "更新用户请求参数")
public class UpdateUserRequest {
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "用户角色", example = "STUDENT")
    private String role;

    @Schema(description = "用户状态", example = "ACTIVE")
    private String status;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "学号")
    private String studentId;

    @Schema(description = "性别（0-未知，1-男，2-女）")
    private Integer gender;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "学校名称")
    private String schoolName;
    @Schema(description = "学院/系")
    private String department;
    @Schema(description = "专业")
    private String major;
    @Schema(description = "年级")
    private String grade;
}
