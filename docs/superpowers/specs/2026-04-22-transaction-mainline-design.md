# Transaction Mainline Design

## Scope

This spec covers only the trading mainline:

`item list/detail -> create order -> pay -> seller ships -> buyer confirms receipt -> buyer reviews`

It does not include chat, full refund workflow, admin exports, timeout automation, or broad platform-wide performance work.

## Context And Problem Summary

Based on the conversation and codebase review, the current trading flow has three classes of issues:

1. Functional defects
   The frontend order page calls routes that do not match the backend contract, including `/api/orders/my`, `/api/orders/{id}/confirm`, and `/api/reviews`.
2. Logic defects
   Order state naming and transitions are inconsistent across docs, frontend code, backend services, and tests. The current backend uses `PAID`, `SHIPPED`, `DELIVERED`, `COMPLETED`, while the frontend interprets `PAID` as "waiting for receipt".
3. UX and performance issues
   Users do not get a clear next action for each order state, some buttons are shown at the wrong time, login redirect behavior is inconsistent, and order lists depend on unstable field shapes.

These issues block the core buyer/seller transaction flow and make the current automated tests low confidence.

## Goals

1. Restore a single, working trading mainline for buyers and sellers.
2. Make the backend state machine match the documented business flow.
3. Make frontend pages consume one stable API contract.
4. Remove obvious runtime defects and flow breaks in the order and item-detail pages.
5. Add enough automated coverage to prove the repaired flow works end to end.

## Non-Goals

1. Full refund workflow redesign.
2. Chat integration.
3. Admin-side order-management redesign outside the mainline.
4. Scheduled jobs for auto-cancel or auto-complete.
5. Large-scale infrastructure tuning.

## User-Facing Business Flow

The intended business flow is:

1. Buyer opens an item detail page and creates an order.
2. Order enters `PENDING_PAYMENT`.
3. Buyer pays successfully.
4. Order enters `PENDING_SHIPMENT`.
5. Seller ships the order.
6. Order enters `SHIPPED`.
7. Buyer confirms receipt.
8. Order enters `COMPLETED`.
9. Buyer can submit one review for the completed order.

Cancel branch:

1. If the order is still `PENDING_PAYMENT`, the buyer can cancel it.
2. The order enters `CANCELLED`.

## Canonical Order State Machine

The system will use this state machine as the single source of truth for the mainline:

- `PENDING_PAYMENT`
  Order created, waiting for payment.
- `PENDING_SHIPMENT`
  Buyer has paid, seller has not shipped yet.
- `SHIPPED`
  Seller has shipped, buyer has not confirmed receipt yet.
- `COMPLETED`
  Buyer confirmed receipt, order is complete and reviewable.
- `CANCELLED`
  Unpaid order cancelled by buyer.

The refund-related states remain in the model for compatibility:

- `REFUND_REQUESTED`
- `REFUNDED`

For the mainline, these old status names are not kept as active business steps:

- `PAID`
- `DELIVERED`

Historical rows using those values must be migrated so the frontend no longer needs special-case mapping for them.

## Backend Design

### Domain Rules

The backend will enforce these rules:

1. Only an item in `ON_SALE` can be ordered.
2. A buyer cannot buy their own item.
3. A buyer cannot create another active order for the same item.
4. Paying an order transitions it from `PENDING_PAYMENT` to `PENDING_SHIPMENT`.
5. Shipping an order is seller-only and transitions `PENDING_SHIPMENT` to `SHIPPED`.
6. Confirming receipt is buyer-only and transitions `SHIPPED` to `COMPLETED`.
7. Only `COMPLETED` orders can be reviewed.
8. A completed order can only be reviewed once by its buyer.

### API Contract

The frontend will consume only these routes for the mainline:

- `POST /api/orders`
  Create a buyer order.
- `GET /api/orders`
  Get buyer orders.
- `GET /api/orders/seller`
  Get seller orders.
- `GET /api/orders/{id}`
  Get one order detail.
- `POST /api/orders/{id}/pay`
  Pay an order.
- `POST /api/orders/{id}/cancel`
  Cancel an unpaid order. Requires a request body with a cancel reason.
- `POST /api/orders/{id}/ship`
  Seller ships an order.
- `POST /api/orders/{id}/confirm-receive`
  Buyer confirms receipt.
