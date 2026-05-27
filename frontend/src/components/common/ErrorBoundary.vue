<template>
  <div v-if="error" class="error-boundary">
    <div class="error-content">
      <h2>页面出现错误</h2>
      <p>{{ error.message }}</p>
      <button @click="handleRefresh">刷新页面</button>
    </div>
  </div>
  <slot v-else />
</template>

<script setup lang="ts">
import { ref, onErrorCaptured } from 'vue';

const error = ref<Error | null>(null);

onErrorCaptured((err: Error) => {
  error.value = err;
  return false;
});

const handleRefresh = () => {
  error.value = null;
  window.location.reload();
};
</script>

<style scoped>
.error-boundary {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: var(--bg-base);
}

.error-content {
  text-align: center;
  padding: var(--space-10);
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}

.error-content h2 {
  color: var(--error-color);
  margin-bottom: var(--space-4);
}

.error-content p {
  color: var(--text-secondary);
  margin-bottom: var(--space-6);
}

.error-content button {
  padding: var(--space-3) var(--space-6);
  background-color: var(--primary-color);
  color: var(--text-inverse);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: var(--text-base);
}

.error-content button:hover {
  background-color: var(--primary-light);
}
</style>
