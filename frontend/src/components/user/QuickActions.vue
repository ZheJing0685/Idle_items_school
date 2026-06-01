<template>
  <div class="quick-actions">
    <router-link
      v-for="action in actions"
      :key="action.path"
      :to="action.path"
      class="action-item"
    >
      <div class="action-icon">
        <component :is="getIcon(action.icon)" :size="24" stroke-width="1.5" />
      </div>
      <span class="action-text">{{ action.name }}</span>
    </router-link>
  </div>
</template>

<script setup lang="ts">
import type { PropType } from 'vue';
import { Plus, ShoppingBag, MessageSquare } from 'lucide-vue-next';

const iconComponentMap: Record<string, any> = {
  plus: Plus,
  'shopping-bag': ShoppingBag,
  message: MessageSquare,
};

const getIcon = (iconName: string) => {
  return iconComponentMap[iconName] || Plus;
};

interface ActionItem {
  path: string
  name: string
  icon: string
}

defineProps({
  actions: {
    type: Array as PropType<ActionItem[]>,
    required: true
  }
});
</script>

<style scoped>
.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-3);
  padding: var(--space-5);
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-subtle);
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-5);
  background: oklch(62% 0.14 195 / 0.06);
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: all var(--duration-fast);
}

.action-item:hover {
  background: oklch(62% 0.14 195 / 0.12);
  transform: translateY(-2px);
}

.action-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-color);
  color: var(--text-inverse);
  border-radius: var(--radius-md);
}

.action-icon :deep(svg) {
  width: 24px;
  height: 24px;
}

.action-text {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-primary);
  white-space: nowrap;
}

@media (max-width: 768px) {
  .quick-actions {
    grid-template-columns: repeat(3, 1fr);
    padding: var(--space-4);
    gap: var(--space-2);
  }

  .action-item {
    padding: var(--space-3) var(--space-2);
  }

  .action-icon {
    width: 44px;
    height: 44px;
  }

  .action-icon :deep(svg) {
    width: 22px;
    height: 22px;
  }

  .action-text {
    font-size: var(--text-xs);
  }
}
</style>
