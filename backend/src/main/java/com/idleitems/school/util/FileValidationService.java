package com.idleitems.school.util;

import com.idleitems.school.module.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileValidationService {

    private final ConfigService configService;

    private static final String CONFIG_MAX_FILE_SIZE = "file_max_size";
    private static final String CONFIG_ALLOWED_FILE_TYPES = "file_allowed_types";
    private static final String CONFIG_ALLOWED_CONTENT_TYPES = "file_allowed_content_types";

    private static final long DEFAULT_MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long DEFAULT_MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB
    private static final Set<String> DEFAULT_ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png"));
    private static final Set<String> DEFAULT_ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg",
            "image/png"
    ));

    /**
     * 文件魔数(Magic Bytes)签名映射
     * 用于验证文件实际内容是否与声明的类型一致
     */
    private static final Map<String, byte[]> MAGIC_BYTES = new HashMap<>();
    static {
        // JPEG: FF D8 FF
        MAGIC_BYTES.put("jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        MAGIC_BYTES.put("jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        MAGIC_BYTES.put("png", new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47,
                (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A});
        // WebP: RIFF....WEBP
        MAGIC_BYTES.put("webp", new byte[]{(byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46});
        // MP4: ftyp box (00 00 00 XX 66 74 79 70)
        MAGIC_BYTES.put("mp4", new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00});
        // MOV: ftyp (00 00 00 XX 66 74 79 70)
        MAGIC_BYTES.put("mov", new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00});
    }

    private long getMaxFileSize() {
        Long maxSize = configService.getConfigLong(CONFIG_MAX_FILE_SIZE);
        return maxSize != null ? maxSize : DEFAULT_MAX_FILE_SIZE;
    }

    private Set<String> getAllowedContentTypes() {
        String allowedTypes = configService.getConfigValue(CONFIG_ALLOWED_CONTENT_TYPES);
        if (allowedTypes != null && !allowedTypes.isEmpty()) {
            Set<String> types = Arrays.stream(allowedTypes.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(java.util.stream.Collectors.toSet());
            return types;
        }
        return DEFAULT_ALLOWED_CONTENT_TYPES;
    }

    private Set<String> getAllowedExtensions() {
        String allowedTypes = configService.getConfigValue(CONFIG_ALLOWED_FILE_TYPES);
        if (allowedTypes != null && !allowedTypes.isEmpty()) {
            Set<String> extensions = Arrays.stream(allowedTypes.split(","))
                    .map(String::trim)
                    .map(ext -> ext.startsWith(".") ? ext.substring(1) : ext)
                    .map(String::toLowerCase)
                    .collect(java.util.stream.Collectors.toSet());
            return extensions;
        }
        return DEFAULT_ALLOWED_EXTENSIONS;
    }

    /**
     * 验证图片文件并返回图片信息
     * 包含：扩展名验证、MIME类型验证、Magic Byte验证、图片可读性验证
     */
    public ImageValidationResult validateImage(MultipartFile file) throws IllegalArgumentException, IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        long maxSize = getMaxFileSize();
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过" + (maxSize / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String decodedFilename = URLDecoder.decode(originalFilename, StandardCharsets.UTF_8);
        if (decodedFilename.contains("../") || decodedFilename.contains("..\\")
                || decodedFilename.contains("..%2f") || decodedFilename.contains("..%5c")
                || decodedFilename.contains("..%252f")) {
            throw new IllegalArgumentException("文件名包含非法字符");
        }

        // 1. 扩展名验证
        String extension = getFileExtension(originalFilename).toLowerCase();
        Set<String> allowedExtensions = getAllowedExtensions();
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("文件类型不支持，仅支持" + String.join("、", allowedExtensions).toUpperCase() + "格式");
        }

        // 2. MIME类型验证
        String contentType = file.getContentType();
        if (contentType == null || !getAllowedContentTypes().contains(contentType)) {
            throw new IllegalArgumentException("文件类型不支持，仅支持JPG、PNG、WebP格式");
        }

        // 3. Magic Byte验证 - 验证文件实际内容与声明类型一致
        byte[] fileHeader = readFileHeader(file, 8);
        log.warn("文件校验: 扩展名={}, 文件大小={}, content-type={}, 前8字节={}", extension, file.getSize(), file.getContentType(), bytesToHex(fileHeader));
        if (!validateMagicBytes(extension, fileHeader)) {
            log.warn("文件类型伪造检测: 扩展名={}, 实际前8字节={}, 期望={}", extension, bytesToHex(fileHeader), magicBytesToHex(extension));
            throw new IllegalArgumentException("文件内容与声明的类型不匹配，请上传真实的图片文件");
        }

        // 4. 图片可读性验证
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IllegalArgumentException("无效的图片文件");
            }
            return new ImageValidationResult(image.getWidth(), image.getHeight(), extension);
        }
    }

    /**
     * 读取文件头字节
     */
    private byte[] readFileHeader(MultipartFile file, int length) throws IOException {
        byte[] header = new byte[length];
        try (InputStream is = file.getInputStream()) {
            int bytesRead = is.read(header, 0, length);
            if (bytesRead < length) {
                header = Arrays.copyOf(header, bytesRead);
            }
        }
        return header;
    }

    /**
     * 验证Magic Bytes是否匹配
     */
    private boolean validateMagicBytes(String extension, byte[] fileHeader) {
        byte[] expected = MAGIC_BYTES.get(extension);
        if (expected == null) {
            // 未配置魔数的类型，跳过验证
            return true;
        }
        if (fileHeader.length < expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (fileHeader[i] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    public static class ImageValidationResult {
        private final int width;
        private final int height;
        private final String extension;

        public ImageValidationResult(int width, int height, String extension) {
            this.width = width;
            this.height = height;
            this.extension = extension;
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public String getExtension() { return extension; }
    }

    public String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    public void validateFileSize(long size) throws IllegalArgumentException {
        long maxSize = getMaxFileSize();
        if (size > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过" + (maxSize / 1024 / 1024) + "MB");
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    private String magicBytesToHex(String extension) {
        byte[] magic = MAGIC_BYTES.get(extension);
        if (magic == null) return "未配置";
        return bytesToHex(magic);
    }

    public void validateFileType(String extension, String contentType) throws IllegalArgumentException {
        Set<String> allowedExtensions = getAllowedExtensions();
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("文件类型不支持，仅支持" + String.join("、", allowedExtensions).toUpperCase() + "格式");
        }

        if (contentType == null || !DEFAULT_ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("文件类型不支持，仅支持JPG、PNG、WebP格式");
        }
    }

    public VideoValidationResult validateVideo(MultipartFile file) throws IllegalArgumentException, IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (file.getSize() > DEFAULT_MAX_VIDEO_SIZE) {
            throw new IllegalArgumentException("视频文件大小不能超过" + (DEFAULT_MAX_VIDEO_SIZE / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        Set<String> videoExtensions = new HashSet<>(Arrays.asList("mp4", "mov", "avi", "webm", "mkv"));
        if (!videoExtensions.contains(extension)) {
            throw new IllegalArgumentException("视频格式不支持，仅支持 MP4、MOV、AVI、WebM、MKV 格式");
        }

        Set<String> videoContentTypes = new HashSet<>(Arrays.asList(
                "video/mp4", "video/quicktime", "video/x-msvideo",
                "video/webm", "video/x-matroska"
        ));
        String contentType = file.getContentType();
        if (contentType == null || !videoContentTypes.contains(contentType)) {
            throw new IllegalArgumentException("视频文件类型不匹配");
        }

        return new VideoValidationResult(extension);
    }

    public static class VideoValidationResult {
        private final String extension;

        public VideoValidationResult(String extension) {
            this.extension = extension;
        }

        public String getExtension() { return extension; }
    }
}
