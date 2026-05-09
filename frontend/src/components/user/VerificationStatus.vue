<template>
  <div class="verification-status" :class="statusClass">
    <div class="status-icon">
      <svg v-if="status === 'approved'" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M22 11.08V12a10 10 0 11-5.93-9.14"/>
        <polyline points="22 4 12 14.01 9 11.01"/>
      </svg>
      <svg v-else-if="status === 'pending'" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"/>
        <polyline points="12 6 12 12 16 14"/>
      </svg>
      <svg v-else width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"/>
        <line x1="15" y1="9" x2="9" y2="15"/>
        <line x1="9" y1="9" x2="15" y2="15"/>
      </svg>
    </div>
    <div class="status-content">
      <h3 class="status-title">{{ title }}</h3>
      <p class="status-desc">{{ description }}</p>
      <p class="status-reason" v-if="reason">驳回原因：{{ reason }}</p>
    </div>
    <div class="status-action" v-if="$slots.action">
      <slot name="action"></slot>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  status: {
    type: String,
    required: true,
    validator: (v) => ['approved', 'pending', 'rejected'].includes(v)
  },
  title: String,
  description: String,
  reason: String
});

defineEmits(['retry']);

const statusClass = computed(() => `status-${props.status}`);
</script>

<style scoped>
.verification-status {
  display: flex;
  align-items: flex-start;
  gap: var(--space-5);
  padding: var(--space-6);
  border-radius: var(--radius-lg);
  margin-bottom: var(--space-6);
}

.verification-status.status-approved {
  background: oklch(62% 0.12 158 / 0.1);
  border: 1px solid oklch(62% 0.12 158 / 0.3);
}

.verification-status.status-pending {
  background: oklch(75% 0.14 85 / 0.1);
  border: 1px solid oklch(75% 0.14 85 / 0.3);
}

.verification-status.status-rejected {
  background: oklch(60% 0.20 25 / 0.1);
  border: 1px solid oklch(60% 0.20 25 / 0.3);
}

.status-icon {
  flex-shrink: 0;
}

.status-approved .status-icon {
  color: var(--success-color);
}

.status-pending .status-icon {
  color: var(--warning-color);
}

.status-rejected .status-icon {
  color: var(--error-color);
}

.status-content {
  flex: 1;
}

.status-title {
  font-size: var(--text-lg);
  font-weight: 600;
  margin: 0 0 var(--space-2);
}

.status-approved .status-title {
  color: var(--success-color);
}

.status-pending .status-title {
  color: var(--warning-color);
}

.status-rejected .status-title {
  color: var(--error-color);
}

.status-desc {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
}

.status-reason {
  font-size: var(--text-sm);
  color: var(--error-color);
  margin: var(--space-2) 0 0;
  padding: var(--space-3);
  background: oklch(60% 0.20 25 / 0.05);
  border-radius: var(--radius-md);
}

.status-action {
  flex-shrink: 0;
}
</style>