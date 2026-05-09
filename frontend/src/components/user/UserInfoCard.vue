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
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
            <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
          </svg>
          编辑资料
        </router-link>
      </div>
      
      <div class="user-meta">
        <span class="meta-item" v-if="user?.schoolName">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 21h18M3 7v1a3 3 0 006 0V7m0 1a3 3 0 006 0V7m0 1a3 3 0 006 0V7H3l2-4h14l2 4M4 21V10.87M20 21V10.87"/>
          </svg>
          {{ user.schoolName }}
        </span>
        <span class="meta-divider" v-if="user?.schoolName && user?.studentId">·</span>
        <span class="meta-item" v-if="user?.studentId">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
          </svg>
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

<script setup>
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
  border: 3px solid white;
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
  color: white;
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