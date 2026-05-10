<template>
  <div class="loading-overlay" v-if="visible">
    <div class="loading-spinner">
      <svg class="spinner" viewBox="0 0 50 50">
        <circle
          class="path"
          cx="25"
          cy="25"
          r="20"
          fill="none"
          stroke-width="5"
        ></circle>
      </svg>
      <p class="loading-text" v-if="text">{{ text }}</p>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  text: {
    type: String,
    default: '加载中...',
  },
});
</script>

<style scoped src="../../styles/components/loading.css"></style>
