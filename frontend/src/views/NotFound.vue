<template>
  <div class="not-found-page">
    <div class="not-found-container">
      <!-- Left: 404 Graphic -->
      <div class="not-found-graphic">
        <div class="graphic-circle">
          <svg width="200" height="200" viewBox="0 0 200 200" fill="none">
            <circle cx="100" cy="100" r="90" fill="oklch(56% 0.15 150 / 0.10)" />
            <circle cx="100" cy="100" r="70" fill="oklch(56% 0.15 150 / 0.06)" />
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
        <div class="deco-leaf leaf-1">🌱</div>
        <div class="deco-leaf leaf-2">🍃</div>
      </div>

      <!-- Right: Content -->
      <div class="not-found-content">
        <h1 class="not-found-title">页面不存在</h1>
        <p class="not-found-description">
          抱歉，您访问的页面不存在或已被删除。<br />
          这片"绿色"暂时无人认领，不如去看看首页有什么好物？
        </p>

        <div class="not-found-actions">
          <router-link to="/" class="action-btn action-btn-primary">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
              <polyline points="9,22 9,12 15,12 15,22" />
            </svg>
            返回首页
          </router-link>
          <router-link to="/items" class="action-btn action-btn-secondary">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
            </svg>
            浏览好物
          </router-link>
        </div>

        <div class="eco-message">
          <span>♻️</span>
          <span>每一次浏览都在为环保贡献力量</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.not-found-page {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 64px 24px;
}

.not-found-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
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
  font-size: 40px;
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
  font-size: 24px;
}

.not-found-content {
  text-align: left;
}

.not-found-title {
  font-family: var(--font-display);
  font-size: clamp(24px, 4vw, 36px);
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 16px;
  letter-spacing: -0.03em;
}

.not-found-description {
  font-size: 16px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0 0 32px;
  max-width: 400px;
}

.not-found-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  font-weight: 600;
  font-size: 15px;
  border-radius: 12px;
  text-decoration: none;
  transition: all 0.15s;
}

.action-btn svg {
  width: 18px;
  height: 18px;
}

.action-btn-primary {
  background: var(--primary-color);
  color: var(--text-inverse);
}

.action-btn-primary:hover {
  background: var(--primary-dark);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px oklch(56% 0.15 150 / 0.35);
}

.action-btn-secondary {
  background: var(--bg-surface);
  color: var(--text-primary);
  border: 1.5px solid var(--border-default);
}

.action-btn-secondary:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.eco-message {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: var(--accent-light);
  border-radius: 9999px;
  font-size: 14px;
  font-weight: 500;
  color: var(--accent);
}

@keyframes float {
  0%, 100% {
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
    gap: 32px;
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
    padding: 40px 16px;
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
