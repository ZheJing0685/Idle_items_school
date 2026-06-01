package com.idleitems.school.service.impl;

import com.idleitems.school.service.ConfigService;
import com.idleitems.school.service.FileService;
import com.idleitems.school.util.FileValidationService;
import com.idleitems.school.util.ImageProcessingService;
import com.idleitems.school.util.storage.StorageAdapter;
import com.idleitems.school.util.storage.StorageServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.base-url:#{null}}")
    private String baseUrl;

    private final StorageServiceFactory storageServiceFactory;
    private final FileValidationService fileValidationService;
    private final ImageProcessingService imageProcessingService;
    private final ConfigService configService;

    private static final String CONFIG_MAX_FILE_SIZE = "file_max_size";
    private static final String CONFIG_ALLOWED_FILE_TYPES = "file_allowed_types";

    private static final long DEFAULT_MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> DEFAULT_ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".webp"));

    @Override
    public Map<String, Object> uploadImage(MultipartFile file) throws Exception {
        FileValidationService.ImageValidationResult validation = fileValidationService.validateImage(file);

        StorageAdapter storageAdapter = storageServiceFactory.getStorageAdapter();

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "unknown";
        }
        String extension = validation.getExtension();
        String fileName = UUID.randomUUID().toString() + "." + extension;
        String contentType = file.getContentType();

        Path tempDir = Files.createTempDirectory("upload_");
        try {
            Path tempFile = tempDir.resolve("original." + extension);
            file.transferTo(tempFile.toFile());

            Path processedFile = tempDir.resolve("processed." + extension);
            ImageProcessingService.ImageInfo imageInfo = imageProcessingService.processImage(
                    tempFile.toFile(), processedFile.toFile(), extension,
                    validation.getWidth(), validation.getHeight()
            );

            Map<String, Object> storageResult = storageAdapter.upload(
                    processedFile.toFile(), fileName, contentType
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
            safeDeleteDirectory(tempDir);
        }
    }

    /**
     * 安全删除临时目录，记录删除失败的文件
     */
    private void safeDeleteDirectory(Path dir) {
        try {
            Files.walk(dir)
                 .sorted(Comparator.reverseOrder())
                 .forEach(p -> {
                     try {
                         Files.deleteIfExists(p);
                     } catch (IOException e) {
                         log.warn("临时文件删除失败: {}, 原因: {}", p.getFileName(), e.getMessage());
                     }
                 });
        } catch (IOException e) {
            log.error("遍历临时目录失败: {}", dir, e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String directory) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        long maxSize = getMaxFileSize();
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过" + (maxSize / 1024 / 1024) + "MB");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !isAllowedFileType(fileName)) {
            Set<String> allowedExtensions = getAllowedExtensions();
            throw new IllegalArgumentException("只支持" + allowedExtensions.stream()
                    .map(ext -> ext.replace(".", "").toUpperCase())
                    .collect(Collectors.joining("、")) + "格式的图片");
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadDir = Paths.get(uploadPath, directory, datePath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String uniqueFileName = UUID.randomUUID().toString() + getFileExtension(fileName);
        Path filePath = uploadDir.resolve(uniqueFileName);

        try (InputStream inputStream = file.getInputStream()) {
            if (ImageIO.read(inputStream) == null) {
                throw new IllegalArgumentException("无效的图片文件");
            }
        }

        file.transferTo(filePath.toFile());

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

        long maxSize = getMaxFileSize();
        Set<String> allowedExtensions = getAllowedExtensions();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                errors.add("文件不能为空");
                continue;
            }

            if (file.getSize() > maxSize) {
                errors.add("文件大小不能超过" + (maxSize / 1024 / 1024) + "MB");
                continue;
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || !isAllowedFileType(fileName)) {
                errors.add("只支持" + allowedExtensions.stream()
                        .map(ext -> ext.replace(".", "").toUpperCase())
                        .collect(Collectors.joining("、")) + "格式的图片");
            }
        }

        return errors;
    }

    /**
     * 获取文件访问URL
     * 支持配置完整的base URL（如CDN地址）
     */
    @Override
    public String getFileUrl(String fileName) {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return baseUrl.endsWith("/") ? baseUrl + fileName : baseUrl + "/" + fileName;
        }
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
        Set<String> allowedExtensions = getAllowedExtensions();
        return allowedExtensions.contains(extension);
    }

    @Override
    public long getMaxFileSize() {
        Long maxSize = configService.getConfigLong(CONFIG_MAX_FILE_SIZE);
        return maxSize != null ? maxSize : DEFAULT_MAX_FILE_SIZE;
    }

    private Set<String> getAllowedExtensions() {
        String allowedTypes = configService.getConfigValue(CONFIG_ALLOWED_FILE_TYPES);
        if (allowedTypes != null && !allowedTypes.isEmpty()) {
            Set<String> extensions = Arrays.stream(allowedTypes.split(","))
                    .map(String::trim)
                    .map(ext -> ext.startsWith(".") ? ext : "." + ext)
                    .map(String::toLowerCase)
                    .collect(java.util.stream.Collectors.toSet());
            return extensions;
        }
        return DEFAULT_ALLOWED_EXTENSIONS;
    }
}
