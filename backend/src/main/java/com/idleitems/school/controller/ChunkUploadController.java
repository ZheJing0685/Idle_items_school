package com.idleitems.school.controller;

import com.idleitems.school.common.ErrorCode;
import com.idleitems.school.common.Result;
import com.idleitems.school.service.ChunkUploadService;
import com.idleitems.school.service.FileService;
import com.idleitems.school.util.FileValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Tag(name = "分片上传", description = "大文件分片上传接口")
@RestController
@RequestMapping("/api/items/upload")
public class ChunkUploadController {

    private final ChunkUploadService chunkUploadService;
    private final FileService fileService;
    private final FileValidationService fileValidationService;

    public ChunkUploadController(ChunkUploadService chunkUploadService,
                                  FileService fileService,
                                  FileValidationService fileValidationService) {
        this.chunkUploadService = chunkUploadService;
        this.fileService = fileService;
        this.fileValidationService = fileValidationService;
    }

    @Operation(summary = "上传分片")
    @PostMapping("/chunk")
    public Result<Map<String, Object>> uploadChunk(
            @RequestParam("fileHash") String fileHash,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("originalFileName") String originalFileName,
            @RequestParam("chunk") MultipartFile chunk) {

        try {
            chunkUploadService.saveChunk(fileHash, chunkIndex, chunk);

            List<Integer> uploaded = chunkUploadService.getUploadedChunks(fileHash);

            Map<String, Object> result = new HashMap<>();
            result.put("uploadedChunks", uploaded);
            result.put("isComplete", chunkUploadService.isUploadComplete(fileHash, totalChunks));

            return Result.success(result);
        } catch (Exception e) {
            log.error("分片上传失败", e);
            return Result.error("分片上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "检查已上传分片")
    @GetMapping("/check")
    public Result<Map<String, Object>> checkUploadedChunks(
            @RequestParam("fileHash") String fileHash,
            @RequestParam("totalChunks") int totalChunks) {

        try {
            List<Integer> uploaded = chunkUploadService.getUploadedChunks(fileHash);

            Map<String, Object> result = new HashMap<>();
            result.put("uploadedChunks", uploaded);
            result.put("isComplete", chunkUploadService.isUploadComplete(fileHash, totalChunks));

            return Result.success(result);
        } catch (Exception e) {
            log.error("检查已上传分片失败", e);
            return Result.error("检查失败: " + e.getMessage());
        }
    }

    @Operation(summary = "合并分片")
    @PostMapping("/complete")
    public Result<Map<String, Object>> completeUpload(
            @RequestParam("fileHash") String fileHash,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("originalFileName") String originalFileName) {

        try {
            if (!chunkUploadService.isUploadComplete(fileHash, totalChunks)) {
                return Result.error(ErrorCode.BAD_REQUEST, "分片不完整，请先上传所有分片");
            }

            File mergedFile = chunkUploadService.mergeChunks(fileHash, totalChunks, originalFileName);

            MultipartFile multipartFile = fileToMultipartFile(mergedFile, originalFileName);

            try {
                fileValidationService.validateImage(multipartFile);
            } catch (IllegalArgumentException e) {
                if (!mergedFile.delete()) {
                    log.warn("Failed to delete merged file: {}", mergedFile.getAbsolutePath());
                }
                return Result.error(ErrorCode.BAD_REQUEST, "合并后的文件验证失败: " + e.getMessage());
            }

            Map<String, Object> result = fileService.uploadImage(multipartFile);

            if (!mergedFile.delete()) {
                log.warn("Failed to delete merged file: {}", mergedFile.getAbsolutePath());
            }

            chunkUploadService.deleteChunks(fileHash);

            return Result.success(result);
        } catch (Exception e) {
            log.error("合并分片失败", e);
            return Result.error("合并失败: " + e.getMessage());
        }
    }

    private MultipartFile fileToMultipartFile(File file, String originalFileName) throws IOException {
        byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return originalFileName;
            }

            @Override
            public String getContentType() {
                String ext = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
                return switch (ext) {
                    case "jpg", "jpeg" -> "image/jpeg";
                    case "png" -> "image/png";
                    case "webp" -> "image/webp";
                    default -> "application/octet-stream";
                };
            }

            @Override
            public boolean isEmpty() {
                return fileBytes.length == 0;
            }

            @Override
            public long getSize() {
                return fileBytes.length;
            }

            @Override
            public byte[] getBytes() {
                return fileBytes;
            }

            @Override
            public java.io.InputStream getInputStream() {
                return new java.io.ByteArrayInputStream(fileBytes);
            }

            @Override
            public void transferTo(File dest) throws IOException {
                java.nio.file.Files.copy(file.toPath(), dest.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        };
    }
}
