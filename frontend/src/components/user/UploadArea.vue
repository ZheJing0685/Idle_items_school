<template>
  <div class="upload-area" :class="{ 'has-file': modelValue, 'is-dragging': isDragging }" @click="triggerUpload" @dragover.prevent="isDragging = true" @dragleave="isDragging = false" @drop.prevent="handleDrop">
    <input ref="fileInput" type="file" :accept="accept" @change="handleFileChange" hidden />
    
    <template v-if="modelValue">
      <img :src="modelValue" class="preview-image" alt="预览" />
      <div class="preview-overlay">
        <button class="remove-btn" @click.stop="removeFile">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6L6 18M6 6l12 12"/>
          </svg>
        </button>
      </div>
    </template>
    
    <template v-else>
      <div class="upload-icon">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/>
          <polyline points="17 8 12 3 7 8"/>
          <line x1="12" y1="3" x2="12" y2="15"/>
        </svg>
      </div>
      <p class="upload-text">{{ text }}</p>
      <p class="upload-hint" v-if="hint">{{ hint }}</p>
    </template>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  modelValue: String,
  accept: {
    type: String,
    default: 'image/*'
  },
  text: {
    type: String,
    default: '点击上传图片'
  },
  hint: String,
  maxSize: {
    type: Number,
    default: 5
  }
});

const emit = defineEmits(['update:modelValue', 'upload']);

const fileInput = ref(null);
const isDragging = ref(false);

const triggerUpload = () => {
  if (!props.modelValue) {
    fileInput.value?.click();
  }
};

const handleFileChange = (e) => {
  const file = e.target.files?.[0];
  if (file) processFile(file);
};

const handleDrop = (e) => {
  isDragging.value = false;
  const file = e.dataTransfer.files?.[0];
  if (file) processFile(file);
};

const processFile = (file) => {
  if (file.size > props.maxSize * 1024 * 1024) {
    alert(`文件大小不能超过${props.maxSize}MB`);
    return;
  }
  
  const reader = new FileReader();
  reader.onload = (e) => {
    emit('update:modelValue', e.target.result);
    emit('upload', file);
  };
  reader.readAsDataURL(file);
};

const removeFile = () => {
  emit('update:modelValue', '');
  if (fileInput.value) fileInput.value.value = '';
};
</script>

<style scoped>
.upload-area {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 160px;
  padding: var(--space-6);
  background: var(--bg-muted);
  border: 2px dashed var(--border-default);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out-quart);
}

.upload-area:hover {
  border-color: var(--primary-color);
  background: oklch(62% 0.14 195 / 0.04);
}

.upload-area.is-dragging {
  border-color: var(--primary-color);
  background: oklch(62% 0.14 195 / 0.08);
}

.upload-area.has-file {
  padding: 0;
  border-style: solid;
  border-color: var(--border-subtle);
  cursor: default;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: calc(var(--radius-lg) - 2px);
}

.preview-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: oklch(0% 0 0 / 0.4);
  opacity: 0;
  transition: opacity var(--duration-fast);
  border-radius: calc(var(--radius-lg) - 2px);
}

.upload-area.has-file:hover .preview-overlay {
  opacity: 1;
}

.remove-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: var(--radius-full);
  color: var(--error-color);
  transition: transform var(--duration-fast);
}

.remove-btn:hover {
  transform: scale(1.1);
}

.upload-icon {
  color: var(--text-muted);
  margin-bottom: var(--space-3);
}

.upload-text {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
}

.upload-hint {
  font-size: var(--text-xs);
  color: var(--text-muted);
  margin: var(--space-1) 0 0;
}
</style>