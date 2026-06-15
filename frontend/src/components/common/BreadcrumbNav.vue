<template>
  <nav class="breadcrumb-nav" aria-label="面包屑导航">
    <ol class="breadcrumb-list">
      <li
        v-for="(item, index) in breadcrumbs"
        :key="item.path"
        class="breadcrumb-item"
        :class="{ 'is-active': index === breadcrumbs.length - 1 }"
      >
        <router-link
          v-if="index < breadcrumbs.length - 1"
          :to="item.path"
          class="breadcrumb-link"
        >
          {{ item.title }}
        </router-link>
        <span v-else class="breadcrumb-current" aria-current="page">
          {{ item.title }}
        </span>
        <ChevronRight
          v-if="index < breadcrumbs.length - 1"
          class="breadcrumb-separator"
          :size="14"
          aria-hidden="true"
        />
      </li>
    </ol>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { ChevronRight } from 'lucide-vue-next';

const route = useRoute();

interface BreadcrumbItem {
  path: string
  title: string
}

const breadcrumbs = computed<BreadcrumbItem[]>(() => {
  const matched = route.matched.filter((record) => record.meta?.title);
  const items: BreadcrumbItem[] = matched.map((record) => ({
    path: record.path || '/',
    title: (record.meta?.title as string) || '',
  }));

  // 确保首页始终在首位
  if (items.length > 0 && items[0].path !== '/') {
    items.unshift({ path: '/', title: '首页' });
  }
  return items;
});
</script>

<style scoped>
.breadcrumb-nav {
  margin-bottom: 16px;
}

.breadcrumb-list {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  list-style: none;
  margin: 0;
  padding: 0;
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.breadcrumb-link {
  color: var(--fg-secondary, #666);
  text-decoration: none;
  transition: color 0.2s;
}

.breadcrumb-link:hover {
  color: var(--accent, #409eff);
  text-decoration: underline;
}

.breadcrumb-current {
  color: var(--accent, #409eff);
  font-weight: 600;
}

.breadcrumb-separator {
  color: var(--fg-secondary, #999);
  flex-shrink: 0;
}

.is-active .breadcrumb-current {
  cursor: default;
}
</style>
