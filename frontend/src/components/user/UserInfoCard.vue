<template>
  <div class="user-info-card">
    <div class="avatar-section">
      <div class="avatar-wrapper">
        <el-avatar :size="80" :src="user?.avatar" class="avatar">
          {{ user?.nickname?.charAt(0) || user?.username?.charAt(0) || '用' }}
        </el-avatar>
        <div class="avatar-glow"></div>
      </div>
    </div>

    <div class="info-section">
      <div class="info-header">
        <h2 class="user-name">{{ user?.nickname || user?.username || '用户' }}</h2>
        <router-link to="/user/profile" class="edit-btn">
          <Edit3 :size="14" />
          编辑资料
        </router-link>
      </div>

      <div class="user-meta">
        <span class="meta-item" v-if="user?.schoolName">
          <School :size="14" />
          {{ user.schoolName }}
        </span>
        <span class="meta-divider" v-if="user?.schoolName && user?.studentId">·</span>
        <span class="meta-item" v-if="user?.studentId">
          <FileText :size="14" />
          {{ user.studentId }}
        </span>
        <span class="meta-divider" v-if="user?.gender">·</span>
        <span class="meta-item" v-if="user?.gender">
          {{ user.gender === 1 ? '男' : '女' }}
        </span>
      </div>

      <p class="user-bio">{{ user?.bio || '这个人很懒，什么都没写~' }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Edit3, School, FileText } from 'lucide-vue-next';

defineProps({
  user: Object
});
</script>

<style scoped>
.user-info-card {
  display: flex;
  gap: var(--space-6);
  padding: var(--space-6);
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-subtle);
}

.avatar-section {
  flex-shrink: 0;
}

.avatar-wrapper {
  position: relative;
}

.avatar {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  font-size: var(--text-3xl);
  font-weight: 700;
  border: 3px solid var(--bg-surface);
  box-shadow: var(--shadow-md);
}

.avatar-glow {
  position: absolute;
  inset: -8px;
  background: radial-gradient(circle, oklch(62% 0.14 195 / 0.3) 0%, transparent 70%);
  border-radius: var(--radius-full);
  animation: glow-pulse 3s ease-in-out infinite;
  pointer-events: none;
}

@keyframes glow-pulse {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.05); }
}

.info-section {
  flex: 1;
  min-width: 0;
}

.info-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-3);
}

.user-name {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.edit-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  background: var(--primary-color);
  color: var(--text-inverse);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  font-weight: 500;
  text-decoration: none;
  transition: all var(--duration-fast);
}

.edit-btn:hover {
  background: var(--primary-dark);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.user-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.meta-divider {
  color: var(--border-default);
}

.user-bio {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

@media (max-width: 768px) {
  .user-info-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .info-header {
    flex-direction: column;
    gap: var(--space-2);
  }

  .user-meta {
    justify-content: center;
  }
}
</style>
