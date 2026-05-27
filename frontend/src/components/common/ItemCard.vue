<template>
  <div class="item-card" @click="navigateToDetail" @keydown.enter="navigateToDetail" @keydown.space.prevent="navigateToDetail" tabindex="0" role="button" :aria-label="`查看 ${item.title} 详情`">
    <div class="card-image">
      <img :src="item.coverImage || defaultImage" :alt="item.title" />
      <div v-if="item.isBargainAllowed" class="bargain-badge">可砍价</div>
    </div>
    <div class="card-content">
      <h3 class="item-title">{{ item.title }}</h3>
      <div class="item-price">
        <span class="current-price">¥{{ item.price }}</span>
        <span v-if="item.originalPrice" class="original-price"
          >¥{{ item.originalPrice }}</span
        >
      </div>
      <div class="item-meta">
        <span class="seller">{{ item.sellerNickname || '未知卖家' }}</span>
        <span class="view-count"><Eye :size="14" /> {{ item.viewCount || 0 }}</span>
        <span class="time">{{ formatTime(item.createdAt) }}</span>
      </div>
      <div
        class="item-tags"
        v-if="item.tags && parseTags(item.tags).length > 0"
      >
        <span
          class="item-tag"
          v-for="(tag, index) in parseTags(item.tags).slice(0, 3)"
          :key="index"
        >
          {{ tag }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { Eye } from 'lucide-vue-next';

const props = defineProps({
  item: {
    type: Object,
    required: true,
  },
});

const router = useRouter();

const defaultImage = '/placeholder-item.svg';

const navigateToDetail = () => {
  router.push(`/item/${props.item.id}`);
};

const formatTime = (time: string) => {
  if (!time) return '';
  const now = new Date().getTime();
  const createdAt = new Date(time).getTime();
  const diff = now - createdAt;
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const minutes = Math.floor(diff / (1000 * 60));

  if (days > 0) return `${days}天前`;
  if (hours > 0) return `${hours}小时前`;
  if (minutes > 0) return `${minutes}分钟前`;
  return '刚刚';
};

const parseTags = (tagsStr: string) => {
  if (!tagsStr) return [];
  try {
    const tags = JSON.parse(tagsStr);
    return Array.isArray(tags) ? tags : [];
  } catch (e) {
    return [];
  }
};
</script>

<style scoped src="../../styles/components/item-card.css"></style>
