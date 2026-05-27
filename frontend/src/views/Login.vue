<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-visual">
        <div class="visual-content">
          <div class="visual-logo">
            <Package :size="64" stroke-width="1" color="var(--primary-color)" />
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
              <label class="form-label" for="login-username">用户名</label>
              <el-input
                id="login-username"
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                class="form-input"
              >
                <template #prefix>
                  <User :size="18" />
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="password" class="form-item">
              <label class="form-label" for="login-password">密码</label>
              <el-input
                id="login-password"
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                size="large"
                class="form-input"
              >
                <template #prefix>
                  <Lock :size="18" />
                </template>
              </el-input>
            </el-form-item>

            <div class="form-options">
              <el-checkbox v-model="rememberMe" class="remember-check">
                <span class="check-label">记住我</span>
              </el-checkbox>
              <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
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

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { userStore } from '../store';
import { Package, User, Lock } from 'lucide-vue-next';

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
  } catch (error: any) {
    const msg = error.response?.data?.message || error.message || '登录失败';
    ElMessage.error(msg);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped src="../styles/pages/login.css"></style>
