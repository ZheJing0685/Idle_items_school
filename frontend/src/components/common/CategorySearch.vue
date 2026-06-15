<template>
  <div class="category-search" ref="containerRef">
    <!-- 输入框 -->
    <div class="category-search-input-wrapper">
      <svg
        class="search-icon"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        aria-hidden="true"
      >
        <circle cx="11" cy="11" r="8" />
        <path d="M21 21l-4.35-4.35" />
      </svg>
      <input
        type="text"
        class="category-search-input"
        :placeholder="placeholder"
        v-model="keyword"
        @input="onInput"
        @focus="onFocus"
        @keydown.escape="closeDropdown"
        @keydown.enter.prevent="onEnter"
        @keydown.down.prevent="onArrowDown"
        @keydown.up.prevent="onArrowUp"
        aria-label="搜索分类"
        aria-autocomplete="list"
        role="combobox"
        :aria-expanded="dropdownVisible"
        :aria-controls="dropdownId"
        :aria-activedescendant="highlightedIndex >= 0 ? `${dropdownId}-item-${highlightedIndex}` : undefined"
      />
      <button
        v-if="keyword"
        class="search-clear"
        @click="clearInput"
        aria-label="清除搜索"
        type="button"
      >
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <path d="M18 6L6 18M6 6l12 12" />
        </svg>
      </button>
    </div>

    <!-- 下拉建议列表 -->
    <Transition name="dropdown">
      <div
        v-if="dropdownVisible && suggestions.length > 0"
        :id="dropdownId"
        class="category-search-dropdown"
        role="listbox"
      >
        <div
          v-for="(suggestion, index) in suggestions"
          :key="suggestion.id"
          :id="`${dropdownId}-item-${index}`"
          class="category-search-item"
          :class="{ highlighted: highlightedIndex === index }"
          role="option"
          :aria-selected="highlightedIndex === index"
          @click="selectSuggestion(suggestion)"
          @mouseenter="highlightedIndex = index"
        >
          <span class="category-search-item-icon">{{ suggestion.icon || '📂' }}</span>
          <div class="category-search-item-info">
            <span class="category-search-item-name">{{ suggestion.name }}</span>
            <span class="category-search-item-path" v-if="suggestion.path">
              {{ suggestion.path }}
            </span>
          </div>
        </div>
        <div class="category-search-footer">
          {{ suggestions.length }} 个分类匹配
        </div>
      </div>
    </Transition>

    <!-- 空状态提示 -->
    <Transition name="dropdown">
      <div
        v-if="dropdownVisible && keyword && keyword.trim() && suggestions.length === 0 && !loading"
        class="category-search-dropdown category-search-empty"
      >
        <div class="category-search-empty-icon">🔍</div>
        <div class="category-search-empty-text">未找到匹配的分类</div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import api from '../../api';
import { useCategoryStore } from '../../store/category';
import type { CategoryInfo } from '../../types/api';

withDefaults(defineProps<{
  placeholder?: string
}>(), {
  placeholder: '搜索分类',
});

const emit = defineEmits<{
  select: [categoryId: number, categoryName: string]
}>();

const categoryStore = useCategoryStore();
const containerRef = ref<HTMLElement | null>(null);
const keyword = ref('');
const suggestions = ref<(CategoryInfo & { path?: string })[]>([]);
const loading = ref(false);
const highlightedIndex = ref(-1);
const dropdownVisible = ref(false);

const dropdownId = `category-search-dropdown-${Math.random().toString(36).slice(2, 8)}`;

let debounceTimer: ReturnType<typeof setTimeout> | null = null;

/** 获取分类的完整路径文字（如 "数码电子 > 手机"） */
function getCategoryPathText(category: CategoryInfo): string {
  if (category.parentId == null) return '';
  const path = categoryStore.getCategoryPath(category.id);
  if (path.length <= 1) return '';
  return path
    .slice(0, -1) // 去掉自身
    .map(c => c.name)
    .join(' > ');
}

/** 搜索分类 */
async function fetchSuggestions(kw: string) {
  if (!kw || kw.trim().length === 0) {
    suggestions.value = [];
    dropdownVisible.value = false;
    return;
  }
  loading.value = true;
  try {
    const res = await api.category.suggestCategories(kw.trim());
    if (res.code === 200 && Array.isArray(res.data)) {
      suggestions.value = (res.data as CategoryInfo[])
        .slice(0, 8)
        .map(c => ({
          ...c,
          path: getCategoryPathText(c),
        }));
      dropdownVisible.value = suggestions.value.length > 0;
    } else {
      suggestions.value = [];
      dropdownVisible.value = false;
    }
  } catch {
    // 接口失败时，用本地分类降级
    const kwLower = kw.trim().toLowerCase();
    const local = categoryStore.flatCategories
      .filter(c => c.name.toLowerCase().includes(kwLower))
      .slice(0, 8)
      .map(c => ({
        ...c,
        path: getCategoryPathText(c),
      }));
    suggestions.value = local;
    dropdownVisible.value = local.length > 0;
  } finally {
    loading.value = false;
  }
}

function onInput() {
  if (debounceTimer) clearTimeout(debounceTimer);
  highlightedIndex.value = -1;
  debounceTimer = setTimeout(() => {
    fetchSuggestions(keyword.value);
  }, 250);
}

