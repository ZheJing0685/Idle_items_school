<template>
  <div class="profile-page">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h3>个人信息</h3>
          <span class="header-tip">完善资料有助于获得更多信任</span>
        </div>
      </template>
      <el-form :model="form" label-width="100px" class="profile-form">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <el-avatar :size="100" :src="form.avatar">
              {{ form.nickname?.charAt(0) || form.username?.charAt(0) || '用' }}
            </el-avatar>
            <el-upload
              class="avatar-upload"
              action="/api/upload"
              :headers="{ Authorization: `Bearer ${store.token}` }"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
            >
              <el-button size="small" type="primary">更换头像</el-button>
            </el-upload>
          </div>
        </div>

        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>

        <el-form-item label="昵称">
          <el-input
            v-model="form.nickname"
            maxlength="50"
            placeholder="请输入昵称"
          />
        </el-form-item>

        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="生日">
          <el-date-picker
            v-model="form.birthday"
            type="date"
            placeholder="选择生日"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="个人简介">
          <el-input
            v-model="form.bio"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="介绍一下自己吧"
          />
        </el-form-item>

        <div class="form-divider"></div>

        <el-form-item label="邮箱">
          <el-input v-model="form.email" disabled />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input
            v-model="form.phone"
            maxlength="20"
            placeholder="请输入手机号"
          />
        </el-form-item>

        <div class="form-divider"></div>

        <el-form-item label="学校名称">
          <el-input
            v-model="form.schoolName"
            maxlength="100"
            placeholder="请输入学校名称"
          />
        </el-form-item>

        <el-form-item label="学号">
          <el-input
            v-model="form.studentId"
            maxlength="20"
            placeholder="请输入学号"
          />
        </el-form-item>

        <div class="form-divider"></div>

        <el-form-item label="注册时间">
          <el-input v-model="form.createdAt" disabled />
        </el-form-item>

        <el-form-item label="最后登录">
          <el-input v-model="form.lastLoginTime" disabled />
        </el-form-item>

        <el-form-item label="信用评分">
          <div class="credit-score">
            <el-progress
              :percentage="form.creditScore || 100"
              :color="getScoreColor(form.creditScore)"
            />
            <span class="score-text">{{ form.creditScore || 100 }} 分</span>
          </div>
        </el-form-item>

        <el-form-item label="交易统计">
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
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving"
            >保存修改</el-button
          >
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { userStore } from '../../store';

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

const getScoreColor = (score) => {
  if (score >= 80) return '#67C23A';
  if (score >= 60) return '#E6A23C';
  return '#F56C6C';
};

const loadUserInfo = async () => {
  try {
    const response = await fetch('/api/users/profile', {
      headers: { Authorization: `Bearer ${store.token}` },
    });
    if (response.ok) {
      const data = await response.json();
      if (data.code === 200) {
        const user = data.data;
        form.value = {
          username: user.username || '',
          nickname: user.nickname || '',
          email: user.email || '',
          phone: user.phone || '',
          avatar: user.avatar || '',
          gender: user.gender || 0,
          birthday: user.birthday || '',
          bio: user.bio || '',
          schoolName: user.schoolName || '',
          studentId: user.studentId || '',
          createdAt: user.createdAt || '',
          lastLoginTime: user.lastLoginTime || '',
          creditScore: user.creditScore || 100,
          totalTransactions: user.totalTransactions || 0,
          totalSales: user.totalSales || 0,
          totalPurchases: user.totalPurchases || 0,
        };
      }
    }
  } catch (error) {
    const user = JSON.parse(localStorage.getItem('user') || 'null');
    if (user) {
      form.value = {
        username: user.username || '',
        nickname: user.nickname || '',
        email: user.email || '',
        phone: user.phone || '',
        avatar: user.avatar || '',
        gender: user.gender || 0,
        birthday: user.birthday || '',
        bio: user.bio || '',
        schoolName: user.schoolName || '',
        studentId: user.studentId || '',
        createdAt: user.createdAt || '',
        lastLoginTime: user.lastLoginTime || '',
        creditScore: user.creditScore || 100,
        totalTransactions: user.totalTransactions || 0,
        totalSales: user.totalSales || 0,
        totalPurchases: user.totalPurchases || 0,
      };
    }
  }
};

const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    form.value.avatar = response.data.url;
    ElMessage.success('头像上传成功');
  }
};

const beforeAvatarUpload = (file) => {
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
    const response = await fetch('/api/users/profile', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${store.token}`,
      },
      body: JSON.stringify(form.value),
    });
    const data = await response.json();
    if (data.code === 200) {
      ElMessage.success('保存成功');
      const user = JSON.parse(localStorage.getItem('user') || 'null');
      if (user) {
        Object.assign(user, form.value);
        localStorage.setItem('user', JSON.stringify(user));
      }
    } else {
      ElMessage.error(data.message || '保存失败');
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  loadUserInfo();
});
</script>

<style scoped src="../../styles/pages/user-profile.css"></style>
