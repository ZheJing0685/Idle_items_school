import { defineStore } from 'pinia';

export default defineStore('cart', {
  state: () => ({
    items: [],
  }),
  getters: {
    totalItems: (state) => state.items.length,
    totalPrice: (state) => {
      return state.items.reduce((total, item) => {
        return total + item.price * item.quantity;
      }, 0);
    },
  },
  actions: {
    addItem(item) {
      const existingItem = this.items.find((i) => i.id === item.id);
      if (existingItem) {
        existingItem.quantity++;
      } else {
        this.items.push({
          ...item,
          quantity: 1,
        });
      }
    },
    removeItem(itemId) {
      this.items = this.items.filter((item) => item.id !== itemId);
    },
    updateQuantity(itemId, quantity) {
      const item = this.items.find((i) => i.id === itemId);
      if (item) {
        item.quantity = Math.max(1, quantity);
      }
    },
    clearCart() {
      this.items = [];
    },
  },
});
