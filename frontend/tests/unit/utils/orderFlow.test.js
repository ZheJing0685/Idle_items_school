import {
  ORDER_VIEWS,
  getOrderActions,
  getOrderHint,
  getOrderStatusClass,
  getOrderStatusOptions,
  getOrderStatusText,
  normalizeOrder,
  sanitizeOrderStatus,
  sanitizeOrderView,
} from '@/utils/orderFlow';

describe('orderFlow', () => {
  it('normalizes legacy order fields to the new contract', () => {
    expect(
      normalizeOrder({
        id: 1,
        status: 'SHIPPED',
        totalAmount: 99,
        itemImage: 'cover.jpg',
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

  it('returns buyer actions based on the new state machine', () => {
    expect(
      getOrderActions({ orderStatus: 'PENDING_PAYMENT' }, ORDER_VIEWS.BUYER).map(
        (action) => action.key
      )
    ).toEqual(['pay', 'cancel']);

    expect(
      getOrderActions(
        { orderStatus: 'COMPLETED', reviewed: false },
        ORDER_VIEWS.BUYER
      ).map((action) => action.key)
    ).toEqual(['review']);

    expect(
      getOrderActions(
        { orderStatus: 'COMPLETED', reviewed: true },
        ORDER_VIEWS.BUYER
      )
    ).toEqual([]);
  });

  it('returns seller actions and hints correctly', () => {
    expect(
      getOrderActions(
        { orderStatus: 'PENDING_SHIPMENT' },
        ORDER_VIEWS.SELLER
      ).map((action) => action.key)
    ).toEqual(['ship']);

    expect(
      getOrderHint({ orderStatus: 'SHIPPED' }, ORDER_VIEWS.SELLER)
    ).toBe('等待买家确认收货');
  });

  it('sanitizes views and statuses', () => {
    expect(sanitizeOrderView('seller')).toBe('seller');
    expect(sanitizeOrderView('unknown')).toBe('buyer');
    expect(sanitizeOrderStatus('SHIPPED', ORDER_VIEWS.BUYER)).toBe('SHIPPED');
    expect(sanitizeOrderStatus('PENDING_PAYMENT', ORDER_VIEWS.SELLER)).toBe('ALL');
  });

  it('exposes consistent status labels and classes', () => {
    expect(getOrderStatusText('PENDING_SHIPMENT')).toBe('待发货');
    expect(getOrderStatusClass('SHIPPED')).toBe('status-shipped');
    expect(getOrderStatusOptions(ORDER_VIEWS.BUYER)[0]).toEqual({
      value: 'ALL',
      label: '全部',
    });
  });
});
