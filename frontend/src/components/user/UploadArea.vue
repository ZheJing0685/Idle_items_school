<template>
  <div class="upload-area" :class="{ 'has-file': modelValue, 'is-dragging': isDragging }" @click="triggerUpload" @keydown.enter="triggerUpload" @keydown.space.prevent="triggerUpload" tabindex="0" role="button" aria-label="点击上传图片" @dragover.prevent="isDragging = true" @dragleave="isDragging = false" @drop.prevent="handleDrop">
    <input ref="fileInput" type="file" :accept="accept" @change="handleFileChange" hidden />

    <template v-if="modelValue">
      <img :src="modelValue" class="preview-image" alt="预览" />
      <div class="preview-overlay">
        <button class="remove-btn" @click.stop="removeFile" aria-label="删除图片">
          <X :size="20" />
        </button>
      </div>
    </template>

    <template v-else>
      <div class="upload-icon">
        <Upload :size="48" stroke-width="1.5" />
      </div>
      <p class="upload-text">{{ text }}</p>
      <p class="upload-hint" v-if="hint">{{ hint }}</p>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { X, Upload } from 'lucide-vue-next';

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

const fileInput = ref<HTMLInputElement | null>(null);
const isDragging = ref(false);

const triggerUpload = () => {
  if (!props.modelValue) {
    fileInput.value?.click();
  }
};

const handleFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0];
  if (file) processFile(file);
};

const handleDrop = (e: DragEvent) => {
  isDragging.value = false;
  const file = e.dataTransfer?.files?.[0];
  if (file) processFile(file);
};

const processFile = (file: File) => {
  if (file.size > props.maxSize * 1024 * 1024) {
    alert(`文件大小不能超过${props.maxSize}MB`);
    return;
  }

  const reader = new FileReader();
  reader.onload = (e: ProgressEvent<FileReader>) => {
    emit('update:modelValue', (e.target as FileReader).result);
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
  background: var(--bg-surface);
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
