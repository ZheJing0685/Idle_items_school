<template>
  <div class="publish-page">
    <div class="container">
      <!-- Header -->
      <div class="publish-header">
        <h2>{{ isEdit ? '编辑物品' : '发布闲置物品' }}</h2>
        <p>让你的闲置物品找到新主人，为绿色校园贡献力量</p>
      </div>

      <!-- Form -->
      <div class="publish-form">
        <!-- Image Upload -->
        <div class="form-group">
          <label class="form-label">物品图片</label>
          <div class="upload-previews" v-if="form.images.length > 0">
            <div
              v-for="(img, index) in form.images"
              :key="index"
              class="upload-preview"
              :style="{ background: getPreviewColor(index) }"
            >
              <span>{{ getPreviewIcon(index) }}</span>
              <button class="remove-btn" @click="removeImage(index)">×</button>
            </div>
          </div>
          <div class="upload-zone" @click="triggerUpload" v-if="form.images.length < 9">
            <input
              type="file"
              ref="fileInput"
              accept="image/*"
              multiple
              @change="handleFileChange"
              style="display: none"
            />
            <div class="upload-zone-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <rect x="3" y="3" width="18" height="18" rx="2" />
                <circle cx="8.5" cy="8.5" r="1.5" />
                <path d="M21 15l-5-5L5 21" />
              </svg>
            </div>
            <div class="upload-zone-title">点击或拖拽上传图片</div>
            <div class="upload-zone-hint">支持 JPG、PNG，最多 9 张</div>
          </div>
        </div>

        <!-- Title -->
        <div class="form-group">
          <label class="form-label">物品名称</label>
          <input
            class="form-input"
            type="text"
            v-model="form.title"
            placeholder="简洁描述你的物品，如「MacBook Air M2 256G」"
          />
        </div>

        <!-- Category & Condition -->
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">分类</label>
            <select class="form-select" v-model="form.categoryId">
              <option value="">选择分类</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">新旧程度</label>
            <div class="radio-group">
              <span
                v-for="option in conditionOptions"
                :key="option.value"
                class="radio-pill"
                :class="{ active: form.condition === option.value }"
                @click="form.condition = option.value"
              >
                {{ option.label }}
              </span>
            </div>
          </div>
        </div>

        <!-- Price Type Toggle -->
        <div class="form-group">
          <label class="form-label">交易方式</label>
          <div class="toggle-group">
            <div
              class="toggle-option"
              :class="{ active: priceType === 'sell' }"
              @click="priceType = 'sell'"
            >
              💰 出售
            </div>
            <div
              class="toggle-option"
              :class="{ active: priceType === 'exchange' }"
              @click="priceType = 'exchange'"
            >
              ♻️ 交换
            </div>
            <div
              class="toggle-option"
              :class="{ active: priceType === 'free' }"
              @click="priceType = 'free'"
            >
              🎁 免费送
            </div>
          </div>
        </div>

        <!-- Price -->
        <div class="form-group" v-if="priceType !== 'free'">
          <label class="form-label">{{ priceType === 'exchange' ? '期望交换物品' : '出售价格' }}</label>
          <input
            class="form-input"
            type="number"
            v-model.number="form.price"
            :placeholder="priceType === 'exchange' ? '描述你想交换的物品' : '输入价格（元）'"
            style="max-width: 240px;"
          />
          <div class="form-hint" v-if="priceType === 'sell'">建议参考原价的 30%–60%，合理定价更容易成交</div>
        </div>

        <!-- Description -->
        <div class="form-group">
          <label class="form-label">物品描述</label>
          <textarea
            class="form-textarea"
            v-model="form.description"
            placeholder="描述物品的使用情况、购买时间、功能状态等，越详细越容易卖出哦~"
          ></textarea>
        </div>

        <!-- Location -->
        <div class="form-group">
          <label class="form-label">交易地点</label>
          <select class="form-select" v-model="form.location">
            <option value="">选择校区</option>
            <option value="主校区 — 图书馆附近">主校区 — 图书馆附近</option>
            <option value="主校区 — 食堂门口">主校区 — 食堂门口</option>
            <option value="东校区 — 宿舍楼下">东校区 — 宿舍楼下</option>
            <option value="南校区">南校区</option>
            <option value="其他">其他（自行填写）</option>
          </select>
        </div>

        <!-- Submit -->
        <div class="publish-actions">
          <button class="btn btn-primary btn-lg" @click="handleSubmit" :disabled="submitting">
            {{ submitting ? '发布中...' : '发布物品' }}
          </button>
          <button class="btn btn-secondary btn-lg" @click="$router.back()">
            存为草稿
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '../api';

