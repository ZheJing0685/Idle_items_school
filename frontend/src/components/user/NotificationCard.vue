<template>
  <div class="notification-card" :class="{ unread: !isRead }" @click="$emit('click')">
    <div class="card-icon" :style="{ background: iconBg }">
      <svg v-if="type === 1" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-2 2 2 2 0 01-2-2v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83 0 2 2 0 010-2.83l.06-.06a1.65 1.65 0 00.33-1.82 1.65 1.65 0 00-1.51-1H3a2 2 0 01-2-2 2 2 0 012-2h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 010-2.83 2 2 0 012.83 0l.06.06a1.65 1.65 0 001.82.33H9a1.65 1.65 0 001-1.51V3a2 2 0 012-2 2 2 0 012 2v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 0 2 2 0 010 2.83l-.06.06a1.65 1.65 0 00-.33 1.82V9a1.65 1.65 0 001.51 1H21a2 2 0 012 2 2 2 0 01-2 2h-.09a1.65 1.65 0 00-1.51 1z"/>
      </svg>
      <svg v-else-if="type === 2" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/>
      </svg>
      <svg v-else-if="type === 3" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
      </svg>
      <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/>
      </svg>
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

<script setup>
import { computed } from 'vue';

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
  const map = {
    1: 'oklch(58% 0.01 195 / 0.1)',
    2: 'oklch(62% 0.14 250 / 0.1)',
    3: 'oklch(62% 0.12 158 / 0.1)',
    4: 'oklch(75% 0.14 85 / 0.1)'
  };
  return map[props.type] || map[4];
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
  background: oklch(62% 0.14 195 / 0.04);
  border-color: oklch(62% 0.14 195 / 0.2);
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
