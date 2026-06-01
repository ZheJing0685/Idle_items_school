<template>
  <aside class="sidebar" :class="{ collapsed }">
    <div class="sidebar-header">
      <button class="toggle-btn" @click="$emit('toggle')" :aria-label="collapsed ? '展开侧边栏' : '收起侧边栏'">
        <ChevronRight :size="20" v-if="collapsed" />
        <ChevronLeft :size="20" v-else />
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
        <span class="nav-icon">
          <component :is="getIcon(item.icon)" :size="20" />
        </span>
        <span class="nav-text" v-if="!collapsed">{{ item.name }}</span>
        <span class="nav-badge" v-if="item.badge && !collapsed">{{ item.badge }}</span>
      </router-link>
    </nav>
  </aside>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router';
import type { PropType } from 'vue';
import { ChevronRight, ChevronLeft, User, Package, ShoppingBag, Heart, MessageSquare, Bell, Shield, CheckCircle, Plus, FileText } from 'lucide-vue-next';

interface MenuItem {
  path: string
  name: string
  icon: string
  badge?: string | number
}

const iconComponentMap: Record<string, any> = {
  user: User,
  box: Package,
  'shopping-bag': ShoppingBag,
  heart: Heart,
  message: MessageSquare,
  bell: Bell,
  shield: Shield,
  check: CheckCircle,
  plus: Plus,
  file: FileText,
};

const getIcon = (iconName: string) => {
  return iconComponentMap[iconName] || Package;
};

const props = defineProps({
  collapsed: Boolean,
  menuItems: {
    type: Array as PropType<MenuItem[]>,
    required: true
  }
});

defineEmits(['toggle']);

const route = useRoute();

const isActive = (path: string) => {
  return route.path === path || route.path.startsWith(path + '/');
};
</script>

<style scoped>
.sidebar {
  position: sticky;
  top: 0;
  width: 200px;
  height: 100vh;
  min-height: 100vh;
  background: var(--bg-surface);
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  transition: width var(--duration-normal) var(--ease-out-quart);
  flex-shrink: 0;
  z-index: 50;
}

.sidebar.collapsed {
  width: 72px;
}

.sidebar-header {
  display: flex;
  justify-content: flex-end;
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.toggle-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--radius-md);
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--duration-fast);
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.toggle-btn:hover {
  background: var(--bg-muted);
  color: var(--text-primary);
}

.sidebar-nav {
  flex: 1;
  padding: var(--space-4);
  overflow-y: auto;
  overflow-x: hidden;
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
  white-space: nowrap;
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
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-badge {
  margin-left: auto;
  padding: 2px 8px;
  font-size: var(--text-xs);
  font-weight: 600;
  background: var(--error-color);
  color: var(--text-inverse);
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

/* 移动端隐藏侧边栏 */
@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
}
</style>
