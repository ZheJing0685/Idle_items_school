<template>
  <div class="item-card" @click="$emit('click')">
    <div class="card-image">
      <img :src="coverImage || defaultImage" :alt="title" loading="lazy"/>
      <div class="image-overlay" v-if="status">
        <span class="status-tag" :class="statusClass">{{ statusText }}</span>
      </div>
    </div>
    <div class="card-content">
      <h3 class="item-title">{{ title }}</h3>
      <div class="item-price">¥{{ price }}</div>
      <div class="item-meta">
        <span class="view-count" v-if="viewCount !== undefined">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 12S5 4 12 4s11 8 11 8-4 8-11 8-11-8-11-8z"/>
            <circle cx="12" cy="12" r="3"/>
          </svg>
          {{ viewCount }}浏览
        </span>
        <span class="time" v-if="time">{{ time }}</span>
      </div>
    </div>
    <div class="card-actions" v-if="$slots.actions">
      <slot name="actions"></slot>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  id: [String, Number],
  title: String,
  price: [String, Number],
  coverImage: String,
  status: String,
  statusText: String,
  viewCount: Number,
  time: String
});

defineEmits(['click']);

const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="200" height="150" viewBox="0 0 200 150"%3E%3Crect fill="%23f0f0f0" width="200" height="150"/%3E%3Cpath fill="%23ccc" d="M80 60h40v30h-40z"/%3E%3Ccircle cx="90" cy="50" r="8" fill="%23ccc"/%3E%3C/svg%3E';

const statusClass = computed(() => {
  const map = {
    'ON_SALE': 'status-on-sale',
    'SOLD': 'status-sold',
    'PENDING': 'status-pending',
    'OFF_SHELF': 'status-off-shelf'
  };
  return map[props.status] || 'status-default';
});
</script>

<style scoped>
.item-card {
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-subtle);
  transition: all var(--duration-fast) var(--ease-out-quart);
  cursor: pointer;
}

.item-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
}

.card-image {
  position: relative;
  aspect-ratio: 16/9;
  overflow: hidden;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--duration-normal) var(--ease-out-quart);
}

.item-card:hover .card-image img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: var(--space-3);
  right: var(--space-3);
}

.status-tag {
  display: inline-block;
  padding: 4px 10px;
  font-size: var(--text-xs);
  font-weight: 500;
  border-radius: var(--radius-full);
  backdrop-filter: blur(8px);
}

.status-on-sale {
  background: oklch(62% 0.12 158 / 0.9);
  color: white;
}

.status-sold {
  background: oklch(58% 0.01 195 / 0.9);
  color: white;
}

.status-pending {
  background: oklch(75% 0.14 85 / 0.9);
  color: white;
}

.status-off-shelf {
  background: oklch(60% 0.20 25 / 0.9);
  color: white;
}

.card-content {
  padding: var(--space-4);
}

.item-title {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.item-price {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--error-color);
  margin-bottom: var(--space-2);
}

.item-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.view-count {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.card-actions {
  display: flex;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border-top: 1px solid var(--border-subtle);
}
</style>
