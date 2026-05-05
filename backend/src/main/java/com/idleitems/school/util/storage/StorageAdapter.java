package com.idleitems.school.util.storage;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * 存储适配器接口
 * 支持不同的存储实现，如本地文件系统、云存储等
 */
public interface StorageAdapter {

    /**
     * 上传文件
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param contentType 文件类型
     * @return 存储结果，包含文件路径、URL等信息
     * @throws Exception 存储异常
     */
    Map<String, Object> upload(InputStream inputStream, String fileName, String contentType) throws Exception;

    /**
     * 上传文件
     * @param file 文件对象
     * @param fileName 文件名
     * @param contentType 文件类型
     * @return 存储结果，包含文件路径、URL等信息
     * @throws Exception 存储异常
     */
    Map<String, Object> upload(File file, String fileName, String contentType) throws Exception;

    /**
     * 删除文件
     * @param filePath 文件路径
     * @return 是否删除成功
     * @throws Exception 删除异常
     */
    boolean delete(String filePath) throws Exception;

    /**
     * 获取文件URL
     * @param filePath 文件路径
     * @return 文件URL
     */
    String getFileUrl(String filePath);

    /**
     * 检查文件是否存在
     * @param filePath 文件路径
     * @return 是否存在
     */
    boolean exists(String filePath);
}
