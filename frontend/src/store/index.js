import { createPinia } from 'pinia';
import { useUserStore } from './modules/user';
import { useItemStore } from './modules/item';
import { useCartStore } from './modules/cart';

const pinia = createPinia();

export {
  pinia,
  useUserStore as userStore,
  useItemStore as itemStore,
  useCartStore as cartStore,
};

export default pinia;
