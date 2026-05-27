package com.idleitems.school.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Controller
public class FileController {

    @Value("${file.upload-path}")
    private String uploadPath;

    @GetMapping("/uploads/**")
    public void serveFile(jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response) {
        try {
            String requestURI = request.getRequestURI();
            String relativePath = requestURI.substring("/uploads/".length());
            relativePath = URLDecoder.decode(relativePath, StandardCharsets.UTF_8);

            if (relativePath.contains("verification")) {
                response.sendError(403);
                return;
            }

            Path filePath = Paths.get(uploadPath, relativePath).normalize();
            if (!filePath.startsWith(Paths.get(uploadPath).normalize())) {
                response.sendError(403);
                return;
            }
            File file = filePath.toFile();

            if (!file.exists() || !file.isFile()) {
                log.warn("File not found: {}", filePath);
                response.sendError(404);
                return;
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            response.setContentType(contentType);
            response.setHeader("Cache-Control", "private, no-cache, max-age=0");
            response.setContentLengthLong(file.length());

            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (Exception e) {
            log.error("Error serving file: {}", e.getMessage());
            try {
                response.sendError(500);
            } catch (Exception ignored) {
            }
        }
    }
}
