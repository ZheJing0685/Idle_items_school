<template>
  <div class="profile-page">
    <PageHeader title="个人信息" subtitle="完善资料有助于获得更多信任" />

    <div class="profile-sections">
      <!-- 头像区域 -->
      <div class="section-card">
        <div class="avatar-upload-wrapper">
          <div class="avatar-container">
            <el-avatar :size="100" :src="form.avatar" class="profile-avatar">
              {{ form.nickname?.charAt(0) || form.username?.charAt(0) || '用' }}
            </el-avatar>
            <el-upload
              action="/api/upload"
              :with-credentials="true"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              class="avatar-upload-trigger"
            >
              <div class="avatar-overlay">
                <el-icon :size="20"><Edit /></el-icon>
                <span>更换头像</span>
              </div>
            </el-upload>
          </div>
          <p class="avatar-hint">支持 JPG、PNG 格式，大小不超过 5MB</p>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="section-card">
        <h3 class="section-title">基本信息</h3>
        <el-form :model="form" label-position="top" class="profile-form">
          <div class="form-row">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" maxlength="50" placeholder="请输入昵称" />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :value="0">未知</el-radio>
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="生日">
              <el-date-picker v-model="form.birthday" type="date" placeholder="选择生日" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="个人简介">
              <el-input v-model="form.bio" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="介绍一下自己吧" />
            </el-form-item>
          </div>
        </el-form>
      </div>

      <!-- 联系方式 -->
      <div class="section-card">
        <h3 class="section-title">联系方式</h3>
        <el-form :model="form" label-position="top" class="profile-form">
          <div class="form-row">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" disabled />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" maxlength="20" placeholder="请输入手机号" />
            </el-form-item>
          </div>
        </el-form>
      </div>

      <!-- 学校信息 -->
      <div class="section-card">
        <h3 class="section-title">学校信息</h3>
        <el-form :model="form" label-position="top" class="profile-form">
          <div class="form-row">
            <el-form-item label="学校名称">
              <el-input v-model="form.schoolName" maxlength="100" placeholder="请输入学校名称" />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="学号">
              <el-input v-model="form.studentId" maxlength="20" placeholder="请输入学号" />
            </el-form-item>
          </div>
        </el-form>
      </div>

      <!-- 账户信息 -->
      <div class="section-card">
        <h3 class="section-title">账户信息</h3>
        <div class="account-info">
          <div class="info-row">
            <span class="info-label">注册时间</span>
            <span class="info-value">{{ form.createdAt || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">最后登录</span>
            <span class="info-value">{{ form.lastLoginTime || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">信用评分</span>
            <div class="credit-score">
              <el-progress :percentage="form.creditScore || 100" :color="getScoreColor(form.creditScore)" />
              <span class="score-text">{{ form.creditScore || 100 }} 分</span>
            </div>
          </div>
          <div class="info-row">
            <span class="info-label">交易统计</span>
            <div class="transaction-stats">
              <div class="stat-item">
                <span class="stat-value">{{ form.totalTransactions || 0 }}</span>
                <span class="stat-label">总交易</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ form.totalSales || 0 }}</span>
                <span class="stat-label">售出</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ form.totalPurchases || 0 }}</span>
                <span class="stat-label">购买</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 保存按钮 -->
      <div class="form-actions">
        <el-button type="primary" @click="handleSave" :loading="saving" size="large">
          保存修改
        </el-button>
        <el-button @click="$emit('change-tab', 'change-password')" size="large" class="change-password-btn">
          修改密码
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Edit } from '@element-plus/icons-vue';
import { userStore } from '../../store';
import api from '../../api';
import PageHeader from '../../components/user/PageHeader.vue';

const store = userStore();

const form = ref({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
  gender: 0,
  birthday: '',
  bio: '',
  schoolName: '',
  studentId: '',
  createdAt: '',
  lastLoginTime: '',
  creditScore: 100,
  totalTransactions: 0,
  totalSales: 0,
  totalPurchases: 0,
});

const saving = ref(false);

const getScoreColor = (score: number) => {
    if (score >= 80) return 'var(--color-success)';
    if (score >= 60) return 'var(--color-warning)';
    return 'var(--color-danger)';
};

const mapUserToForm = (user: any) => ({
  username: user.username || '',
  nickname: user.nickname || '',
  email: user.email || '',
  phone: user.phone || '',
  avatar: user.avatar || '',
  gender: user.gender ?? 0,
  birthday: user.birthday || '',
  bio: user.bio || '',
  schoolName: user.schoolName || '',
  studentId: user.studentId || '',
  createdAt: user.createdAt || '',
  lastLoginTime: user.lastLoginTime || '',
  creditScore: user.creditScore ?? 100,
  totalTransactions: user.totalTransactions ?? 0,
  totalSales: user.totalSales ?? 0,
  totalPurchases: user.totalPurchases ?? 0,
});

const loadUserInfo = async () => {
  const cached = store.user;
  if (cached) {
    form.value = mapUserToForm(cached);
  }
  try {
    const res = await api.user.getProfile();
    if (res.code === 200) {
      form.value = mapUserToForm(res.data);
    }
  } catch {
    // store 缓存兜底，静默失败
  }
};

const handleAvatarSuccess = (response: any) => {
  if (response.code === 200) {
    form.value.avatar = response.data.url;
    ElMessage.success('头像上传成功');
  }
};

const beforeAvatarUpload = (file: any) => {
  const isImage = file.type.startsWith('image/');
  const isLt5M = file.size / 1024 / 1024 < 5;

  if (!isImage) {
    ElMessage.error('只能上传图片文件');
    return false;
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB');
    return false;
  }
  return true;
};

const handleSave = async () => {
  saving.value = true;
  try {
    const res = await store.updateProfile(form.value);
    ElMessage.success('保存成功');
  } catch (error: any) {
    const msg = error.response?.data?.message || error.message || '保存失败';
    ElMessage.error(msg);
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  loadUserInfo();
});
</script>

<style scoped src="../../styles/pages/user-profile.css"></style>
