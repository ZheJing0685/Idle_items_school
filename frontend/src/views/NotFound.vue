<template>
  <div class="not-found-page">
    <div class="not-found-container">
      <div class="not-found-graphic">
        <div class="graphic-circle">
          <svg width="200" height="200" viewBox="0 0 200 200" fill="none">
            <circle
              cx="100"
              cy="100"
              r="90"
              class="circle-bg-primary"
            />
            <circle
              cx="100"
              cy="100"
              r="70"
              class="circle-bg-primary-light"
            />
            <text
              x="100"
              y="115"
              text-anchor="middle"
              font-family="var(--font-display)"
              font-size="48"
              font-weight="800"
              fill="var(--primary-color)"
            >
              404
            </text>
          </svg>
        </div>
        <div class="deco-leaf leaf-1">
          <Smile :size="40" fill="var(--secondary-color)" color="var(--secondary-color)" />
        </div>
        <div class="deco-leaf leaf-2">
          <Circle :size="24" fill="var(--primary-light)" color="var(--primary-light)" />
        </div>
      </div>

      <div class="not-found-content">
        <h1 class="not-found-title">页面不存在</h1>
        <p class="not-found-description">
          抱歉，您访问的页面不存在或已被删除。<br />
          这片"绿色"暂时无人认领，不如去看看首页有什么好物？
        </p>

        <div class="not-found-actions">
          <router-link to="/" class="action-btn action-btn-primary">
            <Home :size="18" />
            返回首页
          </router-link>
          <router-link to="/items" class="action-btn action-btn-secondary">
            <Grid :size="18" />
            浏览好物
          </router-link>
        </div>

        <div class="eco-message">
          <Smile :size="20" fill="var(--secondary-color)" color="var(--secondary-color)" />
          <span>每一次浏览都在为环保贡献力量</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Smile, Circle, Home, Grid } from 'lucide-vue-next';
</script>

<style scoped>
.not-found-page {
  min-height: calc(100vh - var(--header-height) - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-16) var(--space-6);
}

.not-found-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-12);
  align-items: center;
  max-width: 900px;
  width: 100%;
}

.not-found-graphic {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.graphic-circle {
  animation: float 4s ease-in-out infinite;
}

.deco-leaf {
  position: absolute;
  animation: float 3s ease-in-out infinite;
}

.leaf-1 {
  top: 10%;
  right: 10%;
  animation-delay: 0.5s;
}

.leaf-2 {
  bottom: 20%;
  left: 10%;
  animation-delay: 1s;
}

.not-found-content {
  text-align: left;
}

.not-found-title {
  font-family: var(--font-display);
  font-size: clamp(2rem, 4vw, 3rem);
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 var(--space-4);
  letter-spacing: -0.03em;
}

.not-found-description {
  font-size: var(--text-lg);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  margin: 0 0 var(--space-8);
  max-width: 400px;
}

.not-found-actions {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-8);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4) var(--space-6);
  font-weight: 600;
  font-size: var(--text-base);
  border-radius: var(--radius-lg);
  text-decoration: none;
  transition: all var(--transition-fast);
}

.action-btn-primary {
  background: var(--primary-color);
  color: var(--text-inverse);
  box-shadow: 0 4px 14px var(--color-primary-alpha-20);
}

.circle-bg-primary {
  fill: var(--color-primary-alpha-15);
}

.circle-bg-primary-light {
  fill: var(--color-primary-alpha-10);
}

.action-btn-primary:hover {
  background: var(--primary-dark);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px var(--color-primary-alpha-30);
  color: var(--text-inverse);
}

.action-btn-secondary {
  background: var(--bg-surface);
  color: var(--text-primary);
  border: 2px solid var(--border-default);
}

.action-btn-secondary:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: translateY(-2px);
}

.eco-message {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  background: var(--color-success-alpha-10);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--secondary-dark);
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@media (max-width: 768px) {
  .not-found-container {
    grid-template-columns: 1fr;
    text-align: center;
    gap: var(--space-8);
  }

  .not-found-graphic {
    order: -1;
  }

  .graphic-circle svg {
    width: 150px;
    height: 150px;
  }

  .not-found-content {
    text-align: center;
  }

  .not-found-description {
    margin-left: auto;
    margin-right: auto;
  }

  .not-found-actions {
    justify-content: center;
    flex-wrap: wrap;
  }

  .eco-message {
    margin: 0 auto;
  }
}

@media (max-width: 480px) {
  .not-found-page {
    padding: var(--space-10) var(--space-4);
  }

  .not-found-actions {
    flex-direction: column;
    width: 100%;
  }

  .action-btn {
    justify-content: center;
    width: 100%;
  }
}
</style>
