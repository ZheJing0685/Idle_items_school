import { createPinia } from 'pinia';
import { useUserStore } from './modules/user';
import { useItemStore } from './modules/item';

const pinia = createPinia();

export {
  pinia,
  useUserStore,
  useItemStore,
  useUserStore as userStore,
  useItemStore as itemStore,
};

export default pinia;
