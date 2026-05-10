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

<script setup>
import { ref, onErrorCaptured } from 'vue';

const error = ref(null);

onErrorCaptured((err) => {
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
  background-color: #f5f5f5;
}

.error-content {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.error-content h2 {
  color: #f56c6c;
  margin-bottom: 16px;
}

.error-content p {
  color: #666;
  margin-bottom: 24px;
}

.error-content button {
  padding: 10px 24px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.error-content button:hover {
  background-color: #66b1ff;
}
</style>
