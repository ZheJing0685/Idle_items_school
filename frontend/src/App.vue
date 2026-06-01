<template>
  <div class="app">
    <a href="#main-content" class="skip-link">跳转到主要内容</a>
    <Header v-if="showHeaderFooter" />
    <main id="main-content" class="main-content" :class="{ 'no-padding': !showHeaderFooter }" tabindex="-1">
      <ErrorBoundary>
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" v-if="Component" />
          </transition>
        </router-view>
      </ErrorBoundary>
    </main>
    <Footer v-if="showHeaderFooter" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import Header from './components/Header.vue';
import Footer from './components/Footer.vue';
import ErrorBoundary from './components/common/ErrorBoundary.vue';
import { userStore } from './store';

const route = useRoute();
const store = userStore();

// 管理后台页面不显示Header和Footer
const showHeaderFooter = computed(() => {
  return !route.path.startsWith('/admin');
});

onMounted(async () => {
  // 页面刷新后，以服务端 /auth/me 校验结果恢复用户状态。
  await store.restoreSession();
});
</script>

<style scoped>
.app {
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-base);
}

.main-content {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 0 16px;
}

.main-content.no-padding {
  padding: 0;
}

.main-content:focus {
  outline: none;
}

.skip-link {
  position: fixed;
  top: -100%;
  left: var(--space-4);
  z-index: 9999;
  padding: var(--space-2) var(--space-4);
  background: var(--primary-color);
  color: var(--text-inverse);
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  text-decoration: none;
  transition: top 150ms cubic-bezier(0.25, 1, 0.5, 1);
}

.skip-link:focus {
  top: var(--space-2);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 300ms cubic-bezier(0.25, 1, 0.5, 1);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (min-width: 768px) {
  .main-content {
    padding: 0 24px;
  }

  .main-content.no-padding {
    padding: 0;
  }
}

@media (max-width: 767px) {
  .main-content {
    padding: 0 12px;
  }

  .main-content.no-padding {
    padding: 0;
  }
}
</style>
