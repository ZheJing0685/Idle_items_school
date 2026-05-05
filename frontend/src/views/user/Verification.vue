<template>
  <div class="verification-container">
    <div class="verification-header">
      <h2>实名认证</h2>
      <p class="verification-subtitle">
        完成实名认证后，您将获得更多平台功能权限
      </p>
    </div>

    <div class="verification-content">
      <!-- 认证状态展示 -->
      <div v-if="verificationStatus" class="status-section">
        <div class="status-card" :class="`status-${verificationStatus.status}`">
          <div class="status-icon">
            <svg
              v-if="verificationStatus.status === 'approved'"
              width="48"
              height="48"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
            <svg
              v-else-if="verificationStatus.status === 'pending'"
              width="48"
              height="48"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
            <svg
              v-else-if="verificationStatus.status === 'rejected'"
              width="48"
              height="48"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <circle cx="12" cy="12" r="10" />
              <line x1="15" y1="9" x2="9" y2="15" />
              <line x1="9" y1="9" x2="15" y2="15" />
            </svg>
          </div>
          <div class="status-info">
            <h3>{{ getStatusText(verificationStatus.status) }}</h3>
            <p v-if="verificationStatus.status === 'pending'">
              您的认证信息正在审核中，通常需要1-2个工作日
            </p>
            <p v-else-if="verificationStatus.status === 'rejected'">
              很抱歉，您的认证信息未通过审核
            </p>
            <p v-else-if="verificationStatus.status === 'approved'">
              恭喜您，认证已通过！
            </p>
            <p v-if="verificationStatus.reason" class="status-reason">
              驳回原因：{{ verificationStatus.reason }}
            </p>
          </div>
          <div class="status-actions">
            <el-button
              v-if="verificationStatus.status === 'rejected'"
              @click="resetVerification"
              type="primary"
            >
              重新提交
            </el-button>
          </div>
        </div>
      </div>

      <!-- 认证表单 -->
      <div v-else class="form-section">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
          <el-form-item label="认证类型" prop="verificationType">
            <el-select v-model="form.verificationType" placeholder="请选择认证类型">
              <el-option label="身份证认证" value="1" />
              <el-option label="学生证认证" value="2" />
              <el-option label="教师证认证" value="3" />
            </el-select>
          </el-form-item>

          <el-form-item label="真实姓名" prop="name">
            <el-input
              v-model="form.name"
              placeholder="请输入您的真实姓名"
              maxlength="20"
            />
          </el-form-item>

          <!-- 身份证认证字段 -->
          <el-form-item v-if="form.verificationType === '1'" label="身份证号" prop="idCard">
            <el-input
              v-model="form.idCard"
              placeholder="请输入您的身份证号"
              maxlength="18"
            />
          </el-form-item>

          <el-form-item v-if="form.verificationType === '1'" label="身份证正面照片" prop="idCardFront">
            <div class="upload-section">
              <el-upload
                class="avatar-uploader"
                :action="uploadUrl"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="(options) => customUpload(options, 'idCardFront')"
              >
                <img
                  v-if="form.idCardFront"
                  :src="form.idCardFront"
                  class="avatar"
                />
                <div v-else class="upload-placeholder">
                  <svg
                    width="48"
                    height="48"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                    <circle cx="8.5" cy="8.5" r="1.5" />
                    <polyline points="21 15 16 10 5 21" />
                  </svg>
                  <span>点击上传身份证正面照片</span>
                </div>
              </el-upload>
              <p class="upload-hint">请上传清晰的身份证正面照片，确保信息可见</p>
            </div>
          </el-form-item>

          <el-form-item v-if="form.verificationType === '1'" label="身份证反面照片" prop="idCardBack">
            <div class="upload-section">
              <el-upload
                class="avatar-uploader"
                :action="uploadUrl"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="(options) => customUpload(options, 'idCardBack')"
              >
                <img
                  v-if="form.idCardBack"
                  :src="form.idCardBack"
                  class="avatar"
                />
                <div v-else class="upload-placeholder">
                  <svg
                    width="48"
                    height="48"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                    <circle cx="8.5" cy="8.5" r="1.5" />
                    <polyline points="21 15 16 10 5 21" />
                  </svg>
                  <span>点击上传身份证反面照片</span>
                </div>
              </el-upload>
              <p class="upload-hint">请上传清晰的身份证反面照片，确保信息可见</p>
            </div>
          </el-form-item>

          <!-- 学生证认证字段 -->
          <el-form-item v-if="form.verificationType === '2'" label="学号" prop="studentId">
            <el-input
              v-model="form.studentId"
              placeholder="请输入您的学号"
              maxlength="20"
            />
          </el-form-item>

          <el-form-item v-if="form.verificationType === '2'" label="学校" prop="school">
            <el-input
              v-model="form.school"
              placeholder="请输入您的学校名称"
              maxlength="50"
            />
          </el-form-item>

          <el-form-item v-if="form.verificationType === '2'" label="学生证照片" prop="studentCard">
            <div class="upload-section">
              <el-upload
                class="avatar-uploader"
                :action="uploadUrl"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="(options) => customUpload(options, 'studentCard')"
              >
                <img
                  v-if="form.studentCard"
                  :src="form.studentCard"
                  class="avatar"
                />
                <div v-else class="upload-placeholder">
                  <svg
                    width="48"
                    height="48"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                    <circle cx="8.5" cy="8.5" r="1.5" />
                    <polyline points="21 15 16 10 5 21" />
                  </svg>
                  <span>点击上传学生证照片</span>
                </div>
              </el-upload>
              <p class="upload-hint">请上传清晰的学生证照片，确保信息可见</p>
            </div>
          </el-form-item>

          <!-- 教师证认证字段 -->
          <el-form-item v-if="form.verificationType === '3'" label="教师证号" prop="teacherId">
            <el-input
              v-model="form.teacherId"
              placeholder="请输入您的教师证号"
              maxlength="20"
            />
          </el-form-item>

          <el-form-item v-if="form.verificationType === '3'" label="学校" prop="school">
            <el-input
              v-model="form.school"
              placeholder="请输入您的学校名称"
              maxlength="50"
            />
          </el-form-item>

          <el-form-item v-if="form.verificationType === '3'" label="教师证照片" prop="teacherCard">
            <div class="upload-section">
              <el-upload
                class="avatar-uploader"
                :action="uploadUrl"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="(options) => customUpload(options, 'teacherCard')"
              >
                <img
                  v-if="form.teacherCard"
                  :src="form.teacherCard"
                  class="avatar"
                />
                <div v-else class="upload-placeholder">
                  <svg
                    width="48"
                    height="48"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                    <circle cx="8.5" cy="8.5" r="1.5" />
                    <polyline points="21 15 16 10 5 21" />
                  </svg>
                  <span>点击上传教师证照片</span>
                </div>
              </el-upload>
              <p class="upload-hint">请上传清晰的教师证照片，确保信息可见</p>
            </div>
          </el-form-item>

          <el-form-item label="隐私协议" prop="agreePrivacy">
            <el-checkbox v-model="form.agreePrivacy">
              我已阅读并同意<a href="#" class="privacy-link">《隐私协议》</a>，
              了解个人信息的使用范围和目的
            </el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitForm" :loading="loading">
              提交认证
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { userStore } from '../../store';
import api from '../../api';
import UploadUtil from '../../utils/upload/uploadUtil';

