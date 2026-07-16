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
            <div class="avatar-upload-trigger" @click="triggerAvatarUpload">
              <div class="avatar-overlay">
                <el-icon :size="20"><Edit /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
            <input ref="avatarInput" type="file" accept="image/*" hidden @change="onAvatarFileSelected" />
          </div>
          <p class="avatar-hint">支持 JPG、PNG 格式，大小不超过 5MB</p>
        </div>
      </div>

      <!-- 自定义全屏头像裁剪弹窗（替换 el-dialog） -->
      <Teleport to="body">
        <Transition name="crop-modal">
          <div v-if="cropDialogVisible" class="crop-modal-overlay" @keydown.escape="handleCancel">
            <div class="crop-modal-content">
              <!-- 顶部操作栏 -->
              <div class="crop-toolbar">
                <button class="crop-toolbar-action" @click="handleCancel" :disabled="cropUploading" aria-label="取消裁剪">
                  <X :size="20" stroke-width="2" />
                  <span>取消</span>
                </button>
                <span class="crop-toolbar-title">裁剪头像</span>
                <button class="crop-toolbar-action crop-toolbar-done" @click="confirmCrop" :disabled="cropUploading" aria-label="完成裁剪">
                  <span>完成</span>
                  <Check :size="20" stroke-width="2.5" />
                </button>
              </div>

              <!-- 裁剪区域 -->
              <div class="crop-area" :class="{ 'is-uploading': cropUploading }">
                <img ref="cropImage" :src="cropImageSrc" class="crop-image" alt="待裁剪头像" />

                <!-- 上传进度遮罩层 -->
                <div v-if="cropUploading" class="crop-progress-overlay">
                  <div class="crop-progress-card">
                    <div class="crop-progress-ring">
                      <svg viewBox="0 0 80 80" class="crop-progress-svg">
                        <circle cx="40" cy="40" r="34" fill="none" class="crop-progress-track" />
                        <circle cx="40" cy="40" r="34" fill="none" class="crop-progress-fill-ring"
                          stroke-dasharray="213.6"
                          :style="{ strokeDashoffset: 213.6 - (213.6 * cropProgress) / 100 }" />
                      </svg>
                      <span class="crop-progress-pct">{{ cropProgress }}%</span>
                    </div>
                    <p class="crop-progress-label">正在上传头像…</p>
                  </div>
                </div>
              </div>

              <!-- 底部操作提示 -->
              <div class="crop-footer">
                <div class="crop-hint">
                  <Move :size="14" stroke-width="1.5" />
                  <span>拖动调整位置</span>
                </div>
                <div class="crop-hint">
                  <ZoomIn :size="14" stroke-width="1.5" />
                  <span>滚动缩放</span>
                </div>
              </div>
            </div>
          </div>
        </Transition>
      </Teleport>

      <!-- 基本信息 -->
      <div class="section-card">
        <h3 class="section-title">基本信息</h3>
        <el-form ref="profileFormRef" :model="form" :rules="profileRules" label-position="top" class="profile-form" @submit.prevent="handleSave">
          <div class="form-row">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="昵称" prop="nickname">
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

          <div class="form-row">
            <el-form-item label="学院/系">
              <el-input v-model="form.department" maxlength="100" placeholder="例如：计算机科学与技术学院" />
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="专业">
              <el-input v-model="form.major" maxlength="100" placeholder="例如：计算机科学与技术" />
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="年级">
              <el-select v-model="form.grade" placeholder="选择年级">
                <el-option label="大一" value="大一" />
                <el-option label="大二" value="大二" />
                <el-option label="大三" value="大三" />
                <el-option label="大四" value="大四" />
                <el-option label="研一" value="研一" />
                <el-option label="研二" value="研二" />
                <el-option label="研三" value="研三" />
                <el-option label="博士" value="博士" />
                <el-option label="教职工" value="教职工" />
                <el-option label="其他" value="其他" />
              </el-select>
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
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { ElMessage, type FormInstance } from 'element-plus';
import { userStore } from '../../store';
import api from '../../api';
import PageHeader from '../../components/user/PageHeader.vue';
import Cropper from 'cropperjs';
import { X, Check, Move, ZoomIn } from 'lucide-vue-next';

const store = userStore();
const profileFormRef = ref<FormInstance>();
const avatarInput = ref<HTMLInputElement>();
const cropDialogVisible = ref(false);
const cropImageSrc = ref('');
const cropImage = ref<HTMLImageElement>();
const cropUploading = ref(false);
const cropProgress = ref(0);
let cropper: Cropper | null = null;

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
  department: '',
  major: '',
  grade: '',
  studentId: '',
  createdAt: '',
  lastLoginTime: '',
  creditScore: 100,
  totalTransactions: 0,
  totalSales: 0,
  totalPurchases: 0,
});

const saving = ref(false);

const profileRules = {
  nickname: [
    { min: 2, max: 20, message: '昵称长度2-20个字符', trigger: 'blur' },
  ],
};

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
  department: user.department || '',
  major: user.major || '',
  grade: user.grade || '',
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

const triggerAvatarUpload = () => {
  avatarInput.value?.click();
};

const onAvatarFileSelected = async (e: Event) => {
  const target = e.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件');
    return;
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB');
    return;
  }

  const reader = new FileReader();
  reader.onload = () => {
    cropImageSrc.value = reader.result as string;
    cropDialogVisible.value = true;
    nextTick(() => initCropper());
  };
  reader.readAsDataURL(file);
  target.value = '';
};

