<template>
  <div class="verification-status" :class="statusClass">
    <div class="status-icon">
      <CheckCircle v-if="status === 'approved'" :size="48" />
      <Clock v-else-if="status === 'pending'" :size="48" />
      <XCircle v-else :size="48" />
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

<script setup lang="ts">
import { computed } from 'vue';
import { CheckCircle, Clock, XCircle } from 'lucide-vue-next';

const props = defineProps({
  status: {
    type: String,
    required: true,
    validator: (v: string) => ['approved', 'pending', 'rejected'].includes(v)
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
  background: var(--color-success-alpha-10);
  border: 1px solid var(--color-success-alpha-20);
}

.verification-status.status-pending {
  background: var(--color-warning-alpha-10);
  border: 1px solid var(--color-warning-alpha-20);
}

.verification-status.status-rejected {
  background: var(--color-danger-alpha-10);
  border: 1px solid var(--color-danger-alpha-20);
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
