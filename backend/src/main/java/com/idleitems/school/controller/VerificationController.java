package com.idleitems.school.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.entity.VerificationRecord;
import com.idleitems.school.repository.VerificationRecordRepository;
import com.idleitems.school.util.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {

    @Autowired
    private ImageUtil imageUtil;
    
    @Autowired
    private VerificationRecordRepository verificationRecordRepository;

    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = imageUtil.uploadImage(file);
            return Result.success("上传成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/submit")
    public Result<Void> submitVerification(@RequestAttribute("userId") Long userId, @RequestBody Map<String, Object> data) {
        try {
            // 查找现有认证记录
            VerificationRecord record = verificationRecordRepository.findByUserId(userId)
                    .orElse(new VerificationRecord());
            
            // 更新认证信息
            record.setUserId(userId);
            record.setRealName((String) data.get("name"));
            
            // 根据认证类型设置不同的字段
            String verificationType = (String) data.get("verificationType");
            if ("1".equals(verificationType)) {
                // 身份证认证
                record.setIdCard((String) data.get("idCard"));
                record.setIdCardFront((String) data.get("idCardFront"));
                record.setIdCardBack((String) data.get("idCardBack"));
                record.setType(VerificationRecord.Type.ID_CARD);
            } else if ("2".equals(verificationType)) {
                // 学生证认证
                record.setStudentId((String) data.get("studentId"));
                record.setSchool((String) data.get("school"));
                record.setStudentCard((String) data.get("studentCard"));
                record.setType(VerificationRecord.Type.STUDENT_CARD);
            } else if ("3".equals(verificationType)) {
                // 教师证认证
                record.setTeacherId((String) data.get("teacherId"));
                record.setSchool((String) data.get("school"));
                record.setTeacherCard((String) data.get("teacherCard"));
                record.setType(VerificationRecord.Type.TEACHER_CARD);
            }
            
            record.setStatus(VerificationRecord.Status.PENDING);
            
            // 保存到数据库
            verificationRecordRepository.save(record);
            
            return Result.success("提交成功", null);
        } catch (Exception e) {
            return Result.error("提交失败: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public Result<Map<String, Object>> getVerificationStatus(@RequestAttribute("userId") Long userId) {
        try {
            // 从数据库查询用户的所有认证记录，按创建时间降序排序，获取最新的一条
            List<VerificationRecord> records = verificationRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
            VerificationRecord record = records.isEmpty() ? null : records.get(0);
            
            Map<String, Object> status;
            if (record != null) {
                status = Map.of(
                        "status", record.getStatus().name().toLowerCase(),
                        "message", getStatusMessage(record.getStatus())
                );
            } else {
                status = Map.of(
                        "status", "unverified",
                        "message", "未认证"
                );
            }
            
            return Result.success("获取状态成功", status);
        } catch (Exception e) {
            return Result.error("获取状态失败: " + e.getMessage());
        }
    }

    @PostMapping("/resubmit")
    public Result<Void> resubmitVerification(@RequestAttribute("userId") Long userId, @RequestBody Map<String, Object> data) {
        try {
            // 查找现有认证记录
            VerificationRecord record = verificationRecordRepository.findByUserId(userId)
                    .orElse(new VerificationRecord());
            
            // 更新认证信息
            record.setUserId(userId);
            record.setRealName((String) data.get("name"));
            
            // 根据认证类型设置不同的字段
            String verificationType = (String) data.get("verificationType");
            if ("1".equals(verificationType)) {
                // 身份证认证
                record.setIdCard((String) data.get("idCard"));
                record.setIdCardFront((String) data.get("idCardFront"));
                record.setIdCardBack((String) data.get("idCardBack"));
                record.setType(VerificationRecord.Type.ID_CARD);
            } else if ("2".equals(verificationType)) {
                // 学生证认证
                record.setStudentId((String) data.get("studentId"));
                record.setSchool((String) data.get("school"));
                record.setStudentCard((String) data.get("studentCard"));
                record.setType(VerificationRecord.Type.STUDENT_CARD);
            } else if ("3".equals(verificationType)) {
                // 教师证认证
                record.setTeacherId((String) data.get("teacherId"));
                record.setSchool((String) data.get("school"));
                record.setTeacherCard((String) data.get("teacherCard"));
                record.setType(VerificationRecord.Type.TEACHER_CARD);
            }
            
            record.setStatus(VerificationRecord.Status.PENDING);
            
            // 保存到数据库
            verificationRecordRepository.save(record);
            
            return Result.success("重新提交成功", null);
        } catch (Exception e) {
            return Result.error("重新提交失败: " + e.getMessage());
        }
    }
    
    private String getStatusMessage(VerificationRecord.Status status) {
        switch (status) {
            case PENDING:
                return "审核中";
            case APPROVED:
                return "已通过";
            case REJECTED:
                return "已拒绝";
            default:
                return "未知状态";
        }
    }
}
