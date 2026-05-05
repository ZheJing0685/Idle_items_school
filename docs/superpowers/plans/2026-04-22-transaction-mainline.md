# Transaction Mainline Repair Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore the buyer/seller transaction mainline so order creation, payment, shipping, receipt confirmation, and review all work end to end against one consistent business state machine.

**Architecture:** Normalize the backend order lifecycle first, then expose a stable order DTO contract to the frontend, then rebuild the order and item-detail pages around that contract. Finish by adding backend rule tests, frontend unit tests, and one deterministic Playwright transaction flow to verify the repaired path.

**Tech Stack:** Spring Boot 3, Spring MVC, Spring Data JPA, Flyway, Vue 3, Pinia, Axios, Vitest, Playwright, Maven, npm

***

**Repository note:** `D:\Project\Idle_items_school` is currently not a git repository, so the plan uses review checkpoints instead of required commit steps. If `.git` is initialized later, convert each checkpoint into a commit using the suggested message.

## File Structure

- Create: `backend/src/main/resources/db/migration/V7__normalize_order_statuses.sql`
  Normalize legacy order statuses in existing rows.
- Create: `backend/src/main/java/com/idleitems/school/dto/order/OrderSummaryResponse.java`
  Stable response model for buyer/seller order lists and detail pages.
- Create: `backend/src/test/java/com/idleitems/school/controller/OrderControllerTest.java`
  Contract test for order list endpoints.
- Create: `backend/src/test/java/com/idleitems/school/service/ReviewServiceTest.java`
  Review business rule tests for completed-only review flow.
- Create: `frontend/tests/unit/views/ItemDetail.test.js`
  Unit tests for login redirect and order creation flow on the item detail page.
- Create: `frontend/tests/unit/views/OrderList.test.js`
  Unit tests for order state rendering and buyer/seller actions.
- Modify: `backend/src/main/java/com/idleitems/school/entity/Order.java`
  Canonical order status enum and entity timestamps.
- Modify: `backend/src/main/java/com/idleitems/school/service/OrderService.java`
  Mainline order state transitions and DTO mapping helpers.
- Modify: `backend/src/main/java/com/idleitems/school/controller/OrderController.java`
  Return stable DTOs and keep only the canonical mainline routes.
- Modify: `backend/src/main/java/com/idleitems/school/repository/ReviewRepository.java`
  Batch lookup for reviewed order IDs.
- Modify: `backend/src/main/java/com/idleitems/school/service/ReviewService.java`
  Allow reviews only for completed orders.
- Modify: `backend/src/test/java/com/idleitems/school/service/OrderServiceTest.java`
  Align tests with the new order state machine.
- Modify: `frontend/src/api/index.js`
  Shared order/review request methods used by pages.
- Modify: `frontend/src/views/ItemDetail.vue`
  Login redirect, shared API usage, and post-order navigation.
- Modify: `frontend/src/views/OrderList.vue`
  Buyer/seller order views and state-driven actions.
- Modify: `frontend/tests/e2e/user-flows.spec.js`
  Deterministic end-to-end transaction mainline test.

### Task 1: Normalize the backend order state machine

**Files:**

- Create: `backend/src/main/resources/db/migration/V7__normalize_order_statuses.sql`
- Modify: `backend/src/main/java/com/idleitems/school/entity/Order.java`
- Modify: `backend/src/main/java/com/idleitems/school/service/OrderService.java`
- Modify: `backend/src/test/java/com/idleitems/school/service/OrderServiceTest.java`
- [ ] **Step 1: Write the failing order state tests**

Update `backend/src/test/java/com/idleitems/school/service/OrderServiceTest.java` so the flow asserts the documented business states:

```java
@Test
@DisplayName("测试支付订单 - 成功")
void testPayOrderSuccess() {
    when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
    when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Order result = orderService.payOrder(1L, 1L, "WECHAT_PAY");

    assertNotNull(result);
    assertEquals(Order.OrderStatus.PENDING_SHIPMENT, result.getOrderStatus());
}

@Test
@DisplayName("测试发货订单 - 成功")
void testShipOrderSuccess() {
    testOrder.setOrderStatus(Order.OrderStatus.PENDING_SHIPMENT);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Order result = orderService.shipOrder(1L, 2L);

    assertNotNull(result);
    assertEquals(Order.OrderStatus.SHIPPED, result.getOrderStatus());
}

@Test
@DisplayName("测试确认收货 - 成功")
void testConfirmReceiveSuccess() {
    testOrder.setOrderStatus(Order.OrderStatus.SHIPPED);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Order result = orderService.confirmReceive(1L, 1L);

    assertNotNull(result);
    assertEquals(Order.OrderStatus.COMPLETED, result.getOrderStatus());
}
```

