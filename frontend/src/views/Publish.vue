<template>
  <div class="publish-page">
    <div class="page-header">
      <div class="container">
        <div class="header-content">
          <h1 class="page-title">{{ isEdit ? '编辑物品' : '发布闲置' }}</h1>
          <p class="page-subtitle">
            {{ isEdit ? '修改你的物品信息' : '让闲置找到新主人，变废为宝' }}
          </p>
        </div>
      </div>
    </div>

    <div class="container">
      <div class="publish-layout">
        <div class="publish-main">
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            class="publish-form"
          >
            <div class="form-section">
              <h3 class="section-title">
                <Image :size="20" />
                物品图片
              </h3>
              <div class="image-upload-area">
                <div class="uploaded-images" v-if="form.images.length > 0">
                  <div
                    v-for="(img, index) in form.images"
                    :key="index"
                    class="uploaded-image"
                  >
                    <img :src="img" alt="上传图片" />
                    <button class="remove-btn" @click="removeImage(index)" aria-label="删除图片">
                      <X :size="16" />
                    </button>
                    <span class="cover-badge" v-if="index === 0">封面</span>
                  </div>
                </div>
                <div
                  class="upload-trigger"
                  @click="triggerUpload"
                  v-if="form.images.length < 9"
                >
                  <input
                    type="file"
                    ref="fileInput"
                    accept="image/*"
                    multiple
                    @change="handleFileChange"
                    style="display: none"
                  />
                  <div class="upload-content">
                    <Image :size="48" color="var(--text-muted)" stroke-width="1.5" />
                    <p class="upload-text">点击上传图片</p>
                    <p class="upload-hint">支持 JPG、PNG、WebP，最多9张</p>
                  </div>
                </div>
              </div>
              <p class="image-tip" v-if="form.images.length === 0">
                请至少上传一张图片，第一张将作为封面图
              </p>
            </div>

            <div class="form-section">
              <h3 class="section-title">
                <Edit3 :size="20" />
                物品信息
              </h3>

              <el-form-item label="物品标题" prop="title" class="form-item">
                <el-input
                  v-model="form.title"
                  placeholder="简洁明了的标题能让买家快速了解你的物品"
                  maxlength="60"
                  show-word-limit
                  size="large"
                />
              </el-form-item>

              <div class="form-row">
                <el-form-item label="分类" prop="categoryId" class="form-item">
                  <el-cascader
                    v-model="form.categoryId"
                    :options="categoryTreeOptions"
                    :props="{
                      value: 'id',
                      label: 'name',
                      children: 'children',
                      emitPath: false,
                    }"
                    placeholder="选择分类"
                    size="large"
                    class="form-select"
                  />
                </el-form-item>

                <el-form-item label="成色" prop="condition" class="form-item">
                  <el-select
                    v-model="form.condition"
                    placeholder="选择成色"
                    size="large"
                    class="form-select"
                  >
                    <el-option
                      v-for="option in conditionOptions"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    />
                  </el-select>
                </el-form-item>
              </div>

              <div class="form-row">
                <el-form-item
                  label="配送方式"
                  prop="deliveryMethod"
                  class="form-item"
                >
                  <el-select
                    v-model="form.deliveryMethod"
                    placeholder="选择配送方式"
                    size="large"
                    class="form-select"
                  >
                    <el-option
                      v-for="option in deliveryMethodOptions"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    />
                  </el-select>
                </el-form-item>

                <el-form-item
                  label="联系方式"
                  prop="contactType"
                  class="form-item"
                >
                  <el-select
                    v-model="form.contactType"
                    placeholder="选择联系方式"
                    size="large"
                    class="form-select"
                  >
                    <el-option
                      v-for="option in contactTypeOptions"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    />
                  </el-select>
                </el-form-item>
              </div>

              <div class="form-row">
                <el-form-item label="是否允许议价" class="form-item">
                  <el-switch
                    v-model="form.isBargainAllowed"
                    active-text="是"
                    inactive-text="否"
                  />
                </el-form-item>

                <el-form-item label="最低接受价格（选填）" class="form-item">
                  <el-input
                    v-model.number="form.minPrice"
                    type="number"
                    placeholder="输入最低接受价格"
                    size="large"
                    class="price-input"
                  >
                    <template #prefix>
                      <span class="input-prefix">¥</span>
                    </template>
                  </el-input>
                </el-form-item>
              </div>

              <div class="form-row">
                <el-form-item label="品牌（选填）" class="form-item">
                  <el-input
                    v-model="form.brand"
                    placeholder="输入品牌名称"
                    size="large"
                  />
                </el-form-item>

                <el-form-item label="购买日期（选填）" class="form-item">
                  <el-date-picker
                    v-model="form.purchaseDate"
                    type="date"
                    placeholder="选择购买日期"
                    size="large"
                    class="form-date"
                  />
                </el-form-item>
              </div>

              <el-form-item label="商品标签（选填）" class="form-item">
                <el-tag
                  v-for="(tag, index) in form.tags"
                  :key="index"
                  closable
                  @close="form.tags.splice(index, 1)"
                  class="tag-item"
                >
                  {{ tag }}
                </el-tag>
                <el-input
                  v-model="tagInput"
                  placeholder="输入标签后按回车添加"
                  @keyup.enter="addTag"
                  size="large"
                  class="tag-input"
                />
              </el-form-item>

              <el-form-item label="保修信息（选填）" class="form-item">
                <el-input
                  v-model="form.warrantyInfo"
                  type="textarea"
                  :rows="3"
                  placeholder="输入保修信息"
                />
              </el-form-item>

              <div class="form-row">
                <el-form-item label="出售价格" prop="price" class="form-item">
                  <el-input
                    v-model.number="form.price"
                    type="number"
                    placeholder="输入价格"
                    size="large"
                    class="price-input"
                  >
                    <template #prefix>
                      <span class="input-prefix">¥</span>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item label="原价（选填）" class="form-item">
                  <el-input
                    v-model.number="form.originalPrice"
                    type="number"
                    placeholder="输入原价"
                    size="large"
                    class="price-input"
                  >
                    <template #prefix>
                      <span class="input-prefix">¥</span>
                    </template>
                  </el-input>
                </el-form-item>
              </div>

              <el-form-item label="交易地点" prop="location" class="form-item">
                <el-input
                  v-model="form.location"
                  placeholder="如：校园南门、宿舍楼下等"
                  size="large"
                >
                  <template #prefix>
                    <MapPin :size="18" />
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item
                label="物品描述"
                prop="description"
                class="form-item"
              >
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="5"
                  placeholder="详细描述物品的品牌型号、入手渠道、使用感受等，让买家更了解你的物品"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </div>

            <div class="form-section">
              <h3 class="section-title">
                <ClipboardList :size="20" />
                联系方式
              </h3>

              <div class="form-row">
                <el-form-item
                  label="联系人"
                  prop="contactName"
                  class="form-item"
                >
                  <el-input
                    v-model="form.contactName"
                    placeholder="你的称呼"
                    size="large"
                  />
                </el-form-item>

                <el-form-item
                  label="联系电话"
                  prop="contactPhone"
                  class="form-item"
                >
                  <el-input
                    v-model="form.contactPhone"
                    placeholder="手机号码"
                    size="large"
                  />
                </el-form-item>
              </div>

              <!-- 根据联系方式类型动态显示对应的输入框 -->
              <div class="form-row" v-if="form.contactType === '2'">
                <el-form-item
                  label="微信号"
                  prop="contactInfo"
                  class="form-item"
                >
                  <el-input
                    v-model="form.contactInfo"
                    placeholder="请输入微信号"
                    size="large"
                  />
                </el-form-item>
              </div>

              <div class="form-row" v-if="form.contactType === '3'">
                <el-form-item label="QQ号" prop="contactInfo" class="form-item">
                  <el-input
                    v-model="form.contactInfo"
                    placeholder="请输入QQ号"
                    size="large"
                  />
                </el-form-item>
              </div>

              <div class="form-row" v-if="form.contactType === '1'">
                <el-form-item label="平台内联系方式" class="form-item">
                  <el-input disabled value="通过平台内消息联系" size="large" />
                </el-form-item>
              </div>
            </div>

            <div class="form-actions">
              <el-button
                size="large"
                @click="$router.back()"
                class="cancel-btn"
              >
                取消
              </el-button>
              <el-button
                size="large"
                type="primary"
                :loading="submitting"
                @click="handleSubmit"
                class="submit-btn"
              >
                {{
                  submitting ? '发布中...' : isEdit ? '保存修改' : '立即发布'
                }}
              </el-button>
            </div>
          </el-form>
        </div>

        <aside class="publish-sidebar">
          <div class="sidebar-card">
              <h4 class="card-title">
                <Smile :size="18" color="var(--secondary-color)" />
                发布小贴士
              </h4>
            <ul class="tips-list">
              <li>清晰真实的图片能提高成交率</li>
              <li>详细描述物品的品牌型号和使用情况</li>
              <li>合理定价，参考同类商品价格</li>
              <li>选择方便的交易地点</li>
              <li>保持联系方式畅通</li>
            </ul>
          </div>

        </aside>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElForm } from 'element-plus';
