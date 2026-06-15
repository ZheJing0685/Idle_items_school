package com.idleitems.school.module.user.service.impl;

import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.module.notification.entity.Notification;
import com.idleitems.school.module.notification.service.NotificationService;
import com.idleitems.school.module.user.dto.SubmitVerificationRequest;
import com.idleitems.school.module.user.entity.User;
import com.idleitems.school.module.user.entity.VerificationRecord;
import com.idleitems.school.module.user.repository.UserRepository;
import com.idleitems.school.module.user.repository.VerificationRecordRepository;
import com.idleitems.school.module.user.service.VerificationService;
import com.idleitems.school.util.DataEncryptionUtil;
import com.idleitems.school.util.IdCardValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationRecordRepository verificationRecordRepository;
    private final UserRepository userRepository;
    private final DataEncryptionUtil dataEncryptionUtil;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public VerificationRecord submit(Long userId, SubmitVerificationRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        // 校验认证类型
        SubmitVerificationRequest.VerificationType type = request.toVerificationType();
        if (type == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效的认证类型");
        }

        // 校验必填字段完整性
        if (!request.isFieldsComplete()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请填写完整的认证信息");
        }

        // 身份证号格式校验
        if (type == SubmitVerificationRequest.VerificationType.ID_CARD) {
            String idCard = request.getIdCard().trim().toUpperCase();
            if (!IdCardValidator.isValid(idCard)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "身份证号格式不正确，请检查后重新输入");
            }
            request.setIdCard(idCard);
        }

        // 检查是否有待审核的记录，如有则拒绝重复提交
        List<VerificationRecord> existingRecords = verificationRecordRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
        if (!existingRecords.isEmpty()) {
            VerificationRecord latest = existingRecords.get(0);
            if (latest.getStatus() == VerificationRecord.Status.PENDING) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "您已提交认证申请，请等待审核完成");
            }
        }

        // 创建新的认证记录（保留历史记录）
        VerificationRecord record = new VerificationRecord();
        record.setUserId(userId);
        record.setRealName(request.getRealName());

        switch (type) {
            case ID_CARD:
                // 身份证号加密存储
                record.setIdCard(dataEncryptionUtil.encrypt(request.getIdCard()));
                record.setIdCardFront(request.getIdCardFront());
                record.setIdCardBack(request.getIdCardBack());
                record.setType(VerificationRecord.Type.ID_CARD);
                break;
            case STUDENT_CARD:
                record.setStudentId(request.getStudentId());
                record.setSchool(request.getSchool());
                record.setStudentCard(request.getStudentCard());
                record.setType(VerificationRecord.Type.STUDENT_CARD);
                break;
            case TEACHER_CARD:
                record.setTeacherId(request.getTeacherId());
                record.setSchool(request.getSchool());
                record.setTeacherCard(request.getTeacherCard());
                record.setType(VerificationRecord.Type.TEACHER_CARD);
                break;
        }

        record.setStatus(VerificationRecord.Status.PENDING);
        VerificationRecord saved = verificationRecordRepository.save(record);
        log.info("用户{}提交实名认证申请，记录ID: {}", userId, saved.getId());

        try {
            List<User> admins = userRepository.findByRole(User.Role.ADMIN, Pageable.unpaged()).getContent();
            String typeName = switch (type) {
                case ID_CARD -> "身份证";
                case STUDENT_CARD -> "学生证";
                case TEACHER_CARD -> "教师证";
            };
            for (User admin : admins) {
                notificationService.createNotification(
                        admin.getId(),
                        Notification.NotificationType.SYSTEM.getCode(),
                        "新实名认证申请",
                        "用户提交了" + typeName + "认证申请（" + request.getRealName() + "），等待审核",
                        saved.getId(),
                        "VERIFICATION"
                );
            }
        } catch (Exception e) {
            log.warn("发送认证审核通知失败: recordId={}", saved.getId(), e);
        }

        return saved;
    }

    @Override
    public Map<String, Object> getStatus(Long userId) {
        List<VerificationRecord> records = verificationRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
        VerificationRecord record = records.isEmpty() ? null : records.get(0);

        if (record != null) {
            String statusName = record.getStatus().name().toLowerCase();
            return Map.of(
                    "status", statusName,
                    "message", getStatusMessage(record.getStatus()),
                    "rejectReason", record.getRejectReason() != null ? record.getRejectReason() : ""
            );
        }
        return Map.of("status", "unverified", "message", "未认证");
    }

    /**
     * 解密身份证号（用于管理端展示）
     */
    public String decryptIdCard(String encryptedIdCard) {
        if (encryptedIdCard == null || encryptedIdCard.isEmpty()) {
            return null;
        }
        try {
            return dataEncryptionUtil.decrypt(encryptedIdCard);
        } catch (Exception e) {
            log.warn("身份证号解密失败，可能是旧数据未加密: {}", e.getMessage());
            return encryptedIdCard;
        }
    }

    /**
     * 脱敏身份证号（用于前端展示）
     */
    public String maskIdCard(String idCard) {
        if (idCard == null) return null;
        return IdCardValidator.mask(idCard);
    }

    private String getStatusMessage(VerificationRecord.Status status) {
        switch (status) {
            case PENDING: return "审核中";
            case APPROVED: return "已通过";
            case REJECTED: return "已拒绝";
            default: return "未知状态";
        }
    }
}