- [ ] **Step 2: Run the service tests to verify they fail**

Run in `backend/`:

```bash
mvn -Dtest=OrderServiceTest test
```

Expected: FAIL because `Order.OrderStatus.PENDING_SHIPMENT` does not exist yet and `confirmReceive()` still returns `DELIVERED`.

- [ ] **Step 3: Implement the normalized order states and migration**

Update `backend/src/main/java/com/idleitems/school/entity/Order.java`:

```java
public enum OrderStatus {
    PENDING_PAYMENT,
    PENDING_SHIPMENT,
    SHIPPED,
    COMPLETED,
    CANCELLED,
    REFUND_REQUESTED,
    REFUNDED
}
```

Update `backend/src/main/java/com/idleitems/school/service/OrderService.java`:

```java
List<Order.OrderStatus> activeStatuses = List.of(
        Order.OrderStatus.PENDING_PAYMENT,
        Order.OrderStatus.PENDING_SHIPMENT,
        Order.OrderStatus.SHIPPED,
        Order.OrderStatus.REFUND_REQUESTED
);
```

Update the order snapshot inside `createOrder()`:

```java
order.setItemTitle(item.getTitle());
order.setItemImage(item.getCoverImage() != null ? item.getCoverImage() : "");
```

```java
public Order payOrder(Long orderId, Long userId, String paymentMethod) {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

    if (!order.getBuyerId().equals(userId)) {
        throw new IllegalArgumentException("无权操作此订单");
    }

    if (order.getOrderStatus() != Order.OrderStatus.PENDING_PAYMENT) {
        throw new IllegalArgumentException("订单状态不正确，无法支付");
    }

    order.setOrderStatus(Order.OrderStatus.PENDING_SHIPMENT);
    order.setPaymentMethod(paymentMethod);
    order.setPaymentTime(LocalDateTime.now());

    Item item = itemRepository.findById(order.getItemId())
            .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
    item.setStatus(Item.ItemStatus.SOLD);
    itemRepository.save(item);

    return orderRepository.save(order);
}
```

```java
public Order shipOrder(Long orderId, Long userId) {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

    if (!order.getSellerId().equals(userId)) {
        throw new IllegalArgumentException("无权操作此订单");
    }

    if (order.getOrderStatus() != Order.OrderStatus.PENDING_SHIPMENT) {
        throw new IllegalArgumentException("只有待发货的订单才能发货");
    }

    order.setOrderStatus(Order.OrderStatus.SHIPPED);
    order.setShipTime(LocalDateTime.now());
    return orderRepository.save(order);
}
```

```java
public Order confirmReceive(Long orderId, Long userId) {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在"));

    if (!order.getBuyerId().equals(userId)) {
        throw new IllegalArgumentException("无权操作此订单");
    }

    if (order.getOrderStatus() != Order.OrderStatus.SHIPPED) {
        throw new IllegalArgumentException("只有已发货的订单才能确认收货");
    }

    order.setOrderStatus(Order.OrderStatus.COMPLETED);
    order.setCompleteTime(LocalDateTime.now());
    return orderRepository.save(order);
}
```

Create `backend/src/main/resources/db/migration/V7__normalize_order_statuses.sql`:

```sql
UPDATE orders
SET order_status = 'PENDING_SHIPMENT'
WHERE order_status = 'PAID';

UPDATE orders
SET order_status = 'COMPLETED',
    complete_time = COALESCE(complete_time, deliver_time, updated_at, created_at)
WHERE order_status = 'DELIVERED';
```

- [ ] **Step 4: Run the backend service tests again**

Run in `backend/`:

```bash
mvn -Dtest=OrderServiceTest test
```

Expected: PASS

- [ ] **Step 5: Review checkpoint**

Verify that only these files changed:

- `backend/src/main/resources/db/migration/V7__normalize_order_statuses.sql`
- `backend/src/main/java/com/idleitems/school/entity/Order.java`
- `backend/src/main/java/com/idleitems/school/service/OrderService.java`
- `backend/src/test/java/com/idleitems/school/service/OrderServiceTest.java`

Optional once git exists:

```bash
git add backend/src/main/resources/db/migration/V7__normalize_order_statuses.sql backend/src/main/java/com/idleitems/school/entity/Order.java backend/src/main/java/com/idleitems/school/service/OrderService.java backend/src/test/java/com/idleitems/school/service/OrderServiceTest.java
git commit -m "fix: normalize transaction order state machine"
```

