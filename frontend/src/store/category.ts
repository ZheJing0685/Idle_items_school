import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import api from '../api';
import type { CategoryInfo } from '../types/api';

const EXPIRY_MS = 5 * 60 * 1000;

/** 基于分类名称 hash 的确定性 emoji 池 */
const EMOJI_POOL = ['📦', '💻', '📚', '🧴', '👟', '⚽', '🪑', '🎨', '🔧', '🎵', '🌿', '📷'];

function getFallbackEmoji(name: string): string {
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = ((hash << 5) - hash) + name.charCodeAt(i);
  }
  return EMOJI_POOL[Math.abs(hash) % EMOJI_POOL.length];
}

export const useCategoryStore = defineStore('category', () => {
  const categoryTree = ref<CategoryInfo[]>([]);
  const loading = ref(false);
  const loaded = ref(false);
  const error = ref('');
  const lastFetched = ref(0);

  /** 将树形分类拍平（含 parentId、level 标记），供下拉选择等场景使用 */
  const flatCategories = computed<CategoryInfo[]>(() => {
    const result: CategoryInfo[] = [];
    const flatten = (nodes: CategoryInfo[], parentId: number | null, level: number) => {
      for (const node of nodes) {
        result.push({ ...node, parentId, level, children: undefined });
        if (node.children?.length) flatten(node.children, node.id, level + 1);
      }
    };
    flatten(categoryTree.value, null, 1);
    return result;
  });

  /** categories 作为 flatCategories 的别名（兼容旧用法） */
  const categories = flatCategories;

  /** 获取分类列表（带缓存），优先使用树接口 */
  async function fetchAll(force = false) {
    if (!force && loaded.value && Date.now() - lastFetched.value < EXPIRY_MS) return;
    loading.value = true;
    error.value = '';
    try {
      const res = await api.category.getCategoryTree();
      if (res.code === 200 && Array.isArray(res.data)) {
        categoryTree.value = res.data as CategoryInfo[];
        loaded.value = true;
        lastFetched.value = Date.now();
      }
    } catch {
      error.value = '分类数据加载失败';
    } finally {
      loading.value = false;
    }
  }

  /** 获取分类图标（优先使用 API 返回的 icon，无则基于名称 hash 确定性回退） */
  function getCategoryIcon(categoryName: string): string {
    const found = categories.value.find(c => c.name === categoryName);
    return found?.icon || getFallbackEmoji(categoryName);
  }

  /** 获取从根到指定分类的路径数组 */
  function getCategoryPath(id: number): CategoryInfo[] {
    const path: CategoryInfo[] = [];
    const findPath = (nodes: CategoryInfo[]): boolean => {
      for (const node of nodes) {
        path.push(node);
        if (node.id === id) return true;
        if (node.children?.length && findPath(node.children)) return true;
        path.pop();
      }
      return false;
    };
    findPath(categoryTree.value);
    return path;
  }

  /** 获取父级分类名称 */
  function getParentName(id: number): string {
    const node = categories.value.find(c => c.id === id);
    if (!node || node.parentId == null) return '';
    const parent = categories.value.find(c => c.id === node.parentId);
    return parent?.name || '';
  }

  return {
    categoryTree, flatCategories, categories,
    loading, loaded, error, lastFetched,
    fetchAll, getCategoryIcon, getCategoryPath, getParentName,
  };
});
