import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import api from '../api';

const itemStore = defineStore('item', () => {
  const items = ref([]);
  const hotItems = ref([]);
  const currentItem = ref(null);
  const loading = ref(false);
  const page = ref(1);
  const total = ref(0);
  const size = ref(20);

  const fetchItems = async (params = {}) => {
    loading.value = true;
    try {
      const response = await api.item.getItems({
        page: params.page || page.value,
        size: size.value,
        categoryId: params.categoryId,
        sortBy: params.sortBy,
        condition: params.condition,
        deliveryMethod: params.deliveryMethod,
      });
      items.value = response.data.content;
      total.value = response.data.totalElements;
      page.value = params.page || page.value;
      return response;
    } finally {
      loading.value = false;
    }
  };

  const searchItems = async (keyword, pageNum = 1, sortBy = 'createdAt') => {
    loading.value = true;
    try {
      const response = await api.item.searchItems(keyword, pageNum, size.value, sortBy);
      items.value = response.data.content;
      total.value = response.data.totalElements;
      page.value = pageNum;
      return response;
    } finally {
      loading.value = false;
    }
  };

  const fetchHotItems = async () => {
    try {
      const response = await api.item.getHotItems();
      hotItems.value = response.data;
      return response;
    } catch (error) {
      console.error('获取热门物品失败', error);
    }
  };

  const fetchItemDetail = async (id) => {
    loading.value = true;
    try {
      const response = await api.item.getItem(id);
      currentItem.value = response.data;
      return response;
    } finally {
      loading.value = false;
    }
  };

  const publishItem = async (itemData) => {
    loading.value = true;
    try {
      const response = await api.item.createItem(itemData);
      return response;
    } finally {
      loading.value = false;
    }
  };

  const uploadImage = async (file) => {
    loading.value = true;
    try {
      const formData = new FormData();
      const fileToUpload = file.raw || file;
      formData.append('file', fileToUpload);
      const response = await api.item.uploadImage(formData);
      return response;
    } finally {
      loading.value = false;
    }
  };

  return {
    items,
    hotItems,
    currentItem,
    loading,
    page,
    total,
    size,
    fetchItems,
    searchItems,
    fetchHotItems,
    fetchItemDetail,
    publishItem,
    uploadImage,
  };
});

export const useItemStore = itemStore;
export default itemStore;
