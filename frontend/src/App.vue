<template>
  <div class="app">
    <a href="#main-content" class="skip-link">跳转到主要内容</a>
    <Header />
    <main id="main-content" class="main-content" tabindex="-1">
      <ErrorBoundary>
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" v-if="Component" />
          </transition>
        </router-view>
      </ErrorBoundary>
    </main>
    <Footer />
  </div>
</template>

<script setup lang="ts">
import Header from './components/Header.vue';
import Footer from './components/Footer.vue';
import ErrorBoundary from './components/common/ErrorBoundary.vue';
</script>

<style scoped>
.app {
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-color);
}

.main-content {
  flex: 1;
  padding: var(--spacing-xl);
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
  border-radius: var(--radius-md);
  font-weight: 600;
  font-size: var(--text-sm);
  text-decoration: none;
  transition: top var(--duration-fast) var(--ease-out-quart);
}

.skip-link:focus {
  top: var(--space-2);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--transition-normal);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .main-content {
    padding: var(--spacing-md);
  }
}
</style>
