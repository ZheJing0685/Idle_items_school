# 实现任务分解

## 任务概览

| ID | 任务 | 依赖 | 预估时间 |
|----|------|------|----------|
| T1 | 分析现有导航结构和用户角色 | 无 | 30分钟 |
| T2 | 设计角色导航配置 | T1 | 45分钟 |
| T3 | 重构Header.vue组件 | T2 | 2小时 |
| T4 | 更新导航样式 | T3 | 1小时 |
| T5 | 测试不同角色导航 | T4 | 45分钟 |
| T6 | 移动端适配优化 | T5 | 1小时 |

---

## 任务详情

### Task T1: 分析现有导航结构和用户角色

**文件:**
- 查看: `src/components/Header.vue`
- 查看: `src/store/index.js`
- 查看: `src/router/index.js`

**步骤:**

1. 分析当前Header.vue的导航项结构
   ```
   当前导航项：
   - 首页 (/)
   - 浏览好物 (/items)
   - 分类标签 (动态)
   - 绿色校园标识
   ```

2. 分析用户角色定义
   ```
   角色类型：
   - 游客 (未登录)
   - 普通用户 (已登录)
   - 管理员 (isAdmin: true)
   ```

3. 检查用户状态管理
   ```javascript
   // store中的用户状态
   store.isLoggedIn
   store.user
   store.isAdmin
   ```

**验证:**
- [ ] 理解当前导航结构
- [ ] 确认角色判断逻辑
- [ ] 识别需要修改的代码位置

---

### Task T2: 设计角色导航配置

**文件:**
- 创建: `src/config/navigation.js`

**步骤:**

1. 创建导航配置文件
   ```javascript
   // src/config/navigation.js
   export const navigationConfig = {
     guest: [
       { name: '首页', path: '/', icon: 'home' },
       { name: '浏览好物', path: '/items', icon: 'grid' },
     ],
     user: [
       { name: '首页', path: '/', icon: 'home' },
       { name: '浏览好物', path: '/items', icon: 'grid' },
       { name: '发布', path: '/publish', icon: 'plus-circle', requiresAuth: true },
     ],
     admin: [
       { name: '首页', path: '/', icon: 'home' },
       { name: '浏览好物', path: '/items', icon: 'grid' },
       { name: '发布', path: '/publish', icon: 'plus-circle', requiresAuth: true },
       { name: '管理后台', path: '/admin', icon: 'shield', requiresAdmin: true },
     ],
   };
   ```

2. 定义导航项图标映射
   ```javascript
   export const iconMap = {
     home: '<path d="M3 9L12 2L21 9V20C21 20.5304 20.7893 21.0391 20.4142 21.4142C20.0391 21.7893 19.5304 22 19 22H5C4.46957 22 3.96086 21.7893 3.58579 21.4142C3.21071 21.0391 3 20.5304 3 20V9Z" />',
     grid: '<rect x="3" y="3" width="7" height="7" /><rect x="14" y="3" width="7" height="7" /><rect x="14" y="14" width="7" height="7" /><rect x="3" y="14" width="7" height="7" />',
     'plus-circle': '<circle cx="12" cy="12" r="10" /><path d="M12 8V16" /><path d="M8 12H16" />',
     shield: '<path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" /><path d="M12 8V12" /><path d="M12 16H12.01" />',
   };
   ```

3. 创建获取导航项的工具函数
   ```javascript
   export function getNavigationItems(userRole) {
     switch (userRole) {
       case 'admin':
         return navigationConfig.admin;
       case 'user':
         return navigationConfig.user;
       default:
         return navigationConfig.guest;
     }
   }
   ```

**验证:**
- [ ] 导航配置文件创建成功
- [ ] 图标映射完整
- [ ] 工具函数逻辑正确

---

### Task T3: 重构Header.vue组件

**文件:**
- 修改: `src/components/Header.vue`

**步骤:**

1. 导入导航配置
   ```javascript
   import { getNavigationItems, iconMap } from '../config/navigation';
   ```

2. 计算当前用户角色
   ```javascript
   const userRole = computed(() => {
     if (!store.isLoggedIn) return 'guest';
     if (store.isAdmin) return 'admin';
     return 'user';
   });
   ```

3. 计算导航项
   ```javascript
   const navigationItems = computed(() => {
     return getNavigationItems(userRole.value);
   });
   ```

