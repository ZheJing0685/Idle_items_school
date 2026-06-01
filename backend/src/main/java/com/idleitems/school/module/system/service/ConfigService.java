package com.idleitems.school.module.system.service;

import com.idleitems.school.module.system.entity.SystemConfig;
import com.idleitems.school.module.system.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CONFIG_CACHE_PREFIX = "config:";
    private static final String CONFIG_ALL_CACHE_KEY = "config:all";
    private static final long CONFIG_CACHE_TTL_HOURS = 1;
    private static final String NULL_SENTINEL = "NULL_SENTINEL";
    private static final long NULL_CACHE_TTL_MINUTES = 5;

    /**
     * 获取所有配置
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAllConfigs() {
        Object cached = redisTemplate.opsForValue().get(CONFIG_ALL_CACHE_KEY);
        if (cached instanceof Map) {
            return (Map<String, Object>) cached;
        }

        List<SystemConfig> configs = systemConfigRepository.findAll();
        Map<String, Object> result = configs.stream()
                .collect(Collectors.toMap(
                        SystemConfig::getConfigKey,
                        config -> parseConfigValue(config.getConfigValue(), config.getConfigType()),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        redisTemplate.opsForValue().set(CONFIG_ALL_CACHE_KEY, result, CONFIG_CACHE_TTL_HOURS, TimeUnit.HOURS);
        return result;
    }

    /**
     * 获取指定配置值
     */
    public Object getConfig(String configKey) {
        String cacheKey = CONFIG_CACHE_PREFIX + configKey;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            if (NULL_SENTINEL.equals(cached)) {
                return null;
            }
            return cached;
        }

        Optional<SystemConfig> configOpt = systemConfigRepository.findByConfigKey(configKey);
        if (configOpt.isPresent()) {
            SystemConfig config = configOpt.get();
            Object value = parseConfigValue(config.getConfigValue(), config.getConfigType());
            redisTemplate.opsForValue().set(cacheKey, value, CONFIG_CACHE_TTL_HOURS, TimeUnit.HOURS);
            return value;
        }

        redisTemplate.opsForValue().set(cacheKey, NULL_SENTINEL, NULL_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return null;
    }

    /**
     * 获取配置值（字符串）
     */
    public String getConfigValue(String configKey) {
        Object value = getConfig(configKey);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取配置值（整数）
     */
    public Integer getConfigInt(String configKey) {
        Object value = getConfig(configKey);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取配置值（长整数）
     */
    public Long getConfigLong(String configKey) {
        Object value = getConfig(configKey);
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取配置值（布尔）
     */
    public Boolean getConfigBoolean(String configKey) {
        Object value = getConfig(configKey);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }

    /**
     * 获取配置值（浮点数）
     */
    public Float getConfigFloat(String configKey) {
        Object value = getConfig(configKey);
        if (value instanceof Float) {
            return (Float) value;
        } else if (value instanceof Double) {
            return ((Double) value).floatValue();
        } else if (value instanceof String) {
            try {
                return Float.parseFloat((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取指定分组的配置
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getConfigsByGroup(String groupName) {
        String cacheKey = CONFIG_CACHE_PREFIX + "group:" + groupName;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Map) {
            return (Map<String, Object>) cached;
        }

        List<SystemConfig> configs = systemConfigRepository.findAll();
        Map<String, Object> result = configs.stream()
                .filter(config -> groupName.equals(config.getGroupName()))
                .collect(Collectors.toMap(
                        SystemConfig::getConfigKey,
                        config -> parseConfigValue(config.getConfigValue(), config.getConfigType()),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        redisTemplate.opsForValue().set(cacheKey, result, CONFIG_CACHE_TTL_HOURS, TimeUnit.HOURS);
        return result;
    }

    /**
     * 保存或更新配置
     */
    @Transactional
    public SystemConfig saveConfig(String configKey, String configValue, String description) {
        Optional<SystemConfig> existingConfig = systemConfigRepository.findByConfigKey(configKey);
        
        SystemConfig config;
        if (existingConfig.isPresent()) {
            config = existingConfig.get();
            config.setConfigValue(configValue);
            if (description != null) {
                config.setDescription(description);
            }
        } else {
            config = new SystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setDescription(description);
            config.setConfigType(1); // 默认文本类型
            config.setIsEditable(true);
            config.setGroupName("general");
            config.setSortOrder(0);
        }

        SystemConfig savedConfig = systemConfigRepository.save(config);
        clearConfigCache();
        log.info("配置 {} 已保存/更新", configKey);
        return savedConfig;
    }

    /**
     * 删除配置
     */
    @Transactional
    public boolean deleteConfig(String configKey) {
        Optional<SystemConfig> config = systemConfigRepository.findByConfigKey(configKey);
        if (config.isPresent()) {
            systemConfigRepository.delete(config.get());
            clearConfigCache();
            log.info("配置 {} 已删除", configKey);
            return true;
        }
        return false;
    }

    /**
     * 清除配置缓存
     * 使用SCAN命令替代KEYS命令，避免阻塞Redis
     */
    public void clearConfigCache() {
        ScanOptions options = ScanOptions.scanOptions()
                .match(CONFIG_CACHE_PREFIX + "*")
                .count(100)
                .build();
        
        List<String> keysToDelete = new ArrayList<>();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keysToDelete.add(cursor.next());
            }
        }
        
        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }
        redisTemplate.delete(CONFIG_ALL_CACHE_KEY);
        log.info("配置缓存已清除，共清除{}个缓存键", keysToDelete.size());
    }

    /**
     * 重新加载配置缓存
     */
    public void reloadConfigCache() {
        clearConfigCache();
        getAllConfigs();
        log.info("配置缓存已重新加载");
    }

    private Object parseConfigValue(String value, Integer configType) {
        if (value == null) {
            return null;
        }

        switch (configType) {
            case 2: // 数字
                try {
                    if (value.contains(".")) {
                        return Double.parseDouble(value);
                    }
                    return Long.parseLong(value);
                } catch (NumberFormatException e) {
                    return value;
                }
            case 3: // 布尔
                return Boolean.parseBoolean(value);
            case 4: // JSON
                return value; // 返回原始JSON字符串，由调用方解析
            default: // 文本
                return value;
        }
    }
}