const store = userStore();
const formRef = ref(null);
const uploadRef = ref(null);
const loading = ref(false);
const verificationStatus = ref(null);

// 表单数据
const form = reactive({
  verificationType: '',
  name: '',
  idCard: '',
  idCardFront: '',
  idCardBack: '',
  studentId: '',
  school: '',
  studentCard: '',
  teacherId: '',
  teacherCard: '',
  agreePrivacy: false,
});

// 表单验证规则
const rules = {
  verificationType: [
    { required: true, message: '请选择认证类型', trigger: 'change' },
  ],
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    {
      pattern: /^[\u4e00-\u9fa5]{2,20}$/,
      message: '请输入正确的中文姓名',
      trigger: 'blur',
    },
  ],
  idCard: [
    {
      required: true,
      message: '请输入身份证号',
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (form.verificationType === '1' && !value) {
          callback(new Error('请输入身份证号'));
        } else {
          callback();
        }
      },
    },
    {
      pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
      message: '请输入正确的身份证号',
      trigger: 'blur',
    },
  ],
  idCardFront: [
    {
      required: true,
      message: '请上传身份证正面照片',
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (form.verificationType === '1' && !value) {
          callback(new Error('请上传身份证正面照片'));
        } else {
          callback();
        }
      },
    },
  ],
  idCardBack: [
    {
      required: true,
      message: '请上传身份证反面照片',
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (form.verificationType === '1' && !value) {
          callback(new Error('请上传身份证反面照片'));
        } else {
          callback();
        }
      },
    },
  ],
  studentId: [
    {
      required: true,
      message: '请输入学号',
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (form.verificationType === '2' && !value) {
          callback(new Error('请输入学号'));
        } else {
          callback();
        }
      },
    },
    {
      pattern: /^[0-9a-zA-Z]{1,20}$/,
      message: '学号只能包含数字和字母',
      trigger: 'blur',
    },
  ],
  school: [
    {
      required: true,
      message: '请输入学校名称',
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if ((form.verificationType === '2' || form.verificationType === '3') && !value) {
          callback(new Error('请输入学校名称'));
        } else {
          callback();
        }
      },
    },
    {
      min: 2,
      max: 50,
      message: '学校名称长度在2-50个字符之间',
      trigger: 'blur',
    },
  ],
  studentCard: [
    {
      required: true,
      message: '请上传学生证照片',
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (form.verificationType === '2' && !value) {
          callback(new Error('请上传学生证照片'));
        } else {
          callback();
        }
      },
    },
  ],
  teacherId: [
    {
      required: true,
      message: '请输入教师证号',
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (form.verificationType === '3' && !value) {
          callback(new Error('请输入教师证号'));
        } else {
          callback();
        }
      },
    },
    {
      pattern: /^[0-9a-zA-Z]{1,20}$/,
      message: '教师证号只能包含数字和字母',
      trigger: 'blur',
    },
  ],
  teacherCard: [
    {
      required: true,
      message: '请上传教师证照片',
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (form.verificationType === '3' && !value) {
          callback(new Error('请上传教师证照片'));
        } else {
          callback();
        }
      },
    },
  ],
  agreePrivacy: [
    { required: true, message: '请阅读并同意隐私协议', trigger: 'change' },
  ],
};