### Task 2: Expose a stable order response contract

**Files:**

- Create: `backend/src/main/java/com/idleitems/school/dto/order/OrderSummaryResponse.java`
- Create: `backend/src/test/java/com/idleitems/school/controller/OrderControllerTest.java`
- Modify: `backend/src/main/java/com/idleitems/school/repository/ReviewRepository.java`
- Modify: `backend/src/main/java/com/idleitems/school/service/OrderService.java`
- Modify: `backend/src/main/java/com/idleitems/school/controller/OrderController.java`
- [ ] **Step 1: Write a failing controller contract test**

Create `backend/src/test/java/com/idleitems/school/controller/OrderControllerTest.java`:

```java
package com.idleitems.school.controller;

import com.idleitems.school.dto.order.OrderSummaryResponse;
import com.idleitems.school.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void getBuyerOrdersReturnsStableContract() throws Exception {
        OrderSummaryResponse summary = new OrderSummaryResponse();
        summary.setId(1L);
        summary.setOrderNo("ORD202604220001");
        summary.setItemId(11L);
        summary.setItemTitle("二手手机");
        summary.setItemCover("/uploads/item-11-cover.jpg");
        summary.setBuyerId(1L);
        summary.setSellerId(2L);
        summary.setPrice(BigDecimal.valueOf(3999));
        summary.setOrderStatus("PENDING_PAYMENT");
        summary.setCreatedAt(LocalDateTime.of(2026, 4, 22, 12, 0));
        summary.setReviewed(false);

        when(orderService.getBuyerOrderSummaries(eq(1L), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/orders").requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].itemCover").value("/uploads/item-11-cover.jpg"))
                .andExpect(jsonPath("$.data.content[0].orderStatus").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.data.content[0].reviewed").value(false));
    }
}
```

- [ ] **Step 2: Run the controller test to verify it fails**

Run in `backend/`:

```bash
mvn -Dtest=OrderControllerTest test
```

Expected: FAIL because `OrderSummaryResponse` and `getBuyerOrderSummaries()` do not exist yet.

- [ ] **Step 3: Implement the DTO, review lookup, and controller mapping**

Create `backend/src/main/java/com/idleitems/school/dto/order/OrderSummaryResponse.java`:

```java
package com.idleitems.school.dto.order;

import com.idleitems.school.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderSummaryResponse {
    private Long id;
    private String orderNo;
    private Long itemId;
    private String itemTitle;
    private String itemCover;
    private Long buyerId;
    private Long sellerId;
    private BigDecimal price;
    private String orderStatus;
    private LocalDateTime createdAt;
    private boolean reviewed;

    public static OrderSummaryResponse from(Order order, boolean reviewed) {
        OrderSummaryResponse response = new OrderSummaryResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setItemId(order.getItemId());
        response.setItemTitle(order.getItemTitle());
        response.setItemCover(order.getItemImage());
        response.setBuyerId(order.getBuyerId());
        response.setSellerId(order.getSellerId());
        response.setPrice(order.getPrice());
        response.setOrderStatus(order.getOrderStatus().name());
        response.setCreatedAt(order.getCreatedAt());
        response.setReviewed(reviewed);
        return response;
    }
}
```

Update `backend/src/main/java/com/idleitems/school/repository/ReviewRepository.java`:

```java
@Query("SELECT r.orderId FROM Review r WHERE r.orderId IN :orderIds AND r.reviewerId = :reviewerId")
java.util.List<Long> findReviewedOrderIds(
        @Param("orderIds") java.util.List<Long> orderIds,
        @Param("reviewerId") Long reviewerId
);
```

Update `backend/src/main/java/com/idleitems/school/service/OrderService.java`:

```java
private final ReviewRepository reviewRepository;
```

```java
public Page<OrderSummaryResponse> getBuyerOrderSummaries(Long buyerId, Order.OrderStatus status, Pageable pageable) {
    Page<Order> orders = getBuyerOrders(buyerId, status, pageable);
    java.util.List<Long> orderIds = orders.getContent().stream()
            .map(Order::getId)
            .toList();
    java.util.Set<Long> reviewedOrderIds = new java.util.HashSet<>(
            orderIds.isEmpty() ? java.util.List.of() : reviewRepository.findReviewedOrderIds(orderIds, buyerId)
    );
    return orders.map(order -> OrderSummaryResponse.from(order, reviewedOrderIds.contains(order.getId())));
}

public Page<OrderSummaryResponse> getSellerOrderSummaries(Long sellerId, Order.OrderStatus status, Pageable pageable) {
    Page<Order> orders = getSellerOrders(sellerId, status, pageable);
    return orders.map(order -> OrderSummaryResponse.from(order, false));
}
```

