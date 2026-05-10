package com.idleitems.school.controller.admin;

import com.idleitems.school.annotation.RequireRole;
import com.idleitems.school.common.Result;
import com.idleitems.school.dto.VerificationRecordDTO;
import com.idleitems.school.entity.User;
import com.idleitems.school.entity.VerificationRecord;
import com.idleitems.school.repository.UserRepository;
import com.idleitems.school.repository.VerificationRecordRepository;
import com.idleitems.school.service.AdminLogService;
import jakarta.servlet.http.HttpServletRequest;
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

@Slf4j
@RestController
@RequestMapping("/api/admin/verifications")
@RequiredArgsConstructor
@RequireRole(value = {User.Role.ADMIN}, message = "需要管理员权限")
public class AdminVerificationController {

    private final VerificationRecordRepository verificationRecordRepository;
    private final UserRepository userRepository;
    private final AdminLogService adminLogService;

    @GetMapping
    public Result<Page<VerificationRecordDTO>> getVerifications(
            @RequestParam(value = "page", defaultValue = "1") int page,
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
            VerificationRecordDTO dto = VerificationRecordDTO.fromEntity(record);
            userRepository.findById(record.getUserId()).ifPresent(user -> {
                dto.setUsername(user.getUsername());
            });
            return dto;
        });

        return Result.success(result);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getVerificationStats() {
        try {
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
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/approve")
    public Result<VerificationRecordDTO> approveVerification(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            HttpServletRequest request) {
        VerificationRecord record = verificationRecordRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("认证记录不存在"));
        record.setStatus(VerificationRecord.Status.APPROVED);
        record.setReviewerId(adminId);
        record.setReviewedAt(LocalDateTime.now());
        VerificationRecord saved = verificationRecordRepository.save(record);

        User user = userRepository.findById(record.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setVerified(true);
        userRepository.save(user);

        Map<String, Object> details = new HashMap<>();
        details.put("recordId", id);
        details.put("userId", record.getUserId());
        details.put("realName", record.getRealName());
        adminLogService.logOperation(adminId, "通过实名认证", "VERIFICATION", id, details, request);

        return Result.success("认证已通过", VerificationRecordDTO.fromEntity(saved));
    }

    @PutMapping("/{id}/reject")
    public Result<VerificationRecordDTO> rejectVerification(
            @RequestAttribute("userId") Long adminId,
            @PathVariable Long id,
            @RequestParam String reason,
            HttpServletRequest request) {
        VerificationRecord record = verificationRecordRepository.findById(id.longValue())
                .orElseThrow(() -> new IllegalArgumentException("认证记录不存在"));
        record.setStatus(VerificationRecord.Status.REJECTED);
        record.setRejectReason(reason);
        record.setReviewerId(adminId);
        record.setReviewedAt(LocalDateTime.now());
        VerificationRecord saved = verificationRecordRepository.save(record);

        Map<String, Object> details = new HashMap<>();
        details.put("recordId", id);
        details.put("userId", record.getUserId());
        details.put("realName", record.getRealName());
        details.put("reason", reason);
        adminLogService.logOperation(adminId, "拒绝实名认证", "VERIFICATION", id, details, request);

        return Result.success("认证已拒绝", VerificationRecordDTO.fromEntity(saved));
    }

    @PutMapping("/batch/approve")
    @Transactional
    public Result<Void> batchApproveVerifications(
            @RequestAttribute("userId") Long adminId,
            @RequestBody List<Long> verificationIds,
            HttpServletRequest request) {
        for (Long id : verificationIds) {
            VerificationRecord record = verificationRecordRepository.findById(id.longValue())
                    .orElseThrow(() -> new IllegalArgumentException("认证记录不存在"));
            record.setStatus(VerificationRecord.Status.APPROVED);
            record.setReviewerId(adminId);
            record.setReviewedAt(LocalDateTime.now());
            verificationRecordRepository.save(record);

            User user = userRepository.findById(record.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
            user.setVerified(true);
            userRepository.save(user);

            Map<String, Object> details = new HashMap<>();
            details.put("recordId", id);
            details.put("userId", record.getUserId());
            details.put("realName", record.getRealName());
            adminLogService.logOperation(adminId, "批量通过实名认证", "VERIFICATION", id, details, request);
        }
        return Result.success("批量审批成功", null);
    }

    @PutMapping("/batch/reject")
    @Transactional
    public Result<Void> batchRejectVerifications(
            @RequestAttribute("userId") Long adminId,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        List<Number> verificationNumberIds = (List<Number>) requestBody.get("verificationIds");
        String reason = (String) requestBody.get("reason");

        List<Long> verificationIds = verificationNumberIds.stream()
                .map(Number::longValue)
                .toList();

        for (Long id : verificationIds) {
            VerificationRecord record = verificationRecordRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("认证记录不存在"));
            record.setStatus(VerificationRecord.Status.REJECTED);
            record.setRejectReason(reason);
            record.setReviewerId(adminId);
            record.setReviewedAt(LocalDateTime.now());
            verificationRecordRepository.save(record);

            Map<String, Object> details = new HashMap<>();
            details.put("recordId", id);
            details.put("userId", record.getUserId());
            details.put("realName", record.getRealName());
            details.put("reason", reason);
            adminLogService.logOperation(adminId, "批量拒绝实名认证", "VERIFICATION", id, details, request);
        }
        return Result.success("批量拒绝成功", null);
    }
}
