<template>
  <div class="empty-state">
    <div class="empty-icon">
      <CheckCircle v-if="type === 'success'" :size="80" color="var(--success-color)" />
      <AlertCircle v-else-if="type === 'warning'" :size="80" color="var(--warning-color)" />
      <XCircle v-else-if="type === 'error'" :size="80" color="var(--error-color)" />
      <Inbox v-else :size="80" color="var(--text-muted)" />
    </div>
    <h3 class="empty-title">{{ title }}</h3>
    <p class="empty-desc" v-if="description">{{ description }}</p>
    <div class="empty-action" v-if="$slots.action">
      <slot name="action"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { CheckCircle, AlertCircle, XCircle, Inbox } from 'lucide-vue-next';

defineProps({
  type: {
    type: String,
    default: 'default',
    validator: (v: string) => ['default', 'success', 'warning', 'error'].includes(v)
  },
  title: {
    type: String,
    default: '暂无数据'
  },
  description: {
    type: String,
    default: ''
  }
});
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-16) var(--space-6);
  text-align: center;
}

.empty-icon {
  margin-bottom: var(--space-6);
  opacity: 0.6;
}

.empty-title {
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--text-secondary);
  margin: 0 0 var(--space-2);
}

.empty-desc {
  font-size: var(--text-sm);
  color: var(--text-muted);
  margin: 0 0 var(--space-6);
  max-width: 300px;
}

.empty-action {
  display: flex;
  gap: var(--space-3);
}
</style>
