<template>
  <div class="login-page">
    <div class="login-container">
      <!-- Left Visual Panel -->
      <div class="login-visual">
        <div class="visual-content">
          <div class="visual-logo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17 8C8 10 5.9 16.17 3.82 21.34l1.89.66.95-2.3c.48.17.98.3 1.34.3C19 20 22 3 22 3c-1 2-8 2.25-13 3.25S2 11.5 2 13.5s1.75 3.75 1.75 3.75C7 8 17 8 17 8z" />
            </svg>
            <span class="visual-brand">GreenLoop</span>
          </div>
          <h1 class="visual-title">
            让闲置流动<br />
            <span class="visual-accent">让校园更绿</span>
          </h1>
          <p class="visual-description">
            在这里，每一件闲置物品都找到新主人，减少浪费，传递价值。
          </p>
          <div class="visual-stats">
            <div class="visual-stat">
              <strong>2,847</strong>
              件物品在流转
            </div>
            <div class="visual-stat">
              <strong>128kg</strong>
              本月减碳量
            </div>
            <div class="visual-stat">
              <strong>1,206</strong>
              位同学参与
            </div>
          </div>
        </div>
      </div>

      <!-- Right Login Form -->
      <div class="login-content">
        <div class="login-card">
          <div class="card-header">
            <h2 class="card-title">欢迎回来</h2>
            <p class="card-subtitle">登录账号，开始闲置交易之旅</p>
          </div>

          <form class="login-form" @submit.prevent="handleLogin">
            <div class="form-group">
              <label class="form-label" for="login-username">用户名</label>
              <input
                id="login-username"
                v-model="loginForm.username"
                type="text"
                placeholder="请输入用户名"
                class="form-input"
                required
              />
            </div>

            <div class="form-group">
              <label class="form-label" for="login-password">密码</label>
              <input
                id="login-password"
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                class="form-input"
                required
              />
            </div>

            <div class="form-options">
              <label class="remember-check">
                <input type="checkbox" v-model="rememberMe" />
                <span class="check-label">记住我</span>
              </label>
              <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
            </div>

            <button
              type="submit"
              class="submit-btn"
              :disabled="loading"
            >
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </form>

          <div class="card-footer">
            <span class="footer-text">还没有账号？</span>
            <router-link to="/register" class="footer-link">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { userStore } from '../store';

const router = useRouter();
const loading = ref(false);
const rememberMe = ref(false);
const store = userStore();

const loginForm = reactive({
  username: '',
  password: '',
});

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }

  try {
    loading.value = true;
    await store.login(loginForm.username, loginForm.password, rememberMe.value);
    ElMessage.success('登录成功');

    const redirectPath = localStorage.getItem('redirectPath');
    if (redirectPath) {
      localStorage.removeItem('redirectPath');
      router.push(redirectPath);
    } else {
      router.push('/');
    }
  } catch (error: any) {
    const msg = error.response?.data?.message || error.message || '登录失败';
    ElMessage.error(msg);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped src="../styles/pages/login.css"></style>
