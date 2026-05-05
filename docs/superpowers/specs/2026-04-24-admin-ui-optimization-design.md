# 后台管理页面 UI 优化设计

## 概述

对校园闲置物品交易平台后台管理所有页面进行视觉风格升级和用户体验优化，覆盖布局、配色体系、组件一致性、API 调用规范、加载/空状态处理等方面。

## 设计原则

1. **风格统一** — 蓝绿清新风贯穿所有页面，CSS 变量集中管理设计 Token
2. **一致性修复** — 统一 el-table/自定义表格样式、统一 API 调用模式（迁移 fetch → 集中式 api 服务）
3. **体验增强** — 骨架屏加载态、空状态提示、页面过渡动画
4. **Bug 修复** — 客户端分页问题、Mock 数据替换为真实 API

## 1. 设计 Token 系统（CSS 变量）

在 `admin.css` 中定义全局 CSS 变量，所有页面样式引用这些变量。

### 配色

| 变量 | 值 | 用途 |
|------|-----|------|
| `--admin-primary` | `#0891b2` (cyan-600) | 主色 |
| `--admin-primary-light` | `#06b6d4` (cyan-500) | 主色亮 |
| `--admin-primary-dark` | `#0e7490` (cyan-700) | 主色暗 |
| `--admin-primary-bg` | `#ecfeff` (cyan-50) | 主色背景 |
| `--admin-success` | `#10b981` (emerald-500) | 成功 |
| `--admin-warning` | `#f59e0b` (amber-500) | 警告 |
| `--admin-danger` | `#ef4444` (red-500) | 危险 |
| `--admin-info` | `#6366f1` (indigo-500) | 信息 |
| `--admin-accent` | `#f97316` (orange-500) | 珊瑚点缀 |
| `--admin-accent-light` | `#ffedd5` (orange-50) | 点缀背景 |
| `--admin-bg` | `#f0fdfa` | 页面背景 |
| `--admin-bg-card` | `#ffffff` | 卡片背景 |
| `--admin-text-primary` | `#0f172a` (slate-900) | 主文字 |
| `--admin-text-secondary` | `#475569` (slate-600) | 副文字 |
| `--admin-text-muted` | `#94a3b8` (slate-400) | 弱文字 |
| `--admin-border` | `#e2e8f0` (slate-200) | 边框 |
| `--admin-sidebar-bg` | `#0f172a` (slate-900) | 侧边栏背景 |
| `--admin-sidebar-text` | `#cbd5e1` (slate-300) | 侧边栏文字 |
| `--admin-sidebar-active` | `#0891b2` | 侧边栏激活 |

### 尺寸

| 变量 | 值 |
|------|-----|
| `--admin-radius-sm` | `6px` |
| `--admin-radius-md` | `10px` |
| `--admin-radius-lg` | `14px` |
| `--admin-shadow-sm` | `0 1px 2px rgba(0,0,0,0.05)` |
| `--admin-shadow-md` | `0 4px 12px rgba(0,0,0,0.08)` |
| `--admin-shadow-lg` | `0 8px 24px rgba(0,0,0,0.12)` |

## 2. Admin.vue 布局

### 侧边栏
- 宽度 `260px` → `240px`
- Logo 区：使用 `#06b6d4 → #0891b2` 渐变色标题 + 闪电图标
- 导航项：悬停时左侧出现 4px 蓝绿竖条指示器 + 背景微亮 (`rgba(255,255,255,0.05)`)
- 激活项：蓝绿渐变背景 + 左侧 4px 实心竖条
- 底部「返回前台」：悬浮上移动效 (`transform: translateY(-1px)`)

### 顶栏
- 左侧：当前页面标题（动态），`18px` 加粗
- 右侧：用户头像圆形（首字母）+ 名称 + 角色标签（蓝绿 tag）+ 退出按钮（ghost 风格）
- 退出 hover 变 `#ef4444`

### 路由过渡
- `<router-view>` 外包 `<transition name="fade">`，渐入/渐出 0.2s

## 3. 子页面改造

### Statistics.vue
- 统计卡片 4 张，每张左侧 4px 不同颜色竖条（蓝/绿/橙/紫）
- 统计数字白色背景 → 带 `--admin-shadow-sm` 阴影
- 图表卡片标题加小图标装饰，头部加 border-bottom 分割
- 骨架屏：`.skeleton-card` 脉冲动画