import api from '../api';
import { itemRules } from '../utils/validator';
import { useDictStore } from '../store/dict.js';
import { Image, X, Edit3, MapPin, ClipboardList, Smile } from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();
const formRef = ref<InstanceType<typeof ElForm> | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const submitting = ref(false);
const categoryTreeOptions = ref<any[]>([]);
const dictStore = useDictStore();

const isEdit = computed(() => !!route.query.edit);

const form = reactive({
  title: '',
  description: '',
  price: null,
  originalPrice: null,
  minPrice: null,
  categoryId: null as number | null,
  condition: null,
  deliveryMethod: null,
  contactType: null,
  isBargainAllowed: true,
  location: '',
  brand: '',
  purchaseDate: null,
  warrantyInfo: '',
  tags: [] as string[],
  contactName: '',
  contactPhone: '',
  contactInfo: '',
  images: [] as string[],
});

const tagInput = ref('');

// 获取字典选项
const conditionOptions = computed(() => {
  const options = dictStore.getDictOptions('ITEM_CONDITION');
  if (options.length > 0) return options;
  return [
    { value: 'NEW', label: '全新' },
    { value: 'LIKE_NEW', label: '九成新' },
    { value: 'GOOD', label: '八成新' },
    { value: 'FAIR', label: '七成新' },
    { value: 'POOR', label: '六成新及以下' },
  ];
});
const deliveryMethodOptions = computed(() =>
  dictStore.getDictOptions('DELIVERY_METHOD')
);
const contactTypeOptions = computed(() =>
  dictStore.getDictOptions('CONTACT_TYPE')
);

