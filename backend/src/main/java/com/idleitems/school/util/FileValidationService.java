package com.idleitems.school.util;

import com.idleitems.school.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileValidationService {

    private final ConfigService configService;

    private static final String CONFIG_MAX_FILE_SIZE = "file_max_size";
    private static final String CONFIG_ALLOWED_FILE_TYPES = "file_allowed_types";

    private static final long DEFAULT_MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> DEFAULT_ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "webp"));
    private static final Set<String> DEFAULT_ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp"
    ));

    /**
     * 获取最大文件大小
     */
    private long getMaxFileSize() {
        Long maxSize = configService.getConfigLong(CONFIG_MAX_FILE_SIZE);
        return maxSize != null ? maxSize : DEFAULT_MAX_FILE_SIZE;
    }

    /**
     * 获取允许的文件扩展名
     */
    private Set<String> getAllowedExtensions() {
        String allowedTypes = configService.getConfigValue(CONFIG_ALLOWED_FILE_TYPES);
        if (allowedTypes != null && !allowedTypes.isEmpty()) {
            Set<String> extensions = Arrays.stream(allowedTypes.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(java.util.stream.Collectors.toSet());
            return extensions;
        }
        return DEFAULT_ALLOWED_EXTENSIONS;
    }

    /**
     * 验证图片文件
     * @param file 图片文件
     * @throws IllegalArgumentException 验证失败异常
     * @throws IOException IO异常
     */
    public void validateImage(MultipartFile file) throws IllegalArgumentException, IOException {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件大小
        long maxSize = getMaxFileSize();
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过" + (maxSize / 1024 / 1024) + "MB");
        }

        // 检查文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 检查文件名安全性
        if (originalFilename.contains("../") || originalFilename.contains("..\\")) {
            throw new IllegalArgumentException("文件名包含非法字符");
        }

        // 检查文件扩展名
        String extension = getFileExtension(originalFilename).toLowerCase();
        Set<String> allowedExtensions = getAllowedExtensions();
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("文件类型不支持，仅支持" + String.join("、", allowedExtensions).toUpperCase() + "格式");
        }

        // 检查文件内容类型
        String contentType = file.getContentType();
        if (contentType == null || !DEFAULT_ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("文件类型不支持，仅支持JPG、PNG、WebP格式");
        }

        // 验证文件是否为有效的图片
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IllegalArgumentException("无效的图片文件");
            }
        }
    }

    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 扩展名
     */
    public String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * 验证文件大小
     * @param size 文件大小
     * @throws IllegalArgumentException 大小验证失败异常
     */
    public void validateFileSize(long size) throws IllegalArgumentException {
        long maxSize = getMaxFileSize();
        if (size > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过" + (maxSize / 1024 / 1024) + "MB");
        }
    }

    /**
     * 验证文件类型
     * @param extension 文件扩展名
     * @param contentType 文件内容类型
     * @throws IllegalArgumentException 类型验证失败异常
     */
    public void validateFileType(String extension, String contentType) throws IllegalArgumentException {
        Set<String> allowedExtensions = getAllowedExtensions();
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("文件类型不支持，仅支持" + String.join("、", allowedExtensions).toUpperCase() + "格式");
        }

        if (contentType == null || !DEFAULT_ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("文件类型不支持，仅支持JPG、PNG、WebP格式");
        }
    }
}
