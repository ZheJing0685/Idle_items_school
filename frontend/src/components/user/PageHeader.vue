<template>
  <div class="page-header">
    <div class="header-content">
      <div class="header-left">
        <h2 class="header-title">{{ title }}</h2>
        <p class="header-subtitle" v-if="subtitle">{{ subtitle }}</p>
      </div>
      <div class="header-right" v-if="$slots.action">
        <slot name="action"></slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  }
});
</script>

<style scoped>
.page-header {
  margin-bottom: var(--space-6);
}

.header-content {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
}

.header-left {
  flex: 1;
}

.header-title {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  position: relative;
  padding-left: var(--space-4);
}

.header-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg, var(--primary-color), var(--secondary-color));
  border-radius: 2px;
}

.header-subtitle {
  font-size: var(--text-sm);
  color: var(--text-muted);
  margin: var(--space-2) 0 0;
  padding-left: var(--space-4);
}

.header-right {
  flex-shrink: 0;
}
</style>
