# Implementation Tasks

## Task Overview

| ID | Task | Dependencies | Estimated |
|----|------|--------------|-----------|
| T1 | 后端用户管理API扩展 | None | 2h |
| T2 | 后端物品管理API扩展 | None | 1h |
| T3 | 后端日志管理API扩展 | None | 1.5h |
| T4 | 后端批量操作API扩展 | None | 1h |
| T5 | 前端用户管理功能完善 | T1 | 3h |
| T6 | 前端物品管理功能完善 | T2 | 1.5h |
| T7 | 前端日志管理功能完善 | T3 | 2h |
| T8 | 前端纠纷管理页面对接 | T1 | 2h |

---

## Tasks

### Task T1: 后端用户管理API扩展

**Files:**
- Modify: `backend/src/main/java/com/idleitems/school/controller/admin/AdminUserController.java`
- Create: `backend/src/main/java/com/idleitems/school/dto/admin/CreateUserRequest.java`
- Create: `backend/src/main/java/com/idleitems/school/dto/admin/UpdateUserRequest.java`
- Modify: `backend/src/main/java/com/idleitems/school/service/UserService.java`
- Modify: `backend/src/main/java/com/idleitems/school/repository/UserRepository.java`

**Steps:**

1. 创建CreateUserRequest DTO
   ```java
   @Data
   public class CreateUserRequest {
       @NotBlank(message = "用户名不能为空")
       private String username;
       @NotBlank(message = "邮箱不能为空")
       @Email(message = "邮箱格式不正确")
       private String email;
       @NotBlank(message = "密码不能为空")
       @Size(min = 6, message = "密码长度不能少于6位")
       private String password;
       private String phone;
       private String role = "STUDENT";
       private String status = "ACTIVE";
   }
   ```

2. 创建UpdateUserRequest DTO
   ```java
   @Data
   public class UpdateUserRequest {
       private String email;
       private String phone;
       private String role;
       private String status;
   }
   ```

3. 在UserService中添加创建用户方法
   ```java
   public User createUser(CreateUserRequest request) {
       // 检查用户名唯一性
       // 检查邮箱唯一性
       // 加密密码
       // 创建用户
   }
   ```

4. 在UserService中添加更新用户方法
   ```java
   public User updateUser(Long id, UpdateUserRequest request) {
       // 查找用户
       // 更新字段
       // 保存用户
   }
   ```

5. 在UserService中添加导出用户方法
   ```java
   public List<User> exportUsers(String keyword, String role, String status) {
       // 根据条件查询用户
       // 返回用户列表
   }
   ```

6. 在AdminUserController中添加创建用户端点
   ```java
   @PostMapping
   public Result<User> createUser(@RequestBody @Valid CreateUserRequest request) {
       // 调用userService创建用户
       // 记录操作日志
   }
   ```

7. 在AdminUserController中添加更新用户端点
   ```java
   @PutMapping("/{id}")
   public Result<User> updateUser(@PathVariable Long id, 
                                  @RequestBody @Valid UpdateUserRequest request) {
       // 调用userService更新用户
       // 记录操作日志
   }
   ```

8. 在AdminUserController中添加导出用户端点
   ```java
   @GetMapping("/export")
   public void exportUsers(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) String role,
                           @RequestParam(required = false) String status,
                           HttpServletResponse response) {
       // 调用userService导出用户
       // 生成CSV/Excel文件
       // 返回文件流
   }
   ```

9. 运行测试验证功能
   ```bash
   cd backend && mvn test
   ```
   Expected: PASS

10. 提交代码
    ```bash
    git add backend/
    git commit -m "feat: 完善用户管理API（创建、编辑、导出）"
    ```

**Verification:**
- [ ] 创建用户API正常工作
- [ ] 编辑用户API正常工作
- [ ] 导出用户API正常工作
- [ ] 所有测试通过

---

### Task T2: 后端物品管理API扩展

**Files:**
- Modify: `backend/src/main/java/com/idleitems/school/controller/admin/AdminItemController.java`
- Modify: `backend/src/main/java/com/idleitems/school/service/ItemService.java`

**Steps:**

