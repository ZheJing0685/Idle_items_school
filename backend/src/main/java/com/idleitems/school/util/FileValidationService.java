package com.idleitems.school.util;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 文件验证服务
 * 验证文件的类型、大小等信息
 */
public class FileValidationService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "webp"));
    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp"
    ));

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
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
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
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("文件类型不支持，仅支持JPG、PNG、WebP格式");
        }

        // 检查文件内容类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
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
        if (size > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }
    }

    /**
     * 验证文件类型
     * @param extension 文件扩展名
     * @param contentType 文件内容类型
     * @throws IllegalArgumentException 类型验证失败异常
     */
    public void validateFileType(String extension, String contentType) throws IllegalArgumentException {
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("文件类型不支持，仅支持JPG、PNG、WebP格式");
        }

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("文件类型不支持，仅支持JPG、PNG、WebP格式");
        }
    }
}
