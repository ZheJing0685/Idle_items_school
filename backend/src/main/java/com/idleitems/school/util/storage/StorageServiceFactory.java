package com.idleitems.school.util.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 存储服务工厂
 * 根据配置创建不同的存储适配器实例
 */
@Component
public class StorageServiceFactory {

    @Value("${storage.type:local}")
    private String storageType;

    @Autowired
    private LocalStorageAdapter localStorageAdapter;

    /**
     * 获取存储适配器
     * @return 存储适配器实例
     */
    public StorageAdapter getStorageAdapter() {
        switch (storageType.toLowerCase()) {
            case "local":
                return localStorageAdapter;
            // 后续可以添加云存储适配器
            // case "s3":
            //     return s3StorageAdapter;
            // case "oss":
            //     return ossStorageAdapter;
            default:
                return localStorageAdapter;
        }
    }

    /**
     * 获取存储适配器
     * @param type 存储类型
     * @return 存储适配器实例
     */
    public StorageAdapter getStorageAdapter(String type) {
        switch (type.toLowerCase()) {
            case "local":
                return localStorageAdapter;
            // 后续可以添加云存储适配器
            default:
                return localStorageAdapter;
        }
    }
}
