import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import storage from '../../utils/storage';
import type { CartItem } from '../../types/api';

export const useCartStore = defineStore('cart', () => {
  const storageInstance = storage('cart');
  const items = ref<CartItem[]>(storageInstance.get('items') || []);

  const totalItems = computed(() => items.value.length);
  const totalPrice = computed(() => {
    return items.value.reduce((total, item) => total + item.price * item.quantity, 0);
  });

  const saveToStorage = () => {
    storageInstance.set('items', items.value);
  };

  const addItem = (item: CartItem) => {
    const existingItem = items.value.find((i) => i.id === item.id);
    if (existingItem) {
      existingItem.quantity += 1;
    } else {
      items.value.push({ ...item, quantity: 1 });
    }
    saveToStorage();
  };

  const removeItem = (itemId: number) => {
    items.value = items.value.filter((item) => item.id !== itemId);
    saveToStorage();
  };

  const updateQuantity = (itemId: number, quantity: number) => {
    const item = items.value.find((i) => i.id === itemId);
    if (item && quantity > 0) {
      item.quantity = quantity;
      saveToStorage();
    }
  };

  const clear = () => {
    items.value = [];
    saveToStorage();
  };

  const syncFromStorage = () => {
    const stored = storageInstance.get('items');
    if (stored) {
      items.value = stored;
    }
  };

  if (typeof window !== 'undefined') {
    window.addEventListener('storage', (event: StorageEvent) => {
      if (event.key === 'cart:items') {
        syncFromStorage();
      }
    });
  }

  return { items, totalItems, totalPrice, addItem, removeItem, updateQuantity, clear, syncFromStorage };
});
