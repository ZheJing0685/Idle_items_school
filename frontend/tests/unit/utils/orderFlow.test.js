import {
  OrderStatus,
  getOrderActions,
  getOrderHint,
  getOrderStatusClass,
  getOrderStatusOptions,
  getOrderStatusText,
  normalizeOrder,
  sanitizeOrderStatus,
  sanitizeOrderView,
} from '@/utils/business/orderFlow';

describe('orderFlow', () => {
  it('normalizes legacy order fields to the new contract', () => {
    expect(
      normalizeOrder({
        id: 1,
        orderStatus: 'SHIPPED',
        price: 99,
        itemCover: 'cover.jpg',
        itemTitle: 'Desk Lamp',
      })
    ).toEqual(
      expect.objectContaining({
        id: 1,
        orderStatus: 'SHIPPED',
        price: 99,
        itemCover: 'cover.jpg',
        itemTitle: 'Desk Lamp',
      })
    );
  });

  it('returns buyer actions based on order status', () => {
    expect(
      getOrderActions(OrderStatus.PENDING_PAYMENT, 'buyer').map(
        (action) => action.name
      )
    ).toEqual(['付款', '取消订单']);

    expect(
      getOrderActions(OrderStatus.COMPLETED, 'buyer').map((action) => action.name)
    ).toEqual(['评价']);

    expect(
      getOrderActions(OrderStatus.CANCELLED, 'buyer')
    ).toEqual([]);
  });

  it('returns seller actions and hints correctly', () => {
    expect(
      getOrderActions(OrderStatus.PENDING_SHIPMENT, 'seller').map(
        (action) => action.name
      )
    ).toEqual(['发货']);

    expect(
      getOrderHint(OrderStatus.SHIPPED, 'seller')
    ).toBe('');
    
    expect(
      getOrderHint(OrderStatus.PENDING_SHIPMENT, 'seller')
    ).toBe('请尽快发货，避免影响买家体验');
  });

  it('sanitizes views and statuses', () => {
    expect(sanitizeOrderView('seller')).toBe('seller');
    expect(sanitizeOrderView('unknown')).toBe('buyer');
    expect(sanitizeOrderStatus('SHIPPED')).toBe('SHIPPED');
    expect(sanitizeOrderStatus('INVALID')).toBe(OrderStatus.PENDING_PAYMENT);
  });

  it('exposes consistent status labels and classes', () => {
    expect(getOrderStatusText('PENDING_SHIPMENT')).toBe('待发货');
    expect(getOrderStatusClass('SHIPPED')).toBe('status-shipped');
    expect(getOrderStatusOptions('buyer')[0]).toEqual({
      value: 'ALL',
      label: '全部',
    });
  });
});
