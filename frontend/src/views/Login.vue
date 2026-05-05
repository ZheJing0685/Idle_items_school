<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-visual">
        <div class="visual-content">
          <div class="visual-logo">
            <svg width="64" height="64" viewBox="0 0 36 36" fill="none">
              <circle cx="18" cy="18" r="16" fill="white" />
              <path
                d="M12 18C12 14.6863 14.6863 12 18 12C21.3137 12 24 14.6863 24 18"
                stroke="var(--primary-color)"
                stroke-width="2.5"
                stroke-linecap="round"
              />
              <path
                d="M18 18V24"
                stroke="var(--primary-color)"
                stroke-width="2.5"
                stroke-linecap="round"
              />
              <circle cx="18" cy="14" r="2" fill="var(--primary-color)" />
            </svg>
            <span class="visual-brand">闲置好物</span>
          </div>
          <h1 class="visual-title">
            让闲置找到<br />
            <span class="visual-accent">新主人</span>
          </h1>
          <p class="visual-description">
            变废为宝，绿色校园<br />
            开启你的环保交易之旅
          </p>
          <div class="visual-stats">
            <div class="visual-stat">
              <span class="stat-number">12,847</span>
              <span class="stat-desc">成功交易</span>
            </div>
            <div class="visual-stat">
              <span class="stat-number">128.5</span>
              <span class="stat-desc">吨碳减排</span>
            </div>
          </div>
        </div>
        <div class="visual-decoration">
          <div class="deco-circle deco-1"></div>
          <div class="deco-circle deco-2"></div>
          <div class="deco-circle deco-3"></div>
        </div>
      </div>

      <div class="login-content">
        <div class="login-card">
          <div class="card-header">
            <h2 class="card-title">欢迎回来</h2>
            <p class="card-subtitle">登录账号，开始闲置交易之旅</p>
          </div>

          <el-form
            :model="loginForm"
            :rules="rules"
            ref="loginFormRef"
            class="login-form"
          >
            <el-form-item prop="username" class="form-item">
              <label class="form-label">用户名</label>
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                class="form-input"
              >
                <template #prefix>
                  <svg
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path
                      d="M20 21V19C20 16.7909 18.2091 15 16 15H8C5.79086 15 4 16.7909 4 19V21"
                    />
                    <circle cx="12" cy="7" r="4" />
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="password" class="form-item">
              <label class="form-label">密码</label>
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                size="large"
                class="form-input"
              >
                <template #prefix>
                  <svg
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                    <path
                      d="M7 11V7C7 4.23858 9.23858 2 12 2C14.7614 2 17 4.23858 17 7V11"
                    />
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <div class="form-options">
              <el-checkbox v-model="rememberMe" class="remember-check">
                <span class="check-label">记住我</span>
              </el-checkbox>
              <a href="#" class="forgot-link">忘记密码？</a>
            </div>

            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleLogin"
              class="submit-btn"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form>

          <div class="card-footer">
            <span class="footer-text">还没有账号？</span>
            <router-link to="/register" class="footer-link"
              >立即注册</router-link
            >
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { userStore } from '../store';

const router = useRouter();
const loginFormRef = ref();
const loading = ref(false);
const rememberMe = ref(false);
const store = userStore();

const loginForm = reactive({
  username: '',
  password: '',
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;

  try {
    await loginFormRef.value.validate();
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
  } catch (error) {
    ElMessage.error(error.message || '登录失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped src="../styles/pages/login.css"></style>
