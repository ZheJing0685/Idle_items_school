<template>
  <div class="forgot-password-page">
    <div class="forgot-password-container">
      <div class="card-header">
        <div class="logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 8C8 10 5.9 16.17 3.82 21.34l1.89.66.95-2.3c.48.17.98.3 1.34.3C19 20 22 3 22 3c-1 2-8 2.25-13 3.25S2 11.5 2 13.5s1.75 3.75 1.75 3.75C7 8 17 8 17 8z" />
          </svg>
        </div>
        <h2 class="card-title">忘记密码</h2>
        <p class="card-subtitle" v-if="step === 1">请输入您注册时使用的邮箱</p>
        <p class="card-subtitle" v-else-if="step === 2">验证码已发送到 {{ form.email }}</p>
        <p class="card-subtitle" v-else-if="step === 3">请设置您的新密码</p>
        <p class="card-subtitle" v-else>密码重置成功</p>
      </div>

      <!-- Step 1: Input Email -->
      <div v-if="step === 1" class="step-content">
        <form class="form" @submit.prevent="sendCode">
          <div class="form-group">
            <label class="form-label">邮箱地址</label>
            <input
              v-model="form.email"
              type="email"
              placeholder="请输入邮箱"
              class="form-input"
              required
            />
          </div>
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '发送中...' : '发送验证码' }}
          </button>
        </form>
      </div>

      <!-- Step 2: Input Code -->
      <div v-if="step === 2" class="step-content">
        <form class="form" @submit.prevent="verifyCode">
          <div class="form-group">
            <label class="form-label">验证码</label>
            <input
              v-model="form.code"
              type="text"
              placeholder="请输入6位验证码"
              class="form-input"
              maxlength="6"
              required
            />
          </div>
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '验证中...' : '验证' }}
          </button>
        </form>
      </div>

      <!-- Step 3: Set New Password -->
      <div v-if="step === 3" class="step-content">
        <form class="form" @submit.prevent="resetPassword">
          <div class="form-group">
            <label class="form-label">新密码</label>
            <input
              v-model="form.newPassword"
              type="password"
              placeholder="请输入新密码"
              class="form-input"
              required
            />
          </div>
          <div class="form-group">
            <label class="form-label">确认密码</label>
            <input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请确认新密码"
              class="form-input"
              required
            />
          </div>
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '重置中...' : '重置密码' }}
          </button>
        </form>
      </div>

      <!-- Step 4: Success -->
      <div v-if="step === 4" class="step-content success">
        <div class="success-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
            <polyline points="22 4 12 14.01 9 11.01" />
          </svg>
        </div>
        <h3>密码重置成功</h3>
        <p>请使用新密码登录</p>
        <button class="submit-btn" @click="goToLogin">去登录</button>
      </div>

      <div class="back-link">
        <router-link to="/login">← 返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '../api';

const router = useRouter();
const loading = ref(false);
const step = ref(1);

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
});

const sendCode = async () => {
  if (!form.email) {
    ElMessage.warning('请输入邮箱');
    return;
  }
  try {
    loading.value = true;
    await api.auth.forgotPassword(form.email);
    ElMessage.success('验证码已发送');
    step.value = 2;
  } catch (error: any) {
    const msg = error.response?.data?.message || error.message || '发送失败';
    ElMessage.error(msg);
  } finally {
    loading.value = false;
  }
};

const verifyCode = async () => {
  if (!form.code || form.code.length !== 6) {
    ElMessage.warning('请输入6位验证码');
    return;
  }
  try {
    loading.value = true;
    await api.auth.verifyCode(form.email, form.code);
    ElMessage.success('验证成功');
    step.value = 3;
  } catch (error: any) {
    const msg = error.response?.data?.message || error.message || '验证失败';
    ElMessage.error(msg);
  } finally {
    loading.value = false;
  }
};

const resetPassword = async () => {
  if (!form.newPassword || !form.confirmPassword) {
    ElMessage.warning('请填写所有字段');
    return;
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致');
    return;
  }
  try {
    loading.value = true;
    await api.auth.resetPassword(form.email, form.code, form.newPassword);
    ElMessage.success('密码重置成功');
    step.value = 4;
  } catch (error: any) {
    const msg = error.response?.data?.message || error.message || '重置失败';
    ElMessage.error(msg);
  } finally {
    loading.value = false;
  }
};

const goToLogin = () => {
  router.push('/login');
};
</script>

<style scoped>
.forgot-password-page {
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-base);
  padding: 16px;
}

.forgot-password-container {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: var(--bg-surface);
  border-radius: 20px;
  box-shadow: 0 8px 32px oklch(0% 0 0 / 0.10);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--primary-alpha-15);
  margin-bottom: 16px;
}

.logo svg {
  width: 32px;
  height: 32px;
  color: var(--primary-color);
}

.card-title {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.card-subtitle {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

.step-content {
  margin-bottom: 24px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.form-input {
  width: 100%;
  height: 48px;
  padding: 0 16px;
  background: var(--bg-muted);
  border: 1.5px solid transparent;
  border-radius: 12px;
  font-size: 15px;
  transition: border-color 0.15s, box-shadow 0.15s;
  outline: none;
}

.form-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px oklch(56% 0.15 150 / 0.10);
}

.form-input::placeholder {
  color: var(--text-muted);
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: var(--primary-color);
  color: #fff;
  border: none;
  cursor: pointer;
  transition: all 0.15s;
}

.submit-btn:hover:not(:disabled) {
  background: var(--primary-dark);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px oklch(56% 0.15 150 / 0.35);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.success {
  text-align: center;
}

.success-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--eco-light);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.success-icon svg {
  width: 40px;
  height: 40px;
  color: var(--eco-color);
}

.success h3 {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.success p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0 0 24px;
}

.back-link {
  text-align: center;
}

.back-link a {
  font-size: 14px;
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.15s;
}

.back-link a:hover {
  color: var(--primary-dark);
}
</style>