### OrderManagement.vue
- 7 个统计卡片改为 4+3 两行布局
  - 第一行 4 个大卡：总订单 / 进行中 / 已完成 / 已取消
  - 第二行 3 个小卡：待付款 / 待发货 / 退款中
- 搜索框加前缀 magnifier 图标，圆角增大
- 表格行 hover 背景 `--admin-primary-bg`
- 详情对话框宽度 `720px` → `800px`，分组更清晰

### UserManagement.vue
- **修复**：API 从 `fetch()` 迁移到集中式 `api` 服务
- **修复**：取消客户端分页/排序，使用服务端分页
- **修复**：批量操作改为真实 API 调用
- 用户列：头像 `el-avatar` + 名称 + 角色标签

### VerificationManagement.vue
- **修复**：API 从 `fetch()` 迁移到集中式 `api` 服务
- 图片预览保留 `el-image` `preview-src-list` 功能
- 审核按钮颜色鲜明（绿色/红色），操作后自动刷新

### ItemManagement.vue
- **修复**：API 从 `fetch()` 迁移到集中式 `api` 服务
- **修复**：硬编码分类和统计数据改为后端获取
- 图片圆角 `8px` → `12px`，添加轻阴影
- 下架原因在详情中用浅红背景高亮

### CategoryManagement.vue
- **修复**：API 从 `fetch()` 迁移到集中式 `api` 服务
- **修复**：硬编码统计改为后端获取
- 层级缩进加竖线装饰线
- 上传拖拽区虚线边框美化

### LogManagement.vue
- **修复**：API 从 `fetch()` 迁移到集中式 `api` 服务
- **修复**：移除 fallback fake data（6 条硬编码数据）
- **修复**：原生 `<input type="date">` 改为 `el-date-picker` 日期范围选择器
- JSON 详情格式化为语法高亮显示

## 4. 公共样式统一

| 组件 | 改造 |
|------|------|
| `.stat-card` | 统一 Token 尺寸（`--admin-radius-md`, `--admin-shadow-sm`） |
| `.badge-*` | 固定颜色方案，不再各页面不一致 |
| `.filter-select` | 统一 `min-width: 150px` |
| `.action-btn` | 高度 32px，hover 轻微上移 |
| `.pagination` | 统一 `el-pagination` |

## 5. 不涉及范围

- 不引入新的 npm 包或图标库
- 不改变路由结构
- 不改变后端 API 接口
- 不提取可复用组件（留待后续优化）

## 6. 文件变更清单

| 文件 | 变更类型 |
|------|---------|
| `frontend/src/styles/pages/admin.css` | 重写（设计 Token + 布局样式） |
| `frontend/src/views/admin/Admin.vue` | 重写（侧边栏 + 顶栏 + 过渡动画） |
| `frontend/src/styles/pages/admin-statistics.css` | 重写 |
| `frontend/src/views/admin/Statistics.vue` | 重写（骨架屏 + 卡片样式） |
| `frontend/src/styles/pages/admin-order-management.css` | 重写 |
| `frontend/src/views/admin/OrderManagement.vue` | 重写（布局调整 + 细节优化） |
| `frontend/src/styles/pages/admin-user-management.css` | 重写 |
| `frontend/src/views/admin/UserManagement.vue` | 重写（API 迁移 + 分页修复） |
| `frontend/src/styles/pages/admin-verification-management.css` | 重写 |
| `frontend/src/views/admin/VerificationManagement.vue` | 重写（API 迁移） |
| `frontend/src/styles/pages/admin-item-management.css` | 重写 |
| `frontend/src/views/admin/ItemManagement.vue` | 重写（API 迁移 + Mock 数据修复） |
| `frontend/src/styles/pages/admin-category-management.css` | 重写 |
| `frontend/src/views/admin/CategoryManagement.vue` | 重写（API 迁移 + Mock 数据修复） |
| `frontend/src/styles/pages/admin-log-management.css` | 重写 |
| `frontend/src/views/admin/LogManagement.vue` | 重写（API 迁移 + Mock 移除 + 日期选择器） |
