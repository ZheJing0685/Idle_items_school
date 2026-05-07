package com.idleitems.school.service.impl;

import com.idleitems.school.service.FileService;
import com.idleitems.school.util.FileValidationService;
import com.idleitems.school.util.ImageProcessingService;
import com.idleitems.school.util.storage.StorageAdapter;
import com.idleitems.school.util.storage.StorageServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload-path}")
    private String uploadPath;

    private final StorageServiceFactory storageServiceFactory;
    private final FileValidationService fileValidationService;
    private final ImageProcessingService imageProcessingService;

    public FileServiceImpl(StorageServiceFactory storageServiceFactory,
                           FileValidationService fileValidationService,
                           ImageProcessingService imageProcessingService) {
        this.storageServiceFactory = storageServiceFactory;
        this.fileValidationService = fileValidationService;
        this.imageProcessingService = imageProcessingService;
    }

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(".jpg", ".jpeg", ".png", ".webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Override
    public Map<String, Object> uploadImage(MultipartFile file) throws Exception {
        fileValidationService.validateImage(file);

        StorageAdapter storageAdapter = storageServiceFactory.getStorageAdapter();

        String originalFilename = file.getOriginalFilename();
        String extension = fileValidationService.getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "." + extension;
        String contentType = file.getContentType();

        File tempFile = File.createTempFile("temp", "." + extension);
        file.transferTo(tempFile);

        try {
            File processedFile = File.createTempFile("processed", "." + extension);
            try {
                ImageProcessingService.ImageInfo imageInfo = imageProcessingService.processImage(
                        tempFile, processedFile, extension
                );

                Map<String, Object> storageResult = storageAdapter.upload(
                        processedFile, fileName, contentType
                );

                Map<String, Object> result = new HashMap<>();
                result.put("url", storageResult.get("url"));
                result.put("path", storageResult.get("path"));
                result.put("fileName", fileName);
                result.put("originalName", originalFilename);
                result.put("width", imageInfo.getWidth());
                result.put("height", imageInfo.getHeight());
                result.put("size", imageInfo.getSize());
                result.put("format", imageInfo.getFormat());

                return result;
            } finally {
                processedFile.delete();
            }
        } finally {
            tempFile.delete();
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String directory) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !isAllowedFileType(fileName)) {
            throw new IllegalArgumentException("只支持jpg、jpeg、png、webp格式的图片");
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadDir = Paths.get(uploadPath, directory, datePath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String uniqueFileName = UUID.randomUUID().toString() + getFileExtension(fileName);
        Path filePath = uploadDir.resolve(uniqueFileName);

        Files.write(filePath, file.getBytes());

        return String.format("%s/%s/%s", directory, datePath, uniqueFileName);
    }

    @Override
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        try {
            Path path = Paths.get(uploadPath, filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("删除文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<String> validateFiles(List<MultipartFile> files) throws IOException {
        List<String> errors = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            errors.add("请选择文件");
            return errors;
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                errors.add("文件不能为空");
                continue;
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                errors.add("文件大小不能超过5MB");
                continue;
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || !isAllowedFileType(fileName)) {
                errors.add("只支持jpg、jpeg、png、webp格式的图片");
            }
        }

        return errors;
    }

    @Override
    public String getFileUrl(String fileName) {
        return "/uploads/" + fileName;
    }

    @Override
    public File getFile(String filePath) {
        Path path = Paths.get(uploadPath, filePath);
        return path.toFile();
    }

    @Override
    public long getFileSize(String filePath) {
        try {
            Path path = Paths.get(uploadPath, filePath);
            return Files.size(path);
        } catch (IOException e) {
            log.error("获取文件大小失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    @Override
    public boolean isAllowedFileType(String fileName) {
        if (fileName == null) {
            return false;
        }
        String extension = getFileExtension(fileName).toLowerCase();
        return ALLOWED_IMAGE_TYPES.contains(extension);
    }

    @Override
    public long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }
}
