package com.idleitems.school.module.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FileService {

    Map<String, Object> uploadImage(MultipartFile file) throws Exception;

    Map<String, Object> uploadChatMedia(MultipartFile file) throws Exception;

    String uploadFile(MultipartFile file, String directory) throws IOException;

    boolean deleteFile(String filePath);
    
    List<String> validateFiles(List<MultipartFile> files) throws IOException;
    
    String getFileUrl(String fileName);
    
    File getFile(String filePath);
    
    long getFileSize(String filePath);
    
    String getFileExtension(String fileName);
    
    boolean isAllowedFileType(String fileName);
    
    long getMaxFileSize();
}