function onFocus() {
  if (suggestions.value.length > 0) {
    dropdownVisible.value = true;
  }
}

function closeDropdown() {
  dropdownVisible.value = false;
  highlightedIndex.value = -1;
}

function clearInput() {
  keyword.value = '';
  suggestions.value = [];
  dropdownVisible.value = false;
  highlightedIndex.value = -1;
}

function selectSuggestion(suggestion: CategoryInfo) {
  keyword.value = suggestion.name;
  dropdownVisible.value = false;
  emit('select', suggestion.id, suggestion.name);
}

function onEnter() {
  if (highlightedIndex.value >= 0 && highlightedIndex.value < suggestions.value.length) {
    selectSuggestion(suggestions.value[highlightedIndex.value]);
  } else if (suggestions.value.length > 0) {
    selectSuggestion(suggestions.value[0]);
  }
}

function onArrowDown() {
  if (suggestions.value.length === 0) return;
  highlightedIndex.value = (highlightedIndex.value + 1) % suggestions.value.length;
}

function onArrowUp() {
  if (suggestions.value.length === 0) return;
  highlightedIndex.value = highlightedIndex.value <= 0
    ? suggestions.value.length - 1
    : highlightedIndex.value - 1;
}

/** 点击外部关闭下拉 */
function onClickOutside(e: MouseEvent) {
  if (containerRef.value && !containerRef.value.contains(e.target as Node)) {
    closeDropdown();
  }
}

onMounted(() => {
  document.addEventListener('click', onClickOutside);
  // 确保分类数据已加载
  if (!categoryStore.loaded) {
    categoryStore.fetchAll();
  }
});

onUnmounted(() => {
  document.removeEventListener('click', onClickOutside);
  if (debounceTimer) clearTimeout(debounceTimer);
});
</script>

<style scoped>
.category-search {
  position: relative;
  width: 100%;
}

.category-search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  width: 18px;
  height: 18px;
  color: var(--text-muted);
  pointer-events: none;
  flex-shrink: 0;
}

.category-search-input {
  width: 100%;
  padding: 10px 36px 10px 38px;
  border: 2px solid var(--border);
  border-radius: var(--r-md, var(--radius-md, 12px));
  background: var(--surface, var(--bg-surface));
  color: var(--text-primary);
  font-size: var(--text-sm, 0.875rem);
  outline: none;
  transition: border-color 200ms ease, box-shadow 200ms ease;
}

.category-search-input:focus {
  border-color: var(--accent, var(--primary-color));
  box-shadow: 0 0 0 3px var(--accent-light, oklch(56% 0.15 150 / 0.1));
}

.category-search-input::placeholder {
  color: var(--text-muted);
}

.search-clear {
  position: absolute;
  right: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: var(--radius-full, 9999px);
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  border: none;
  padding: 0;
  transition: background 150ms ease, color 150ms ease;
}

.search-clear:hover {
  background: var(--bg-muted, #f0f0f0);
  color: var(--text-primary);
}

/* Dropdown */
.category-search-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  z-index: 100;
  background: var(--surface, var(--bg-surface));
  border: 1px solid var(--border, var(--border-default));
  border-radius: var(--r-md, var(--radius-md, 12px));
  box-shadow: var(--shadow-lg, 0 8px 32px rgba(0,0,0,0.12));
  max-height: 320px;
  overflow-y: auto;
  padding: 6px;
}

.category-search-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px;
}

.category-search-empty-icon {
  font-size: 28px;
  margin-bottom: 8px;
  opacity: 0.6;
}

.category-search-empty-text {
  font-size: var(--text-sm, 0.875rem);
  color: var(--text-muted);
}

.category-search-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--r-sm, var(--radius-sm, 8px));
  cursor: pointer;
  transition: background 150ms ease;
}

.category-search-item:hover,
.category-search-item.highlighted {
  background: var(--accent-light, oklch(56% 0.15 150 / 0.06));
}

.category-search-item-icon {
  font-size: 20px;
  flex-shrink: 0;
  width: 28px;
  text-align: center;
}

.category-search-item-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.category-search-item-name {
  font-size: var(--text-sm, 0.875rem);
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.category-search-item-path {
  font-size: var(--text-xs, 0.75rem);
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.category-search-footer {
  padding: 6px 12px 4px;
  font-size: var(--text-xs, 0.75rem);
  color: var(--text-muted);
  border-top: 1px solid var(--border, var(--border-subtle));
  margin-top: 4px;
  text-align: center;
}

/* Dropdown Transition */
.dropdown-enter-active {
  transition: opacity 200ms ease, transform 200ms ease;
}

.dropdown-leave-active {
  transition: opacity 150ms ease, transform 150ms ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

.dropdown-enter-to,
.dropdown-leave-from {
  opacity: 1;
  transform: translateY(0);
}

/* Scrollbar */
.category-search-dropdown::-webkit-scrollbar {
  width: 4px;
}

.category-search-dropdown::-webkit-scrollbar-track {
  background: transparent;
}

.category-search-dropdown::-webkit-scrollbar-thumb {
  background: var(--border-strong, #ccc);
  border-radius: 2px;
}
</style>
