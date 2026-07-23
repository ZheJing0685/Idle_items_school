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
              <strong>{{ carbonStats.transactionCount }}</strong>
              笔交易已完成
            </div>
            <div class="visual-stat">
              <strong>{{ carbonStats.monthlySavingKg }}kg</strong>
              本月减碳量
            </div>
            <div class="visual-stat">
              <strong>{{ carbonStats.participantCount }}</strong>
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

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            label-position="top"
            @submit.prevent="handleLogin"
          >
            <el-form-item label="用户名" prop="username" class="form-group">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
              />
            </el-form-item>

            <el-form-item label="密码" prop="password" class="form-group">
              <el-input
                v-model="loginForm.password"
                type="password"
                show-password
                placeholder="请输入密码"
              />
            </el-form-item>

            <div class="form-options">
              <label class="remember-check">
                <input type="checkbox" v-model="rememberMe" />
                <span class="check-label">记住我</span>
              </label>
              <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
            </div>

            <el-button
              type="primary"
              native-type="submit"
              class="submit-btn"
              :loading="loading"
              size="large"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form>

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
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance } from 'element-plus';
import { userStore } from '../store';
import api from '../api';
import type { CarbonStats } from '../api/services/carbon';

const router = useRouter();
const loading = ref(false);
const rememberMe = ref(false);
const store = userStore();
const carbonStats = ref<CarbonStats>({
  monthlySavingKg: 0,
  totalSavingKg: 0,
  treeEquivalent: 0,
  transactionCount: 0,
  participantCount: 0,
});
const loginFormRef = ref<FormInstance>();

const loginForm = reactive({
  username: '',
  password: '',
});

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
};

const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate().catch(() => false);
  if (!valid) return;

  try {
    loading.value = true;
    await store.login(loginForm.username, loginForm.password, rememberMe.value);
    ElMessage.success('登录成功');

    // 检测是否有待处理的购买意图
    const pendingPurchase = sessionStorage.getItem('pendingPurchase');
    if (pendingPurchase) {
      sessionStorage.removeItem('pendingPurchase');
      const purchaseData = JSON.parse(pendingPurchase);
      ElMessage.info('请重新下单');
      router.push(`/item/${purchaseData.itemId}`);
      return;
    }

    const redirectPath = localStorage.getItem('redirectPath');
    if (redirectPath) {
      localStorage.removeItem('redirectPath');
      router.push(redirectPath);
    } else {
      router.push('/');
    }
  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } }; message?: string };
    const msg = err.response?.data?.message || err.message || '登录失败';
    ElMessage.error(msg);
  } finally {
    loading.value = false;
  }
};

onMounted(async () => {
  try {
    const res = await api.carbon.getStats();
    if (res.data) {
      carbonStats.value = {
        monthlySavingKg: res.data.monthlySavingKg ?? 0,
        totalSavingKg: res.data.totalSavingKg ?? 0,
        treeEquivalent: res.data.treeEquivalent ?? 0,
        transactionCount: res.data.transactionCount ?? 0,
        participantCount: res.data.participantCount ?? 0,
      };
    }
  } catch (error) {
    logger.error('获取碳减排统计失败，使用默认值', error);
  }
});

defineExpose({ rules: loginRules });
</script>

<style scoped src="../styles/pages/login.css"></style>
