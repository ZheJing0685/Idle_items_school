package com.idleitems.school.module.file.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChunkUploadService {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.chunk-size:1048576}")
    private long chunkSize;

    public String getChunkDir(String fileHash) {
        return uploadPath + "/chunks/" + fileHash;
    }

    public Path getChunkPath(String fileHash, int chunkIndex) {
        return Paths.get(getChunkDir(fileHash), String.valueOf(chunkIndex));
    }

    public void saveChunk(String fileHash, int chunkIndex, MultipartFile chunk) throws IOException {
        Path chunkPath = getChunkPath(fileHash, chunkIndex);
        Files.createDirectories(chunkPath.getParent());
        chunk.transferTo(chunkPath.toFile());
    }

    public List<Integer> getUploadedChunks(String fileHash) throws IOException {
        Path chunkDir = Paths.get(getChunkDir(fileHash));
        if (!Files.exists(chunkDir)) {
            return new ArrayList<>();
        }

        try (java.util.stream.Stream<Path> paths = Files.list(chunkDir)) {
            return paths.filter(Files::isRegularFile)
                .map(p -> Integer.parseInt(p.getFileName().toString()))
                .sorted()
                .collect(Collectors.toList());
        }
    }

    public boolean isUploadComplete(String fileHash, int totalChunks) throws IOException {
        List<Integer> uploaded = getUploadedChunks(fileHash);
        return uploaded.size() == totalChunks;
    }

    public File mergeChunks(String fileHash, int totalChunks, String originalFileName) throws IOException {
        String datePath = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + ext;

        String targetDir = uploadPath + "/" + datePath;
        Files.createDirectories(Paths.get(targetDir));

        Path targetPath = Paths.get(targetDir, newFileName);

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(targetPath))) {
            for (int i = 0; i < totalChunks; i++) {
                Path chunkPath = getChunkPath(fileHash, i);
                if (!Files.exists(chunkPath)) {
                    throw new IOException("分片 " + i + " 不存在");
                }
                Files.copy(chunkPath, out);
            }
        }

        Path chunkDir = Paths.get(getChunkDir(fileHash));
        if (Files.exists(chunkDir)) {
            try (var stream = Files.walk(chunkDir)) {
                stream.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try { Files.deleteIfExists(path); } catch (IOException e) {
                            log.warn("Failed to delete chunk: {}", path, e);
                        }
                    });
            }
        }

        return targetPath.toFile();
    }

    public void deleteChunks(String fileHash) throws IOException {
        Path chunkDir = Paths.get(getChunkDir(fileHash));
        if (Files.exists(chunkDir)) {
            try (var stream = Files.walk(chunkDir)) {
                stream.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try { Files.deleteIfExists(path); } catch (IOException e) {
                            log.warn("Failed to delete chunk: {}", path, e);
                        }
                    });
            }
        }
    }
}
