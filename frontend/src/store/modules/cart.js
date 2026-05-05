import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import storage from '../../utils/storage';

export const useCartStore = defineStore('cart', () => {
  const storageInstance = storage('cart');
  const items = ref(storageInstance.get('items') || []);

  const totalItems = computed(() => items.value.length);
  const totalPrice = computed(() => {
    return items.value.reduce((total, item) => total + (item.price * item.quantity), 0);
  });

  const addItem = (item) => {
    const existingItem = items.value.find(i => i.id === item.id);
    if (existingItem) {
      existingItem.quantity += 1;
    } else {
      items.value.push({ ...item, quantity: 1 });
    }
    saveToStorage();
  };

  const removeItem = (itemId) => {
    items.value = items.value.filter(item => item.id !== itemId);
    saveToStorage();
  };

  const updateQuantity = (itemId, quantity) => {
    const item = items.value.find(i => i.id === itemId);
    if (item && quantity > 0) {
      item.quantity = quantity;
      saveToStorage();
    }
  };

  const clear = () => {
    items.value = [];
    saveToStorage();
  };

  const saveToStorage = () => {
    storageInstance.set('items', items.value);
  };

  return {
    items,
    totalItems,
    totalPrice,
    addItem,
    removeItem,
    updateQuantity,
    clear,
  };
});
