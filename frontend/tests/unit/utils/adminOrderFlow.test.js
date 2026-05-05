import {
  canAdminApproveRefund,
  canAdminCancelOrder,
  getAdminOrderActions,
  getAdminOrderStatusClass,
  getAdminOrderStatusText,
  getAdminOrderStatusTime,
  getAdminPaymentText,
  normalizeAdminOrder,
} from '@/utils/adminOrderFlow';

describe('adminOrderFlow', () => {
  it('normalizes admin order fields', () => {
    expect(
      normalizeAdminOrder({
        id: 1,
        itemImage: 'cover.jpg',
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
    expect(getAdminOrderStatusText('REFUND_REQUESTED')).toBe('退款中');
    expect(getAdminOrderStatusClass('COMPLETED')).toBe('badge-success');
  });

  it('maps payment methods', () => {
    expect(getAdminPaymentText('WECHAT_PAY')).toBe('微信支付');
    expect(getAdminPaymentText(3)).toBe('线下支付');
  });

  it('gates admin actions correctly', () => {
    expect(canAdminCancelOrder({ orderStatus: 'SHIPPED' })).toBe(true);
    expect(canAdminCancelOrder({ orderStatus: 'COMPLETED' })).toBe(false);
    expect(canAdminApproveRefund({ orderStatus: 'REFUND_REQUESTED' })).toBe(true);
    expect(
      getAdminOrderActions({ orderStatus: 'REFUND_REQUESTED' }).map(
        (action) => action.key
      )
    ).toEqual(['view', 'approveRefund']);
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
