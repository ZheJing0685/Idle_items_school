# Admin Order Management Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Align the admin order management page and admin order APIs with the unified transaction state machine and the currently supported admin actions.

**Architecture:** Add stable admin-facing order DTOs and admin order operations in the backend, then rewrite the admin order page to consume only those interfaces and new statuses. Preserve the existing admin layout, but remove fake status transitions and replace them with the real actions the backend supports.

**Tech Stack:** Spring Boot 3.2, Spring Data JPA, Vue 3, Pinia, Element Plus, Vitest, Maven

---

### Task 1: Backend Admin Order Contract

**Files:**
- Create: `backend/src/main/java/com/idleitems/school/dto/order/AdminOrderResponse.java`
- Modify: `backend/src/main/java/com/idleitems/school/repository/OrderRepository.java`
- Modify: `backend/src/main/java/com/idleitems/school/service/OrderService.java`
- Modify: `backend/src/main/java/com/idleitems/school/controller/admin/AdminController.java`

- [ ] Add admin order DTO and repository search query
- [ ] Add admin cancel/approve-refund service methods and stats assembly
- [ ] Switch admin controller order endpoints to unified DTO contract and real actions

### Task 2: Backend Validation

**Files:**
- Modify: `backend/src/test/java/com/idleitems/school/service/OrderServiceTest.java`
- Create: `backend/src/test/java/com/idleitems/school/controller/admin/AdminOrderControllerTest.java`

- [ ] Add service tests for admin cancel and refund approval
- [ ] Add controller tests for admin list, stats, single cancel, and refund approve

### Task 3: Frontend Admin State Mapping

**Files:**
- Create: `frontend/src/utils/adminOrderFlow.js`
- Create: `frontend/tests/unit/utils/adminOrderFlow.test.js`

- [ ] Add pure helpers for admin status text, badges, stats keys, and available actions
- [ ] Add Vitest coverage for status mapping and action gating

### Task 4: Frontend Admin Order Page

**Files:**
- Modify: `frontend/src/views/admin/OrderManagement.vue`

- [ ] Rewrite the page to use unified statuses and real admin actions only
- [ ] Align list filters, detail dialog, bulk operations, and stats cards with new backend contract

### Task 5: Verification

**Files:**
- None

- [ ] Run focused backend tests
- [ ] Run focused frontend unit tests
- [ ] Run frontend build
