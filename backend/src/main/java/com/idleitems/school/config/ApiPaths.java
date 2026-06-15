package com.idleitems.school.config;

/**
 * API路径统一管理
 */
public class ApiPaths {
    // 基础路径
    public static final String API_BASE = "/api";

    // 认证相关
    public static class Auth {
        public static final String BASE = API_BASE + "/auth";
        // 相对于BASE的路径
        public static final String LOGIN_PATH = "/login";
        public static final String REGISTER_PATH = "/register";
        public static final String ME_PATH = "/me";
        public static final String REFRESH_PATH = "/refresh";
        public static final String CHANGE_PASSWORD_PATH = "/change-password";
        public static final String LOGOUT_PATH = "/logout";
        // 完整路径（保留用于需要完整路径的场景）
        public static final String LOGIN = BASE + LOGIN_PATH;
        public static final String REGISTER = BASE + REGISTER_PATH;
        public static final String ME = BASE + ME_PATH;
        public static final String REFRESH = BASE + REFRESH_PATH;
        public static final String CHANGE_PASSWORD = BASE + CHANGE_PASSWORD_PATH;
        public static final String LOGOUT = BASE + LOGOUT_PATH;
    }

    // 物品相关
    public static class Item {
        public static final String BASE = API_BASE + "/items";
        // 相对于BASE的路径
        public static final String LIST_PATH = "";
        public static final String HOT_PATH = "/hot";
        public static final String SEARCH_PATH = "/search";
        public static final String DETAIL_PATH = "/{id}";
        public static final String CREATE_PATH = "";
        public static final String UPDATE_PATH = "/{id}";
        public static final String OFF_SHELF_PATH = "/{id}/off-shelf";
        public static final String UPLOAD_PATH = "/upload";
        public static final String UPLOAD_CHUNK_PATH = "/upload/chunk";
        public static final String UPLOAD_COMPLETE_PATH = "/upload/complete";
        public static final String UPLOAD_CHECK_PATH = "/upload/check";
        public static final String USER_ITEMS_PATH = "/user";
        public static final String RELATED_PATH = "/{id}/related";
        public static final String RECOMMENDED_PATH = "/recommended";
        // 完整路径（保留用于需要完整路径的场景）
        public static final String LIST = BASE + LIST_PATH;
        public static final String HOT = BASE + HOT_PATH;
        public static final String SEARCH = BASE + SEARCH_PATH;
        public static final String DETAIL = BASE + DETAIL_PATH;
        public static final String CREATE = BASE + CREATE_PATH;
        public static final String UPDATE = BASE + UPDATE_PATH;
        public static final String OFF_SHELF = BASE + OFF_SHELF_PATH;
        public static final String UPLOAD = BASE + UPLOAD_PATH;
        public static final String UPLOAD_CHUNK = BASE + UPLOAD_CHUNK_PATH;
        public static final String UPLOAD_COMPLETE = BASE + UPLOAD_COMPLETE_PATH;
        public static final String UPLOAD_CHECK = BASE + UPLOAD_CHECK_PATH;
        public static final String USER_ITEMS = BASE + USER_ITEMS_PATH;
        public static final String RELATED = BASE + RELATED_PATH;
        public static final String RECOMMENDED = BASE + RECOMMENDED_PATH;
    }

    // 分类相关
    public static class Category {
        public static final String BASE = API_BASE + "/categories";
        // 相对于BASE的路径
        public static final String LIST_PATH = "";
        public static final String TREE_PATH = "/tree";
        public static final String SUGGEST_PATH = "/suggest";
        public static final String BREADCRUMB_PATH = "/{id}/breadcrumb";
        // 完整路径
        public static final String LIST = BASE + LIST_PATH;
        public static final String TREE = BASE + TREE_PATH;
        public static final String SUGGEST = BASE + SUGGEST_PATH;
        public static final String BREADCRUMB = BASE + BREADCRUMB_PATH;
    }

    // 收藏相关
    public static class Favorite {
        public static final String BASE = API_BASE + "/favorites";
        // 相对于BASE的路径
        public static final String LIST_PATH = "";
        public static final String ADD_PATH = "";
        public static final String REMOVE_PATH = "/{itemId}";
        // 完整路径
        public static final String LIST = BASE + LIST_PATH;
        public static final String ADD = BASE + ADD_PATH;
        public static final String REMOVE = BASE + REMOVE_PATH;
    }

    // 订单相关
    public static class Order {
        public static final String BASE = API_BASE + "/orders";
        // 相对于BASE的路径
        public static final String CREATE_PATH = "";
        public static final String LIST_PATH = "";
        public static final String DETAIL_PATH = "/{id}";
        public static final String CANCEL_PATH = "/{id}/cancel";
        public static final String PAY_PATH = "/{id}/pay";
        public static final String SHIP_PATH = "/{id}/ship";
        public static final String CONFIRM_PATH = "/{id}/confirm-receive";
        public static final String REFUND_PATH = "/{id}/refund";
        // 完整路径
        public static final String CREATE = BASE + CREATE_PATH;
        public static final String LIST = BASE + LIST_PATH;
        public static final String DETAIL = BASE + DETAIL_PATH;
        public static final String CANCEL = BASE + CANCEL_PATH;
        public static final String PAY = BASE + PAY_PATH;
        public static final String SHIP = BASE + SHIP_PATH;
        public static final String CONFIRM = BASE + CONFIRM_PATH;
        public static final String REFUND = BASE + REFUND_PATH;
    }