1. 在ItemService中添加导出物品方法
   ```java
   public List<Item> exportItems(String keyword, String status, Long categoryId) {
       // 根据条件查询物品
       // 返回物品列表
   }
   ```

2. 在AdminItemController中添加导出物品端点
   ```java
   @GetMapping("/export")
   public void exportItems(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) Long categoryId,
                           HttpServletResponse response) {
       // 调用itemService导出物品
       // 生成CSV/Excel文件
       // 返回文件流
   }
   ```

3. 运行测试验证功能
   ```bash
   cd backend && mvn test
   ```
   Expected: PASS

4. 提交代码
   ```bash
    git add backend/
    git commit -m "feat: 完善物品管理API（导出功能）"
    ```

**Verification:**
- [ ] 导出物品API正常工作
- [ ] 所有测试通过

---

### Task T3: 后端日志管理API扩展

**Files:**
- Modify: `backend/src/main/java/com/idleitems/school/controller/admin/AdminLogController.java`
- Modify: `backend/src/main/java/com/idleitems/school/repository/AdminLogRepository.java`

**Steps:**

1. 在AdminLogRepository中添加带高级过滤的查询方法
   ```java
   public Page<AdminLog> findByFilters(String keyword, String adminId, 
                                       String type, String logType,
                                       LocalDateTime startDate, LocalDateTime endDate,
                                       Pageable pageable) {
       // 构建动态查询
   }
   ```

2. 在AdminLogController中修改列表查询端点，添加新参数
   ```java
   @GetMapping
   public Result<Page<AdminLog>> listLogs(
           @RequestParam(required = false) String keyword,
           @RequestParam(required = false) String adminId,
           @RequestParam(required = false) String type,
           @RequestParam(required = false) String logType,
           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
           @RequestParam(defaultValue = "0") int page,
           @RequestParam(defaultValue = "20") int size) {
       // 调用repository查询
   }
   ```

3. 在AdminLogController中添加导出日志端点
   ```java
   @GetMapping("/export")
   public void exportLogs(@RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String adminId,
                          @RequestParam(required = false) String type,
                          @RequestParam(required = false) String logType,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                          HttpServletResponse response) {
       // 调用repository查询
       // 生成CSV/Excel文件
       // 返回文件流
   }
   ```

4. 运行测试验证功能
   ```bash
   cd backend && mvn test
   ```
   Expected: PASS

5. 提交代码
   ```bash
    git add backend/
    git commit -m "feat: 完善日志管理API（高级过滤、导出）"
    ```

**Verification:**
- [ ] 日志高级过滤API正常工作
- [ ] 日志导出API正常工作
- [ ] 所有测试通过

---

### Task T4: 后端批量操作API扩展

**Files:**
- Modify: `backend/src/main/java/com/idleitems/school/controller/admin/AdminBatchController.java`
- Modify: `backend/src/main/java/com/idleitems/school/service/UserService.java`

**Steps:**

1. 在UserService中添加批量删除用户方法
   ```java
   public void batchDeleteUsers(List<Long> userIds) {
       // 检查是否有超级管理员
       // 批量删除用户
       // 记录操作日志
   }
   ```

2. 在AdminBatchController中添加批量删除用户端点
   ```java
   @PostMapping("/users/delete")
   public Result<Void> batchDeleteUsers(@RequestBody List<Long> userIds) {
       // 调用userService批量删除
       // 记录操作日志
   }
   ```

3. 运行测试验证功能
   ```bash
   cd backend && mvn test
   ```
   Expected: PASS

4. 提交代码
   ```bash
    git add backend/
    git commit -m "feat: 完善批量操作API（批量删除用户）"
    ```

**Verification:**
- [ ] 批量删除用户API正常工作
- [ ] 所有测试通过

---

### Task T5: 前端用户管理功能完善

**Files:**
- Modify: `frontend/src/views/admin/UserManagement.vue`
- Modify: `frontend/src/api/services/admin.js`
- Modify: `frontend/src/api/config/paths.js`

**Dependencies:** T1

**Steps:**

