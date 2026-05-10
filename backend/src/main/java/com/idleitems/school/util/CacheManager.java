package com.idleitems.school.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheManager {

    private final RedisTemplate<String, Object> redisTemplate;

    // 缓存键前缀
    private static final String PREFIX_ITEM = "item:";
    private static final String PREFIX_USER = "user:";
    private static final String PREFIX_CATEGORY = "category:";
    private static final String PREFIX_ORDER = "order:";
    private static final String PREFIX_FAVORITE = "favorite:";
    private static final String PREFIX_STATISTICS = "statistics:";
    private static final String PREFIX_SELLER = "seller:";

    // 缓存键类型
    private static final String SUFFIX_DETAIL = ":detail";
    private static final String SUFFIX_LIST = ":list";
    private static final String SUFFIX_HOT = ":hot";
    private static final String SUFFIX_COUNT = ":count";
    private static final String SUFFIX_TREE = ":tree";
    private static final String SUFFIX_ALL = ":all";

    /**
     * 设置缓存
     * @param key 缓存键
     * @param value 缓存值
     * @param expire 过期时间（秒）
     */
    public void set(String key, Object value, long expire) {
        try {
            redisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("设置缓存失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取缓存
     * @param key 缓存键
     * @return 缓存值
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取缓存失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 删除缓存
     * @param key 缓存键
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除缓存失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 清除匹配模式的缓存（使用SCAN替代KEYS，生产环境安全）
     * @param pattern 键模式，如 "item:*"
     */
    @SuppressWarnings("deprecation")
    public void deletePattern(String pattern) {
        try {
            Set<String> keys = new HashSet<>();
            // 使用SCAN迭代查找键，避免阻塞Redis
            redisTemplate.execute((RedisCallback<Void>) connection -> {
                Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions()
                    .match(pattern)
                    .count(100)
                    .build());
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next()));
                }
                return null;
            });
            
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("清除了 {} 个缓存键: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            log.error("清除缓存失败: {}", e.getMessage(), e);
        }
    }

    // ==================== 物品相关缓存键 ====================

    /**
     * 生成物品详情缓存键
     * @param itemId 物品ID
     * @return 缓存键
     */
    public static String getItemKey(Long itemId) {
        return PREFIX_ITEM + itemId + SUFFIX_DETAIL;
    }

    /**
     * 生成物品列表缓存键
     * @param page 页码
     * @param size 每页大小
     * @param category 分类
     * @param sortBy 排序字段
     * @param condition 条件
     * @param deliveryMethod 配送方式
     * @return 缓存键
     */
    public static String getItemListKey(int page, int size, String category, String sortBy, String condition, Integer deliveryMethod) {
        return PREFIX_ITEM + SUFFIX_LIST + ":" + page + ":" + size + ":" + 
               (category != null ? category : "all") + ":" + 
               (sortBy != null ? sortBy : "createdAt") + ":" + 
               (condition != null ? condition : "none") + ":" + 
               (deliveryMethod != null ? deliveryMethod : "none");
    }

    /**
     * 生成热门物品缓存键
     * @return 缓存键
     */
    public static String getHotItemsKey() {
        return PREFIX_ITEM + SUFFIX_HOT;
    }

    /**
     * 生成用户物品列表缓存键
     * @param userId 用户ID
     * @param status 物品状态
     * @param page 页码
     * @param size 每页大小
     * @return 缓存键
     */
    public static String getUserItemsKey(Long userId, String status, int page, int size) {
        return PREFIX_ITEM + "user:" + userId + ":" + (status != null ? status : "all") + ":" + page + ":" + size;
    }

    // ==================== 用户相关缓存键 ====================

    /**
     * 生成用户详情缓存键
     * @param userId 用户ID
     * @return 缓存键
     */
    public static String getUserKey(Long userId) {
        return PREFIX_USER + userId + SUFFIX_DETAIL;
    }

    /**
     * 生成用户物品数量缓存键
     * @param userId 用户ID
     * @return 缓存键
     */
    public static String getSellerItemCountKey(Long userId) {
        return PREFIX_SELLER + "item_count:" + userId;
    }

    // ==================== 分类相关缓存键 ====================

    /**
     * 生成分类详情缓存键
     * @param categoryId 分类ID
     * @return 缓存键
     */
    public static String getCategoryKey(Long categoryId) {
        return PREFIX_CATEGORY + categoryId + SUFFIX_DETAIL;
    }

    /**
     * 生成所有分类缓存键
     * @return 缓存键
     */
    public static String getAllCategoriesKey() {
        return PREFIX_CATEGORY + SUFFIX_ALL;
    }

    /**
     * 生成分类树缓存键
     * @return 缓存键
     */
    public static String getCategoryTreeKey() {
        return PREFIX_CATEGORY + SUFFIX_TREE;
    }

    // ==================== 订单相关缓存键 ====================

    /**
     * 生成订单详情缓存键
     * @param orderId 订单ID
     * @return 缓存键
     */
    public static String getOrderKey(Long orderId) {
        return PREFIX_ORDER + orderId + SUFFIX_DETAIL;
    }

    /**
     * 生成用户订单列表缓存键
     * @param userId 用户ID
     * @param status 订单状态
     * @param page 页码
     * @param size 每页大小
     * @return 缓存键
     */
    public static String getUserOrdersKey(Long userId, String status, int page, int size) {
        return PREFIX_ORDER + "user:" + userId + ":" + (status != null ? status : "all") + ":" + page + ":" + size;
    }

    // ==================== 收藏相关缓存键 ====================

    /**
     * 生成用户收藏列表缓存键
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 缓存键
     */
    public static String getUserFavoritesKey(Long userId, int page, int size) {
        return PREFIX_FAVORITE + "user:" + userId + ":" + page + ":" + size;
    }

    /**
     * 生成用户收藏状态缓存键
     * @param userId 用户ID
     * @param itemId 物品ID
     * @return 缓存键
     */
    public static String getUserFavoriteStatusKey(Long userId, Long itemId) {
        return PREFIX_FAVORITE + "status:" + userId + ":" + itemId;
    }

    // ==================== 统计相关缓存键 ====================

    /**
     * 生成统计数据缓存键
     * @param type 统计类型
     * @param period 统计周期
     * @return 缓存键
     */
    public static String getStatisticsKey(String type, String period) {
        return PREFIX_STATISTICS + type + ":" + (period != null ? period : "all");
    }
}