4. 修改模板中的导航菜单部分
   ```vue
   <div class="nav-menu">
     <router-link
       v-for="item in navigationItems"
       :key="item.path"
       :to="item.path"
       class="nav-item"
       :class="{ active: isActiveRoute(item.path) }"
     >
       <svg
         width="18"
         height="18"
         viewBox="0 0 24 24"
         fill="none"
         stroke="currentColor"
         stroke-width="2"
         v-html="iconMap[item.icon]"
       />
       <span>{{ item.name }}</span>
     </router-link>
     
     <!-- 保留分类标签和绿色校园标识 -->
     <div class="nav-eco">
       <!-- 现有内容 -->
     </div>
   </div>
   ```

5. 添加路由激活判断函数
   ```javascript
   const isActiveRoute = (path) => {
     if (path === '/') {
       return route.path === '/';
     }
     return route.path.startsWith(path);
   };
   ```

**验证:**
- [ ] 导航项根据角色动态显示
- [ ] 当前页面高亮正确
- [ ] 所有导航链接可点击

---

### Task T4: 更新导航样式

**文件:**
- 修改: `src/styles/components/header.css`

**步骤:**

1. 确保导航项样式一致
   ```css
   .nav-item {
     display: flex;
     align-items: center;
     gap: var(--space-2);
     padding: var(--space-2) var(--space-4);
     color: var(--text-secondary);
     font-weight: 500;
     font-size: var(--text-sm);
     border-radius: var(--radius-md);
     transition: all var(--transition-normal);
     text-decoration: none;
     white-space: nowrap;
   }
   ```

2. 添加导航项激活状态样式
   ```css
   .nav-item.active {
     color: var(--primary-color);
     background: oklch(62% 0.14 195 / 0.12);
     border-color: oklch(62% 0.14 195 / 0.3);
     font-weight: 600;
   }
   ```

3. 优化移动端样式
   ```css
   @media (max-width: 768px) {
     .nav-item span {
       display: none;
     }
     
     .nav-item {
       padding: var(--space-2) var(--space-3);
     }
   }
   ```

**验证:**
- [ ] 导航项样式符合设计系统
- [ ] 激活状态视觉效果正确
- [ ] 移动端样式正常

---

### Task T5: 测试不同角色导航

**文件:**
- 测试: `src/components/Header.vue`

**步骤:**

1. 测试游客模式
   - 未登录状态访问首页
   - 验证显示：首页、浏览好物、登录、注册

2. 测试普通用户模式
   - 登录普通用户账号
   - 验证显示：首页、浏览好物、发布、用户菜单

3. 测试管理员模式
   - 登录管理员账号
   - 验证显示：首页、浏览好物、发布、管理后台、用户菜单

4. 测试导航功能
   - 点击每个导航项验证跳转正确
   - 验证当前页面高亮正确
   - 测试用户菜单下拉功能

**验证:**
- [ ] 游客模式导航正确
- [ ] 普通用户模式导航正确
- [ ] 管理员模式导航正确
- [ ] 所有导航链接功能正常

---

### Task T6: 移动端适配优化

**文件:**
- 修改: `src/components/Header.vue`
- 修改: `src/styles/components/header.css`

**步骤:**

1. 优化移动端导航布局
   ```css
   @media (max-width: 768px) {
     .header-content {
       flex-wrap: wrap;
       gap: var(--space-4);
     }
     
     .search-section {
       order: 3;
       flex: 100%;
       max-width: none;
     }
     
     .nav-actions {
       gap: var(--space-2);
     }
   }
   ```

2. 确保触摸目标尺寸
   ```css
   .nav-item {
     min-height: 44px;
     min-width: 44px;
   }
   ```

3. 测试不同屏幕尺寸
   - 375px (iPhone SE)
   - 390px (iPhone 14)
   - 768px (iPad)
   - 1024px (iPad Pro)

**验证:**
- [ ] 移动端导航布局正常
- [ ] 触摸目标尺寸足够
- [ ] 不同屏幕尺寸适配良好

---

## 并行任务

以下任务可以并行执行：
- T1 和 T2（无依赖关系）
- T4 和 T6（样式调整可并行）

## 注意事项

- 保持现有设计系统的一致性
- 确保无障碍访问（键盘导航、屏幕阅读器）
- 测试不同角色的权限控制
- 考虑性能影响（动态导入导航配置）
