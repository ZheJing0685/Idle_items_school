import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import api from '../../api';
import { ErrorHandler } from '../../utils/error';

export const useItemStore = defineStore('item', () => {
  const items = ref([]);
  const total = ref(0);
  const loading = ref(false);
  const hotItems = ref([]);
  const currentItem = ref(null);
  const searchResults = ref([]);
  const searchTotal = ref(0);
  const userItems = ref([]);
  const userItemsTotal = ref(0);

  const fetchItems = async (params = {}) => {
    loading.value = true;
    try {
      const response = await api.item.getItems({
        page: params.page || 1,
        size: params.size || 20,
        categoryId: params.categoryId,
        sortBy: params.sortBy,
        condition: params.condition,
        deliveryMethod: params.deliveryMethod,
      });
      items.value = response.data.content;
      total.value = response.data.totalElements;
      return response;
    } catch (error) {
      ErrorHandler.handle(error);
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const fetchHotItems = async () => {
    try {
      const response = await api.item.getHotItems();
      if (response.code === 200) {
        hotItems.value = response.data;
      }
      return response;
    } catch (error) {
      ErrorHandler.handle(error);
      return null;
    }
  };

  const searchItems = async (
    keyword,
    page = 1,
    size = 20,
    sortBy = 'createdAt'
  ) => {
    loading.value = true;
    try {
      const response = await api.item.searchItems(keyword, page, size, sortBy);
      if (response.code === 200) {
        searchResults.value = response.data.content;
        searchTotal.value = response.data.totalElements;
      }
      return response;
    } catch (error) {
      ErrorHandler.handle(error);
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const getItem = async (id) => {
    try {
      const response = await api.item.getItem(id);
      if (response.code === 200) {
        currentItem.value = response.data;
      }
      return response;
    } catch (error) {
      ErrorHandler.handle(error);
      throw error;
    }
  };

  const createItem = async (itemData) => {
    loading.value = true;
    try {
      const response = await api.item.createItem(itemData);
      if (response.code === 200) {
        await api.clearCache('/items');
      }
      return response;
    } catch (error) {
      ErrorHandler.handle(error);
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const updateItem = async (id, itemData) => {
    loading.value = true;
    try {
      const response = await api.item.updateItem(id, itemData);
      if (response.code === 200) {
        await api.clearCache(`/items/${id}`);
        await api.clearCache('/items');
      }
      return response;
    } catch (error) {
      ErrorHandler.handle(error);
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const offShelfItem = async (id) => {
    loading.value = true;
    try {
      const response = await api.item.offShelf(id);
      if (response.code === 200) {
        await api.clearCache(`/items/${id}`);
        await api.clearCache('/items');
      }
      return response;
    } catch (error) {
      ErrorHandler.handle(error);
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const fetchUserItems = async (status, page = 1, size = 20) => {
    loading.value = true;
    try {
      const response = await api.user.getItems(status, page, size);
      if (response.code === 200) {
        userItems.value = response.data.content;
        userItemsTotal.value = response.data.totalElements;
      }
      return response;
    } catch (error) {
      ErrorHandler.handle(error);
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const uploadImage = async (formData) => {
    loading.value = true;
    try {
      const response = await api.item.uploadImage(formData);
      return response;
    } catch (error) {
      ErrorHandler.handle(error);
      throw error;
    } finally {
      loading.value = false;
    }
  };

  return {
    items,
    total,
    loading,
    hotItems,
    currentItem,
    searchResults,
    searchTotal,
    userItems,
    userItemsTotal,
    fetchItems,
    fetchHotItems,
    searchItems,
    getItem,
    createItem,
    updateItem,
    offShelfItem,
    fetchUserItems,
    uploadImage,
  };
});