const route = useRoute();
const router = useRouter();
const fileInput = ref<HTMLInputElement | null>(null);
const submitting = ref(false);
const priceType = ref('sell');

const categories = ref([
  { id: 1, name: '数码电子' },
  { id: 2, name: '教材书籍' },
  { id: 3, name: '生活用品' },
  { id: 4, name: '服饰鞋包' },
  { id: 5, name: '运动户外' },
  { id: 6, name: '家具家电' },
  { id: 7, name: '其他' },
]);

const conditionOptions = [
  { value: 'NEW', label: '全新' },
  { value: 'LIKE_NEW', label: '九五新' },
  { value: 'GOOD', label: '九成新' },
  { value: 'FAIR', label: '八成新' },
  { value: 'POOR', label: '七成新' },
];

const isEdit = computed(() => !!route.query.edit);

const form = reactive({
  title: '',
  description: '',
  price: null as number | null,
  originalPrice: null as number | null,
  categoryId: null as number | null,
  condition: 'LIKE_NEW',
  location: '',
  images: [] as string[],
});

const previewColors = ['#dce8f7', '#f5edd6', '#d8f0e0', '#e8d8f0', '#f0e0d0'];
const previewIcons = ['📷', '🖼️', '📸', '🎨', '✏️'];

const getPreviewColor = (index: number) => previewColors[index % previewColors.length];
const getPreviewIcon = (index: number) => previewIcons[index % previewIcons.length];

const triggerUpload = () => {
  fileInput.value?.click();
};

const handleFileChange = async (e: Event) => {
  const target = e.target as HTMLInputElement;
  const files = Array.from(target.files || []);
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
      }
    } catch (error) {
      ElMessage.warning('图片上传失败');
    }
  }
  (e.target as HTMLInputElement).value = '';
};

const removeImage = (index: number) => {
  form.images.splice(index, 1);
};

const handleSubmit = async () => {
  if (!form.title) {
    ElMessage.warning('请输入物品名称');
    return;
  }
  if (!form.categoryId) {
    ElMessage.warning('请选择分类');
    return;
  }
  if (priceType.value === 'sell' && !form.price) {
    ElMessage.warning('请输入价格');
    return;
  }

  try {
    submitting.value = true;
    const payload = {
      ...form,
      price: priceType.value === 'free' ? 0 : form.price,
    };

    if (isEdit.value) {
      const response = await api.item.updateItem(route.query.edit as string, payload as any);
      if (response.code === 200) {
        ElMessage.success('修改成功');
        router.push('/user/items');
      }
    } else {
      const response = await api.item.createItem(payload as any);
      if (response.code === 200) {
        ElMessage.success('🎉 发布成功！你的物品已进入审核，预计 10 分钟内上架。');
        router.push('/user/items');
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败');
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  if (isEdit.value) {
    try {
      const response = await api.item.getItem(route.query.edit as string);
      const item = response.data;
      form.title = item.title;
      form.description = item.description;
      form.price = item.price;
      form.originalPrice = item.originalPrice;
      form.categoryId = item.categoryId;
      form.condition = item.condition;
      form.location = item.location;
      form.images = item.images || [];
    } catch (error) {
      ElMessage.error('获取物品信息失败');
    }
  }
});
</script>

<style scoped src="../styles/pages/publish.css"></style>