// 上传URL
const uploadUrl = computed(() => {
  return 'http://localhost:7000/api/verification/upload';
});

// 检查认证状态
const checkVerificationStatus = async () => {
  try {
    const response = await api.verification.getStatus();
    if (response.code === 200) {
      // 只有当状态不是unverified时才显示状态卡片
      if (response.data.status && response.data.status !== 'unverified') {
        verificationStatus.value = response.data;
      } else {
        verificationStatus.value = null;
      }
    }
  } catch (error) {
    console.error('获取认证状态失败:', error);
    verificationStatus.value = null;
  }
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        // 根据认证类型构建提交数据
        let submitData = {
          verificationType: form.verificationType,
          name: form.name,
        };

        // 根据认证类型添加不同的字段
        if (form.verificationType === '1') {
          // 身份证认证
          submitData.idCard = form.idCard;
          submitData.idCardFront = form.idCardFront;
          submitData.idCardBack = form.idCardBack;
        } else if (form.verificationType === '2') {
          // 学生证认证
          submitData.studentId = form.studentId;
          submitData.school = form.school;
          submitData.studentCard = form.studentCard;
        } else if (form.verificationType === '3') {
          // 教师证认证
          submitData.teacherId = form.teacherId;
          submitData.school = form.school;
          submitData.teacherCard = form.teacherCard;
        }

        const response = await api.verification.submit(submitData);
        if (response.code === 200) {
          ElMessage.success('认证信息提交成功，正在审核中');
          // 提交成功后从后端获取最新状态
          await checkVerificationStatus();
        }
      } catch (error) {
        ElMessage.error('提交失败，请稍后重试');
        console.error('提交认证信息失败:', error);
      } finally {
        loading.value = false;
      }
    }
  });
};

// 自定义上传方法
const customUpload = async (options, fieldName) => {
  try {
    const response = await uploadUtil.uploadFile(
      options.file,
      (progress) => {
        options.onProgress({ percent: progress });
      },
      null,
      'verification'
    );
    if (response.code === 200) {
      // 直接更新表单数据
      form[fieldName] = response.data.url;
      options.onSuccess(response);
      ElMessage.success('图片上传成功');
    } else {
      const errorMsg = response.message || '上传失败';
      options.onError(new Error(errorMsg));
      ElMessage.error(errorMsg);
    }
  } catch (error) {
    const errorMsg = error.message || '上传失败';
    options.onError(error);
    ElMessage.error(errorMsg);
  }
};

// 上传前验证
const beforeAvatarUpload = (file) => {
  const isJPG =
    file.type === 'image/jpeg' ||
    file.type === 'image/png' ||
    file.type === 'image/webp';
  const isLt5M = file.size / 1024 / 1024 < 5;

  if (!isJPG) {
    ElMessage.error('只能上传 JPG、PNG 或 WebP 格式的图片');
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB');
  }

  return isJPG && isLt5M;
};

// 重置认证
const resetVerification = () => {
  ElMessageBox.confirm('确定要重新提交认证信息吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    verificationStatus.value = null;
    form.name = '';
    form.studentId = '';
    form.school = '';
    form.studentCard = '';
    form.agreePrivacy = false;
  });
};

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    pending: '审核中',
    approved: '认证通过',
    rejected: '认证驳回',
  };
  return statusMap[status] || '未知状态';
};

onMounted(() => {
  checkVerificationStatus();
});
</script>

<style scoped src="../../styles/pages/user-verification.css"></style>
