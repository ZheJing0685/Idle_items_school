package com.idleitems.school.dto;

import com.idleitems.school.entity.VerificationRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRecordDTO {
    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String studentId;
    private String idCard;
    private String teacherId;
    private String school;
    private String studentCard;
    private String idCardFront;
    private String idCardBack;
    private String teacherCard;
    private VerificationRecord.Type type;
    private Integer verificationType;
    private VerificationRecord.Status status;
    private String rejectReason;
    private Long reviewerId;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VerificationRecordDTO fromEntity(VerificationRecord record) {
        if (record == null) {
            return null;
        }
        return VerificationRecordDTO.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .realName(record.getRealName())
                .studentId(record.getStudentId())
                .idCard(record.getIdCard())
                .teacherId(record.getTeacherId())
                .school(record.getSchool())
                .studentCard(record.getStudentCard())
                .idCardFront(record.getIdCardFront())
                .idCardBack(record.getIdCardBack())
                .teacherCard(record.getTeacherCard())
                .type(record.getType())
                .verificationType(record.getType() == null ? null : record.getType().ordinal() + 1)
                .status(record.getStatus())
                .rejectReason(record.getRejectReason())
                .reviewerId(record.getReviewerId())
                .reviewedAt(record.getReviewedAt())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