1. 在paths.js中添加用户管理相关API路径
   ```javascript
   ADMIN: {
       // ... 现有路径
       CREATE_USER: '/admin/users',
       UPDATE_USER: '/admin/users/:id',
       EXPORT_USERS: '/admin/users/export',
   }
   ```

2. 在admin.js中添加用户管理API方法
   ```javascript
   users: {
       // ... 现有方法
       createUser: (data) => api.post(API_PATHS.ADMIN.CREATE_USER, data),
       updateUser: (id, data) => api.put(API_PATHS.ADMIN.UPDATE_USER.replace(':id', id), data),
       exportUsers: (params) => api.get(API_PATHS.ADMIN.EXPORT_USERS, { params, responseType: 'blob' }),
   }
   ```

3. 在UserManagement.vue中添加用户编辑对话框
   ```vue
   <template>
     <!-- 编辑用户对话框 -->
     <el-dialog v-model="editDialogVisible" title="编辑用户" width="500px">
       <el-form :model="editForm" :rules="editRules" ref="editFormRef">
         <!-- 表单字段 -->
       </el-form>
       <template #footer>
         <el-button @click="editDialogVisible = false">取消</el-button>
         <el-button type="primary" @click="submitEdit">确定</el-button>
       </template>
     </el-dialog>
   </template>
   ```

4. 实现编辑用户逻辑
   ```javascript
   const editDialogVisible = ref(false)
   const editForm = ref({})
   const editFormRef = ref(null)

   const handleEdit = (user) => {
     editForm.value = { ...user }
     editDialogVisible.value = true
   }

   const submitEdit = async () => {
     await editFormRef.value.validate()
     await adminAPI.users.updateUser(editForm.value.id, editForm.value)
     ElMessage.success('更新成功')
     editDialogVisible.value = false
     fetchUsers()
   }
   ```

5. 在UserManagement.vue中添加添加用户对话框
   ```vue
   <template>
     <!-- 添加用户对话框 -->
     <el-dialog v-model="addDialogVisible" title="添加用户" width="500px">
       <el-form :model="addForm" :rules="addRules" ref="addFormRef">
         <!-- 表单字段 -->
       </el-form>
       <template #footer>
         <el-button @click="addDialogVisible = false">取消</el-button>
         <el-button type="primary" @click="submitAdd">确定</el-button>
       </template>
     </el-dialog>
   </template>
   ```

6. 实现添加用户逻辑
   ```javascript
   const addDialogVisible = ref(false)
   const addForm = ref({})
   const addFormRef = ref(null)

   const handleAdd = () => {
     addForm.value = {
       username: '',
       email: '',
       password: '',
       phone: '',
       role: 'STUDENT',
       status: 'ACTIVE'
     }
     addDialogVisible.value = true
   }

   const submitAdd = async () => {
     await addFormRef.value.validate()
     await adminAPI.users.createUser(addForm.value)
     ElMessage.success('创建成功')
     addDialogVisible.value = false
     fetchUsers()
   }
   ```

7. 实现导出用户功能
   ```javascript
   const handleExport = async () => {
     const params = {
       keyword: searchKeyword.value,
       role: filterRole.value,
       status: filterStatus.value
     }
     const response = await adminAPI.users.exportUsers(params)
     // 创建下载链接
     const url = window.URL.createObjectURL(new Blob([response.data]))
     const link = document.createElement('a')
     link.href = url
     link.setAttribute('download', `users_${new Date().getTime()}.csv`)
     document.body.appendChild(link)
     link.click()
     link.remove()
   }
   ```

8. 更新导出按钮的点击事件
   ```vue
   <el-button @click="handleExport" :icon="Download">导出</el-button>
   ```

9. 更新添加用户按钮的点击事件
   ```vue
   <el-button @click="handleAdd" :icon="Plus">添加用户</el-button>
   ```

10. 运行前端测试验证功能
    ```bash
    cd frontend && npm run test:unit
    ```
    Expected: PASS

11. 提交代码
    ```bash
    git add frontend/
    git commit -m "feat: 完善用户管理前端功能（编辑、添加、导出）"
    ```