Update `backend/src/main/java/com/idleitems/school/controller/OrderController.java`:

```java
@GetMapping
public Result<Page<OrderSummaryResponse>> getBuyerOrders(
        @RequestAttribute("userId") Long userId,
        @RequestParam(value = "status", required = false) Order.OrderStatus status,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    return Result.success(orderService.getBuyerOrderSummaries(userId, status, pageable));
}

@GetMapping("/seller")
public Result<Page<OrderSummaryResponse>> getSellerOrders(
        @RequestAttribute("userId") Long userId,
        @RequestParam(value = "status", required = false) Order.OrderStatus status,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    return Result.success(orderService.getSellerOrderSummaries(userId, status, pageable));
}
```

- [ ] **Step 4: Run the controller contract test**

Run in `backend/`:

```bash
mvn -Dtest=OrderControllerTest test
```

Expected: PASS

- [ ] **Step 5: Review checkpoint**

Verify that only these files changed:

- `backend/src/main/java/com/idleitems/school/dto/order/OrderSummaryResponse.java`
- `backend/src/test/java/com/idleitems/school/controller/OrderControllerTest.java`
- `backend/src/main/java/com/idleitems/school/repository/ReviewRepository.java`
- `backend/src/main/java/com/idleitems/school/service/OrderService.java`
- `backend/src/main/java/com/idleitems/school/controller/OrderController.java`

Optional once git exists:

```bash
git add backend/src/main/java/com/idleitems/school/dto/order/OrderSummaryResponse.java backend/src/test/java/com/idleitems/school/controller/OrderControllerTest.java backend/src/main/java/com/idleitems/school/repository/ReviewRepository.java backend/src/main/java/com/idleitems/school/service/OrderService.java backend/src/main/java/com/idleitems/school/controller/OrderController.java
git commit -m "feat: expose stable transaction order response contract"
```

### Task 3: Restrict reviews to completed orders only

**Files:**

- Create: `backend/src/test/java/com/idleitems/school/service/ReviewServiceTest.java`
- Modify: `backend/src/main/java/com/idleitems/school/service/ReviewService.java`
- [ ] **Step 1: Write the failing review rule tests**

Create `backend/src/test/java/com/idleitems/school/service/ReviewServiceTest.java`:

```java
package com.idleitems.school.service;

import com.idleitems.school.dto.order.CreateReviewRequest;
import com.idleitems.school.entity.Order;
import com.idleitems.school.repository.OrderRepository;
import com.idleitems.school.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@DisplayName("ReviewService 单元测试")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Order order;
    private CreateReviewRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        order = new Order();
        order.setId(1L);
        order.setBuyerId(1L);
        order.setSellerId(2L);
        order.setItemId(3L);

        request = new CreateReviewRequest();
        request.setRating(5);
        request.setContent("物品描述准确，交易顺利");
    }

    @Test
    @DisplayName("非已完成订单不能评价")
    void shouldRejectReviewForNonCompletedOrder() {
        order.setOrderStatus(Order.OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(1L, 1L, request));
    }

    @Test
    @DisplayName("已评价订单不能重复评价")
    void shouldRejectDuplicateReview() {
        order.setOrderStatus(Order.OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(reviewRepository.existsByOrderIdAndReviewerId(1L, 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(1L, 1L, request));
    }
}
```

- [ ] **Step 2: Run the review service tests to verify they fail**

Run in `backend/`:

```bash
mvn -Dtest=ReviewServiceTest test
```

Expected: FAIL because `ReviewService` still allows `DELIVERED`.

- [ ] **Step 3: Implement the completed-only review rule**

Update `backend/src/main/java/com/idleitems/school/service/ReviewService.java`:

```java
if (order.getOrderStatus() != Order.OrderStatus.COMPLETED) {
    throw new IllegalArgumentException("只能在订单完成后评价");
}
```

Keep the duplicate review rule:

```java
if (reviewRepository.existsByOrderIdAndReviewerId(orderId, reviewerId)) {
    throw new IllegalArgumentException("您已评价过此订单");
}
```

- [ ] **Step 4: Run the order and review service tests together**

Run in `backend/`:

```bash
mvn -Dtest=OrderServiceTest,ReviewServiceTest test
```

Expected: PASS

- [ ] **Step 5: Review checkpoint**

Verify that only these files changed:

- `backend/src/test/java/com/idleitems/school/service/ReviewServiceTest.java`
- `backend/src/main/java/com/idleitems/school/service/ReviewService.java`

