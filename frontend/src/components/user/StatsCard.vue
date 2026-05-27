<template>
  <div class="stats-card">
    <div class="stat-item" v-for="stat in stats" :key="stat.label" :class="{ accent: stat.accent }">
      <span class="stat-value">{{ stat.value }}</span>
      <span class="stat-label">{{ stat.label }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { PropType } from 'vue';

interface StatItem {
  value: string | number
  label: string
  accent?: boolean
}

defineProps({
  stats: {
    type: Array as PropType<StatItem[]>,
    required: true,
    validator: (v: StatItem[]) => v.every(s => 'value' in s && 'label' in s)
  }
});
</script>

<style scoped>
.stats-card {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-3);
  padding: var(--space-5);
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-subtle);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4);
  background: var(--bg-muted);
  border-radius: var(--radius-md);
  transition: all var(--duration-fast);
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.stat-item.accent {
  background: oklch(62% 0.14 195 / 0.1);
}

.stat-value {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: 800;
  color: var(--text-primary);
}

.stat-item.accent .stat-value {
  color: var(--primary-color);
}

.stat-label {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .stats-card {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