**Verification:**
- [ ] 编辑用户对话框正常显示
- [ ] 添加用户对话框正常显示
- [ ] 导出用户功能正常工作
- [ ] 所有测试通过

---

### Task T6: 前端物品管理功能完善

**Files:**
- Modify: `frontend/src/views/admin/ItemManagement.vue`
- Modify: `frontend/src/api/services/admin.js`
- Modify: `frontend/src/api/config/paths.js`

**Dependencies:** T2

**Steps:**

1. 在paths.js中添加物品导出API路径
   ```javascript
   ADMIN: {
       // ... 现有路径
       EXPORT_ITEMS: '/admin/items/export',
   }
   ```

2. 在admin.js中添加物品导出API方法
   ```javascript
   items: {
       // ... 现有方法
       exportItems: (params) => api.get(API_PATHS.ADMIN.EXPORT_ITEMS, { params, responseType: 'blob' }),
   }
   ```

3. 实现导出物品功能
   ```javascript
   const handleExport = async () => {
     const params = {
       keyword: searchKeyword.value,
       status: filterStatus.value,
       categoryId: filterCategory.value
     }
     const response = await adminAPI.items.exportItems(params)
     // 创建下载链接
     const url = window.URL.createObjectURL(new Blob([response.data]))
     const link = document.createElement('a')
     link.href = url
     link.setAttribute('download', `items_${new Date().getTime()}.csv`)
     document.body.appendChild(link)
     link.click()
     link.remove()
   }
   ```

4. 更新导出按钮的点击事件
   ```vue
   <el-button @click="handleExport" :icon="Download">导出</el-button>
   ```

5. 运行前端测试验证功能
   ```bash
   cd frontend && npm run test:unit
   ```
   Expected: PASS

6. 提交代码
   ```bash
   git add frontend/
   git commit -m "feat: 完善物品管理前端功能（导出）"
   ```

**Verification:**
- [ ] 导出物品功能正常工作
- [ ] 所有测试通过

---

### Task T7: 前端日志管理功能完善

**Files:**
- Modify: `frontend/src/views/admin/LogManagement.vue`
- Modify: `frontend/src/api/services/admin.js`
- Modify: `frontend/src/api/config/paths.js`

**Dependencies:** T3

**Steps:**

1. 在paths.js中添加日志导出API路径
   ```javascript
   ADMIN: {
       // ... 现有路径
       EXPORT_LOGS: '/admin/logs/export',
   }
   ```

2. 在admin.js中添加日志导出API方法
   ```javascript
   logs: {
       // ... 现有方法
       exportLogs: (params) => api.get(API_PATHS.ADMIN.EXPORT_LOGS, { params, responseType: 'blob' }),
   }
   ```

3. 在LogManagement.vue中添加高级过滤表单
   ```vue
   <template>
     <!-- 高级过滤 -->
     <el-form :inline="true" :model="filterForm">
       <el-form-item label="操作类型">
         <el-select v-model="filterForm.type" placeholder="全部" clearable>
           <el-option label="用户管理" value="USER" />
           <el-option label="物品管理" value="ITEM" />
           <el-option label="订单管理" value="ORDER" />
           <el-option label="分类管理" value="CATEGORY" />
         </el-select>
       </el-form-item>
       <el-form-item label="日志类型">
         <el-select v-model="filterForm.logType" placeholder="全部" clearable>
           <el-option label="创建" value="CREATE" />
           <el-option label="更新" value="UPDATE" />
           <el-option label="删除" value="DELETE" />
           <el-option label="审核" value="REVIEW" />
         </el-select>
       </el-form-item>
       <el-form-item label="时间范围">
         <el-date-picker
           v-model="filterForm.dateRange"
           type="daterange"
           range-separator="至"
           start-placeholder="开始日期"
           end-placeholder="结束日期"
         />
       </el-form-item>
       <el-form-item>
         <el-button type="primary" @click="fetchLogs">查询</el-button>
         <el-button @click="resetFilters">重置</el-button>
       </el-form-item>
     </el-form>
   </template>
   ```