Optional once git exists:

```bash
git add backend/src/test/java/com/idleitems/school/service/ReviewServiceTest.java backend/src/main/java/com/idleitems/school/service/ReviewService.java
git commit -m "fix: allow transaction reviews only for completed orders"
```

### Task 4: Repair shared frontend transaction requests and item detail flow

**Files:**

- Create: `frontend/tests/unit/views/ItemDetail.test.js`
- Modify: `frontend/src/api/index.js`
- Modify: `frontend/src/views/ItemDetail.vue`
- [ ] **Step 1: Write the failing item detail flow test**

Create `frontend/tests/unit/views/ItemDetail.test.js`:

```javascript
import { describe, it, expect, vi, beforeAll, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'

const push = vi.fn()
const route = { params: { id: '1' }, fullPath: '/item/1' }

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push })
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn()
  }
}))

vi.mock('@/api', () => ({
  default: {
    item: { getItem: vi.fn(), offShelfItem: vi.fn() },
    review: { getReviewsByItem: vi.fn() },
    favorite: { checkFavorite: vi.fn(), addFavorite: vi.fn(), removeFavorite: vi.fn() },
    order: { createOrder: vi.fn() }
  }
}))

let ItemDetail
beforeAll(async () => {
  ItemDetail = (await import('@/views/ItemDetail.vue')).default
})

describe('ItemDetail transaction flow', () => {
  beforeEach(() => {
    localStorage.clear()
    push.mockReset()
  })

  it('redirects unauthenticated buyers to login before purchase', async () => {
    const wrapper = mount(ItemDetail, {
      global: {
        stubs: ['el-button', 'el-dialog', 'el-form', 'el-form-item', 'el-input', 'el-avatar']
      }
    })

    await wrapper.vm.handleBuy()

    expect(localStorage.getItem('redirectPath')).toBe('/item/1')
    expect(push).toHaveBeenCalledWith('/login')
  })
})
```

- [ ] **Step 2: Run the frontend unit test to verify it fails**

Run in `frontend/`:

```bash
npm run test:unit -- tests/unit/views/ItemDetail.test.js
```

Expected: FAIL because `ItemDetail.vue` still mixes `userInfo`, direct `axios`, and old order flow assumptions.

- [ ] **Step 3: Implement the shared order/review API methods and repair item detail**

Update `frontend/src/api/index.js`:

```javascript
const review = {
  createReview: (orderId, data) => instance.post(`/reviews/order/${orderId}`, data),
  getReviewsByItem: (itemId, page = 1, size = 10) =>
    instance.get(`/reviews/item/${itemId}`, { params: { page, size } }),
}

const order = {
  createOrder: (data) => instance.post('/orders', data),
  getBuyerOrders: (status, page, size) => instance.get('/orders', { params: { status, page, size } }),
  getSellerOrders: (status, page, size) => instance.get('/orders/seller', { params: { status, page, size } }),
  getOrder: (id) => instance.get(`/orders/${id}`),
  payOrder: (id, paymentMethod = 'OFFLINE') =>
    instance.post(`/orders/${id}/pay`, null, { params: { paymentMethod } }),
  cancelOrder: (id, reason) => instance.post(`/orders/${id}/cancel`, { reason }),
  shipOrder: (id) => instance.post(`/orders/${id}/ship`),
  confirmReceive: (id) => instance.post(`/orders/${id}/confirm-receive`),
}
```

Add this method inside the existing `item` API object:

```javascript
offShelfItem: (id) => instance.put(`/items/${id}/off-shelf`),
```

Update `frontend/src/views/ItemDetail.vue`:

```javascript
const currentUserId = computed(() => {
  const rawUser = localStorage.getItem('user')
  if (!rawUser) return null
  try {
    return JSON.parse(rawUser).id ?? null
  } catch (error) {
    return null
  }
})
```

```javascript
const handleBuy = () => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    localStorage.setItem('redirectPath', route.fullPath)
    router.push('/login')
    return
  }
  showBuyDialog.value = true
}
```

```javascript
const confirmBuy = async () => {
  try {
    await api.order.createOrder({
      itemId: item.value.id,
      ...orderForm.value,
    })
    ElMessage.success('订单已创建，请完成支付')
    showBuyDialog.value = false
    router.push('/orders?view=buyer')
  } catch (error) {
    ElMessage.error(error.message || '下单失败')
  }
}
```

```javascript
const offShelf = async () => {
  try {
    await api.item.offShelfItem(item.value.id)
    ElMessage.success('物品已下架')
    await fetchItemDetail()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}
```

