<template>
  <div class="change-password-page">
    <PageHeader title="修改密码" subtitle="修改后需要重新登录" />

    <div class="section-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="password-form"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="form.oldPassword"
            type="password"
            show-password
            placeholder="请输入当前密码"
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码（8-32个字符，包含大小写字母、数字和特殊字符）"
          />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>

        <div class="form-actions">
          <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large">
            确认修改
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance } from 'element-plus';
import api from '../../api';
import PageHeader from '../../components/user/PageHeader.vue';
import { formRules } from '../../utils/validator';

const router = useRouter();
const formRef = ref<FormInstance>();

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const rules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
  ],
  newPassword: formRules.password,
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (err?: Error) => void) => {
        if (!value) {
          callback(new Error('请再次输入新密码'));
        } else if (value !== form.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur',
    },
  ],
};

const submitting = ref(false);

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    await api.auth.changePassword(form.oldPassword, form.newPassword);
    await ElMessageBox.alert('密码修改成功，请重新登录', '提示', {
      confirmButtonText: '确定',
      type: 'success',
    });
    router.push('/login');
  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } }; message?: string };
    const msg = err.response?.data?.message || err.message || '修改密码失败';
    ElMessage.error(msg);
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.change-password-page {
  max-width: 600px;
}

.section-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 32px;
}

.password-form {
  max-width: 400px;
}

.form-actions {
  margin-top: 24px;
}
</style>
