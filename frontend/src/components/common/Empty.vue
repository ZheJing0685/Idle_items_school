<template>
  <div class="empty-container">
    <div class="empty-content">
      <div class="empty-icon">
        <XCircle :size="80" color="var(--text-muted)" stroke-width="1.5" />
      </div>
      <h3 class="empty-title">{{ title }}</h3>
      <p class="empty-description">{{ description }}</p>
      <div class="empty-actions" v-if="actions">
        <slot name="actions">{{ actions }}</slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { XCircle } from 'lucide-vue-next';

const props = defineProps({
  title: {
    type: String,
    default: '暂无数据',
  },
  description: {
    type: String,
    default: '这里还没有内容',
  },
  actions: {
    type: [String, Object, Array],
    default: null,
  },
});
</script>

<style scoped src="../../styles/components/empty.css"></style>
