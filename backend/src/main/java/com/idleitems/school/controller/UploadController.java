package com.idleitems.school.controller;

import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.common.Result;
import com.idleitems.school.service.FileService;
import com.idleitems.school.util.FileValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "文件上传", description = "通用文件上传接口")
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final FileService fileService;
    private final FileValidationService fileValidationService;

    public UploadController(FileService fileService, FileValidationService fileValidationService) {
        this.fileService = fileService;
        this.fileValidationService = fileValidationService;
    }

    @Operation(summary = "上传图片", description = "支持头像、分类图标等通用图片上传")
    @PostMapping
    public Result<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false) String category) {

        if (file.isEmpty()) {
            return Result.error(ErrorCode.VALIDATION_ERROR, "文件不能为空");
        }

        try {
            fileValidationService.validateImage(file);
        } catch (IllegalArgumentException e) {
            return Result.error(ErrorCode.VALIDATION_ERROR, "文件验证失败：" + e.getMessage());
        } catch (Exception e) {
            return Result.error(ErrorCode.VALIDATION_ERROR, "文件验证失败");
        }

        try {
            Map<String, Object> result = fileService.uploadImage(file);

            Map<String, Object> response = new HashMap<>(result);
            if (category != null) {
                response.put("category", category);
            }

            return Result.success(response);
        } catch (Exception e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
