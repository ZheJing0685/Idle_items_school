<template>
  <div class="loading-overlay" v-if="visible">
    <div class="loading-spinner">
      <Loader2 class="spinner path" :size="40" />
      <p class="loading-text" v-if="text">{{ text }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Loader2 } from 'lucide-vue-next';

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
