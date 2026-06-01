package com.idleitems.school.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(description = "更新个人信息请求参数")
public class UpdateProfileRequest {
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "头像URL")
    private String avatar;
    @Schema(description = "学号")
    private String studentId;
    @Schema(description = "性别（0-未知，1-男，2-女）")
    private Integer gender;
    @Schema(description = "生日")
    private String birthday;
    @Schema(description = "个人简介")
    private String bio;
    @Schema(description = "学校名称")
    private String schoolName;
}
