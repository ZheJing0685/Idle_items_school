// ============================================
// Dispute Types
// ============================================

/** 纠纷状态枚举 */
export type DisputeStatus = 'PENDING' | 'ASSIGNED' | 'PROCESSING' | 'ESCALATED' | 'RESOLVED' | 'CLOSED' | 'CANCELLED';

/** 纠纷类型 */
export type DisputeType = 1 | 2 | 3 | 4;

/** 纠纷列表项 */
export interface DisputeItem {
  id: number
  orderId: number
  disputeNo?: string
  itemTitle?: string
  reason: string
  description?: string
  disputeStatus: DisputeStatus
  disputeType?: DisputeType
  expectResult?: string
  expectRefundAmount?: number
  evidenceImages?: string[]
  result?: string
  processLogs?: string
  satisfaction?: number
  satisfactionRemark?: string
  createdAt: string
  updatedAt?: string
}

/** 纠纷表单数据 */
export interface DisputeForm {
  disputeType: DisputeType | number
  reason: string
  description: string
  expectResult: string
  expectRefundAmount: number | null
}

/** 纠纷统计数据 */
export interface DisputeStats {
  total: number
  pending: number
  processing: number
  resolved: number
}

/** 纠纷回复表单 */
export interface DisputeReplyForm {
  content: string
}

/** 纠纷评价表单 */
export interface DisputeEvaluateForm {
  score: number
  remark: string
}

/** 纠纷处理日志 */
export interface DisputeLog {
  time: string
  content: string
}

/** 订单启用纠纷的检查结果 */
export interface CanDisputeResult {
  canDispute: boolean
  reason?: string
  itemTitle?: string
  orderAmount?: number
  price?: number
  orderStatus?: string
}