- [ ] **Step 4: Run the repaired frontend unit test**

Run in `frontend/`:

```bash
npm run test:unit -- tests/unit/views/ItemDetail.test.js
```

Expected: PASS

- [ ] **Step 5: Review checkpoint**

Verify that only these files changed:

- `frontend/tests/unit/views/ItemDetail.test.js`
- `frontend/src/api/index.js`
- `frontend/src/views/ItemDetail.vue`

Optional once git exists:

```bash
git add frontend/tests/unit/views/ItemDetail.test.js frontend/src/api/index.js frontend/src/views/ItemDetail.vue
git commit -m "fix: repair item detail transaction entry flow"
```

### Task 5: Rebuild the order page around the canonical transaction flow

**Files:**

- Create: `frontend/tests/unit/views/OrderList.test.js`
- Modify: `frontend/src/views/OrderList.vue`
- [ ] **Step 1: Write the failing order page tests**

Create `frontend/tests/unit/views/OrderList.test.js`:

```javascript
import { describe, it, expect, vi, beforeAll, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'

const push = vi.fn()
const apiMock = {
  order: {
    getBuyerOrders: vi.fn(),
    getSellerOrders: vi.fn(),
    payOrder: vi.fn(),
    cancelOrder: vi.fn(),
    shipOrder: vi.fn(),
    confirmReceive: vi.fn(),
  },
  review: {
    createReview: vi.fn(),
  },
}

vi.mock('vue-router', () => ({
  useRouter: () => ({ push })
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
  },
  ElMessageBox: {
    confirm: vi.fn().mockResolvedValue(true),
  }
}))

vi.mock('@/api', () => ({
  default: apiMock
}))

let OrderList
beforeAll(async () => {
  OrderList = (await import('@/views/OrderList.vue')).default
})

describe('OrderList mainline actions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('shows pay and cancel for buyer pending payment orders', async () => {
    apiMock.order.getBuyerOrders.mockResolvedValue({
      code: 200,
      data: {
        content: [{
          id: 1,
          itemId: 11,
          itemTitle: '二手手机',
          itemCover: '/uploads/item-11-cover.jpg',
          price: 3999,
          orderStatus: 'PENDING_PAYMENT',
          reviewed: false,
          createdAt: '2026-04-22T12:00:00'
        }],
        totalElements: 1
      }
    })

    const wrapper = mount(OrderList, {
      global: {
        stubs: ['el-button', 'el-dialog', 'el-input', 'el-pagination']
      }
    })

    await Promise.resolve()
    expect(wrapper.text()).toContain('立即支付')
    expect(wrapper.text()).toContain('取消订单')
  })

  it('shows ship action for seller pending shipment orders', async () => {
    apiMock.order.getBuyerOrders.mockResolvedValue({
      code: 200,
      data: { content: [], totalElements: 0 }
    })
    apiMock.order.getSellerOrders.mockResolvedValue({
      code: 200,
      data: {
        content: [{
          id: 2,
          itemId: 12,
          itemTitle: '教材',
          itemCover: '/uploads/item-12-cover.jpg',
          price: 25,
          orderStatus: 'PENDING_SHIPMENT',
          reviewed: false,
          createdAt: '2026-04-22T12:00:00'
        }],
        totalElements: 1
      }
    })

    const wrapper = mount(OrderList, {
      global: {
        stubs: ['el-button', 'el-dialog', 'el-input', 'el-pagination']
      }
    })

    wrapper.vm.currentView = 'seller'
    await wrapper.vm.loadOrders()
    await Promise.resolve()

    expect(wrapper.text()).toContain('发货')
  })
})
```

- [ ] **Step 2: Run the order page unit tests to verify they fail**

Run in `frontend/`:

```bash
npm run test:unit -- tests/unit/views/OrderList.test.js
```

Expected: FAIL because `OrderList.vue` still calls deprecated routes, reads `status`, and has no seller shipment flow.

- [ ] **Step 3: Implement the buyer/seller order page and canonical actions**

Update `frontend/src/views/OrderList.vue` so it uses `currentView`, `orderStatus`, and the shared API methods:

```javascript
const route = useRoute()
const currentView = ref(route.query.view === 'seller' ? 'seller' : 'buyer')

const statusTextMap = {
  PENDING_PAYMENT: '待支付',
  PENDING_SHIPMENT: '待发货',
  SHIPPED: '待收货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

const loadOrders = async () => {
  const request = currentView.value === 'buyer'
    ? api.order.getBuyerOrders
    : api.order.getSellerOrders

  const response = await request(currentTab.value === 'ALL' ? undefined : currentTab.value, currentPage.value, pageSize.value)
  if (response.code === 200) {
    orders.value = response.data.content || []
    total.value = response.data.totalElements || 0
  }
}
```

