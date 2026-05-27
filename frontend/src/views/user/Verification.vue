<template>
  <div class="verification-page">
    <PageHeader title="实名认证" subtitle="完成实名认证后，您将获得更多平台功能权限" />

    <!-- 认证状态展示 -->
    <VerificationStatus
      v-if="verificationStatus"
      :status="verificationStatus.status"
      :title="getStatusText(verificationStatus.status)"
      :description="getStatusDescription(verificationStatus.status)"
      :reason="verificationStatus.rejectReason"
    >
      <template #action>
        <el-button v-if="verificationStatus.status === 'rejected'" @click="resetVerification" type="primary">
          重新提交
        </el-button>
      </template>
    </VerificationStatus>

    <!-- 认证表单 -->
    <div v-else class="form-card">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="认证类型" prop="verificationType">
          <el-select v-model="form.verificationType" placeholder="请选择认证类型" style="width: 100%">
            <el-option label="身份证认证" value="1" />
            <el-option label="学生证认证" value="2" />
            <el-option label="教师证认证" value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="真实姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入您的真实姓名" maxlength="20" />
        </el-form-item>

        <!-- 身份证认证字段 -->
        <template v-if="form.verificationType === '1'">
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="form.idCard" placeholder="请输入您的身份证号" maxlength="18" />
          </el-form-item>

          <el-form-item label="身份证正面照片" prop="idCardFront">
            <UploadArea v-model="form.idCardFront" text="点击上传身份证正面照片" hint="请上传清晰的身份证正面照片，确保信息可见" />
          </el-form-item>

          <el-form-item label="身份证反面照片" prop="idCardBack">
            <UploadArea v-model="form.idCardBack" text="点击上传身份证反面照片" hint="请上传清晰的身份证反面照片，确保信息可见" />
          </el-form-item>
        </template>

        <!-- 学生证认证字段 -->
        <template v-if="form.verificationType === '2'">
          <el-form-item label="学号" prop="studentId">
            <el-input v-model="form.studentId" placeholder="请输入您的学号" maxlength="20" />
          </el-form-item>

          <el-form-item label="学校" prop="school">
            <el-input v-model="form.school" placeholder="请输入您的学校名称" maxlength="50" />
          </el-form-item>

          <el-form-item label="学生证照片" prop="studentCard">
            <UploadArea v-model="form.studentCard" text="点击上传学生证照片" hint="请上传清晰的学生证照片，确保信息可见" />
          </el-form-item>
        </template>

        <!-- 教师证认证字段 -->
        <template v-if="form.verificationType === '3'">
          <el-form-item label="教师证号" prop="teacherId">
            <el-input v-model="form.teacherId" placeholder="请输入您的教师证号" maxlength="20" />
          </el-form-item>

          <el-form-item label="学校" prop="school">
            <el-input v-model="form.school" placeholder="请输入您的学校名称" maxlength="50" />
          </el-form-item>

          <el-form-item label="教师证照片" prop="teacherCard">
            <UploadArea v-model="form.teacherCard" text="点击上传教师证照片" hint="请上传清晰的教师证照片，确保信息可见" />
          </el-form-item>
        </template>

        <el-form-item label="隐私协议" prop="agreePrivacy">
          <el-checkbox v-model="form.agreePrivacy">
            我已阅读并同意<a href="#" class="privacy-link">《隐私协议》</a>，了解个人信息的使用范围和目的
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading" size="large" style="width: 100%">
            提交认证
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox, ElForm } from 'element-plus';
import { userStore } from '../../store';
import api from '../../api';
import PageHeader from '../../components/user/PageHeader.vue';
import VerificationStatus from '../../components/user/VerificationStatus.vue';
import UploadArea from '../../components/user/UploadArea.vue';

const store = userStore();
const formRef = ref<InstanceType<typeof ElForm> | null>(null);
const loading = ref(false);
const verificationStatus = ref<Record<string, string> | null>(null);

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

