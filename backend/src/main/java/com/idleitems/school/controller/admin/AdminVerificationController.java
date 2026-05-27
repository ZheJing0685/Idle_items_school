package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.BusinessException;
import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.common.Result;
import com.idleitems.school.dto.VerificationRecordDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.idleitems.school.entity.User;
import com.idleitems.school.entity.VerificationRecord;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.repository.VerificationRecordRepository;
import com.idleitems.school.service.AdminLogService;
import com.idleitems.school.service.VerificationService;
import com.idleitems.school.service.impl.VerificationServiceImpl;
import com.idleitems.school.config.ApiPaths;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(ApiPaths.Admin.VERIFICATIONS)
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
@Tag(name = "管理员-认证管理", description = "管理员实名认证审核相关接口")
public class AdminVerificationController {

    private final VerificationRecordRepository verificationRecordRepository;
    private final UserRepository userRepository;
    private final AdminLogService adminLogService;
    private final VerificationServiceImpl verificationService;

    private static final int MAX_BATCH_SIZE = 50;

    @GetMapping
    @Operation(summary = "获取认证记录列表", description = "分页查询实名认证记录，支持按状态筛选")
    public Result<Page<VerificationRecordDTO>> getVerifications(
            @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) VerificationRecord.Status status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<VerificationRecord> records;
        if (status != null) {
            records = verificationRecordRepository.findByStatus(status, pageable);
        } else {
            records = verificationRecordRepository.findAll(pageable);
        }

        Page<VerificationRecordDTO> result = records.map(record -> {
            VerificationRecordDTO dto = buildDtoWithDecryptedIdCard(record);
            userRepository.findById(record.getUserId()).ifPresent(user ->
                dto.setUsername(user.getUsername()));
            return dto;
        });