4. 实现高级过滤逻辑
   ```javascript
   const filterForm = ref({
     type: '',
     logType: '',
     dateRange: []
   })

   const fetchLogs = async () => {
     const params = {
       keyword: searchKeyword.value,
       adminId: filterAdmin.value,
       type: filterForm.value.type,
       logType: filterForm.value.logType,
       startDate: filterForm.value.dateRange?.[0],
       endDate: filterForm.value.dateRange?.[1],
       page: currentPage.value,
       size: pageSize.value
     }
     // 调用API获取日志
   }

   const resetFilters = () => {
     filterForm.value = {
       type: '',
       logType: '',
       dateRange: []
     }
     fetchLogs()
   }
   ```

5. 实现导出日志功能
   ```javascript
   const handleExport = async () => {
     const params = {
       keyword: searchKeyword.value,
       adminId: filterAdmin.value,
       type: filterForm.value.type,
       logType: filterForm.value.logType,
       startDate: filterForm.value.dateRange?.[0],
       endDate: filterForm.value.dateRange?.[1]
     }
     const response = await adminAPI.logs.exportLogs(params)
     // 创建下载链接
     const url = window.URL.createObjectURL(new Blob([response.data]))
     const link = document.createElement('a')
     link.href = url
     link.setAttribute('download', `logs_${new Date().getTime()}.csv`)
     document.body.appendChild(link)
     link.click()
     link.remove()
   }
   ```

6. 更新导出按钮的点击事件
   ```vue
   <el-button @click="handleExport" :icon="Download">导出</el-button>
   ```

7. 运行前端测试验证功能
   ```bash
   cd frontend && npm run test:unit
   ```
   Expected: PASS

8. 提交代码
   ```bash
   git add frontend/
   git commit -m "feat: 完善日志管理前端功能（高级过滤、导出）"
   ```

**Verification:**
- [ ] 高级过滤功能正常工作
- [ ] 导出日志功能正常工作
- [ ] 所有测试通过

---

### Task T8: 前端纠纷管理页面对接

