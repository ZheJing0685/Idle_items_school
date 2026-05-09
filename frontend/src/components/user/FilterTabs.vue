<template>
  <div class="filter-tabs">
    <button
      v-for="tab in tabs"
      :key="tab.value"
      class="filter-tab"
      :class="{ active: modelValue === tab.value }"
      @click="$emit('update:modelValue', tab.value)"
    >
      {{ tab.label }}
      <span class="tab-count" v-if="tab.count !== undefined">{{ tab.count }}</span>
    </button>
  </div>
</template>

<script setup>
defineProps({
  tabs: {
    type: Array,
    required: true,
    validator: (v) => v.every(t => 'value' in t && 'label' in t)
  },
  modelValue: {
    type: [String, Number],
    default: ''
  }
});

defineEmits(['update:modelValue']);
</script>

<style scoped>
.filter-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-6);
}

.filter-tab {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  height: 36px;
  padding: 0 var(--space-5);
  background: var(--bg-surface);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out-quart);
}

.filter-tab:hover {
  background: var(--bg-muted);
  border-color: var(--primary-light);
  color: var(--text-primary);
}

.filter-tab.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
}

.tab-count {
  font-size: var(--text-xs);
  padding: 2px 6px;
  background: oklch(0% 0 0 / 0.1);
  border-radius: var(--radius-full);
}

.filter-tab.active .tab-count {
  background: oklch(100% 0 0 / 0.2);
}
</style>