    // 评价相关
    public static class Review {
        public static final String BASE = API_BASE + "/reviews";
        // 相对于BASE的路径
        public static final String CREATE_PATH = "";
        public static final String ITEM_REVIEWS_PATH = "/item/{itemId}";
        public static final String USER_REVIEWS_PATH = "/user";
        // 完整路径
        public static final String CREATE = BASE + CREATE_PATH;
        public static final String ITEM_REVIEWS = BASE + ITEM_REVIEWS_PATH;
        public static final String USER_REVIEWS = BASE + USER_REVIEWS_PATH;
    }

    // 用户相关（含卖家公开端点）
    public static class User {
        public static final String BASE = API_BASE + "/user";
        // 相对于BASE的路径
        public static final String PROFILE_PATH = "/profile";
        public static final String UPDATE_PATH = "/profile";
        public static final String VERIFICATION_PATH = "/verification";
        public static final String STATS_PATH = "/stats";
        // 卖家公开端点路径
        public static final String SELLER_PROFILE_PATH = "/{id}/profile";
        public static final String SELLER_ITEMS_PATH = "/{id}/items";
        public static final String SELLER_REVIEWS_PATH = "/{id}/reviews";
        // 完整路径
        public static final String PROFILE = BASE + PROFILE_PATH;
        public static final String UPDATE = BASE + UPDATE_PATH;
        public static final String VERIFICATION = BASE + VERIFICATION_PATH;
        public static final String STATS = BASE + STATS_PATH;
        // 卖家公开端点完整路径
        public static final String SELLER_PROFILE = BASE + SELLER_PROFILE_PATH;
        public static final String SELLER_ITEMS = BASE + SELLER_ITEMS_PATH;
        public static final String SELLER_REVIEWS = BASE + SELLER_REVIEWS_PATH;
    }

    // 管理员相关
    public static class Admin {
        public static final String BASE = API_BASE + "/admin";
        public static final String DASHBOARD = BASE + "/dashboard";
        public static final String USERS = BASE + "/users";
        public static final String ITEMS = BASE + "/items";
        public static final String ORDERS = BASE + "/orders";
        public static final String CATEGORIES = BASE + "/categories";
        public static final String STATISTICS = BASE + "/statistics";
        public static final String LOGS = BASE + "/logs";
        public static final String MONITOR = BASE + "/monitor";
        public static final String DISPUTES = BASE + "/disputes";
        public static final String BATCH = BASE + "/batch";
        public static final String VERIFICATIONS = BASE + "/verifications";
        public static final String LOGS_ANALYSIS = BASE + "/logs/analysis";
    }

    // 健康检查
    public static class Health {
        public static final String BASE = "/actuator/health";
    }

    // 纠纷相关
    public static class Dispute {
        public static final String BASE = API_BASE + "/disputes";
        public static final String CREATE_PATH = "";
        public static final String LIST_PATH = "";
        public static final String DETAIL_PATH = "/{id}";
        public static final String REPLY_PATH = "/{id}/reply";
        public static final String STATS_PATH = "/stats";
        // 完整路径
        public static final String CREATE = BASE + CREATE_PATH;
        public static final String LIST = BASE + LIST_PATH;
        public static final String DETAIL = BASE + DETAIL_PATH;
        public static final String REPLY = BASE + REPLY_PATH;
        public static final String STATS = BASE + STATS_PATH;
    }

    // 通知相关
    public static class Notification {
        public static final String BASE = API_BASE + "/notifications";
        public static final String LIST_PATH = "";
        public static final String UNREAD_COUNT_PATH = "/unread-count";
        public static final String READ_PATH = "/{id}/read";
        public static final String READ_ALL_PATH = "/read-all";
        public static final String DELETE_PATH = "/{id}";
        // 完整路径
        public static final String LIST = BASE + LIST_PATH;
        public static final String UNREAD_COUNT = BASE + UNREAD_COUNT_PATH;
        public static final String READ = BASE + READ_PATH;
        public static final String READ_ALL = BASE + READ_ALL_PATH;
        public static final String DELETE = BASE + DELETE_PATH;
    }

    // 聊天相关
    public static class Chat {
        public static final String BASE = API_BASE + "/chats";
    }

    // 系统配置
    public static class Config {
        public static final String BASE = API_BASE + "/configs";
    }

    // 数据字典
    public static class Dict {
        public static final String BASE = API_BASE + "/dicts";
    }

    // 首页
    public static class Home {
        public static final String BASE = API_BASE + "/home";
    }

    // 文件上传
    public static class Upload {
        public static final String BASE = API_BASE + "/upload";
    }

    // 实名认证
    public static class Verification {
        public static final String BASE = API_BASE + "/verification";
    }

    // 碳减排统计
    public static class Carbon {
        public static final String BASE = API_BASE + "/carbon";
        public static final String STATS_PATH = "/stats";
        public static final String STATS = BASE + STATS_PATH;
    }
}