**Files:**
- Create: `frontend/src/views/admin/DisputeManagement.vue`
- Modify: `frontend/src/api/services/admin.js`
- Modify: `frontend/src/api/config/paths.js`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/views/admin/Admin.vue`

**Dependencies:** T1

**Steps:**

1. 在paths.js中添加纠纷管理API路径
   ```javascript
   ADMIN: {
       // ... 现有路径
       DISPUTES: '/admin/disputes',
       DISPUTE_STATS: '/admin/disputes/stats',
       HANDLE_DISPUTE: '/admin/disputes/:id/handle',
   }
   ```

2. 在admin.js中添加纠纷管理API方法
   ```javascript
   disputes: {
       list: (params) => api.get(API_PATHS.ADMIN.DISPUTES, { params }),
       stats: () => api.get(API_PATHS.ADMIN.DISPUTE_STATS),
       handle: (id, data) => api.put(API_PATHS.ADMIN.HANDLE_DISPUTE.replace(':id', id), data),
   }
   ```

3. 创建DisputeManagement.vue组件
   ```vue
   <template>
     <div class="dispute-management">
       <!-- 页面头部 -->
       <div class="page-header">
         <h2>纠纷管理</h2>
       </div>

       <!-- 统计卡片 -->
       <el-row :gutter="20" class="stats-row">
         <el-col :span="6">
           <el-card shadow="hover">
             <div class="stat-card">
               <div class="stat-value">{{ stats.total }}</div>
               <div class="stat-label">总纠纷数</div>
             </div>
           </el-card>
         </el-col>
         <el-col :span="6">
           <el-card shadow="hover">
             <div class="stat-card">
               <div class="stat-value">{{ stats.pending }}</div>
               <div class="stat-label">待处理</div>
             </div>
           </el-card>
         </el-col>
         <el-col :span="6">
           <el-card shadow="hover">
             <div class="stat-card">
               <div class="stat-value">{{ stats.processing }}</div>
               <div class="stat-label">处理中</div>
             </div>
           </el-card>
         </el-col>
         <el-col :span="6">
           <el-card shadow="hover">
             <div class="stat-card">
               <div class="stat-value">{{ stats.resolved }}</div>
               <div class="stat-label">已解决</div>
             </div>
           </el-card>
         </el-col>
       </el-row>

       <!-- 筛选条件 -->
       <el-form :inline="true" :model="filterForm" class="filter-form">
         <el-form-item label="纠纷状态">
           <el-select v-model="filterForm.status" placeholder="全部" clearable>
             <el-option label="待处理" value="PENDING" />
             <el-option label="处理中" value="PROCESSING" />
             <el-option label="已解决" value="RESOLVED" />
             <el-option label="已关闭" value="CLOSED" />
           </el-select>
         </el-form-item>
         <el-form-item>
           <el-button type="primary" @click="fetchDisputes">查询</el-button>
         </el-form-item>
       </el-form>

       <!-- 纠纷列表 -->
       <el-table :data="disputes" style="width: 100%">
         <el-table-column prop="id" label="纠纷编号" width="100" />
         <el-table-column prop="itemTitle" label="物品名称" />
         <el-table-column prop="buyerName" label="买家" width="120" />
         <el-table-column prop="sellerName" label="卖家" width="120" />
         <el-table-column prop="status" label="状态" width="100">
           <template #default="{ row }">
             <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
           </template>
         </el-table-column>
         <el-table-column prop="createdAt" label="创建时间" width="180" />
         <el-table-column label="操作" width="150">
           <template #default="{ row }">
             <el-button type="primary" link @click="viewDispute(row)">查看</el-button>
             <el-button type="success" link @click="handleDispute(row)" v-if="row.status === 'PENDING'">处理</el-button>
           </template>
         </el-table-column>
       </el-table>

       <!-- 分页 -->
       <el-pagination
         v-model:current-page="currentPage"
         v-model:page-size="pageSize"
         :page-sizes="[10, 20, 50]"
         layout="total, sizes, prev, pager, next"
         :total="total"
         @size-change="fetchDisputes"
         @current-change="fetchDisputes"
       />

       <!-- 纠纷详情对话框 -->
       <el-dialog v-model="detailDialogVisible" title="纠纷详情" width="600px">
         <el-descriptions :column="1" border>
           <el-descriptions-item label="纠纷编号">{{ currentDispute.id }}</el-descriptions-item>
           <el-descriptions-item label="物品名称">{{ currentDispute.itemTitle }}</el-descriptions-item>
           <el-descriptions-item label="买家">{{ currentDispute.buyerName }}</el-descriptions-item>
           <el-descriptions-item label="卖家">{{ currentDispute.sellerName }}</el-descriptions-item>
           <el-descriptions-item label="纠纷原因">{{ currentDispute.reason }}</el-descriptions-item>
           <el-descriptions-item label="详细描述">{{ currentDispute.description }}</el-descriptions-item>
           <el-descriptions-item label="状态">{{ getStatusLabel(currentDispute.status) }}</el-descriptions-item>
           <el-descriptions-item label="创建时间">{{ currentDispute.createdAt }}</el-descriptions-item>
         </el-descriptions>
       </el-dialog>

       <!-- 处理纠纷对话框 -->
       <el-dialog v-model="handleDialogVisible" title="处理纠纷" width="500px">
         <el-form :model="handleForm" :rules="handleRules" ref="handleFormRef">
           <el-form-item label="处理结果" prop="result">
             <el-radio-group v-model="handleForm.result">
               <el-radio label="APPROVE_REFUND">同意退款</el-radio>
               <el-radio label="REJECT">驳回</el-radio>
               <el-radio label="CLOSE">关闭</el-radio>
             </el-radio-group>
           </el-form-item>
           <el-form-item label="处理说明" prop="remark">
             <el-input v-model="handleForm.remark" type="textarea" :rows="3" placeholder="请输入处理说明" />
           </el-form-item>
         </el-form>
         <template #footer>
           <el-button @click="handleDialogVisible = false">取消</el-button>
           <el-button type="primary" @click="submitHandle">确定</el-button>
         </template>
       </el-dialog>
     </div>
   </template>
   ```

4. 实现纠纷管理逻辑
   ```javascript
   import { ref, onMounted } from 'vue'
   import { ElMessage } from 'element-plus'
   import { adminAPI } from '@/api'

   const disputes = ref([])
   const stats = ref({ total: 0, pending: 0, processing: 0, resolved: 0 })
   const filterForm = ref({ status: '' })
   const currentPage = ref(1)
   const pageSize = ref(20)
   const total = ref(0)

   const detailDialogVisible = ref(false)
   const handleDialogVisible = ref(false)
   const currentDispute = ref({})
   const handleForm = ref({ result: '', remark: '' })
   const handleFormRef = ref(null)

   const fetchDisputes = async () => {
     const params = {
       status: filterForm.value.status,
       page: currentPage.value - 1,
       size: pageSize.value
     }
     const response = await adminAPI.disputes.list(params)
     disputes.value = response.data.content
     total.value = response.data.totalElements
   }

   const fetchStats = async () => {
     const response = await adminAPI.disputes.stats()
     stats.value = response.data
   }

   const viewDispute = (dispute) => {
     currentDispute.value = dispute
     detailDialogVisible.value = true
   }

   const handleDispute = (dispute) => {
     currentDispute.value = dispute
     handleForm.value = { result: '', remark: '' }
     handleDialogVisible.value = true
   }

   const submitHandle = async () => {
     await handleFormRef.value.validate()
     await adminAPI.disputes.handle(currentDispute.value.id, handleForm.value)
     ElMessage.success('处理成功')
     handleDialogVisible.value = false
     fetchDisputes()
     fetchStats()
   }

   const getStatusType = (status) => {
     const types = {
       PENDING: 'warning',
       PROCESSING: 'primary',
       RESOLVED: 'success',
       CLOSED: 'info'
     }
     return types[status] || 'info'
   }

   const getStatusLabel = (status) => {
     const labels = {
       PENDING: '待处理',
       PROCESSING: '处理中',
       RESOLVED: '已解决',
       CLOSED: '已关闭'
     }
     return labels[status] || status
   }

   onMounted(() => {
     fetchDisputes()
     fetchStats()
   })
   ```

5. 在router/index.js中添加纠纷管理路由
   ```javascript
   {
     path: 'disputes',
     name: 'AdminDisputes',
     component: () => import('@/views/admin/DisputeManagement.vue'),
     meta: { title: '纠纷管理', requiresAuth: true, requiresAdmin: true }
   }
   ```

6. 在Admin.vue侧边栏中添加纠纷管理菜单项
   ```vue
   <el-menu-item index="/admin/disputes">
     <el-icon><Warning /></el-icon>
     <span>纠纷管理</span>
   </el-menu-item>
   ```

7. 创建纠纷管理样式文件
   ```css
   /* frontend/src/styles/pages/admin-dispute-management.css */
   .dispute-management {
     padding: 20px;
   }

   .page-header {
     margin-bottom: 20px;
   }

   .stats-row {
     margin-bottom: 20px;
   }

   .stat-card {
     text-align: center;
   }

   .stat-value {
     font-size: 24px;
     font-weight: bold;
     color: #409eff;
   }

   .stat-label {
     font-size: 14px;
     color: #909399;
     margin-top: 8px;
   }

   .filter-form {
     margin-bottom: 20px;
   }
   ```

8. 运行前端测试验证功能
   ```bash
   cd frontend && npm run test:unit
   ```
   Expected: PASS

9. 提交代码
   ```bash
   git add frontend/
   git commit -m "feat: 完善纠纷管理前端页面对接"
   ```

**Verification:**
- [ ] 纠纷管理页面正常显示
- [ ] 纠纷列表数据正确
- [ ] 纠纷详情查看正常
- [ ] 纠纷处理功能正常
- [ ] 所有测试通过

---

## Parallel Tasks

以下任务可以并行执行：
- T1、T2、T3、T4（后端API扩展，相互独立）
- T5、T6、T7（前端功能完善，依赖各自后端任务）

## Notes

- 所有新增API需要管理员权限验证
- 导出功能需要考虑大数据量的性能优化
- 批量删除用户需要检查权限（不能删除超级管理员）
- 纠纷处理需要联动订单状态更新
- 所有操作需要记录管理日志
