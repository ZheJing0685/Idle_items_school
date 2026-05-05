<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-visual">
        <div class="visual-content">
          <div class="visual-logo">
            <svg width="64" height="64" viewBox="0 0 36 36" fill="none">
              <circle cx="18" cy="18" r="16" fill="white" />
              <path
                d="M12 18C12 14.6863 14.6863 12 18 12C21.3137 12 24 14.6863 24 18"
                stroke="var(--secondary-color)"
                stroke-width="2.5"
                stroke-linecap="round"
              />
              <path
                d="M18 18V24"
                stroke="var(--secondary-color)"
                stroke-width="2.5"
                stroke-linecap="round"
              />
              <circle cx="18" cy="14" r="2" fill="var(--secondary-color)" />
            </svg>
            <span class="visual-brand">闲置好物</span>
          </div>
          <h1 class="visual-title">
            加入我们<br />
            <span class="visual-accent">变废为宝</span>
          </h1>
          <p class="visual-description">
            注册即享100积分<br />
            首单立减10元
          </p>
          <div class="visual-features">
            <div class="feature-item">
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                <path d="M22 4L12 14.01l-3-3" />
              </svg>
              <span>实名认证交易</span>
            </div>
            <div class="feature-item">
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                <path d="M22 4L12 14.01l-3-3" />
              </svg>
              <span>快捷发布闲置</span>
            </div>
            <div class="feature-item">
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                <path d="M22 4L12 14.01l-3-3" />
              </svg>
              <span>环保绿色校园</span>
            </div>
          </div>
        </div>
        <div class="visual-decoration">
          <div class="deco-circle deco-1"></div>
          <div class="deco-circle deco-2"></div>
        </div>
      </div>

      <div class="register-content">
        <div class="register-card">
          <div class="card-header">
            <h2 class="card-title">创建账号</h2>
            <p class="card-subtitle">开启你的闲置交易之旅</p>
          </div>

          <el-form
            :model="registerForm"
            :rules="rules"
            ref="registerFormRef"
            class="register-form"
            label-position="top"
          >
            <el-form-item prop="username" class="form-item">
              <label class="form-label">用户名</label>
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名（3-20个字符）"
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
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码（8-32个字符，包含大小写字母、数字和特殊字符）"
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

            <el-form-item prop="nickname" class="form-item">
              <label class="form-label">昵称</label>
              <el-input
                v-model="registerForm.nickname"
                placeholder="请输入昵称"
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
                      d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                    />
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="email" class="form-item">
              <label class="form-label">邮箱</label>
              <el-input
                v-model="registerForm.email"
                placeholder="请输入邮箱"
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
                      d="M4 4H20C21.1 4 22 4.9 22 6V18C22 19.1 21.1 20 20 20H4C2.9 20 2 19.1 2 18V6C2 4.9 2.9 4 4 4Z"
                    />
                    <path d="M22 6L12 13L2 6" />
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="phone" class="form-item">
              <label class="form-label">手机号</label>
              <el-input
                v-model="registerForm.phone"
                placeholder="请输入手机号"
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
                      d="M22 16.92V19.92C22 20.48 21.56 20.96 21 20.99C20.62 21 20.23 21 19.84 21C10.83 21 3.00003 13.17 3.00003 4.16C3.00003 3.77 3.00003 3.38 3.01003 3C3.04003 2.44 3.52003 2 4.08003 2H7.08003C7.56003 2 7.98003 2.34 8.07003 2.81C8.23003 3.67 8.49003 4.5 8.85003 5.29L7.03003 7.11C8.59003 10.39 11.61 13.41 14.89 14.97L16.71 13.15C17.5 13.51 18.33 13.77 19.19 13.93C19.66 14.02 20 14.44 20 14.92V16.92"
                    />
                  </svg>
                </template>
              </el-input>
            </el-form-item>

            <div class="form-terms">
              <el-checkbox v-model="agreedToTerms">
                <span class="terms-text">
                  我已阅读并同意
                  <a href="#" class="terms-link">《用户服务协议》</a>
                  和
                  <a href="#" class="terms-link">《隐私政策》</a>
                </span>
              </el-checkbox>
            </div>

            <el-button
              type="primary"
              size="large"
              :loading="loading"
              :disabled="!agreedToTerms"
              @click="handleRegister"
              class="submit-btn"
            >
              {{ loading ? '注册中...' : '立即注册' }}
            </el-button>
          </el-form>

          <div class="card-footer">
            <span class="footer-text">已有账号？</span>
            <router-link to="/login" class="footer-link">立即登录</router-link>
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
import { formRules, validatePassword } from '../utils/validator';

const router = useRouter();
const registerFormRef = ref();
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

const rules = formRules;

const handleRegister = async () => {
  if (!registerFormRef.value) return;

  if (!agreedToTerms.value) {
    ElMessage.warning('请先阅读并同意用户协议');
    return;
  }

  try {
    await registerFormRef.value.validate();
    loading.value = true;

    await store.register(registerForm);
    ElMessage.success('注册成功！');

    router.push('/login');
  } catch (error) {
    ElMessage.error(error.message || '注册失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped src="../styles/pages/register.css"></style>
