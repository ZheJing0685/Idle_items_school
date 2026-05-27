<template>
  <div class="forgot-password-page">
    <div class="forgot-password-container">
      <h2>忘记密码</h2>

      <!-- 步骤1: 输入邮箱 -->
      <div v-if="step === 1" class="step-content">
        <p class="step-desc">请输入您注册时使用的邮箱，我们将发送验证码到该邮箱</p>
        <el-form :model="form" :rules="rules" ref="formRef">
          <el-form-item prop="email">
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱"
              prefix-icon="Message"
            />
          </el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="sendCode"
            class="full-width"
          >
            发送验证码
          </el-button>
        </el-form>
      </div>

      <!-- 步骤2: 输入验证码 -->
      <div v-if="step === 2" class="step-content">
        <p class="step-desc">验证码已发送到 {{ form.email }}，请查收</p>
        <el-form :model="form" :rules="rules" ref="formRef">
          <el-form-item prop="code">
            <el-input
              v-model="form.code"
              placeholder="请输入6位验证码"
              prefix-icon="Key"
              maxlength="6"
            />
          </el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="verifyCode"
            class="full-width"
          >
            验证
          </el-button>
        </el-form>
      </div>

      <!-- 步骤3: 设置新密码 -->
      <div v-if="step === 3" class="step-content">
        <p class="step-desc">请设置您的新密码</p>
        <el-form :model="form" :rules="rules" ref="formRef">
          <el-form-item prop="newPassword">
            <el-input
              v-model="form.newPassword"
              type="password"
              placeholder="请输入新密码"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请确认新密码"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="resetPassword"
            class="full-width"
          >
            重置密码
          </el-button>
        </el-form>
      </div>

      <!-- 步骤4: 成功 -->
      <div v-if="step === 4" class="step-content success">
        <el-icon :size="64" class="success-icon"><CircleCheck /></el-icon>
        <h3>密码重置成功</h3>
        <p>请使用新密码登录</p>
        <el-button type="primary" @click="goToLogin">去登录</el-button>
      </div>

      <div class="back-link">
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElForm } from 'element-plus';
import { CircleCheck } from '@element-plus/icons-vue';
import api from '@/api';
import { validatePassword } from '../utils/validator';

const router = useRouter();
const formRef = ref<InstanceType<typeof ElForm> | null>(null);
const loading = ref(false);
const step = ref(1);

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
});

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 32, message: '密码长度8-32个字符', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
        if (!validatePassword(value)) {
          callback(new Error('密码必须包含大小写字母、数字和特殊字符'));
        } else {
          callback();
        }
      },
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

// 发送验证码
const sendCode = async () => {
  try {
    await formRef.value!.validate();
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

// 验证验证码
const verifyCode = async () => {
  try {
    await formRef.value!.validate();
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

// 重置密码
const resetPassword = async () => {
  try {
    await formRef.value!.validate();
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

// 跳转到登录页
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
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
}

.forgot-password-container {
  width: 400px;
  max-width: 90vw;
  padding: var(--space-10);
  background: var(--bg-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
}

h2 {
  text-align: center;
  margin-bottom: var(--space-6);
  color: var(--text-primary);
}

.step-desc {
  text-align: center;
  color: var(--text-secondary);
  margin-bottom: var(--space-6);
}

.step-content {
  margin-bottom: var(--space-5);
}

.success {
  text-align: center;
}

.success-icon {
  color: var(--success-color);
}

.success h3 {
  margin: var(--space-4) 0 var(--space-2);
  color: var(--text-primary);
}

.success p {
  color: var(--text-secondary);
  margin-bottom: var(--space-6);
}

.back-link {
  text-align: center;
  margin-top: var(--space-5);
}

.back-link a {
  color: var(--primary-color);
  text-decoration: none;
}

.full-width {
  width: 100%;
}
</style>