        return Result.success(result);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取认证统计", description = "获取实名认证的统计数据")
    public Result<Map<String, Object>> getVerificationStats() {
        long total = verificationRecordRepository.count();
        long pending = verificationRecordRepository.countByStatus(VerificationRecord.Status.PENDING);
        long approved = verificationRecordRepository.countByStatus(VerificationRecord.Status.APPROVED);
        long rejected = verificationRecordRepository.countByStatus(VerificationRecord.Status.REJECTED);

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        long newThisWeek = verificationRecordRepository.countByCreatedAtAfter(oneWeekAgo);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("rejected", rejected);
        stats.put("newThisWeek", newThisWeek);

        return Result.success(stats);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "通过实名认证", description = "审核通过指定用户的实名认证申请")
    @Transactional
    public Result<VerificationRecordDTO> approveVerification(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        VerificationRecord record = verificationRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "认证记录不存在"));

        if (record.getStatus() == VerificationRecord.Status.APPROVED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该认证记录已通过审批");
        }

        record.setStatus(VerificationRecord.Status.APPROVED);
        record.setReviewerId(adminId);
        record.setReviewedAt(LocalDateTime.now());
        VerificationRecord saved = verificationRecordRepository.save(record);

        User user = userRepository.findById(record.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setVerified(true);
        userRepository.save(user);

        Map<String, Object> details = new HashMap<>();
        details.put("recordId", id);
        details.put("userId", record.getUserId());
        details.put("realName", record.getRealName());
        adminLogService.logOperation(adminId, "通过实名认证", "VERIFICATION", id, details, request);

        return Result.success("认证已通过", VerificationRecordDTO.fromEntity(saved));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "拒绝实名认证", description = "拒绝指定用户的实名认证申请")
    @Transactional
    public Result<VerificationRecordDTO> rejectVerification(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestParam String reason,
            HttpServletRequest request) {
        VerificationRecord record = verificationRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "认证记录不存在"));

        record.setStatus(VerificationRecord.Status.REJECTED);
        record.setRejectReason(reason);
        record.setReviewerId(adminId);
        record.setReviewedAt(LocalDateTime.now());
        VerificationRecord saved = verificationRecordRepository.save(record);

        userRepository.findById(record.getUserId()).ifPresent(user -> {
            if (user.getVerified() != null && user.getVerified()) {
                user.setVerified(false);
                userRepository.save(user);
            }
        });

        Map<String, Object> details = new HashMap<>();
        details.put("recordId", id);
        details.put("userId", record.getUserId());
        details.put("realName", record.getRealName());
        details.put("reason", reason);
        adminLogService.logOperation(adminId, "拒绝实名认证", "VERIFICATION", id, details, request);

        return Result.success("认证已拒绝", VerificationRecordDTO.fromEntity(saved));
    }

    @PostMapping("/batch/approve")
    @Operation(summary = "批量通过实名认证", description = "批量通过指定ID列表的实名认证申请，单次最多50条")
    @Transactional
    public Result<Void> batchApproveVerifications(
            @RequestAttribute("userId") Long adminId,
            @RequestBody List<Long> verificationIds,
            HttpServletRequest request) {
        if (verificationIds == null || verificationIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要审核的记录");
        }
        if (verificationIds.size() > MAX_BATCH_SIZE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "单次批量操作最多" + MAX_BATCH_SIZE + "条");
        }

        List<VerificationRecord> records = verificationRecordRepository.findAllById(verificationIds);

        Set<Long> userIdsToApprove = records.stream()
                .filter(r -> r.getStatus() != VerificationRecord.Status.APPROVED)
                .peek(r -> {
                    r.setStatus(VerificationRecord.Status.APPROVED);
                    r.setReviewerId(adminId);
                    r.setReviewedAt(LocalDateTime.now());
                })
                .map(VerificationRecord::getUserId)
                .collect(Collectors.toSet());

        if (!userIdsToApprove.isEmpty()) {
            List<User> users = userRepository.findAllById(userIdsToApprove);
            users.forEach(u -> u.setVerified(true));
            userRepository.saveAll(users);
        }

        verificationRecordRepository.saveAll(records);

        for (VerificationRecord record : records) {
            Map<String, Object> details = new HashMap<>();
            details.put("recordId", record.getId());
            details.put("userId", record.getUserId());
            details.put("realName", record.getRealName());
            adminLogService.logOperation(adminId, "批量通过实名认证", "VERIFICATION", record.getId(), details, request);
        }

        return Result.success("批量审批成功", null);
    }

    @PostMapping("/batch/reject")
    @Operation(summary = "批量拒绝实名认证", description = "批量拒绝指定ID列表的实名认证申请，单次最多50条")
    @Transactional
    public Result<Void> batchRejectVerifications(
            @RequestAttribute("userId") Long adminId,
            @RequestBody BatchRejectRequest requestBody,
            HttpServletRequest request) {
        if (requestBody.getVerificationIds() == null || requestBody.getVerificationIds().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要审核的记录");
        }
        if (requestBody.getVerificationIds().size() > MAX_BATCH_SIZE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "单次批量操作最多" + MAX_BATCH_SIZE + "条");
        }
        if (requestBody.getReason() == null || requestBody.getReason().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请填写拒绝原因");
        }

        List<VerificationRecord> records = verificationRecordRepository.findAllById(requestBody.getVerificationIds());

        for (VerificationRecord record : records) {
            record.setStatus(VerificationRecord.Status.REJECTED);
            record.setRejectReason(requestBody.getReason());
            record.setReviewerId(adminId);
            record.setReviewedAt(LocalDateTime.now());

            userRepository.findById(record.getUserId()).ifPresent(user -> {
                if (user.getVerified() != null && user.getVerified()) {
                    user.setVerified(false);
                    userRepository.save(user);
                }
            });
        }

        verificationRecordRepository.saveAll(records);

        for (VerificationRecord record : records) {
            Map<String, Object> details = new HashMap<>();
            details.put("recordId", record.getId());
            details.put("userId", record.getUserId());
            details.put("realName", record.getRealName());
            details.put("reason", requestBody.getReason());
            adminLogService.logOperation(adminId, "批量拒绝实名认证", "VERIFICATION", record.getId(), details, request);
        }

        return Result.success("批量拒绝成功", null);
    }

    /**
     * 构建包含解密身份证号的DTO（管理端使用）
     */
    private VerificationRecordDTO buildDtoWithDecryptedIdCard(VerificationRecord record) {
        VerificationRecordDTO dto = VerificationRecordDTO.fromEntity(record);
        if (record.getIdCard() != null && !record.getIdCard().isEmpty()) {
            String decryptedIdCard = verificationService.decryptIdCard(record.getIdCard());
            dto.setIdCard(decryptedIdCard);
        }
        return dto;
    }

    @lombok.Data
    public static class BatchRejectRequest {
        private List<Long> verificationIds;
        private String reason;
    }
}
