<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-visual">
        <div class="visual-content">
          <div class="visual-logo">
            <Package :size="64" stroke-width="1" color="var(--secondary-color)" />
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
              <CheckCircle :size="20" />
              <span>实名认证交易</span>
            </div>
            <div class="feature-item">
              <CheckCircle :size="20" />
              <span>快捷发布闲置</span>
            </div>
            <div class="feature-item">
              <CheckCircle :size="20" />
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
              <label class="form-label" for="reg-username">用户名</label>
              <el-input
                id="reg-username"
                v-model="registerForm.username"
                placeholder="请输入用户名（3-20个字符）"
                size="large"
                class="form-input"
              >
                <template #prefix>
                  <User :size="18" />
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="password" class="form-item">
              <label class="form-label" for="reg-password">密码</label>
              <el-input
                id="reg-password"
                v-model="registerForm.password"
                placeholder="请输入密码（8-32个字符，包含大小写字母、数字和特殊字符）"
                show-password
                size="large"
                class="form-input"
              >
                <template #prefix>
                  <Lock :size="18" />
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="nickname" class="form-item">
              <label class="form-label" for="reg-nickname">昵称</label>
              <el-input
                id="reg-nickname"
                v-model="registerForm.nickname"
                placeholder="请输入昵称"
                size="large"
                class="form-input"
              >
                <template #prefix>
                  <Star :size="18" />
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="email" class="form-item">
              <label class="form-label" for="reg-email">邮箱</label>
              <el-input
                id="reg-email"
                v-model="registerForm.email"
                placeholder="请输入邮箱"
                size="large"
                class="form-input"
              >
                <template #prefix>
                  <Mail :size="18" />
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="phone" class="form-item">
              <label class="form-label" for="reg-phone">手机号</label>
              <el-input
                id="reg-phone"
                v-model="registerForm.phone"
                placeholder="请输入手机号"
                size="large"
                class="form-input"
              >
                <template #prefix>
                  <Phone :size="18" />
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

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { userStore } from '../store';
import { formRules, validatePassword } from '../utils/validator';
import { Package, CheckCircle, User, Lock, Star, Mail, Phone } from 'lucide-vue-next';

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

    const res = await store.register(registerForm);
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
