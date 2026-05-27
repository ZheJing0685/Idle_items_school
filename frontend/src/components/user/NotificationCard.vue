<template>
  <div class="notification-card" :class="{ unread: !isRead }" @click="$emit('click')" @keydown.enter="$emit('click')" @keydown.space.prevent="$emit('click')" tabindex="0" role="button" aria-label="查看通知详情">
    <div class="card-icon" :style="{ background: iconBg }">
      <Settings v-if="type === 1" :size="24" />
      <ShoppingCart v-else-if="type === 2" :size="24" />
      <MessageSquare v-else-if="type === 3" :size="24" />
      <Bell v-else :size="24" />
    </div>
    <div class="card-content">
      <h4 class="card-title">{{ title }}</h4>
      <p class="card-desc">{{ content }}</p>
      <span class="card-time">{{ time }}</span>
    </div>
    <div class="card-action" v-if="!isRead">
      <button class="read-btn" @click.stop="$emit('read')">标为已读</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Settings, ShoppingCart, MessageSquare, Bell } from 'lucide-vue-next';

const props = defineProps({
  id: [String, Number],
  type: Number,
  title: String,
  content: String,
  time: String,
  isRead: Boolean
});

defineEmits(['click', 'read']);

const iconBg = computed(() => {
  const map: Record<number, string> = {
    1: 'var(--color-info-alpha-10)',
    2: 'var(--color-primary-alpha-10)',
    3: 'var(--color-success-alpha-10)',
    4: 'var(--color-warning-alpha-10)'
  };
  return map[props.type] || 'var(--color-info-alpha-10)';
});

</script>

<style scoped>
.notification-card {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-subtle);
  cursor: pointer;
  transition: all var(--duration-fast);
}

.notification-card:hover {
  box-shadow: var(--shadow-sm);
}

.notification-card.unread {
  background: var(--color-primary-alpha-10);
  border-color: var(--color-primary-alpha-20);
}

.card-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  flex-shrink: 0;
  color: var(--text-secondary);
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-1);
}

.card-desc {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0 0 var(--space-2);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.card-action {
  flex-shrink: 0;
}

.read-btn {
  padding: var(--space-2) var(--space-3);
  background: transparent;
  border: 1px solid var(--border-default);
  border-radius: var(--radius-md);
  font-size: var(--text-xs);
  color: var(--primary-color);
  cursor: pointer;
  transition: all var(--duration-fast);
}

.read-btn:hover {
  background: oklch(62% 0.14 195 / 0.08);
  border-color: var(--primary-color);
}
</style>
