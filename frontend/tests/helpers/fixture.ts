/**
 * 测试数据 Fixture
 */

export const testUser = {
  id: 1,
  username: 'testuser',
  email: 'test@example.com',
  phone: '13800138000',
  nickname: '测试用户',
  role: 'STUDENT',
  status: 'ACTIVE',
};

export const testItem = {
  id: 1,
  title: '测试物品',
  description: '这是一个测试物品',
  price: 100.00,
  category: '电子产品',
  status: 'AVAILABLE',
  userId: 1,
  images: ['test.jpg'],
};

export const testOrder = {
  id: 1,
  itemId: 1,
  buyerId: 2,
  sellerId: 1,
  price: 100.00,
  status: 'PENDING',
  createdAt: '2026-05-28T10:00:00Z',
};

export const testLoginRequest = {
  username: 'testuser',
  password: 'Password@123',
};

export const testRegisterRequest = {
  username: 'newuser',
  password: 'Password@123',
  email: 'new@example.com',
  phone: '13800138001',
};

export const testApiResponse = {
  code: 200,
  message: 'success',
  data: null,
  timestamp: '2026-05-28T10:00:00Z',
};

export const testPageResponse = {
  content: [],
  page: 1,
  size: 10,
  total: 0,
  totalPages: 0,
  first: true,
  last: true,
};

export const testCategory = {
  id: 1,
  name: '电子产品',
  description: '电子设备和配件',
  parentId: null,
  status: 'ACTIVE',
};

export const testNotification = {
  id: 1,
  userId: 1,
  title: '测试通知',
  content: '这是一条测试通知',
  type: 'SYSTEM',
  read: false,
  createdAt: '2026-05-28T10:00:00Z',
};

export const testChatMessage = {
  id: 1,
  chatId: 1,
  senderId: 1,
  content: '你好',
  type: 'TEXT',
  createdAt: '2026-05-28T10:00:00Z',
};

export const testVerification = {
  id: 1,
  userId: 1,
  realName: '张三',
  studentId: '2021001',
  idCard: '110101200001010011',
  status: 'PENDING',
  createdAt: '2026-05-28T10:00:00Z',
};