const rules = {
  verificationType: [{ required: true, message: '请选择认证类型', trigger: 'change' }],
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5]{2,20}$/, message: '请输入正确的中文姓名', trigger: 'blur' }
  ],
  idCard: [
    {
      validator: (rule: any, value: string, callback: any) => {
        if (form.verificationType === '1') {
          if (!value) {
            callback(new Error('请输入身份证号'));
          } else if (!/^\d{17}[\dX]$/i.test(value)) {
            callback(new Error('身份证号格式不正确，应为18位'));
          } else {
            // 校验位验证
            const weight = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
            const checkCode = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'];
            let sum = 0;
            for (let i = 0; i < 17; i++) {
              sum += parseInt(value[i]) * weight[i];
            }
            const expected = checkCode[sum % 11];
            if (expected !== value[17].toUpperCase()) {
              callback(new Error('身份证号校验位不正确'));
            } else {
              callback();
            }
          }
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  idCardFront: [
    {
      validator: (rule: any, value: string, callback: any) => {
        if (form.verificationType === '1' && !value) {
          callback(new Error('请上传身份证正面照片'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ],
  idCardBack: [
    {
      validator: (rule: any, value: string, callback: any) => {
        if (form.verificationType === '1' && !value) {
          callback(new Error('请上传身份证反面照片'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ],
  studentId: [
    {
      validator: (rule: any, value: string, callback: any) => {
        if (form.verificationType === '2') {
          if (!value) {
            callback(new Error('请输入学号'));
          } else if (value.length > 50) {
            callback(new Error('学号长度不能超过50位'));
          } else {
            callback();
          }
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  school: [
    {
      validator: (rule: any, value: string, callback: any) => {
        if ((form.verificationType === '2' || form.verificationType === '3') && !value) {
          callback(new Error('请输入学校名称'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  studentCard: [
    {
      validator: (rule: any, value: string, callback: any) => {
        if (form.verificationType === '2' && !value) {
          callback(new Error('请上传学生证照片'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ],
  teacherId: [
    {
      validator: (rule: any, value: string, callback: any) => {
        if (form.verificationType === '3') {
          if (!value) {
            callback(new Error('请输入教师工号'));
          } else if (value.length > 50) {
            callback(new Error('教师工号长度不能超过50位'));
          } else {
            callback();
          }
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  teacherCard: [
    {
      validator: (rule: any, value: string, callback: any) => {
        if (form.verificationType === '3' && !value) {
          callback(new Error('请上传教师证照片'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ],
  agreePrivacy: [{ required: true, message: '请阅读并同意隐私协议', trigger: 'change' }]
};

const checkVerificationStatus = async () => {
  try {
    const response = await api.verification.getStatus();
    if (response.code === 200) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const data = response.data as any;
      const status = data?.status;
      if (status && status !== 'unverified') {
        verificationStatus.value = data;
      } else {
        verificationStatus.value = null;
      }
    }
  } catch {
    verificationStatus.value = null;
  }
};

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    approved: '已认证',
    pending: '审核中',
    rejected: '未通过'
  };
  return map[status] || status;
};

const getStatusDescription = (status: string) => {
  const map: Record<string, string> = {
    approved: '恭喜您，认证已通过！',
    pending: '您的认证信息正在审核中，通常需要1-2个工作日',
    rejected: '很抱歉，您的认证信息未通过审核'
  };
  return map[status] || '';
};

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const submitData: Record<string, any> = {
          verificationType: form.verificationType,
          realName: form.name,
        };
        if (form.verificationType === '1') {
          submitData.idCard = form.idCard;
          submitData.idCardFront = form.idCardFront;
          submitData.idCardBack = form.idCardBack;
        } else if (form.verificationType === '2') {
          submitData.studentId = form.studentId;
          submitData.school = form.school;
          submitData.studentCard = form.studentCard;
        } else if (form.verificationType === '3') {
          submitData.teacherId = form.teacherId;
          submitData.school = form.school;
          submitData.teacherCard = form.teacherCard;
        }
        const response = await api.verification.submit(submitData as any);
        if (response.code === 200) {
          ElMessage.success('认证信息提交成功，正在审核中');
          await checkVerificationStatus();
        } else {
          ElMessage.error(response.message || '提交失败');
        }
      } catch (error: any) {
        const msg = error?.response?.data?.message || error?.message || '提交失败，请稍后重试';
        ElMessage.error(msg);
      } finally {
        loading.value = false;
      }
    }
  });
};

const resetVerification = () => {
  ElMessageBox.confirm('确定要重新提交认证信息吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    verificationStatus.value = null;
    form.name = '';
    form.studentId = '';
    form.school = '';
    form.studentCard = '';
    form.agreePrivacy = false;
  });
};

onMounted(async () => {
  checkVerificationStatus();
});
</script>

<style scoped src="../../styles/pages/user-verification.css"></style>