```javascript
watch(currentView, async (view) => {
  await router.replace({ query: { ...route.query, view } })
  currentPage.value = 1
  await loadOrders()
})
```

```javascript
const handlePay = async (order) => {
  await ElMessageBox.confirm('确认支付该订单？', '提示', { type: 'warning' })
  await api.order.payOrder(order.id, 'OFFLINE')
  ElMessage.success('支付成功，等待卖家发货')
  await loadOrders()
}

const handleCancel = async (order) => {
  await ElMessageBox.confirm('确认取消该订单？', '提示', { type: 'warning' })
  await api.order.cancelOrder(order.id, '买家主动取消')
  ElMessage.success('订单已取消')
  await loadOrders()
}

const handleShip = async (order) => {
  await ElMessageBox.confirm('确认已发货？', '提示', { type: 'warning' })
  await api.order.shipOrder(order.id)
  ElMessage.success('已标记为发货')
  await loadOrders()
}

const handleConfirmReceive = async (order) => {
  await ElMessageBox.confirm('确认已收到货物？', '提示', { type: 'warning' })
  await api.order.confirmReceive(order.id)
  ElMessage.success('订单已完成，可以评价了')
  await loadOrders()
}
```

```javascript
const submitReview = async () => {
  if (!reviewContent.value.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }

  await api.review.createReview(currentReviewOrder.value.id, {
    rating: reviewRating.value,
    content: reviewContent.value,
    isAnonymous: false,
  })

  ElMessage.success('评价成功')
  showReviewDialog.value = false
  await loadOrders()
}

const viewDetail = (order) => {
  router.push(`/item/${order.itemId}`)
}
```

Render actions by `order.orderStatus` only:

```vue
<template v-if="currentView === 'buyer' && order.orderStatus === 'PENDING_PAYMENT'">
  <el-button type="primary" size="small" @click="handlePay(order)">立即支付</el-button>
  <el-button size="small" @click="handleCancel(order)">取消订单</el-button>
</template>
<template v-else-if="currentView === 'seller' && order.orderStatus === 'PENDING_SHIPMENT'">
  <el-button size="small" @click="handleShip(order)">发货</el-button>
</template>
<template v-else-if="currentView === 'buyer' && order.orderStatus === 'SHIPPED'">
  <el-button size="small" @click="handleConfirmReceive(order)">确认收货</el-button>
</template>
<template v-else-if="currentView === 'buyer' && order.orderStatus === 'COMPLETED'">
  <el-button size="small" @click="handleReview(order)" :disabled="order.reviewed">
    {{ order.reviewed ? '已评价' : '立即评价' }}
  </el-button>
</template>
```

- [ ] **Step 4: Run the order page unit tests**

Run in `frontend/`:

```bash
npm run test:unit -- tests/unit/views/OrderList.test.js
```

Expected: PASS

- [ ] **Step 5: Review checkpoint**

Verify that only these files changed:

- `frontend/tests/unit/views/OrderList.test.js`
- `frontend/src/views/OrderList.vue`

Optional once git exists:

```bash
git add frontend/tests/unit/views/OrderList.test.js frontend/src/views/OrderList.vue
git commit -m "feat: rebuild transaction order page around canonical flow"
```

### Task 6: Add deterministic transaction flow verification

**Files:**

- Modify: `frontend/tests/e2e/user-flows.spec.js`
- [ ] **Step 1: Add a failing end-to-end transaction scenario**

Append this test to `frontend/tests/e2e/user-flows.spec.js`:

