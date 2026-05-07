<template>
  <div class="feedback-page">
    <div class="page-header">
      <h1 class="page-title">分类反馈</h1>
      <p class="page-subtitle">帮助我们改进分类体系，你的反馈很重要</p>
    </div>

    <div class="feedback-form-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="feedback-form"
      >
        <el-form-item label="反馈类型" prop="feedbackType">
          <el-radio-group v-model="form.feedbackType" class="type-radio-group">
            <el-radio-button value="INVALID">分类无效</el-radio-button>
            <el-radio-button value="MISSING">缺少分类</el-radio-button>
            <el-radio-button value="OTHER">其他问题</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="关联分类（可选）">
          <el-cascader
            v-model="form.categoryId"
            :options="categoryTreeOptions"
            :props="{ value: 'id', label: 'name', children: 'children', emitPath: false }"
            placeholder="选择相关分类"
            clearable
            class="form-cascader"
          />
        </el-form-item>

        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="请详细描述你遇到的问题或建议..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button
              type="primary"
              size="large"
              :loading="submitting"
              @click="handleSubmit"
              class="submit-btn"
            >
              提交反馈
            </el-button>
            <el-button size="large" @click="router.push('/user/feedback')" class="view-btn">
              查看我的反馈
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '../api';

const router = useRouter();
const formRef = ref(null);
const submitting = ref(false);
const categoryTreeOptions = ref([]);

const form = reactive({
  feedbackType: 'INVALID',
  categoryId: null,
  description: '',
});

const rules = {
  feedbackType: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
  description: [
    { required: true, message: '请填写描述', trigger: 'blur' },
    { min: 5, message: '描述至少5个字符', trigger: 'blur' },
  ],
};

const loadCategories = async () => {
  try {
    const response = await api.category.getCategoryTree();
    if (response.code === 200) {
      categoryTreeOptions.value = response.data || [];
    }
  } catch {
    // ignore
  }
};

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    const data = {
      feedbackType: form.feedbackType,
      categoryId: form.categoryId || null,
      description: form.description,
    };
    const res = await api.category.submitFeedback(data);
    if (res.code === 200) {
      ElMessage.success('反馈提交成功');
      form.feedbackType = 'INVALID';
      form.categoryId = null;
      form.description = '';
      formRef.value.resetFields();
    } else {
      ElMessage.error(res.message || '提交失败');
    }
  } catch {
    ElMessage.error('提交失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  loadCategories();
});
</script>

<style scoped>
.feedback-page {
  max-width: 720px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-4);
}

.page-header {
  text-align: center;
  margin-bottom: var(--space-8);
}

.page-title {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
}

.page-subtitle {
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin: 0;
}

.feedback-form-card {
  background: var(--bg-surface);
  border-radius: var(--radius-xl);
  padding: var(--space-8);
  box-shadow: var(--shadow-sm);
}

.type-radio-group {
  display: flex;
  gap: 0;
}

.type-radio-group :deep(.el-radio-button__inner) {
  border-radius: 0;
}

.type-radio-group :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: var(--radius-lg) 0 0 var(--radius-lg);
}

.type-radio-group :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 var(--radius-lg) var(--radius-lg) 0;
}

.form-cascader {
  width: 100%;
}

.form-actions {
  display: flex;
  gap: var(--space-3);
  width: 100%;
}

.submit-btn {
  flex: 1;
  height: 48px;
  border-radius: var(--radius-lg);
  font-size: var(--text-base);
  font-weight: 600;
}

.view-btn {
  height: 48px;
  border-radius: var(--radius-lg);
  font-size: var(--text-base);
}
</style>
