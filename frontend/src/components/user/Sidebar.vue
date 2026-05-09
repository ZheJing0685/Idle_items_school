<template>
  <aside class="sidebar" :class="{ collapsed }">
    <div class="sidebar-header">
      <button class="toggle-btn" @click="$emit('toggle')">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" v-if="collapsed">
          <path d="M9 18l6-6-6-6"/>
        </svg>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" v-else>
          <path d="M15 18l-6-6 6-6"/>
        </svg>
      </button>
    </div>
    
    <nav class="sidebar-nav">
      <router-link
        v-for="item in menuItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
        :title="collapsed ? item.name : ''"
      >
        <span class="nav-icon" v-html="item.icon"></span>
        <span class="nav-text" v-if="!collapsed">{{ item.name }}</span>
        <span class="nav-badge" v-if="item.badge && !collapsed">{{ item.badge }}</span>
      </router-link>
    </nav>
  </aside>
</template>

<script setup>
import { useRoute } from 'vue-router';

const props = defineProps({
  collapsed: Boolean,
  menuItems: {
    type: Array,
    required: true
  }
});

defineEmits(['toggle']);

const route = useRoute();

const isActive = (path) => {
  return route.path === path || route.path.startsWith(path + '/');
};
</script>

<style scoped>
.sidebar {
  position: sticky;
  top: 0;
  width: 200px;
  height: 100vh;
  background: var(--bg-surface);
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  transition: width var(--duration-normal) var(--ease-out-quart);
  flex-shrink: 0;
}

.sidebar.collapsed {
  width: 72px;
}

.sidebar-header {
  display: flex;
  justify-content: flex-end;
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-subtle);
}

.toggle-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border-radius: var(--radius-md);
  color: var(--text-muted);
  transition: all var(--duration-fast);
}

.toggle-btn:hover {
  background: var(--bg-muted);
  color: var(--text-primary);
}

.sidebar-nav {
  flex: 1;
  padding: var(--space-4);
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  margin-bottom: var(--space-1);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  text-decoration: none;
  transition: all var(--duration-fast);
  position: relative;
}

.nav-item:hover {
  background: var(--bg-muted);
  color: var(--text-primary);
}

.nav-item.active {
  background: oklch(62% 0.14 195 / 0.08);
  color: var(--primary-color);
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: var(--primary-color);
  border-radius: 0 2px 2px 0;
}

.nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  flex-shrink: 0;
}

.nav-icon :deep(svg) {
  width: 20px;
  height: 20px;
}

.nav-text {
  font-size: var(--text-sm);
  font-weight: 500;
  white-space: nowrap;
}

.nav-badge {
  margin-left: auto;
  padding: 2px 8px;
  font-size: var(--text-xs);
  font-weight: 600;
  background: var(--error-color);
  color: white;
  border-radius: var(--radius-full);
}

.sidebar.collapsed .nav-item {
  justify-content: center;
  padding: var(--space-3);
}

.sidebar.collapsed .nav-item.active::before {
  left: 0;
  width: 3px;
  height: 24px;
}
</style>