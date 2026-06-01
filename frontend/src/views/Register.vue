<template>
  <div class="register-page">
    <div class="register-container">
      <!-- Left Visual Panel -->
      <div class="register-visual">
        <div class="visual-content">
          <div class="visual-logo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17 8C8 10 5.9 16.17 3.82 21.34l1.89.66.95-2.3c.48.17.98.3 1.34.3C19 20 22 3 22 3c-1 2-8 2.25-13 3.25S2 11.5 2 13.5s1.75 3.75 1.75 3.75C7 8 17 8 17 8z" />
            </svg>
            <span class="visual-brand">GreenLoop</span>
          </div>
          <h1 class="visual-title">
            加入我们<br />
            <span class="visual-accent">让校园更绿</span>
          </h1>
          <p class="visual-description">
            注册即享100积分<br />
            首单立减10元
          </p>
          <div class="visual-features">
            <div class="feature-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                <polyline points="22 4 12 14.01 9 11.01" />
              </svg>
              <span>实名认证交易</span>
            </div>
            <div class="feature-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                <polyline points="22 4 12 14.01 9 11.01" />
              </svg>
              <span>快捷发布闲置</span>
            </div>
            <div class="feature-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                <polyline points="22 4 12 14.01 9 11.01" />
              </svg>
              <span>环保绿色校园</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Register Form -->
      <div class="register-content">
        <div class="register-card">
          <div class="card-header">
            <h2 class="card-title">创建账号</h2>
            <p class="card-subtitle">开启你的闲置交易之旅</p>
          </div>

          <form class="register-form" @submit.prevent="handleRegister">
            <div class="form-row">
              <div class="form-group">
                <label class="form-label" for="reg-username">用户名</label>
                <input
                  id="reg-username"
                  v-model="registerForm.username"
                  type="text"
                  placeholder="3-20个字符"
                  class="form-input"
                  required
                />
              </div>
              <div class="form-group">
                <label class="form-label" for="reg-nickname">昵称</label>
                <input
                  id="reg-nickname"
                  v-model="registerForm.nickname"
                  type="text"
                  placeholder="请输入昵称"
                  class="form-input"
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label" for="reg-password">密码</label>
              <input
                id="reg-password"
                v-model="registerForm.password"
                type="password"
                placeholder="8-32个字符，包含大小写字母、数字"
                class="form-input"
                required
              />
            </div>

            <div class="form-row">
              <div class="form-group">
                <label class="form-label" for="reg-email">邮箱</label>
                <input
                  id="reg-email"
                  v-model="registerForm.email"
                  type="email"
                  placeholder="请输入邮箱"
                  class="form-input"
                  required
                />
              </div>
              <div class="form-group">
                <label class="form-label" for="reg-phone">手机号</label>
                <input
                  id="reg-phone"
                  v-model="registerForm.phone"
                  type="tel"
                  placeholder="请输入手机号"
                  class="form-input"
                  required
                />
              </div>
            </div>

            <div class="form-terms">
              <label class="remember-check">
                <input type="checkbox" v-model="agreedToTerms" />
                <span class="terms-text">
                  我已阅读并同意
                  <a href="#" class="terms-link">《用户服务协议》</a>
                  和
                  <a href="#" class="terms-link">《隐私政策》</a>
                </span>
              </label>
            </div>

            <button
              type="submit"
              class="submit-btn"
              :disabled="loading || !agreedToTerms"
            >
              {{ loading ? '注册中...' : '立即注册' }}
            </button>
          </form>

          <div class="card-footer">
            <span class="footer-text">已有账号？</span>
            <router-link to="/login" class="footer-link">立即登录</router-link>
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
const agreedToTerms = ref(false);
const store = userStore();

const registerForm = reactive({
  username: '',
  password: '',
  email: '',
  phone: '',
  nickname: '',
});

const handleRegister = async () => {
  if (!agreedToTerms.value) {
    ElMessage.warning('请先阅读并同意用户协议');
    return;
  }

  if (!registerForm.username || !registerForm.password || !registerForm.email || !registerForm.phone) {
    ElMessage.warning('请填写所有必填字段');
    return;
  }

  try {
    loading.value = true;
    await store.register(registerForm);
    ElMessage.success('注册成功！');
    await store.login(registerForm.username, registerForm.password);
    router.push('/');
  } catch (error: any) {
    const msg = error.response?.data?.message || error.message || '注册失败';
    ElMessage.error(msg);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped src="../styles/pages/register.css"></style>
