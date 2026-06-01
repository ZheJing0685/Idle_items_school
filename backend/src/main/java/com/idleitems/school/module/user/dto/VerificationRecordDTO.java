package com.idleitems.school.module.user.dto;

import com.idleitems.school.module.user.entity.VerificationRecord;
import com.idleitems.school.util.IdCardValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "实名认证记录")
public class VerificationRecordDTO {
    @Schema(description = "认证记录ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "真实姓名")
    private String realName;
    @Schema(description = "学号")
    private String studentId;
    @Schema(description = "身份证号（脱敏显示）")
    private String idCard;
    @Schema(description = "教师工号")
    private String teacherId;
    @Schema(description = "学校")
    private String school;
    @Schema(description = "学生证图片URL")
    private String studentCard;
    @Schema(description = "身份证正面图片URL")
    private String idCardFront;
    @Schema(description = "身份证反面图片URL")
    private String idCardBack;
    @Schema(description = "教师证图片URL")
    private String teacherCard;
    @Schema(description = "认证类型", example = "ID_CARD")
    private VerificationRecord.Type type;
    @Schema(description = "认证类型（数字表示）")
    private Integer verificationType;
    @Schema(description = "认证状态", example = "PENDING")
    private VerificationRecord.Status status;
    @Schema(description = "拒绝原因")
    private String rejectReason;
    @Schema(description = "审核人ID")
    private Long reviewerId;
    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 从实体创建DTO，身份证号脱敏显示
     */
    public static VerificationRecordDTO fromEntity(VerificationRecord record) {
        return fromEntity(record, false);
    }

    /**
     * 从实体创建DTO
     *
     * @param record       认证记录
     * @param decryptIdCard 是否解密身份证号（管理端使用）
     */
    public static VerificationRecordDTO fromEntity(VerificationRecord record, boolean decryptIdCard) {
        if (record == null) {
            return null;
        }

        String displayIdCard = null;
        if (record.getIdCard() != null && !record.getIdCard().isEmpty()) {
            if (decryptIdCard) {
                // 管理端：解密后原样显示
                try {
                    // 注意：解密逻辑应在Service层处理，这里仅做脱敏展示
                    displayIdCard = IdCardValidator.mask(record.getIdCard());
                } catch (Exception e) {
                    displayIdCard = "****";
                }
            } else {
                // 前端：脱敏显示
                displayIdCard = "****";
            }
        }

        return VerificationRecordDTO.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .realName(record.getRealName())
                .studentId(record.getStudentId())
                .idCard(displayIdCard)
                .teacherId(record.getTeacherId())
                .school(record.getSchool())
                .studentCard(record.getStudentCard())
                .idCardFront(record.getIdCardFront())
                .idCardBack(record.getIdCardBack())
                .teacherCard(record.getTeacherCard())
                .type(record.getType())
                .verificationType(record.getType() == null ? null : getTypeCode(record.getType()))
                .status(record.getStatus())
                .rejectReason(record.getRejectReason())
                .reviewerId(record.getReviewerId())
                .reviewedAt(record.getReviewedAt())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    private static Integer getTypeCode(VerificationRecord.Type type) {
        if (type == null) return null;
        switch (type) {
            case ID_CARD: return 1;
            case STUDENT_CARD: return 2;
            case TEACHER_CARD: return 3;
            default: return null;
        }
    }
}
