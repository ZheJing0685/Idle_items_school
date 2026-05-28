/**
 * 前端测试数据工厂
 * 提供各种测试场景的标准化数据
 */

export const TestData = {
  // 用户数据
  users: {
    buyer: {
      id: 1,
      username: 'buyer',
      nickname: '买家',
      role: 'STUDENT',
      verified: true,
      phone: '13800138000',
      email: 'buyer@test.com',
    },
    seller: {
      id: 2,
      username: 'seller',
      nickname: '卖家',
      role: 'STUDENT',
      verified: true,
      phone: '13800138001',
      email: 'seller@test.com',
    },
    admin: {
      id: 100,
      username: 'admin',
      nickname: '管理员',
      role: 'ADMIN',
      verified: true,
    },
  },

  // 物品数据
  items: {
    phone: {
      id: 1,
      title: 'iPhone 15 Pro',
      description: '全新未拆封，国行正品',
      price: 7999,
      originalPrice: 8999,
      condition: 'NEW',
      category: '数码产品',
      location: '北京大学',
    },
    laptop: {
      id: 2,
      title: 'MacBook Pro 14寸',
      description: 'M3 Pro芯片，16GB内存',
      price: 14999,
      originalPrice: 16999,
      condition: 'LIKE_NEW',
      category: '数码产品',
      location: '清华大学',
    },
    book: {
      id: 3,
      title: '高等数学（同济版）',
      description: '九成新，有少量笔记',
      price: 25,
      originalPrice: 45,
      condition: 'GOOD',
      category: '教材书籍',
      location: '北京大学',
    },
  },

  // 订单数据
  orders: {
    pendingPayment: {
      id: 1,
      orderNo: 'ORD2026001',
      orderStatus: 'PENDING_PAYMENT',
      price: 7999,
      itemTitle: 'iPhone 15 Pro',
    },
    paid: {
      id: 2,
      orderNo: 'ORD2026002',
      orderStatus: 'PAID',
      price: 14999,
      itemTitle: 'MacBook Pro 14寸',
    },
    completed: {
      id: 3,
      orderNo: 'ORD2026003',
      orderStatus: 'COMPLETED',
      price: 25,
      itemTitle: '高等数学（同济版）',
    },
  },

  // 表单数据
  forms: {
    validAddress: {
      address: '北京市海淀区颐和园路5号北京大学',
      phone: '13800138000',
      name: '张三',
    },
    invalidAddress: {
      address: '',
      phone: '123',
      name: '',
    },
  },
};

/**
 * 生成随机测试数据
 */
export const RandomData = {
  username: () => `testuser_${Date.now()}`,
  email: () => `test_${Date.now()}@example.com`,
  phone: () => `138${String(Math.floor(Math.random() * 100000000)).padStart(8, '0')}`,
  price: () => Math.floor(Math.random() * 10000) + 100,
  orderNo: () => `ORD${Date.now()}${Math.floor(Math.random() * 1000)}`,
};
