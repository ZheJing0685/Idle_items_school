package com.idleitems.school.util;

import com.idleitems.school.util.storage.StorageAdapter;
import com.idleitems.school.util.storage.StorageServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 图片工具类
 * 处理图片上传、验证、处理等操作
 */
@Component
public class ImageUtil {

    private final StorageServiceFactory storageServiceFactory;
    private final FileValidationService fileValidationService;
    private final ImageProcessingService imageProcessingService;

    @Autowired
    public ImageUtil(StorageServiceFactory storageServiceFactory) {
        this.storageServiceFactory = storageServiceFactory;
        this.fileValidationService = new FileValidationService();
        this.imageProcessingService = new ImageProcessingService();
    }

    /**
     * 上传图片
     * @param file 图片文件
     * @return 上传结果
     * @throws Exception 上传异常
     */
    public Map<String, Object> uploadImage(MultipartFile file) throws Exception {
        // 验证图片
        fileValidationService.validateImage(file);

        // 获取存储适配器
        StorageAdapter storageAdapter = storageServiceFactory.getStorageAdapter();

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = fileValidationService.getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "." + extension;
        String contentType = file.getContentType();

        // 处理图片
        File tempFile = File.createTempFile("temp", "." + extension);
        file.transferTo(tempFile);

        try {
            // 处理图片
            File processedFile = File.createTempFile("processed", "." + extension);
            try {
                ImageProcessingService.ImageInfo imageInfo = imageProcessingService.processImage(
                        tempFile, processedFile, extension
                );

                // 上传图片
                Map<String, Object> storageResult = storageAdapter.upload(
                        processedFile, fileName, contentType
                );

                // 构建返回结果
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

    /**
     * 上传图片（使用字节数组）
     * @param imageBytes 图片字节数组
     * @param originalFilename 原始文件名
     * @return 上传结果
     * @throws Exception 上传异常
     */
    public Map<String, Object> uploadImage(byte[] imageBytes, String originalFilename) throws Exception {
        // 获取存储适配器
        StorageAdapter storageAdapter = storageServiceFactory.getStorageAdapter();

        // 生成文件名
        String extension = fileValidationService.getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "." + extension;
        String contentType = getContentTypeByExtension(extension);

        // 处理图片
        ImageProcessingService.ProcessedImageResult processedResult = imageProcessingService.processImage(
                imageBytes, extension
        );

        // 上传图片
        try (InputStream inputStream = new java.io.ByteArrayInputStream(processedResult.getImageBytes())) {
            Map<String, Object> storageResult = storageAdapter.upload(
                    inputStream, fileName, contentType
            );

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("url", storageResult.get("url"));
            result.put("path", storageResult.get("path"));
            result.put("fileName", fileName);
            result.put("originalName", originalFilename);
            result.put("width", processedResult.getImageInfo().getWidth());
            result.put("height", processedResult.getImageInfo().getHeight());
            result.put("size", processedResult.getImageInfo().getSize());
            result.put("format", processedResult.getImageInfo().getFormat());

            return result;
        }
    }

    /**
     * 根据扩展名获取内容类型
     * @param extension 文件扩展名
     * @return 内容类型
     */
    private String getContentTypeByExtension(String extension) {
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "webp":
                return "image/webp";
            default:
                return "image/jpeg";
        }
    }

    /**
     * 删除图片
     * @param filePath 文件路径
     * @return 是否删除成功
     * @throws Exception 删除异常
     */
    public boolean deleteImage(String filePath) throws Exception {
        StorageAdapter storageAdapter = storageServiceFactory.getStorageAdapter();
        return storageAdapter.delete(filePath);
    }

    /**
     * 获取图片URL
     * @param filePath 文件路径
     * @return 图片URL
     */
    public String getImageUrl(String filePath) {
        StorageAdapter storageAdapter = storageServiceFactory.getStorageAdapter();
        return storageAdapter.getFileUrl(filePath);
    }

    /**
     * 检查图片是否存在
     * @param filePath 文件路径
     * @return 是否存在
     */
    public boolean exists(String filePath) {
        StorageAdapter storageAdapter = storageServiceFactory.getStorageAdapter();
        return storageAdapter.exists(filePath);
    }
}