const initCropper = () => {
  destroyCropper();
  const img = cropImage.value;
  if (!img) return;
  cropProgress.value = 0;
  // 等待 flex 布局计算完容器尺寸后再初始化裁剪器
  requestAnimationFrame(() => {
    const area = img.parentElement;
    if (area) {
      img.style.maxWidth = '100%';
      img.style.maxHeight = '100%';
      img.style.display = 'block';
    }
    cropper = new Cropper(img, {
      aspectRatio: 1,
      viewMode: 1,
      dragMode: 'move',
      autoCropArea: 0.9,
      cropBoxMovable: false,
      cropBoxResizable: false,
      background: false,
      guides: true,
      center: true,
      highlight: false,
    });
  });
};

const destroyCropper = () => {
  if (cropper) {
    cropper.destroy();
    cropper = null;
  }
};

const handleCancel = () => {
  if (cropUploading.value) return;
  destroyCropper();
  cropDialogVisible.value = false;
};

const confirmCrop = async () => {
  if (!cropper) return;
  cropUploading.value = true;
  cropProgress.value = 0;

  try {
    const canvas = cropper.getCroppedCanvas({ width: 300, height: 300 });
    const blob = await new Promise<Blob>((resolve) => canvas.toBlob((b) => resolve(b!), 'image/png'));
    const formData = new FormData();
    formData.append('file', blob, 'avatar.png');

    // 使用 XMLHttpRequest 获取上传进度
    const result = await new Promise<unknown>((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      xhr.open('POST', '/api/upload');
      xhr.timeout = 30000;

      xhr.upload.onprogress = (e) => {
        if (e.lengthComputable) {
          cropProgress.value = Math.min(Math.round((e.loaded / e.total) * 100), 99);
        }
      };

      xhr.onload = () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          try {
            resolve(JSON.parse(xhr.responseText));
          } catch {
            reject(new Error('服务器响应格式异常'));
          }
        } else {
          try {
            const err = JSON.parse(xhr.responseText);
            reject(new Error(err.message || `上传失败 (${xhr.status})`));
          } catch {
            reject(new Error(`上传失败 (${xhr.status})`));
          }
        }
      };

      xhr.onerror = () => reject(new Error('网络异常，上传失败'));
      xhr.ontimeout = () => reject(new Error('上传超时，请重试'));
      xhr.send(formData);
    });

    cropProgress.value = 100;
    // 短暂展示 100% 带给用户的完成感
    await new Promise((r) => setTimeout(r, 300));

    const uploadResult = result as any;
    if (uploadResult?.code === 200 && uploadResult?.data?.url) {
      form.value.avatar = uploadResult.data.url;
      ElMessage.success({ message: '头像上传成功', duration: 2000 });
      cropDialogVisible.value = false;
    } else {
      throw new Error(uploadResult?.message || '上传失败');
    }
  } catch (error: unknown) {
    ElMessage.error(error instanceof Error ? error.message : '头像上传失败');
  } finally {
    cropUploading.value = false;
  }
};

const handleSave = async () => {
  saving.value = true;
  try {
    await store.updateProfile(form.value);
    ElMessage.success('保存成功');
  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } }; message?: string };
    const msg = err.response?.data?.message || err.message || '保存失败';
    ElMessage.error(msg);
  } finally {
    saving.value = false;
  }
};

// 弹窗打开时锁定 body 滚动
watch(cropDialogVisible, (visible) => {
  if (visible) {
    document.body.style.overflow = 'hidden';
  } else {
    document.body.style.overflow = '';
    destroyCropper();
  }
});

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && cropDialogVisible.value && !cropUploading.value) {
    handleCancel();
  }
};

onMounted(() => {
  loadUserInfo();
  document.addEventListener('keydown', handleKeydown);
});

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown);
  document.body.style.overflow = '';
  destroyCropper();
});
</script>

<style scoped src="../../styles/pages/user-profile.css"></style>

<!-- CropperJS 样式必须在非 scoped 的 <style> 块中，:deep() 选择器才能正确工作 -->
<style>
/* Cropper 容器撑满裁剪区域 */
.crop-area .cropper-container {
  width: 100% !important;
  height: 100% !important;
}

.crop-area .cropper-wrap-box,
.crop-area .cropper-canvas {
  width: 100% !important;
  height: 100% !important;
}

/* 圆形裁剪框 */
.crop-area .cropper-view-box,
.crop-area .cropper-face {
  border-radius: 50%;
}

.crop-area .cropper-view-box {
  outline: 2.5px solid var(--accent, #409eff);
  outline-offset: -1px;
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.55);
}

/* 隐藏拖动控制点和边线 */
.crop-area .cropper-point,
.crop-area .cropper-line {
  display: none;
}

/* 裁剪框外区域透明 */
.crop-area .cropper-modal {
  background: transparent;
}

/* 裁剪网格线 */
.crop-area .cropper-dashed {
  border-color: rgba(255, 255, 255, 0.35);
}

/* 中心十字 */
.crop-area .cropper-center::before,
.crop-area .cropper-center::after {
  background: rgba(255, 255, 255, 0.4);
}

/* 暗色模式遮罩加深 */
html.dark .crop-area .cropper-view-box {
  outline-color: oklch(65% 0.16 150);
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.65);
}
</style>