const validateContactInfo = (rule: any, value: string, callback: any) => {
  if (form.contactType === '2' && !value) {
    callback(new Error('请输入微信号'));
  } else if (form.contactType === '3' && !value) {
    callback(new Error('请输入QQ号'));
  } else {
    callback();
  }
};

const rules = {
  ...itemRules,
  condition: [{ required: true, message: '请选择成色', trigger: 'change' }],
  deliveryMethod: [
    { required: true, message: '请选择配送方式', trigger: 'change' },
  ],
  contactType: [
    { required: true, message: '请选择联系方式', trigger: 'change' },
  ],
  location: [{ required: true, message: '请输入交易地点', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  contactInfo: [{ validator: validateContactInfo, trigger: 'blur' }],
};

const loadCategories = async () => {
  try {
    const response = await api.category.getCategoryTree();
    if (response.code === 200) {
      categoryTreeOptions.value = response.data;
    }
  } catch (error) {
    console.error('获取分类失败', error);
  }
};

const loadItemForEdit = async () => {
  if (!isEdit.value) return;
  try {
    const response = await api.item.getItem(route.query.edit as string);
    const item: any = response.data;
    form.title = item.title;
    form.description = item.description;
    form.price = item.price;
    form.originalPrice = item.originalPrice;
    form.minPrice = item.minPrice;
    form.categoryId = item.categoryId;
    form.condition = item.condition;
    form.deliveryMethod = item.deliveryMethod;
    form.contactType = item.contactType;
    form.isBargainAllowed = item.isBargainAllowed;
    form.location = item.location;
    form.brand = item.brand;
    form.purchaseDate = item.purchaseDate;
    form.warrantyInfo = item.warrantyInfo;
    form.tags = item.tags || [];
    form.contactName = item.contactName;
    form.contactPhone = item.contactPhone;
    form.contactInfo = item.contactInfo || '';
    form.images = item.images || [];
  } catch (error) {
    ElMessage.error('获取物品信息失败');
  }
};

const triggerUpload = () => {
  fileInput.value?.click();
};

const handleFileChange = async (e: Event) => {
  const target = e.target as HTMLInputElement;
  const files = Array.from(target.files || []);
  if (files.length === 0) return;

  const remainingSlots = 9 - form.images.length;
  const filesToUpload = files.slice(0, remainingSlots);

  const uploadTasks = filesToUpload
    .filter(file => {
      if (!file.type.startsWith('image/')) {
        ElMessage.warning('只能上传图片文件');
        return false;
      }
      if (file.size > 5 * 1024 * 1024) {
        ElMessage.warning('图片大小不能超过5MB');
        return false;
      }
      return true;
    })
    .map(async (file) => {
      const formData = new FormData();
      formData.append('file', file);
      const response = await api.item.uploadImage(formData);
      if (response.code === 200) {
        form.images.push(response.data.url);
      }
      return response;
    });

  const results = await Promise.allSettled(uploadTasks);
  const failed = results.filter(r => r.status === 'rejected').length;
  if (failed > 0) {
    ElMessage.warning(`${failed} 张图片上传失败`);
  }

  (e.target as HTMLInputElement).value = '';
};

const removeImage = (index: number) => {
  form.images.splice(index, 1);
};

const addTag = () => {
  if (
    tagInput.value &&
    !form.tags.includes(tagInput.value) &&
    form.tags.length < 5
  ) {
    form.tags.push(tagInput.value);
    tagInput.value = '';
  }
};

const handleSubmit = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
    submitting.value = true;

    const payload = {
      ...form,
      tags: form.tags,
    };

    if (isEdit.value) {
      const response = await api.item.updateItem(route.query.edit as string, payload as any);
      if (response.code === 200) {
        ElMessage.success('修改成功');
        router.push('/user/items');
      } else {
        ElMessage.error(response.message || '修改失败');
      }
    } else {
      const response = await api.item.createItem(payload as any);
      if (response.code === 200) {
        ElMessage.success('发布成功');
        router.push('/user/items');
      } else {
        ElMessage.error(response.message || '发布失败');
      }
    }
  } catch (error) {
    if (error.errors) {
      ElMessage.error('请完善表单信息');
    } else {
      ElMessage.error(error.message || '操作失败');
    }
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  // 加载字典数据
  await dictStore.preloadCommonDicts();
  await loadCategories();
  await loadItemForEdit();
});
</script>

<style scoped src="../styles/pages/publish.css"></style>
