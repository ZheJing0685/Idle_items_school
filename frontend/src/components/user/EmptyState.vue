<template>
  <div class="empty-state">
    <div class="empty-icon">
      <svg width="120" height="120" viewBox="0 0 120 120" fill="none">
        <circle cx="60" cy="60" r="58" stroke="var(--border-default)" stroke-width="2" stroke-dasharray="8 4"/>
        <path d="M45 55L55 65L75 45" stroke="var(--text-muted)" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" v-if="type === 'success'"/>
        <path d="M60 40V60M60 70V72" stroke="var(--text-muted)" stroke-width="3" stroke-linecap="round" v-else-if="type === 'warning'"/>
        <path d="M45 45L75 75M75 45L45 75" stroke="var(--text-muted)" stroke-width="3" stroke-linecap="round" v-else-if="type === 'error'"/>
        <g v-else>
          <rect x="35" y="40" width="50" height="35" rx="4" stroke="var(--text-muted)" stroke-width="2"/>
          <path d="M45 55H75M45 65H65" stroke="var(--text-muted)" stroke-width="2" stroke-linecap="round"/>
        </g>
      </svg>
    </div>
    <h3 class="empty-title">{{ title }}</h3>
    <p class="empty-desc" v-if="description">{{ description }}</p>
    <div class="empty-action" v-if="$slots.action">
      <slot name="action"></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'default',
    validator: (v) => ['default', 'success', 'warning', 'error'].includes(v)
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