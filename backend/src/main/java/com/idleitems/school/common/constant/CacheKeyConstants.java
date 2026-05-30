package com.idleitems.school.common.constant;

/**
 * Redis缓存键常量统一管理
 * 所有缓存键前缀、后缀、生成方法集中在此类
 */
public final class CacheKeyConstants {

    private CacheKeyConstants() {}

    // ==================== 缓存键前缀 ====================
    public static final String PREFIX_ITEM = "item:";
    public static final String PREFIX_USER = "user:";
    public static final String PREFIX_CATEGORY = "category:";
    public static final String PREFIX_ORDER = "order:";
    public static final String PREFIX_FAVORITE = "favorite:";
    public static final String PREFIX_STATISTICS = "statistics:";
    public static final String PREFIX_SELLER = "seller:";
    public static final String PREFIX_DICT = "dict:";
    public static final String PREFIX_CONFIG = "config:";

    // ==================== 缓存键后缀 ====================
    public static final String SUFFIX_DETAIL = ":detail";
    public static final String SUFFIX_LIST = ":list";
    public static final String SUFFIX_HOT = ":hot";
    public static final String SUFFIX_COUNT = ":count";
    public static final String SUFFIX_TREE = ":tree";
    public static final String SUFFIX_ALL = ":all";

    // ==================== 安全相关缓存键 ====================
    public static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    public static final String TOKEN_VERSION_PREFIX = "token:user_version:";
    public static final String LOGIN_FAIL_PREFIX = "login:fail:";
    public static final String LOGIN_LOCK_PREFIX = "login:lock:";
    public static final String PASSWORD_RESET_PREFIX = "password_reset:";
    public static final String PASSWORD_RESET_COUNT_PREFIX = "password_reset:count:";

    // ==================== 缓存键生成方法 ====================

    public static String itemDetail(Long itemId) {
        return PREFIX_ITEM + itemId + SUFFIX_DETAIL;
    }

    public static String itemList(int page, int size, String category, String sortBy,
                                  String condition, Integer deliveryMethod) {
        return PREFIX_ITEM + SUFFIX_LIST + ":" + page + ":" + size + ":"
               + (category != null ? category : "all") + ":"
               + (sortBy != null ? sortBy : "createdAt") + ":"
               + (condition != null ? condition : "none") + ":"
               + (deliveryMethod != null ? deliveryMethod : "none");
    }

    public static String hotItems() {
        return PREFIX_ITEM + SUFFIX_HOT;
    }

    public static String userItems(Long userId, String status, int page, int size) {
        return PREFIX_ITEM + "user:" + userId + ":" + (status != null ? status : "all")
               + ":" + page + ":" + size;
    }

    public static String sellerItemCount(Long userId) {
        return PREFIX_SELLER + "item_count:" + userId;
    }

    public static String categoriesAll() {
        return PREFIX_CATEGORY + SUFFIX_ALL;
    }

    public static String categoryTree() {
        return PREFIX_CATEGORY + SUFFIX_TREE;
    }

    public static String userTokenVersion(Long userId) {
        return TOKEN_VERSION_PREFIX + userId;
    }

    public static String tokenBlacklist(String tokenHash) {
        return TOKEN_BLACKLIST_PREFIX + tokenHash;
    }

    public static String tokenUserBlacklist(Long userId, String tokenHash) {
        return TOKEN_BLACKLIST_PREFIX + "user:" + userId + ":" + tokenHash;
    }

    public static String loginFail(String username) {
        return LOGIN_FAIL_PREFIX + username;
    }

    public static String loginLock(String username) {
        return LOGIN_LOCK_PREFIX + username;
    }

    public static String passwordReset(String email) {
        return PASSWORD_RESET_PREFIX + email;
    }

    public static String passwordResetCount(String email) {
        return PASSWORD_RESET_COUNT_PREFIX + email;
    }

    public static String dictType(String typeCode) {
        return PREFIX_DICT + typeCode;
    }

    public static String dictAll() {
        return PREFIX_DICT + SUFFIX_ALL;
    }

    public static String dictLabel(String typeCode, String itemValue) {
        return PREFIX_DICT + typeCode + ":" + itemValue;
    }

    public static String configKey(String configKey) {
        return PREFIX_CONFIG + configKey;
    }

    public static String configAll() {
        return PREFIX_CONFIG + SUFFIX_ALL;
    }

    public static String configGroup(String groupName) {
        return PREFIX_CONFIG + "group:" + groupName;
    }
}
