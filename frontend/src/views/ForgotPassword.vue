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
            style="width: 100%"
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
            style="width: 100%"
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
            style="width: 100%"
          >
            重置密码
          </el-button>
        </el-form>
      </div>

      <!-- 步骤4: 成功 -->
      <div v-if="step === 4" class="step-content success">
        <el-icon :size="64" color="#67C23A"><CircleCheck /></el-icon>
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

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { CircleCheck } from '@element-plus/icons-vue';
import axios from '@/api/config/axios';

const router = useRouter();
const formRef = ref(null);
const loading = ref(false);
const step = ref(1);

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
});

const validateConfirmPassword = (rule, value, callback) => {
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
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

// 发送验证码
const sendCode = async () => {
  try {
    await formRef.value.validate();
    loading.value = true;
    await axios.post('/api/auth/forgot-password', { email: form.email });
    ElMessage.success('验证码已发送');
    step.value = 2;
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发送失败');
  } finally {
    loading.value = false;
  }
};

// 验证验证码
const verifyCode = async () => {
  try {
    await formRef.value.validate();
    loading.value = true;
    await axios.post('/api/auth/verify-code', { 
      email: form.email, 
      code: form.code 
    });
    ElMessage.success('验证成功');
    step.value = 3;
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '验证失败');
  } finally {
    loading.value = false;
  }
};

// 重置密码
const resetPassword = async () => {
  try {
    await formRef.value.validate();
    loading.value = true;
    await axios.post('/api/auth/reset-password', {
      email: form.email,
      code: form.code,
      newPassword: form.newPassword
    });
    ElMessage.success('密码重置成功');
    step.value = 4;
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '重置失败');
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
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.forgot-password-container {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #333;
}

.step-desc {
  text-align: center;
  color: #666;
  margin-bottom: 24px;
}

.step-content {
  margin-bottom: 20px;
}

.success {
  text-align: center;
}

.success h3 {
  margin: 16px 0 8px;
  color: #333;
}

.success p {
  color: #666;
  margin-bottom: 24px;
}

.back-link {
  text-align: center;
  margin-top: 20px;
}

.back-link a {
  color: #409eff;
  text-decoration: none;
}
</style>
