// API数据类型定义

// 认证相关
export const AuthTypes = {
  // 登录请求
  LoginRequest: {
    username: String,
    password: String
  },
  
  // 注册请求
  RegisterRequest: {
    username: String,
    password: String,
    email: String,
    phone: String
  },
  
  // 登录响应
  LoginResponse: {
    token: String,
    refreshToken: String,
    user: Object
  },
  
  // 用户信息
  UserInfo: {
    id: Number,
    username: String,
    email: String,
    phone: String,
    role: String,
    status: String,
    verified: Boolean
  }
};

// 物品相关
export const ItemTypes = {
  // 物品创建请求
  CreateItemRequest: {
    title: String,
    description: String,
    price: Number,
    categoryId: Number,
    condition: String,
    deliveryMethod: Number,
    images: Array
  },
  
  // 物品更新请求
  UpdateItemRequest: {
    title: String,
    description: String,
    price: Number,
    categoryId: Number,
    condition: String,
    deliveryMethod: Number,
    images: Array
  },
  
  // 物品信息
  ItemInfo: {
    id: Number,
    title: String,
    description: String,
    price: Number,
    categoryId: Number,
    categoryName: String,
    condition: String,
    deliveryMethod: Number,
    images: Array,
    status: String,
    createdAt: String,
    updatedAt: String,
    userId: Number,
    username: String,
    sellerItemCount: Number
  },
  
  // 物品列表响应
  ItemListResponse: {
    content: Array,
    totalElements: Number,
    totalPages: Number,
    size: Number,
    number: Number
  }
};

// 分类相关
export const CategoryTypes = {
  // 分类信息
  CategoryInfo: {
    id: Number,
    name: String,
    parentId: Number,
    level: Number,
    children: Array
  }
};

// 订单相关
export const OrderTypes = {
  // 订单创建请求
  CreateOrderRequest: {
    itemId: Number,
    buyerAddress: String,
    buyerPhone: String,
    buyerName: String
  },
  
  // 订单信息
  OrderInfo: {
    id: Number,
    orderNo: String,
    itemId: Number,
    itemTitle: String,
    itemImage: String,
    price: Number,
    buyerId: Number,
    buyerName: String,
    buyerPhone: String,
    buyerAddress: String,
    sellerId: Number,
    orderStatus: String,
    paymentMethod: String,
    paymentTime: String,
    shipTime: String,
    completeTime: String,
    createdAt: String,
    updatedAt: String
  }
};

// 评价相关
export const ReviewTypes = {
  // 评价创建请求
  CreateReviewRequest: {
    itemId: Number,
    rating: Number,
    content: String,
    images: Array
  },
  
  // 评价信息
  ReviewInfo: {
    id: Number,
    itemId: Number,
    userId: Number,
    username: String,
    rating: Number,
    content: String,
    images: Array,
    createdAt: String
  }
};

// 通用响应格式
export const ResponseTypes = {
  // 通用响应
  CommonResponse: {
    code: Number,
    message: String,
    data: Object
  }
};

export default {
  AuthTypes,
  ItemTypes,
  CategoryTypes,
  OrderTypes,
  ReviewTypes,
  ResponseTypes
};