import { describe, it, expect } from 'vitest'
import {
  OrderStatus,
  OrderStatusText,
  OrderStatusColor,
  getOrderStatusText,
  getOrderStatusColor,
  getOrderActions,
  getOrderStatusProgress,
  getOrderHint,
  normalizeOrder,
  sanitizeOrderStatus,
  sanitizeOrderView,
  getOrderStatusOptions,
  getOrderStatusClass
} from '@/utils/business/orderFlow'

describe('OrderFlow 工具', () => {
  describe('OrderStatus 常量', () => {
    it('应该定义所有订单状态', () => {
      expect(OrderStatus.PENDING_PAYMENT).toBe('PENDING_PAYMENT')
      expect(OrderStatus.PENDING_SHIPMENT).toBe('PENDING_SHIPMENT')
      expect(OrderStatus.SHIPPED).toBe('SHIPPED')
      expect(OrderStatus.COMPLETED).toBe('COMPLETED')
      expect(OrderStatus.CANCELLED).toBe('CANCELLED')
      expect(OrderStatus.REFUND_REQUESTED).toBe('REFUND_REQUESTED')
      expect(OrderStatus.REFUNDED).toBe('REFUNDED')
    })
  })

  describe('OrderStatusText 常量', () => {
    it('应该为所有状态提供中文文本', () => {
      expect(OrderStatusText['PENDING_PAYMENT']).toBe('待付款')
      expect(OrderStatusText['PENDING_SHIPMENT']).toBe('待发货')
      expect(OrderStatusText['SHIPPED']).toBe('已发货')
      expect(OrderStatusText['COMPLETED']).toBe('已完成')
      expect(OrderStatusText['CANCELLED']).toBe('已取消')
      expect(OrderStatusText['REFUND_REQUESTED']).toBe('退款申请中')
      expect(OrderStatusText['REFUNDED']).toBe('已退款')
    })
  })

  describe('OrderStatusColor 常量', () => {
    it('应该为所有状态提供颜色', () => {
      expect(OrderStatusColor['PENDING_PAYMENT']).toBe('#faad14')
      expect(OrderStatusColor['PENDING_SHIPMENT']).toBe('#faad14')
      expect(OrderStatusColor['SHIPPED']).toBe('#1890ff')
      expect(OrderStatusColor['COMPLETED']).toBe('#52c41a')
      expect(OrderStatusColor['CANCELLED']).toBe('#8c8c8c')
      expect(OrderStatusColor['REFUND_REQUESTED']).toBe('#ff7a45')
      expect(OrderStatusColor['REFUNDED']).toBe('#ff4d4f')
    })
  })

  describe('getOrderStatusText', () => {
    it('应该返回正确的状态文本', () => {
      expect(getOrderStatusText('PENDING_PAYMENT')).toBe('待付款')
      expect(getOrderStatusText('COMPLETED')).toBe('已完成')
    })

    it('应该返回未知状态当状态不存在', () => {
      expect(getOrderStatusText('UNKNOWN')).toBe('未知状态')
    })
  })

  describe('getOrderStatusColor', () => {
    it('应该返回颜色当状态存在', () => {
      const color = getOrderStatusColor('PENDING_PAYMENT')
      expect(color).toBeDefined()
      expect(typeof color).toBe('string')
    })

    it('应该返回默认颜色当状态不存在', () => {
      expect(getOrderStatusColor('UNKNOWN')).toBe('#8c8c8c')
    })
  })

  describe('getOrderActions', () => {
    it('买家在待付款状态应该有付款和取消操作', () => {
      const actions = getOrderActions('PENDING_PAYMENT', 'buyer')
      expect(actions).toHaveLength(2)
      expect(actions[0].key).toBe('pay')
      expect(actions[1].key).toBe('cancel')
    })

    it('买家在待发货状态应该有确认收货和申请退款操作', () => {
      const actions = getOrderActions('PENDING_SHIPMENT', 'buyer')
      expect(actions).toHaveLength(2)
      expect(actions[0].key).toBe('confirmReceive')
      expect(actions[1].key).toBe('applyRefund')
    })

    it('买家在已发货状态应该有确认收货和申请退款操作', () => {
      const actions = getOrderActions('SHIPPED', 'buyer')
      expect(actions).toHaveLength(2)
      expect(actions[0].key).toBe('confirmReceive')
      expect(actions[1].key).toBe('applyRefund')
    })

    it('买家在已完成状态应该有评价操作', () => {
      const actions = getOrderActions('COMPLETED', 'buyer')
      expect(actions).toHaveLength(1)
      expect(actions[0].key).toBe('review')
    })

    it('卖家在待发货状态应该有确认发货操作', () => {
      const actions = getOrderActions('PENDING_SHIPMENT', 'seller')
      expect(actions).toHaveLength(1)
      expect(actions[0].key).toBe('ship')
    })

    it('应该返回空数组当状态没有对应操作', () => {
      const actions = getOrderActions('CANCELLED', 'buyer')
      expect(actions).toHaveLength(0)
    })
  })

  describe('getOrderStatusProgress', () => {
    it('应该返回正确的步骤进度', () => {
      const progress = getOrderStatusProgress('PENDING_PAYMENT')
      expect(progress.steps).toHaveLength(4)
      expect(progress.current).toBe(0)
    })

    it('已取消状态应该返回单个步骤', () => {
      const progress = getOrderStatusProgress('CANCELLED')
      expect(progress.steps).toHaveLength(1)
      expect(progress.current).toBe(0)
    })

    it('退款申请中状态应该返回单个步骤', () => {
      const progress = getOrderStatusProgress('REFUND_REQUESTED')
      expect(progress.steps).toHaveLength(1)
      expect(progress.current).toBe(0)
    })
  })

  describe('getOrderHint', () => {
    it('买家在待付款状态应该返回正确提示', () => {
      const hint = getOrderHint('PENDING_PAYMENT', 'buyer')
      expect(hint).toBe('请及时付款，超时订单将自动取消')
    })

    it('卖家在待发货状态应该返回正确提示', () => {
      const hint = getOrderHint('PENDING_SHIPMENT', 'seller')
      expect(hint).toBe('买家已付款，请尽快确认发货')
    })

    it('应该返回空字符串当没有对应提示', () => {
      const hint = getOrderHint('COMPLETED', 'buyer')
      expect(hint).toBe('')
    })
  })

  describe('normalizeOrder', () => {
    it('应该返回null当订单为null', () => {
      expect(normalizeOrder(null)).toBeNull()
    })

    it('应该使用默认值填充缺失字段', () => {
      const order = normalizeOrder({})
      expect(order?.orderStatus).toBe('PENDING_PAYMENT')
      expect(order?.price).toBe(0)
      expect(order?.createdAt).toBeDefined()
    })

    it('应该保留现有字段', () => {
      const order = normalizeOrder({
        orderStatus: 'COMPLETED',
        price: 100,
        createdAt: '2024-01-01'
      })
      expect(order?.orderStatus).toBe('COMPLETED')
      expect(order?.price).toBe(100)
      expect(order?.createdAt).toBe('2024-01-01')
    })
  })

  describe('sanitizeOrderStatus', () => {
    it('应该返回有效状态', () => {
      expect(sanitizeOrderStatus('PENDING_PAYMENT')).toBe('PENDING_PAYMENT')
      expect(sanitizeOrderStatus('COMPLETED')).toBe('COMPLETED')
    })

    it('应该返回默认状态当状态无效', () => {
      expect(sanitizeOrderStatus('INVALID')).toBe('PENDING_PAYMENT')
    })
  })

  describe('sanitizeOrderView', () => {
    it('应该返回有效视图', () => {
      expect(sanitizeOrderView('buyer')).toBe('buyer')
      expect(sanitizeOrderView('seller')).toBe('seller')
    })

    it('应该返回默认视图当视图无效', () => {
      expect(sanitizeOrderView('invalid')).toBe('buyer')
    })
  })

  describe('getOrderStatusOptions', () => {
    it('应该返回所有状态选项', () => {
      const options = getOrderStatusOptions()
      expect(options).toHaveLength(8)
      expect(options[0].label).toBe('全部')
      expect(options[0].value).toBe('ALL')
    })
  })

  describe('getOrderStatusClass', () => {
    it('应该返回正确的CSS类', () => {
      expect(getOrderStatusClass('PENDING_PAYMENT')).toBe('status-pending')
      expect(getOrderStatusClass('PENDING_SHIPMENT')).toBe('status-processing')
      expect(getOrderStatusClass('SHIPPED')).toBe('status-shipped')
      expect(getOrderStatusClass('COMPLETED')).toBe('status-completed')
      expect(getOrderStatusClass('CANCELLED')).toBe('status-cancelled')
      expect(getOrderStatusClass('REFUND_REQUESTED')).toBe('status-refund-requested')
      expect(getOrderStatusClass('REFUNDED')).toBe('status-refunded')
    })

    it('应该返回未知类当状态不存在', () => {
      expect(getOrderStatusClass('UNKNOWN')).toBe('status-unknown')
    })
  })
})
