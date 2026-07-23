import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../../api';
import { ErrorHandler } from '../../utils/error';
import type { ItemInfo, ItemSummary, CreateItemRequest, UpdateItemRequest } from '../../types/api';

export const useItemStore = defineStore('item', () => {
  const items = ref<ItemSummary[]>([]);
  const total = ref(0);
  const loading = ref(false);
  const hotItems = ref<ItemSummary[]>([]);
  const currentItem = ref<ItemInfo | null>(null);
  const searchResults = ref<ItemSummary[]>([]);
  const searchTotal = ref(0);
  const userItems = ref<ItemSummary[]>([]);
  const userItemsTotal = ref(0);

  const fetchItems = async (params: any = {}) => {
    loading.value = true;
    try {
      const response: any = await api.item.getItems({
        page: params.page || 1,
        size: params.size || 20,
        categoryId: params.categoryId || undefined,
        sortBy: params.sortBy,
        condition: params.condition || undefined,
        deliveryMethod: params.deliveryMethod || undefined,
        keyword: params.keyword || undefined,
      });
      if (response?.code === 200 && response.data) {
        items.value = response.data.content;
        total.value = response.data.totalElements;
      }
      return response;
    } catch (error) {
      ErrorHandler.handle(error, { silent: true });
      return null;
    } finally {
      loading.value = false;
    }
  };

  const fetchHotItems = async () => {
    try {
      const response: any = await api.item.getHotItems();
      if (response?.code === 200 && response.data) {
        hotItems.value = response.data;
      }
      return response;
    } catch (error) {
      ErrorHandler.handle(error, { silent: true });
      return null;
    }
  };

  const searchItems = async (
    keyword: string,
    page = 1,
    size = 20,
    sortBy = 'createdAt',
  ) => {
    loading.value = true;
    try {
      const response: any = await api.item.searchItems(keyword, page, size, sortBy);
      if (response?.code === 200 && response.data) {
        searchResults.value = response.data.content;
        searchTotal.value = response.data.totalElements;
      }
      return response;
    } catch (error) {
      ErrorHandler.handle(error, { silent: true });
      return null;
    } finally {
      loading.value = false;
    }
  };

  const getItem = async (id: number | string) => {
    try {
      const response: any = await api.item.getItem(id);
      if (response?.code === 200 && response.data) {
        currentItem.value = response.data;
      }
      return response;
    } catch (error) {
      ErrorHandler.handle(error, { silent: true });
      return null;
    }
  };

  const createItem = async (itemData: CreateItemRequest) => {
    loading.value = true;
    try {
      const response: any = await api.item.createItem(itemData);
      if (response.code === 200) {
        await api.clearCache('/items');
      }
      return response;
    } catch (error) {
      ErrorHandler.handle(error, { silent: true });
      return null;
    } finally {
      loading.value = false;
    }
  };

  const updateItem = async (id: number | string, itemData: UpdateItemRequest) => {
    loading.value = true;
    try {
      const response: any = await api.item.updateItem(id, itemData);
      if (response.code === 200) {
        await api.clearCache(`/items/${id}`);
        await api.clearCache('/items');
      }
      return response;
    } catch (error) {
      ErrorHandler.handle(error, { silent: true });
      return null;
    } finally {
      loading.value = false;
    }
  };

  const offShelfItem = async (id: number | string) => {
    loading.value = true;
    try {
      const response: any = await api.item.offShelf(id);
      if (response.code === 200) {
        await api.clearCache(`/items/${id}`);
        await api.clearCache('/items');
      }
      return response;
    } catch (error) {
      ErrorHandler.handle(error, { silent: true });
      return null;
    } finally {
      loading.value = false;
    }
  };

  const fetchUserItems = async (status: string, page = 1, size = 20) => {
    loading.value = true;
    try {
      const response: any = await api.user.getItems(status, page, size);
      if (response?.code === 200 && response.data) {
        userItems.value = response.data.content;
        userItemsTotal.value = response.data.totalElements;
      }
      return response;
    } catch (error) {
      ErrorHandler.handle(error, { silent: true });
      return null;
    } finally {
      loading.value = false;
    }
  };

  const uploadImage = async (formData: FormData) => {
    loading.value = true;
    try {
      const response: any = await api.item.uploadImage(formData);
      return response;
    } catch (error) {
      ErrorHandler.handle(error, { silent: true });
      return null;
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
