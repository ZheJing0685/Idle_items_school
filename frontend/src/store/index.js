import { createPinia } from 'pinia';
import { useUserStore } from './modules/user';
import { useItemStore } from './modules/item';
import { useCartStore } from './modules/cart';

const pinia = createPinia();

export {
  pinia,
  useUserStore,
  useItemStore,
  useCartStore,
  // 兼容旧的导入方式
  useUserStore as userStore,
  useItemStore as itemStore,
  useCartStore as cartStore,
};

export default pinia;
