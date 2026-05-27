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
    expect(getAdminOrderStatusText('REFUND_REQUESTED')).toBe('退款申请中');
    expect(getAdminOrderStatusClass('COMPLETED')).toBe('admin-status-completed');
    expect(getAdminOrderStatusClass('SHIPPED')).toBe('admin-status-shipped');
    expect(getAdminOrderStatusClass('REFUND_REQUESTED')).toBe('admin-status-refunding');
  });

  it('maps payment methods', () => {
    expect(getAdminPaymentText('WECHAT')).toBe('微信支付');
    expect(getAdminPaymentText('ALIPAY')).toBe('支付宝');
    expect(getAdminPaymentText('OFFLINE')).toBe('线下支付');
  });

  it('gates admin actions correctly', () => {
    // canAdminCancelOrder: 对齐后端 adminCancelOrder，PENDING_PAYMENT/PENDING_SHIPMENT/SHIPPED 可取消
    expect(canAdminCancelOrder('SHIPPED')).toBe(true);
    expect(canAdminCancelOrder('PENDING_PAYMENT')).toBe(true);
    expect(canAdminCancelOrder('COMPLETED')).toBe(false);

    // canAdminApproveRefund: 仅 REFUND_REQUESTED 可审批
    expect(canAdminApproveRefund('REFUND_REQUESTED')).toBe(true);
    expect(canAdminApproveRefund('SHIPPED')).toBe(false);
    expect(canAdminApproveRefund('PENDING_PAYMENT')).toBe(false);

    expect(
      getAdminOrderActions('REFUND_REQUESTED').map(
        (action) => action.label
      )
    ).toEqual(['查看详情', '处理退款']);
  });

  it('returns status timestamps for terminal states', () => {
    expect(
      getAdminOrderStatusTime({
        orderStatus: 'REFUNDED',
        refundTime: '2026-04-23T10:00:00',
      })
    ).toBe('2026-04-23T10:00:00');
  });
});