```javascript
test('交易主链路：下单 -> 支付 -> 发货 -> 收货 -> 评价', async ({ page }) => {
  await page.route('**/api/items/1', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 200,
        data: {
          id: 1,
          title: '二手手机',
          price: 3999,
          userId: 2,
          coverImage: '/uploads/item-1-cover.jpg',
          images: '[]'
        }
      })
    })
  })

  let buyerOrders = [{
    id: 101,
    itemId: 1,
    itemTitle: '二手手机',
    itemCover: '/uploads/item-1-cover.jpg',
    price: 3999,
    orderStatus: 'PENDING_PAYMENT',
    reviewed: false,
    createdAt: '2026-04-22T12:00:00'
  }]

  let sellerOrders = [{
    id: 101,
    itemId: 1,
    itemTitle: '二手手机',
    itemCover: '/uploads/item-1-cover.jpg',
    price: 3999,
    orderStatus: 'PENDING_SHIPMENT',
    reviewed: false,
    createdAt: '2026-04-22T12:00:00'
  }]

  await page.route('**/api/orders', async route => {
    const method = route.request().method()
    if (method === 'POST') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 200, data: { id: 101 } })
      })
      return
    }
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ code: 200, data: { content: buyerOrders, totalElements: buyerOrders.length } })
    })
  })

  await page.route('**/api/orders/seller**', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ code: 200, data: { content: sellerOrders, totalElements: sellerOrders.length } })
    })
  })

  await page.route('**/api/orders/101/pay**', async route => {
    buyerOrders = [{ ...buyerOrders[0], orderStatus: 'PENDING_SHIPMENT' }]
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ code: 200, data: {} }) })
  })

  await page.route('**/api/orders/101/ship**', async route => {
    buyerOrders = [{ ...buyerOrders[0], orderStatus: 'SHIPPED' }]
    sellerOrders = [{ ...sellerOrders[0], orderStatus: 'SHIPPED' }]
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ code: 200, data: {} }) })
  })

  await page.route('**/api/orders/101/confirm-receive**', async route => {
    buyerOrders = [{ ...buyerOrders[0], orderStatus: 'COMPLETED', reviewed: false }]
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ code: 200, data: {} }) })
  })

  await page.route('**/api/reviews/order/101', async route => {
    buyerOrders = [{ ...buyerOrders[0], orderStatus: 'COMPLETED', reviewed: true }]
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ code: 200, data: {} }) })
  })

  await page.goto('/login')
  await page.evaluate(() => {
    localStorage.setItem('token', 'buyer-token')
    localStorage.setItem('user', JSON.stringify({ id: 1, username: 'buyer', role: 'STUDENT' }))
  })

  await page.goto('/item/1')
  await page.getByRole('button', { name: '立即购买' }).click()
  await page.getByPlaceholder('请输入收货人姓名').fill('张三')
  await page.getByPlaceholder('请输入联系电话').fill('13800138000')
  await page.getByPlaceholder('请输入详细收货地址').fill('教学楼 101')
  await page.getByRole('button', { name: '确认购买' }).click()

  await page.goto('/orders')
  await page.getByRole('button', { name: '立即支付' }).click()

  await page.evaluate(() => {
    localStorage.setItem('user', JSON.stringify({ id: 2, username: 'seller', role: 'STUDENT' }))
  })
  await page.goto('/orders?view=seller')
  await page.getByRole('button', { name: '发货' }).click()

  await page.evaluate(() => {
    localStorage.setItem('user', JSON.stringify({ id: 1, username: 'buyer', role: 'STUDENT' }))
  })
  await page.goto('/orders')
  await page.getByRole('button', { name: '确认收货' }).click()
  await page.getByRole('button', { name: '立即评价' }).click()
  await page.locator('textarea').fill('交易顺利，描述一致')
  await page.getByRole('button', { name: '提交评价' }).click()
})
```

- [ ] **Step 2: Run the end-to-end test to verify it fails**

Run in `frontend/`:

```bash
npm run test:e2e -- tests/e2e/user-flows.spec.js
```

Expected: FAIL until Tasks 4 and 5 are implemented and the order page supports the canonical flow.

- [ ] **Step 3: Re-run the end-to-end suite after Tasks 4 and 5**

Run in `frontend/`:

```bash
npm run test:e2e -- tests/e2e/user-flows.spec.js
```

Expected: PASS

- [ ] **Step 4: Run the focused regression suites**

Run in `backend/`:

```bash
mvn -Dtest=OrderServiceTest,ReviewServiceTest,OrderControllerTest test
```

Run in `frontend/`:

```bash
npm run test:unit -- tests/unit/views/ItemDetail.test.js tests/unit/views/OrderList.test.js
npm run test:e2e -- tests/e2e/user-flows.spec.js
```

Expected: PASS on all commands

- [ ] **Step 5: Manual acceptance pass**

Verify these manual checks with a running frontend and backend:

1. Buyer can create an order from item detail and is redirected to the order page.
2. Buyer sees `待支付` after order creation and `待发货` after payment.
3. Seller sees `发货` only when the order is `PENDING_SHIPMENT`.
4. Buyer sees `确认收货` only when the order is `SHIPPED`.
5. Buyer sees `立即评价` only when the order is `COMPLETED`.
6. Completed orders cannot be reviewed twice.

Optional once git exists:

```bash
git add frontend/tests/e2e/user-flows.spec.js
git commit -m "test: cover transaction mainline end to end"
```

