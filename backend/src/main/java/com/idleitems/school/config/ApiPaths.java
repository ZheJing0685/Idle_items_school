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
        // 完整路径（保留用于需要完整路径的场景）
        public static final String LOGIN = BASE + LOGIN_PATH;
        public static final String REGISTER = BASE + REGISTER_PATH;
        public static final String ME = BASE + ME_PATH;
        public static final String REFRESH = BASE + REFRESH_PATH;
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
    }

    // 分类相关
    public static class Category {
        public static final String BASE = API_BASE + "/categories";
        // 相对于BASE的路径
        public static final String LIST_PATH = "";
        public static final String TREE_PATH = "/tree";
        // 完整路径
        public static final String LIST = BASE + LIST_PATH;
        public static final String TREE = BASE + TREE_PATH;
    }

    // 收藏相关
    public static class Favorite {
        public static final String BASE = API_BASE + "/favorites";
        // 相对于BASE的路径
        public static final String LIST_PATH = "";
        public static final String ADD_PATH = "";
        public static final String REMOVE_PATH = "/{id}";
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
        public static final String CONFIRM_PATH = "/{id}/confirm";
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

    // 用户相关
    public static class User {
        public static final String BASE = API_BASE + "/user";
        // 相对于BASE的路径
        public static final String PROFILE_PATH = "/profile";
        public static final String UPDATE_PATH = "/profile";
        public static final String VERIFICATION_PATH = "/verification";
        // 完整路径
        public static final String PROFILE = BASE + PROFILE_PATH;
        public static final String UPDATE = BASE + UPDATE_PATH;
        public static final String VERIFICATION = BASE + VERIFICATION_PATH;
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
    }

    // 健康检查
    public static class Health {
        public static final String BASE = "/actuator/health";
    }
}
