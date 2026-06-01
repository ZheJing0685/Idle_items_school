package com.idleitems.school.module.system.service;

import com.idleitems.school.module.system.entity.DictItem;
import com.idleitems.school.module.system.entity.DictType;
import com.idleitems.school.module.system.repository.DictItemRepository;
import com.idleitems.school.module.system.repository.DictTypeRepository;
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
public class DictService {

    private final DictTypeRepository dictTypeRepository;
    private final DictItemRepository dictItemRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DICT_CACHE_PREFIX = "dict:";
    private static final String DICT_ALL_CACHE_KEY = "dict:all";
    private static final long DICT_CACHE_TTL_HOURS = 24;
    private static final String NULL_SENTINEL = "NULL_SENTINEL";
    private static final long NULL_CACHE_TTL_MINUTES = 5;

    /**
     * 获取所有字典数据
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<Map<String, Object>>> getAllDicts() {
        Object cached = redisTemplate.opsForValue().get(DICT_ALL_CACHE_KEY);
        if (cached instanceof Map) {
            return (Map<String, List<Map<String, Object>>>) cached;
        }

        List<DictType> dictTypes = dictTypeRepository.findByStatusTrue();
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();

        for (DictType dictType : dictTypes) {
            List<DictItem> items = dictItemRepository.findByTypeCodeAndStatusTrue(dictType.getTypeCode());
            List<Map<String, Object>> itemList = items.stream()
                    .map(this::convertDictItemToMap)
                    .collect(Collectors.toList());
            result.put(dictType.getTypeCode(), itemList);
        }

        redisTemplate.opsForValue().set(DICT_ALL_CACHE_KEY, result, DICT_CACHE_TTL_HOURS, TimeUnit.HOURS);
        return result;
    }

    /**
     * 获取指定类型的字典数据
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getDictByType(String typeCode) {
        String cacheKey = DICT_CACHE_PREFIX + typeCode;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List) {
            return (List<Map<String, Object>>) cached;
        }

        List<DictItem> items = dictItemRepository.findByTypeCodeAndStatusTrue(typeCode);
        List<Map<String, Object>> result = items.stream()
                .map(this::convertDictItemToMap)
                .collect(Collectors.toList());

        redisTemplate.opsForValue().set(cacheKey, result, DICT_CACHE_TTL_HOURS, TimeUnit.HOURS);
        return result;
    }

    /**
     * 根据类型和值获取字典项标签
     */
    public String getDictLabel(String typeCode, String itemValue) {
        if (itemValue == null || itemValue.isEmpty()) {
            return "";
        }

        String cacheKey = DICT_CACHE_PREFIX + typeCode + ":" + itemValue;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof String) {
            if (NULL_SENTINEL.equals(cached)) {
                return null;
            }
            return (String) cached;
        }

        String label = dictItemRepository.findItemLabelByTypeCodeAndValue(typeCode, itemValue);
        if (label != null) {
            redisTemplate.opsForValue().set(cacheKey, label, DICT_CACHE_TTL_HOURS, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().set(cacheKey, NULL_SENTINEL, NULL_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        return label;
    }

    /**
     * 获取字典项列表（用于前端下拉框）
     */
    public List<Map<String, String>> getDictOptions(String typeCode) {
        List<DictItem> items = dictItemRepository.findByTypeCodeAndStatusTrue(typeCode);
        return items.stream()
                .collect(Collectors.toMap(
                        DictItem::getItemValue,
                        DictItem::getItemLabel,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, String> option = new LinkedHashMap<>();
                    option.put("value", entry.getKey());
                    option.put("label", entry.getValue());
                    return option;
                })
                .collect(Collectors.toList());
    }

    /**
     * 清除字典缓存
     * 使用SCAN命令替代KEYS命令，避免阻塞Redis
     */
    public void clearDictCache() {
        ScanOptions options = ScanOptions.scanOptions()
                .match(DICT_CACHE_PREFIX + "*")
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
        redisTemplate.delete(DICT_ALL_CACHE_KEY);
        log.info("字典缓存已清除，共清除{}个缓存键", keysToDelete.size());
    }

    /**
     * 重新加载指定类型的字典缓存
     */
    public void reloadDictCache(String typeCode) {
        String cacheKey = DICT_CACHE_PREFIX + typeCode;
        redisTemplate.delete(cacheKey);
        getDictByType(typeCode);
        log.info("字典类型 {} 缓存已重新加载", typeCode);
    }

    private Map<String, Object> convertDictItemToMap(DictItem item) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("value", item.getItemValue());
        map.put("label", item.getItemLabel());
        if (item.getItemLabelEn() != null) {
            map.put("labelEn", item.getItemLabelEn());
        }
        if (item.getCssClass() != null) {
            map.put("cssClass", item.getCssClass());
        }
        if (item.getExtraData() != null) {
            map.put("extraData", item.getExtraData());
        }
        return map;
    }
}