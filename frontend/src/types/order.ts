// ============================================
// Order Types (Extends api.ts OrderInfo)
// ============================================

import type { DisputeItem, DisputeStatus, CanDisputeResult } from './dispute';

/** 订单状态枚举 */
export type OrderStatusValue =
  | 'PENDING_PAYMENT'
  | 'PENDING_SHIPMENT'
  | 'SHIPPED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'REFUND_REQUESTED'
  | 'REFUNDED';

/** 订单详情（含纠纷信息） */
export interface OrderDetail {
  id: number
  orderNo: string
  itemId: number
  itemTitle: string
  itemImage?: string
  coverImage?: string
  price: number
  originalPrice?: number
  buyerId: number
  buyerName?: string
  buyerPhone?: string
  buyerAddress?: string
  sellerId: number
  sellerName?: string
  orderStatus: OrderStatusValue
  paymentMethod?: string
  paymentTime?: string
  shipTime?: string
  completeTime?: string
  createdAt: string
  updatedAt?: string
}

/** 订单状态文本映射 */
export const orderStatusTextMap: Record<OrderStatusValue, string> = {
  PENDING_PAYMENT: '待付款',
  PENDING_SHIPMENT: '待发货',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  REFUND_REQUESTED: '退款申请中',
  REFUNDED: '已退款',
};

/** 纠纷状态文本映射 */
export const disputeStatusTextMap: Record<DisputeStatus, string> = {
  PENDING: '待处理',
  ASSIGNED: '已分配',
  PROCESSING: '处理中',
  ESCALATED: '已升级',
  RESOLVED: '已解决',
  CLOSED: '已关闭',
  CANCELLED: '已取消',
};

/** 订单页面数据集合 */
export interface OrderDetailPageData {
  order: OrderDetail | null
  dispute: DisputeItem | null
  canDispute: CanDisputeResult | null
}
