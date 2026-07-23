package com.idleitems.school.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(description = "更新个人信息请求参数")
public class UpdateProfileRequest {
    @Size(max = 50, message = "昵称最多50个字符")
    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Size(max = 20, message = "手机号最多20个字符")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Size(max = 500, message = "头像URL最多500字符")
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Size(max = 30, message = "学号最多30个字符")
    @Schema(description = "学号", example = "2021001001")
    private String studentId;

    @Schema(description = "性别（0-未知，1-男，2-女）", example = "1")
    private Integer gender;

    @Schema(description = "生日", example = "2000-01-01")
    private String birthday;

    @Size(max = 200, message = "个人简介最多200字符")
    @Schema(description = "个人简介", example = "一个热爱生活的学生")
    private String bio;

    @Size(max = 100, message = "学校名称最多100字符")
    @Schema(description = "学校名称", example = "清华大学")
    private String schoolName;

    @Size(max = 100, message = "学院最多100字符")
    @Schema(description = "学院/系", example = "计算机科学与技术学院")
    private String department;

    @Size(max = 100, message = "专业最多100字符")
    @Schema(description = "专业", example = "软件工程")
    private String major;

    @Size(max = 20, message = "年级最多20字符")
    @Schema(description = "年级", example = "2021级")
    private String grade;
}
