package com.idleitems.school.util.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 本地存储适配器
 * 实现本地文件系统的存储操作
 */
@Component
public class LocalStorageAdapter implements StorageAdapter {

    @Value("${file.upload-path}")
    private String baseUploadPath;

    @Value("${file.base-url:http://localhost:7000}")
    private String baseUrl;

    @Override
    public Map<String, Object> upload(InputStream inputStream, String fileName, String contentType) throws Exception {
        // 生成日期路径
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = datePath + "/" + fileName;
        String fullPath = baseUploadPath + relativePath;

        // 创建目录
        Path path = Paths.get(fullPath);
        Files.createDirectories(path.getParent());

        // 写入文件
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("path", relativePath);
        result.put("url", getFileUrl(relativePath));
        result.put("size", Files.size(path));
        result.put("contentType", contentType);

        return result;
    }

    @Override
    public Map<String, Object> upload(File file, String fileName, String contentType) throws Exception {
        // 生成日期路径
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = datePath + "/" + fileName;
        String fullPath = baseUploadPath + relativePath;

        // 创建目录
        Path path = Paths.get(fullPath);
        Files.createDirectories(path.getParent());

        // 复制文件
        Files.copy(file.toPath(), path);

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("path", relativePath);
        result.put("url", getFileUrl(relativePath));
        result.put("size", Files.size(path));
        result.put("contentType", contentType);

        return result;
    }

    @Override
    public boolean delete(String filePath) throws Exception {
        Path path = Paths.get(baseUploadPath + filePath);
        return Files.deleteIfExists(path);
    }

    @Override
    public String getFileUrl(String filePath) {
        return baseUrl + "/uploads/" + filePath;
    }

    @Override
    public boolean exists(String filePath) {
        Path path = Paths.get(baseUploadPath + filePath);
        return Files.exists(path);
    }
}
