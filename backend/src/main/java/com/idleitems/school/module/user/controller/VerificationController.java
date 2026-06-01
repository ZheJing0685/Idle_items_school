package com.idleitems.school.module.user.controller;

import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.module.user.dto.SubmitVerificationRequest;
import com.idleitems.school.module.file.service.FileService;
import com.idleitems.school.module.user.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "实名认证", description = "用户实名认证相关接口")
@RestController
@RequestMapping(ApiPaths.Verification.BASE)
@RequiredArgsConstructor
public class VerificationController {

    private final FileService fileService;
    private final VerificationService verificationService;

    @Operation(summary = "上传认证图片", description = "上传实名认证所需的证件图片")
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        Map<String, Object> result = fileService.uploadImage(file);
        return Result.success("上传成功", result);
    }

    @Operation(summary = "提交实名认证", description = "提交实名认证信息（身份证/学生证/教师证）")
    @PostMapping("/submit")
    public Result<Void> submitVerification(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody SubmitVerificationRequest request) {
        verificationService.submit(userId, request);
        return Result.success("提交成功", null);
    }

    @Operation(summary = "获取认证状态", description = "查询当前用户的实名认证审核状态")
    @GetMapping("/status")
    public Result<Map<String, Object>> getVerificationStatus(@RequestAttribute("userId") Long userId) {
        Map<String, Object> status = verificationService.getStatus(userId);
        return Result.success("获取状态成功", status);
    }

    @Operation(summary = "重新提交认证", description = "认证被驳回后重新提交认证信息")
    @PostMapping("/resubmit")
    public Result<Void> resubmitVerification(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody SubmitVerificationRequest request) {
        verificationService.submit(userId, request);
        return Result.success("重新提交成功", null);
    }
}
