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
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                  <circle cx="8.5" cy="8.5" r="1.5" />
                  <path d="M21 15L16 10L5 21" />
                </svg>
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
                    <button class="remove-btn" @click="removeImage(index)">
                      <svg
                        width="16"
                        height="16"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                      >
                        <line x1="18" y1="6" x2="6" y2="18" />
                        <line x1="6" y1="6" x2="18" y2="18" />
                      </svg>
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
                    <svg
                      width="48"
                      height="48"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="var(--text-muted)"
                      stroke-width="1.5"
                    >
                      <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                      <circle cx="8.5" cy="8.5" r="1.5" />
                      <path d="M21 15L16 10L5 21" />
                    </svg>
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
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path
                    d="M11 4H4C2.89543 4 2 4.89543 2 6V20C2 21.1046 2.89543 22 4 22H18C19.1046 22 20 21.1046 20 20V13"
                  />
                  <path
                    d="M18.5 2.5C19.3284 1.67157 20.6716 1.67157 21.5 2.5C22.3284 3.32843 22.3284 4.67157 21.5 5.5L12 15L8 16L9 12L18.5 2.5Z"
                  />
                </svg>
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
                    :props="{ value: 'id', label: 'name', children: 'children', emitPath: false }"
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
                    <el-option label="全新" value="NEW" />
                    <el-option label="九成新" value="LIKE_NEW" />
                    <el-option label="八成新" value="GOOD" />
                    <el-option label="七成新" value="FAIR" />
                    <el-option label="六成新及以下" value="POOR" />
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
                    <el-option label="自提" value="1" />
                    <el-option label="快递" value="2" />
                    <el-option label="两者皆可" value="3" />
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
                    <el-option label="平台内" value="1" />
                    <el-option label="微信" value="2" />
                    <el-option label="QQ" value="3" />
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
                    <svg
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    >
                      <path
                        d="M21 10C21 17 12 23 12 23C12 23 3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z"
                      />
                      <circle cx="12" cy="10" r="3" />
                    </svg>
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
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path
                    d="M4 4H16C16.5304 4 17.0391 4.21071 17.4142 4.58579C17.7893 4.96086 18 5.46957 18 6V18C18 18.5304 17.7893 19.0391 17.4142 19.4142C17.0391 19.7893 16.5304 20 16 20H4C3.46957 20 2.96086 19.7893 2.58579 19.4142C2.21071 19.0391 2 18.5304 2 18V6C2 5.46957 2.21071 4.96086 2.58579 4.58579C2.96086 4.21071 3.46957 4 4 4Z"
                  />
                  <path d="M2 10H18" />
                </svg>
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
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="var(--secondary-color)"
                stroke-width="2"
              >
                <path
                  d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z"
                />
                <path
                  d="M7 13C7 13 8 15 12 15C16 15 17 13 17 13"
                  stroke="white"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
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

          <div class="sidebar-card eco-card">
            <div class="eco-icon">
              <svg
                width="32"
                height="32"
                viewBox="0 0 24 24"
                fill="var(--secondary-color)"
              >
                <path
                  d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z"
                />
                <path
                  d="M7 13C7 13 8 15 12 15C16 15 17 13 17 13"
                  stroke="white"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
            </div>
            <div class="eco-content">
              <h4>环保交易</h4>
              <p>每完成一笔交易，都在为绿色校园贡献力量</p>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '../api';
import { itemRules } from '../utils/validator';

const route = useRoute();
const router = useRouter();
const formRef = ref();
const fileInput = ref();
const submitting = ref(false);
const categoryTreeOptions = ref([]);

const isEdit = computed(() => !!route.query.edit);

const form = reactive({
  title: '',
  description: '',
  price: null,
  originalPrice: null,
  minPrice: null,
  categoryId: null,
  condition: 'GOOD',
  deliveryMethod: null,
  contactType: null,
  isBargainAllowed: true,
  location: '',
  brand: '',
  purchaseDate: null,
  warrantyInfo: '',
  tags: [],
  contactName: '',
  contactPhone: '',
  images: [],
});

const tagInput = ref('');

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
    const response = await api.item.getItem(route.query.edit);
    const item = response.data;
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
    form.images = item.images || [];
  } catch (error) {
    ElMessage.error('获取物品信息失败');
  }
};

const triggerUpload = () => {
  fileInput.value.click();
};

const handleFileChange = async (e) => {
  const files = Array.from(e.target.files);
  if (files.length === 0) return;

  const remainingSlots = 9 - form.images.length;
  const filesToUpload = files.slice(0, remainingSlots);

  for (const file of filesToUpload) {
    if (!file.type.startsWith('image/')) {
      ElMessage.warning('只能上传图片文件');
      continue;
    }
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.warning('图片大小不能超过5MB');
      continue;
    }

    try {
      const formData = new FormData();
      formData.append('file', file);
      const response = await api.item.uploadImage(formData);
      if (response.code === 200) {
        form.images.push(response.data.url);
        ElMessage.success('图片上传成功');
      }
    } catch (error) {
      ElMessage.error('图片上传失败');
    }
  }

  e.target.value = '';
};

const removeImage = (index) => {
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
      tags: form.tags.join(','),
    };

    if (isEdit.value) {
      const response = await api.item.updateItem(route.query.edit, payload);
      if (response.code === 200) {
        ElMessage.success('修改成功');
        router.push('/user/items');
      } else {
        ElMessage.error(response.message || '修改失败');
      }
    } else {
      const response = await api.item.createItem(payload);
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
  await loadCategories();
  await loadItemForEdit();
});
</script>

<style scoped src="../styles/pages/publish.css"></style>
