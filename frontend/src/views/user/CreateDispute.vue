<template>
  <div class="create-dispute">
    <div class="page-header">
      <h2 class="page-title">申请纠纷</h2>
      <p class="page-desc">如果您与卖家存在交易纠纷，可以在此提交纠纷申请</p>
    </div>

    <div class="order-info-card" v-if="orderInfo">
      <div class="order-header">
        <span class="order-label">订单信息</span>
        <span class="order-no">#{{ orderId }}</span>
      </div>
      <div class="order-detail">
        <div class="info-row">
          <span class="info-label">物品</span>
          <span class="info-value">{{ orderInfo.itemTitle || '-' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">订单金额</span>
          <span class="info-value price">¥{{ orderInfo.price }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">订单状态</span>
          <span class="info-value">{{ getOrderStatusLabel(orderInfo.orderStatus) }}</span>
        </div>
      </div>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="dispute-form">
      <el-form-item label="纠纷类型" prop="disputeType">
        <el-radio-group v-model="form.disputeType">
          <el-radio :value="1">商品问题</el-radio>
          <el-radio :value="2">物流问题</el-radio>
          <el-radio :value="3">退款问题</el-radio>
          <el-radio :value="4">其他</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="纠纷原因" prop="reason">
        <el-input v-model="form.reason" placeholder="请简要描述纠纷原因" maxlength="200" show-word-limit />
      </el-form-item>

      <el-form-item label="详细描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="5" placeholder="请详细描述您遇到的问题，上传图片可以更好地帮助我们了解情况" maxlength="2000" show-word-limit />
      </el-form-item>

      <el-form-item label="期望结果" prop="expectResult">
        <el-select v-model="form.expectResult" placeholder="请选择期望的处理方式">
          <el-option label="全额退款" value="全额退款" />
          <el-option label="部分退款" value="部分退款" />
          <el-option label="重新发货" value="重新发货" />
          <el-option label="其他" value="其他" />
        </el-select>
      </el-form-item>

      <el-form-item v-if="form.expectResult === '部分退款'" label="期望退款金额" prop="expectRefundAmount">
        <el-input-number v-model="form.expectRefundAmount" :min="0.01" :max="orderInfo?.price || 99999" :precision="2" placeholder="请输入期望退款金额" style="width: 200px" />
        <span class="input-tip">订单金额：¥{{ orderInfo?.price || 0 }}</span>
      </el-form-item>

      <el-form-item label="上传证据">
        <div class="upload-area">
          <el-upload
            ref="uploadRef"
            :action="uploadUrl"
            list-type="picture-card"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleRemove"
            :before-upload="beforeUpload"
            :limit="5"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传5张图片，支持 JPG、PNG 格式</div>
        </div>
      </el-form-item>

      <div class="form-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">提交申请</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, type FormInstance } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import api from '../../api';
import type { DisputeForm, DisputeItem } from '../../types/dispute';

const router = useRouter();
const route = useRoute();

const orderId = computed(() => parseInt(route.params.orderId as string));
const uploadUrl = '/api/upload/image';

const formRef = ref<FormInstance | null>(null);
const uploadRef = ref(null);
const orderInfo = ref<{
  itemTitle: string
  price: number
  orderStatus: string
} | null>(null);
const submitting = ref(false);

const form = ref<DisputeForm>({
  disputeType: 1,
  reason: '',
  description: '',
  expectResult: '',
  expectRefundAmount: null,
});

const evidenceImages = ref<string[]>([]);

const rules = {
  disputeType: [{ required: true, message: '请选择纠纷类型', trigger: 'change' }],
  reason: [{ required: true, message: '请输入纠纷原因', trigger: 'blur' },
           { min: 5, max: 200, message: '纠纷原因长度在 5 到 200 个字符', trigger: 'blur' }],
  description: [{ required: true, message: '请输入详细描述', trigger: 'blur' },
                { min: 10, max: 2000, message: '详细描述长度在 10 到 2000 个字符', trigger: 'blur' }],
  expectResult: [{ required: true, message: '请选择期望结果', trigger: 'change' }],
};

const getOrderStatusLabel = (status: string): string => {
  const map: Record<string, string> = {
    PENDING_PAYMENT: '待付款',
    PENDING_SHIPMENT: '待发货',
    SHIPPED: '已发货',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    REFUND_REQUESTED: '退款中',
    REFUNDED: '已退款',
  };
  return map[status] || status;
};

const handleUploadSuccess = (res: { code: number; data?: string; message?: string }, _file: unknown): void => {
  if (res.code === 200 && res.data) {
    evidenceImages.value.push(res.data);
    ElMessage.success('上传成功');
  } else {
    ElMessage.error(res.message || '上传失败');
  }
};

const handleUploadError = (): void => {
  ElMessage.error('上传失败，请重试');
};

const handleRemove = (file: { response?: { data?: string }; url?: string }, _fileList: unknown[]): void => {
  const url = file.response?.data || file.url;
  if (url) {
    evidenceImages.value = evidenceImages.value.filter(img => img !== url);
  }
};

const beforeUpload = (file: File): boolean => {
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

const goBack = (): void => {
  router.back();
};

const submitForm = async (): Promise<void> => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
  } catch (_e: unknown) {
    return;
  }

  if (form.value.expectResult === '部分退款' && !form.value.expectRefundAmount) {
    ElMessage.warning('请输入期望退款金额');
    return;
  }

  submitting.value = true;

  try {
    const data = {
      orderId: orderId.value,
      disputeType: form.value.disputeType,
      reason: form.value.reason,
      description: form.value.description,
      expectResult: form.value.expectResult,
      expectRefundAmount: form.value.expectRefundAmount,
      evidenceImages: JSON.stringify(evidenceImages.value),
    };

    const res = await api.user.disputes.create(data);

    if (res.code === 200) {
      ElMessage.success('纠纷申请提交成功');
      router.push('/user/disputes');
    } else {
      ElMessage.error(res.message || '提交失败');
    }
  } catch (_e: unknown) {
    ElMessage.error('网络错误，请重试');
  } finally {
    submitting.value = false;
  }
};

const fetchOrderInfo = async (): Promise<void> => {
  try {
    const res = await api.user.disputes.canDispute(orderId.value);
    if (res.code === 200 && res.data) {
      orderInfo.value = {
        itemTitle: res.data.itemTitle,
        price: res.data.orderAmount || res.data.price || 0,
        orderStatus: res.data.orderStatus,
      };
    }
  } catch (_e: unknown) { /* 静默处理 */ }
};

onMounted(() => {
  fetchOrderInfo();
});
</script>

<style scoped src="../../styles/pages/user-dispute-management.css"></style>