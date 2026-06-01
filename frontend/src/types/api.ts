// ============================================
// API Response Types
// ============================================
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// ============================================
// Auth Types
// ============================================
export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email: string
  phone: string
}

export interface LoginResponse {
  token: string
  refreshToken: string
  user: UserInfo
}

export interface UserInfo {
  id: number
  username: string
  email: string
  phone: string
  role: 'STUDENT' | 'ADMIN'
  status: string
  verified: boolean
  avatar?: string
  nickname?: string
  school?: string
  createdAt?: string
  updatedAt?: string
}

export interface RefreshTokenRequest {
  refreshToken: string
}

// ============================================
// Item Types
// ============================================
export interface CreateItemRequest {
  title: string
  description: string
  price: number
  categoryId: number
  condition: string
  deliveryMethod: number
  images?: string[]
  tags?: string[]
}

export interface UpdateItemRequest {
  title?: string
  description?: string
  price?: number
  categoryId?: number
  condition?: string
  deliveryMethod?: number
  images?: string[]
  tags?: string[]
}

export interface ItemInfo {
  id: number
  title: string
  description: string
  price: number
  originalPrice?: number
  minPrice?: number
  categoryId: number
  categoryName: string
  condition: string
  deliveryMethod: number
  images: string[]
  tags?: string[]
  status: string
  createdAt: string
  updatedAt: string
  userId: number
  username: string
  sellerItemCount: number
  favoriteCount?: number
  viewCount?: number
  isFavorited?: boolean
  seller?: { id: number; username: string; avatar?: string; joinDate?: string; itemCount?: number }
  isNew?: boolean
  contactType?: string
  contactName?: string
  contactPhone?: string
  contactInfo?: string
  isBargainAllowed?: boolean
  location?: string
  brand?: string
  purchaseDate?: string
  warrantyInfo?: string
}

export interface ItemSummary {
  id: number
  title: string
  price: number
  coverImage: string
  condition: string
  status: string
  createdAt: string
  username?: string
  sellerNickname?: string
  city?: string
}

// ============================================
// Category Types
// ============================================
export interface CategoryInfo {
  id: number
  name: string
  parentId: number | null
  level: number
  icon?: string
  sortOrder?: number
  children?: CategoryInfo[]
}

// ============================================
// Order Types
// ============================================
export interface CreateOrderRequest {
  itemId: number
  buyerAddress: string
  buyerPhone: string
  buyerName: string
}

export interface OrderInfo {
  id: number
  orderNo: string
  itemId: number
  itemTitle: string
  itemImage: string
  price: number
  buyerId: number
  buyerName: string
  buyerPhone: string
  buyerAddress: string
  sellerId: number
  sellerName?: string
  orderStatus: string
  paymentMethod?: string
  paymentTime?: string
  shipTime?: string
  completeTime?: string
  createdAt: string
  updatedAt: string
}

// ============================================
// Review Types
// ============================================
export interface CreateReviewRequest {
  itemId: number
  rating: number
  content: string
  images?: string[]
  isAnonymous?: boolean
}

export interface ReviewInfo {
  id: number
  itemId: number
  userId: number
  username: string
  rating: number
  content: string
  images?: string[]
  createdAt: string
  isAnonymous?: boolean
}

// ============================================
// Favorite Types
// ============================================
export interface FavoriteInfo {
  id: number
  itemId: number
  userId: number
  item: ItemSummary
  createdAt: string
}

// ============================================
// Chat Types
// ============================================
export interface ChatMessage {
  id: number
  chatId: number
  senderId: number
  receiverId: number
  content: string
  messageType: 'TEXT' | 'IMAGE' | 'SYSTEM'
  isRead: boolean
  createdAt: string
}

export interface ChatInfo {
  id: number
  buyerId: number
  sellerId: number
  orderId?: number
  itemId: number
  itemTitle?: string
  itemImage?: string
  lastMessage?: string
  lastMessageTime?: string
  unreadCount?: number
  otherUserName?: string
  otherUserAvatar?: string
}

// ============================================
// Verification Types
// ============================================
export interface VerificationRequest {
  realName: string
  studentId: string
  idCard?: string
  images?: string[]
}

export interface VerificationRecord {
  id: number
  userId: number
  realName: string
  studentId: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  rejectReason?: string
  createdAt: string
  updatedAt?: string
}

// ============================================
// Notification Types
// ============================================
export interface NotificationInfo {
  id: number
  type: string
  title: string
  content: string
  isRead: boolean
  relatedId?: number
  createdAt: string
}

// ============================================
// Dispute Types
// ============================================
export interface DisputeInfo {
  id: number
  orderId: number
  reason: string
  description?: string
  evidenceImages?: string[]
  disputeStatus: 'PENDING' | 'ASSIGNED' | 'PROCESSING' | 'ESCALATED' | 'RESOLVED' | 'CLOSED' | 'CANCELLED'
  resolution?: string
  createdAt: string
  updatedAt?: string
}

// ============================================
// Admin Log Types
// ============================================
export interface AdminLogInfo {
  id: number
  adminId: number
  adminName: string
  operation: string
  targetType: string
  targetId?: number
  details: string
  ipAddress: string
  createdAt: string
}

// ============================================
// Config & Dict Types
// ============================================
export interface SystemConfig {
  id: number
  configKey: string
  configValue: string
  configType: string
  description?: string
}

export interface DictType {
  id: number
  typeCode: string
  typeName: string
  description?: string
}

export interface DictItem {
  id?: number
  typeCode?: string
  value: string
  label: string
  labelEn?: string
  sortOrder?: number
  cssClass?: string
  status?: string
}

// ============================================
// Cart Types
// ============================================
export interface CartItem {
  id: number
  title: string
  price: number
  image: string
  quantity: number
  maxQuantity?: number
}

// ============================================
// Order Flow Types
// ============================================
export interface OrderAction {
  key: string
  label: string
  type: 'primary' | 'warning' | 'danger' | 'info'
}

export interface OrderStep {
  key: string
  title: string
}

export interface OrderStatusInfo {
  steps: OrderStep[]
  current: number
}
