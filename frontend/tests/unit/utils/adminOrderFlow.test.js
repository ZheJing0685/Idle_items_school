import {
  canAdminApproveRefund,
  canAdminCancelOrder,
  getAdminOrderActions,
  getAdminOrderStatusClass,
  getAdminOrderStatusText,
  getAdminOrderStatusTime,
  getAdminPaymentText,
  normalizeAdminOrder,
} from '@/utils/business/adminOrderFlow';

describe('adminOrderFlow', () => {
  it('normalizes admin order fields', () => {
    expect(
      normalizeAdminOrder({
        id: 1,
        itemCover: 'cover.jpg',
        orderStatus: 'PENDING_SHIPMENT',
        price: 88,
      })
    ).toEqual(
      expect.objectContaining({
        id: 1,
        itemCover: 'cover.jpg',
        orderStatus: 'PENDING_SHIPMENT',
        price: 88,
      })
    );
  });

  it('maps status text and class to the unified state machine', () => {
    expect(getAdminOrderStatusText('REFUNDED')).toBe('已退款');
    expect(getAdminOrderStatusText('COMPLETED')).toBe('已完成');
    expect(getAdminOrderStatusClass('COMPLETED')).toBe('admin-status-completed');
    expect(getAdminOrderStatusClass('SHIPPED')).toBe('admin-status-shipped');
  });

  it('maps payment methods', () => {
    expect(getAdminPaymentText('WECHAT')).toBe('微信支付');
    expect(getAdminPaymentText('ALIPAY')).toBe('支付宝');
    expect(getAdminPaymentText('OFFLINE')).toBe('线下支付');
  });

  it('gates admin actions correctly', () => {
    expect(canAdminCancelOrder('SHIPPED')).toBe(false);
    expect(canAdminCancelOrder('PENDING_PAYMENT')).toBe(true);
    expect(canAdminApproveRefund('SHIPPED')).toBe(true);
    expect(canAdminApproveRefund('PAID')).toBe(true);
    expect(canAdminApproveRefund('PENDING_PAYMENT')).toBe(false);
    expect(
      getAdminOrderActions('PAID').map(
        (action) => action.label
      )
    ).toEqual(['查看详情', '取消订单', '标记发货', '处理退款']);
  });

  it('returns status timestamps for terminal states', () => {
    expect(
      getAdminOrderStatusTime({
        orderStatus: 'REFUNDED',
        refundedAt: '2026-04-23T10:00:00',
      })
    ).toBe('2026-04-23T10:00:00');
  });
});
