<template>
  <div class="empty-container">
    <div class="empty-content">
      <div class="empty-icon">
        <svg width="80" height="80" viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="40" cy="40" r="35" stroke="#e4e7ed" stroke-width="2"/>
          <path d="M30 30L50 50M50 30L30 50" stroke="#c0c4cc" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <h3 class="empty-title">{{ title }}</h3>
      <p class="empty-description">{{ description }}</p>
      <div class="empty-actions" v-if="actions">
        <slot name="actions">{{ actions }}</slot>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  title: {
    type: String,
    default: '暂无数据'
  },
  description: {
    type: String,
    default: '这里还没有内容'
  },
  actions: {
    type: [String, Object, Array],
    default: null
  }
});
</script>

<style scoped src="../../styles/components/empty.css"></style>
