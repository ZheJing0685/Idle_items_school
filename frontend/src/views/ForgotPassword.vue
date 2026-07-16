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
  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } }; message?: string };
    const msg = err.response?.data?.message || err.message || '发送失败';
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
  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } }; message?: string };
    const msg = err.response?.data?.message || err.message || '验证失败';
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
  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } }; message?: string };
    const msg = err.response?.data?.message || err.message || '重置失败';
    ElMessage.error(msg);
  } finally {
    loading.value = false;
  }
};

const goToLogin = () => {
  router.push('/login');
};
</script>

<style scoped src="../styles/pages/forgot-password.css"></style>