- `POST /api/reviews/order/{orderId}`
  Buyer submits a review for a completed order.

The frontend must stop calling:

- `/api/orders/my`
- `/api/orders/{id}/confirm`
- `/api/reviews`

### Response Shape

Order responses should not expose the raw entity contract directly to the UI. A stable response shape should include at least:

- `id`
- `orderNo`
- `itemId`
- `itemTitle`
- `itemCover`
- `buyerId`
- `sellerId`
- `price`
- `orderStatus`
- `createdAt`
- `reviewed`

This keeps the UI independent from persistence-level field naming such as `itemImage` or legacy `status` assumptions.

### Performance Considerations

The order list must render from order snapshot fields instead of forcing extra item-detail lookups. The backend should also avoid per-row review existence checks by using a batch query or derived mapping for the order page.

## Frontend Design

### Order Page Behavior

The order page will support two views:

1. Buyer view: orders returned by `GET /api/orders`
2. Seller view: orders returned by `GET /api/orders/seller`

Action visibility will be deterministic:

- `PENDING_PAYMENT`
  Buyer sees `Pay Now` and `Cancel Order`
- `PENDING_SHIPMENT`
  Buyer sees `Waiting for Seller to Ship`
  Seller sees `Ship Order`
- `SHIPPED`
  Buyer sees `Confirm Receipt`
- `COMPLETED`
  Buyer sees `Review Now` or `Reviewed`
- `CANCELLED`
  No action buttons

The page will consume `orderStatus` only. If compatibility mapping is needed briefly, it should live in one adapter path instead of being repeated in page logic.

### Item Detail Behavior

The item detail page will:

1. Redirect unauthenticated buyers to login before purchase.
2. Preserve the return path to the current item detail page.
3. Submit order creation through the shared API layer.
4. After success, redirect to the order page and show a clear next-step message.
5. Stop using direct network calls that bypass the shared request layer.

### UX Improvements

1. Each state should show one clear next action.
2. Buttons that trigger mutations should show loading and block double-submit.
3. Success feedback should explain what changed, not just say "operation succeeded".
4. Empty states should distinguish "no bought orders" from "no sold orders".

## Data Migration

A Flyway migration will normalize historical order states:

- `PAID` -> `PENDING_SHIPMENT`
- `DELIVERED` -> `COMPLETED`

This avoids carrying legacy status translation logic throughout the frontend.

## Error Handling

### Backend

Business rule violations should continue returning clear, user-readable messages such as:

- "Cannot buy your own item"
- "Only unpaid orders can be cancelled"
- "Only completed orders can be reviewed"

### Frontend

The frontend should:

1. Use shared API handling for auth failures and business errors.
2. Show business messages directly when safe.
3. Avoid generic "operation failed" messages when the backend already returned a useful cause.

## Testing Strategy

### Backend Tests

1. Update order service tests to match the new state machine.
2. Add tests for invalid transitions and authorization boundaries.
3. Add controller-level integration tests for the repaired order and review routes.

### Frontend Tests

1. Add unit coverage for order-status-to-action mapping.
2. Add item-detail purchase/login redirect tests.
3. Extend Playwright coverage to a real transaction mainline:
   buyer creates order -> buyer pays -> seller ships -> buyer confirms receipt -> buyer reviews

### Manual Validation

The final manual verification pass should confirm:

1. Buyer flow works end to end without route mismatches or state confusion.
2. Seller can ship orders from the seller order view.
3. Order list items render without broken title/image fields.
4. Completed orders can be reviewed once and only once.
5. Historical migrated orders still render correctly.

## Success Criteria

This sub-project is complete when:

1. The core trading mainline uses one consistent backend state machine.
2. The frontend no longer calls deprecated or incorrect transaction routes.
3. Buyer and seller order actions align with actual business status.
4. The repaired mainline passes automated tests and manual verification.
5. The mainline UX is clearer, faster, and less error-prone than the current implementation.

## Risks And Mitigations

1. Legacy data may still contain old state values.
   Mitigation: normalize them through a migration rather than frontend fallbacks.
2. Frontend may still rely on inconsistent storage keys or field names.
   Mitigation: centralize API and session access in shared modules.
3. Existing tests may pass while the real flow is still broken.
   Mitigation: add a single end-to-end transaction path that exercises both buyer and seller actions.
