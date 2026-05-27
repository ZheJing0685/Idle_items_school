package com.idleitems.school.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "提交实名认证请求参数")
public class SubmitVerificationRequest {

    @NotBlank(message = "认证类型不能为空")
    @Pattern(regexp = "^[123]$", message = "认证类型无效，1=身份证, 2=学生证, 3=教师证")
    @Schema(description = "认证类型: 1=身份证, 2=学生证, 3=教师证")
    private String verificationType;

    @NotBlank(message = "真实姓名不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,20}$", message = "请输入正确的中文姓名")
    @Schema(description = "真实姓名")
    private String realName;

    @Size(max = 18, message = "身份证号长度不能超过18位")
    @Schema(description = "身份证号（身份证认证时必填，格式：18位数字+校验码）")
    private String idCard;

    @Schema(description = "身份证正面照片URL（身份证认证时必填）")
    private String idCardFront;

    @Schema(description = "身份证反面照片URL（身份证认证时必填）")
    private String idCardBack;

    @Size(max = 50, message = "学号长度不能超过50位")
    @Schema(description = "学号（学生证认证时必填）")
    private String studentId;

    @Size(max = 100, message = "学校名称长度不能超过100位")
    @Schema(description = "学校名称（学生证/教师证认证时必填）")
    private String school;

    @Schema(description = "学生证照片URL（学生证认证时必填）")
    private String studentCard;

    @Size(max = 50, message = "教师工号长度不能超过50位")
    @Schema(description = "教师证号（教师证认证时必填）")
    private String teacherId;

    @Schema(description = "教师证照片URL（教师证认证时必填）")
    private String teacherCard;

    public VerificationType toVerificationType() {
        if (verificationType == null) return null;
        switch (verificationType) {
            case "1": return VerificationType.ID_CARD;
            case "2": return VerificationType.STUDENT_CARD;
            case "3": return VerificationType.TEACHER_CARD;
            default: return null;
        }
    }

    /**
     * 校验当前请求的字段是否完整
     */
    public boolean isFieldsComplete() {
        VerificationType type = toVerificationType();
        if (type == null) return false;
        switch (type) {
            case ID_CARD:
                return idCard != null && !idCard.isBlank()
                        && idCardFront != null && !idCardFront.isBlank()
                        && idCardBack != null && !idCardBack.isBlank();
            case STUDENT_CARD:
                return studentId != null && !studentId.isBlank()
                        && school != null && !school.isBlank()
                        && studentCard != null && !studentCard.isBlank();
            case TEACHER_CARD:
                return teacherId != null && !teacherId.isBlank()
                        && school != null && !school.isBlank()
                        && teacherCard != null && !teacherCard.isBlank();
            default:
                return false;
        }
    }

    public enum VerificationType {
        ID_CARD, STUDENT_CARD, TEACHER_CARD
    }
}
