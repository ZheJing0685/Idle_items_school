package com.idleitems.school.shared.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    // ==================== 缓存键前缀 ====================
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

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return clazz.cast(value);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @SuppressWarnings("deprecation")
    public void deletePattern(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
        redisTemplate.execute((RedisConnection connection) -> {
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    redisTemplate.delete(new String(cursor.next()));
                }
            }
            return null;
        });
    }

    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public void increment(String key) {
        redisTemplate.opsForValue().increment(key);
    }

    public void increment(String key, long delta) {
        redisTemplate.opsForValue().increment(key, delta);
    }

    public void decrement(String key) {
        redisTemplate.opsForValue().decrement(key);
    }

    public void decrement(String key, long delta) {
        redisTemplate.opsForValue().decrement(key, delta);
    }

    public void addToSet(String key, Object... values) {
        redisTemplate.opsForSet().add(key, values);
    }

    public Set<Object> getSet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public boolean isMemberOfSet(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    public void removeFromSet(String key, Object... values) {
        redisTemplate.opsForSet().remove(key, values);
    }

    // ==================== 物品相关缓存键 ====================

    public static String getItemKey(Long itemId) {
        return PREFIX_ITEM + itemId + SUFFIX_DETAIL;
    }

    public static String getItemListKey(int page, int size, String category, String sortBy, String condition, Integer deliveryMethod, String keyword) {
        return PREFIX_ITEM + SUFFIX_LIST + ":" + page + ":" + size + ":"
               + (category != null ? category : "all") + ":"
               + (sortBy != null ? sortBy : "createdAt") + ":"
               + (condition != null ? condition : "none") + ":"
               + (deliveryMethod != null ? deliveryMethod : "none") + ":"
               + (keyword != null ? keyword : "");
    }

    public static String getHotItemsKey() {
        return PREFIX_ITEM + SUFFIX_HOT;
    }

    public static String getUserItemsKey(Long userId, String status, int page, int size) {
        return PREFIX_ITEM + "user:" + userId + ":" + (status != null ? status : "all") + ":" + page + ":" + size;
    }

    // ==================== 用户相关缓存键 ====================

    public static String getUserKey(Long userId) {
        return PREFIX_USER + userId + SUFFIX_DETAIL;
    }

    public static String getSellerItemCountKey(Long userId) {
        return PREFIX_SELLER + "item_count:" + userId;
    }

    // ==================== 分类相关缓存键 ====================

    public static String getCategoryKey(Long categoryId) {
        return PREFIX_CATEGORY + categoryId + SUFFIX_DETAIL;
    }

    public static String getAllCategoriesKey() {
        return PREFIX_CATEGORY + SUFFIX_ALL;
    }

    public static String getCategoryTreeKey() {
        return PREFIX_CATEGORY + SUFFIX_TREE;
    }

    // ==================== 订单相关缓存键 ====================

    public static String getOrderKey(Long orderId) {
        return PREFIX_ORDER + orderId + SUFFIX_DETAIL;
    }

    public static String getUserOrdersKey(Long userId, String status, int page, int size) {
        return PREFIX_ORDER + "user:" + userId + ":" + (status != null ? status : "all") + ":" + page + ":" + size;
    }

    // ==================== 收藏相关缓存键 ====================

    public static String getUserFavoritesKey(Long userId, int page, int size) {
        return PREFIX_FAVORITE + "user:" + userId + ":" + page + ":" + size;
    }

    public static String getUserFavoriteStatusKey(Long userId, Long itemId) {
        return PREFIX_FAVORITE + "status:" + userId + ":" + itemId;
    }

    // ==================== 统计相关缓存键 ====================

    public static String getStatisticsKey(String type, String period) {
        return PREFIX_STATISTICS + type + ":" + (period != null ? period : "all");
    }
}
