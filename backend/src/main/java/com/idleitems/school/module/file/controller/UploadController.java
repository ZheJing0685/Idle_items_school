package com.idleitems.school.module.file.controller;

import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.common.Result;
import com.idleitems.school.config.ApiPaths;
import com.idleitems.school.module.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Tag(name = "文件上传", description = "通用文件上传接口")
@RestController
@RequestMapping(ApiPaths.Upload.BASE)
@RequiredArgsConstructor
public class UploadController {

    private final FileService fileService;

    @Operation(summary = "上传图片", description = "支持头像、分类图标等通用图片上传")
    @PostMapping
    public Result<Map<String, Object>> upload(
            @RequestAttribute("userId") Long userId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return Result.error(ErrorCode.VALIDATION_ERROR, "文件不能为空");
        }

        try {
            Map<String, Object> result = fileService.uploadImage(file);
            result.put("uploadUserId", userId);
            return Result.success("上传成功", result);
        } catch (IllegalArgumentException e) {
            log.warn("文件上传验证失败: {}", e.getMessage());
            return Result.error(ErrorCode.FILE_TYPE_NOT_ALLOWED, "文件验证失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error(ErrorCode.FILE_UPLOAD_ERROR, "文件上传失败：" + e.getMessage());
        }
    }

    @Operation(summary = "上传聊天媒体", description = "支持聊天中的图片和视频文件上传")
    @PostMapping("/chat-media")
    public Result<Map<String, Object>> uploadChatMedia(
            @RequestAttribute("userId") Long userId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return Result.error(ErrorCode.VALIDATION_ERROR, "文件不能为空");
        }

        try {
            Map<String, Object> result = fileService.uploadChatMedia(file);
            result.put("uploadUserId", userId);
            return Result.success("上传成功", result);
        } catch (IllegalArgumentException e) {
            log.warn("聊天媒体上传验证失败: {}", e.getMessage());
            return Result.error(ErrorCode.FILE_TYPE_NOT_ALLOWED, "文件验证失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("聊天媒体上传失败", e);
            return Result.error(ErrorCode.FILE_UPLOAD_ERROR, "文件上传失败：" + e.getMessage());
        }
    }
}
