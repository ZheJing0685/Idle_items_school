<template>
  <div class="publish-page">
    <div class="container">
      <!-- Header -->
      <div class="publish-header">
        <h2>{{ isEdit ? '编辑物品' : '发布闲置物品' }}</h2>
        <p>让你的闲置物品找到新主人，为绿色校园贡献力量</p>
      </div>

      <!-- Form -->
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="publish-form">
        <div class="publish-form-card">
        <!-- Image Upload -->
        <el-form-item label="物品图片">
          <div class="upload-previews" v-if="form.images.length > 0">
            <div
              v-for="(img, index) in form.images"
              :key="index"
              class="upload-preview"
              :style="{ background: getPreviewColor(index) }"
            >
              <span>{{ getPreviewIcon(index) }}</span>
              <button class="remove-btn" type="button" @click="removeImage(index)">×</button>
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
        </el-form-item>

        <!-- Title -->
        <el-form-item label="物品名称" prop="title">
          <el-input
            v-model="form.title"
            placeholder="简洁描述你的物品，如「MacBook Air M2 256G」"
            maxlength="100"
            show-word-limit
          />
          <div v-if="suggestedCategory" class="category-suggestion">
            <span class="suggestion-icon">💡</span>
            建议分类：
            <button class="suggestion-btn" @click="applySuggestion(suggestedCategory)">
              {{ suggestedCategory.icon }} {{ suggestedCategory.name }}
            </button>
            <button class="suggestion-dismiss" @click="suggestedCategory = null">×</button>
          </div>
        </el-form-item>

        <!-- Category & Condition -->
        <div class="form-row">
          <el-form-item label="分类" prop="categoryId" style="flex:1">
            <el-cascader
              v-model="cascaderPath"
              :options="categoryStore.categoryTree"
              :disabled="!!categoryStore.error"
              :props="{ label: 'name', value: 'id', children: 'children', expandTrigger: 'hover' as const }"
              :placeholder="categoryStore.error ? '分类暂不可用' : '选择分类 > 子分类'"
              style="width:100%"
              clearable
            />
            <div v-if="categoryStore.error" class="form-hint" style="color:var(--el-color-danger);margin-top:4px;">
              分类数据加载失败，请稍后重试
            </div>
          </el-form-item>
          <el-form-item label="新旧程度" style="flex:1">
            <div class="radio-group">
              <span
                v-for="option in conditionOptions"
                :key="option.value"
                class="radio-pill"
                :class="{ active: form.condition === option.value }"
                role="radio"
                tabindex="0"
                :aria-checked="form.condition === option.value"
                @click="form.condition = option.value"
                @keydown.enter="form.condition = option.value"
                @keydown.space.prevent="form.condition = option.value"
              >
                {{ option.label }}
              </span>
            </div>
          </el-form-item>
        </div>

        <!-- Price -->
        <el-form-item label="出售价格" prop="price">
          <el-input
            v-model.number="form.price"
            type="number"
            placeholder="输入价格（元）"
            style="max-width: 240px;"
          >
            <template #prefix>¥</template>
          </el-input>
          <div class="form-hint">建议参考原价的 30%–60%，合理定价更容易成交</div>
        </el-form-item>

        <!-- Description -->
        <el-form-item label="物品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="描述物品的使用情况、购买时间、功能状态等，越详细越容易卖出哦~"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <!-- Location -->
        <el-form-item label="交易地点" prop="location">
          <el-input v-model="form.location" placeholder="如：主校区图书馆附近、东区宿舍楼下" />
        </el-form-item>

        <!-- Contact -->
        <div class="form-row">
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="form.phone" placeholder="手机号" maxlength="11" />
          </el-form-item>
          <el-form-item label="微信" prop="wechat">
            <el-input v-model="form.wechat" placeholder="选填" />
          </el-form-item>
        </div>
        <el-form-item label="QQ" prop="qq">
          <el-input v-model="form.qq" placeholder="选填" />
        </el-form-item>

        <!-- Submit -->
        <el-form-item>
          <div class="publish-actions">
            <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
              {{ submitting ? '发布中...' : '发布物品' }}
            </el-button>
            <el-button size="large" @click="saveDraft">存为草稿</el-button>
          </div>
        </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import api from '../api';
import { useCategoryStore } from '../store/category';

const route = useRoute();
const router = useRouter();
const fileInput = ref<HTMLInputElement | null>(null);
const formRef = ref<FormInstance | null>(null);
const submitting = ref(false);
const categoryStore = useCategoryStore();

/** 级联选择器绑定路径（如 [1, 5] 表示 数码电子 > 手机） */
const cascaderPath = ref<number[]>([]);

const suggestedCategory = ref<{ id: number; name: string; icon: string } | null>(null);
let suggestionDebounceTimer: ReturnType<typeof setTimeout> | null = null;

/** 关键词 → 分类 ID 映射，用于标题自动建议分类 */
const categoryKeywordMap: Record<string, string> = {
  // 数码电子
  '手机': '数码电子', 'iphone': '数码电子', '华为': '数码电子', '小米': '数码电子',
  '电脑': '数码电子', '笔记本': '数码电子', 'macbook': '数码电子', '键盘': '数码电子',
  '耳机': '数码电子', '相机': '数码电子', '平板': '数码电子', 'ipad': '数码电子',
  '手表': '数码电子', '充电': '数码电子', '数据线': '数码电子', '显示器': '数码电子',
  '鼠标': '数码电子', '硬盘': '数码电子', 'u盘': '数码电子',
  // 教材书籍
  '教材': '教材书籍', '课本': '教材书籍', '书': '教材书籍', '资料': '教材书籍',
  '考研': '教材书籍', '四六级': '教材书籍', '英语': '教材书籍', '数学': '教材书籍',
  '小说': '教材书籍', '文学': '教材书籍', '专业书': '教材书籍',
  // 生活用品
  '台灯': '生活用品', '水杯': '生活用品', '雨伞': '生活用品', '收纳': '生活用品',
  '床': '生活用品', '被子': '生活用品', '枕头': '生活用品', '衣架': '生活用品',
  '风扇': '生活用品', '暖水袋': '生活用品', '洗衣': '生活用品',
  // 运动户外
  '球拍': '运动户外', '篮球': '运动户外', '足球': '运动户外', '羽毛球': '运动户外',
  '哑铃': '运动户外', '瑜伽': '运动户外', '跑步': '运动户外',
  // 服饰鞋包
  '衣服': '服饰鞋包', '外套': '服饰鞋包', '鞋子': '服饰鞋包', '包': '服饰鞋包',
  '帽子': '服饰鞋包', '围巾': '服饰鞋包',
  // 游戏动漫
  '游戏': '游戏动漫', 'switch': '游戏动漫', 'ps5': '游戏动漫', '手办': '游戏动漫',
  '漫画': '游戏动漫', '周边': '游戏动漫',
  // 美妆护肤
  '口红': '美妆护肤', '面霜': '美妆护肤', '精华': '美妆护肤', '防晒': '美妆护肤',
  '粉底': '美妆护肤', '面膜': '美妆护肤',
};

function matchCategoryByTitle(title: string): { id: number; name: string; icon: string } | null {
  const lower = title.toLowerCase();
  // 1) 精确关键词匹配
  for (const [keyword, parentName] of Object.entries(categoryKeywordMap)) {
    if (lower.includes(keyword)) {
      // 找 categoryStore.flatCategories 中 name 匹配的（优先 level=2 子分类）
      const matched = categoryStore.flatCategories.find(
        c => c.name === parentName || (c.level === 2 && c.name === parentName)
      );
      if (matched) {
        // 如果是子分类匹配到了，返回该子分类
        const cat = categoryStore.flatCategories.find(
          c => c.name === parentName && c.level >= 1
        );
        // 尝试找该父分类下匹配关键词的子分类
        if (matched.children?.length) {
          const sub = matched.children.find(
            (child: any) => lower.includes(child.name.toLowerCase())
          );
          if (sub) {
            return {
              id: sub.id,
              name: sub.name,
              icon: sub.icon || '📦',
            };
          }
        }
        return {
          id: matched.id,
          name: matched.name,
          icon: matched.icon || '📦',
        };
      }
      // 找不到精确匹配时，搜索所有分类名称中包含该词的
      const byName = categoryStore.flatCategories.find(
        c => c.name.includes(parentName) || parentName.includes(c.name)
      );
      if (byName) {
        return {
          id: byName.id,
          name: byName.name,
          icon: byName.icon || '📦',
        };
      }
    }
  }
  // 2) 模糊匹配分类名称（取第一个匹配的分类名中的词）
  const titleWords = lower.split(/[\s,，、/\-.]+/).filter(Boolean);
  for (const word of titleWords) {
    if (word.length < 1) continue;
    const byName = categoryStore.flatCategories.find(
      c => c.name.toLowerCase().includes(word) || 
           (c.level === 1 && c.name.includes('其他'))
    );
    if (byName) {
      // 跳过"其他"这类兜底分类，除非真的很像
      if (byName.name.includes('其他') && word.length < 3) continue;
      return {
        id: byName.id,
        name: byName.name,
        icon: byName.icon || '📦',
      };
    }
  }
  return null;
}

/** 应用分类建议：设置 cascaderPath 并清除建议 */
function applySuggestion(cat: { id: number; name: string; icon: string }) {
  const path = categoryStore.getCategoryPath(cat.id);
  cascaderPath.value = path.map(c => c.id);
  form.categoryId = cat.id;
  suggestedCategory.value = null;
}

const form = reactive({
  title: '',
  description: '',
  price: null as number | null,
  originalPrice: null as number | null,
  categoryId: null as number | null,
  condition: 'LIKE_NEW',
  location: '',
  phone: '',
  wechat: '',
  qq: '',
  images: [] as string[],
});

// 监听级联选择器变化，同步更新 form.categoryId（用于表单验证）
watch(cascaderPath, (path) => {
  form.categoryId = path.length > 0 ? path[path.length - 1] : null;
});

// 监听标题变化，自动建议分类
watch(() => form.title, (newVal) => {
  if (suggestionDebounceTimer) clearTimeout(suggestionDebounceTimer);
  if (!newVal || newVal.trim().length < 2) {
    suggestedCategory.value = null;
    return;
  }
  // 如果已手动选择了分类则不提示
  if (cascaderPath.value.length > 0) {
    suggestedCategory.value = null;
    return;
  }
  suggestionDebounceTimer = setTimeout(() => {
    suggestedCategory.value = matchCategoryByTitle(newVal);
  }, 400);
});

const conditionOptions = [
  { value: 'NEW', label: '全新' },
  { value: 'LIKE_NEW', label: '九五新' },
  { value: 'GOOD', label: '九成新' },
  { value: 'FAIR', label: '八成新' },
  { value: 'POOR', label: '七成新' },
];

const isEdit = computed(() => !!route.query.edit);

const rules: FormRules = {
  title: [
    { required: true, message: '请输入物品名称', trigger: 'blur' },
    { min: 2, max: 100, message: '物品名称长度应在 2 到 100 个字符之间', trigger: 'blur' },
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入有效的价格（最多两位小数）', trigger: 'blur' },
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' },
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入有效的手机号', trigger: 'blur' },
  ],
  description: [
    { max: 2000, message: '描述不能超过 2000 个字符', trigger: 'blur' },
  ],
};

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
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  try {
    submitting.value = true;

    // 从级联路径取最后一级作为 categoryId
    const categoryId = cascaderPath.value.length > 0
      ? cascaderPath.value[cascaderPath.value.length - 1]
      : form.categoryId;

    const payload = {
      ...form,
      categoryId,
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
        clearDraft();
        ElMessage.success('🎉 发布成功！你的物品已进入审核，预计 10 分钟内上架。');
        router.push('/user/items');
      }
    }
  } catch (error: unknown) {
    ElMessage.error(error instanceof Error ? error.message : '操作失败');
  } finally {
    submitting.value = false;
  }
};

const DRAFT_KEY = 'publish_draft';

const saveDraft = () => {
  const draft = {
    form: { ...form },
    cascaderPath: [...cascaderPath.value],
    savedAt: Date.now(),
  };
  localStorage.setItem(DRAFT_KEY, JSON.stringify(draft));
  ElMessage.success('草稿已保存');
  router.back();
};

const restoreDraft = () => {
  const raw = localStorage.getItem(DRAFT_KEY);
  if (!raw) return;
  try {
    const draft = JSON.parse(raw);
    Object.assign(form, draft.form);
    cascaderPath.value = draft.cascaderPath;
  } catch {
    localStorage.removeItem(DRAFT_KEY);
  }
};

const clearDraft = () => {
  localStorage.removeItem(DRAFT_KEY);
};

onMounted(async () => {
  categoryStore.fetchAll();

  // 检测草稿
  if (!isEdit.value) {
    const raw = localStorage.getItem(DRAFT_KEY);
    if (raw) {
      try {
        const draft = JSON.parse(raw);
        const elapsed = Date.now() - (draft.savedAt || 0);
        if (elapsed < 24 * 60 * 60 * 1000) {
          ElMessage.confirm('检测到未发布的草稿，是否恢复？', '恢复草稿', {
            confirmButtonText: '恢复',
            cancelButtonText: '放弃',
            type: 'info',
          }).then(() => {
            restoreDraft();
          }).catch(() => {
            clearDraft();
          });
        } else {
          clearDraft();
        }
      } catch {
        clearDraft();
      }
    }
  }

  if (isEdit.value) {
    try {
      const response = await api.item.getItem(route.query.edit as string);
      const item = response.data;
      form.title = item.title ?? '';
      form.description = item.description ?? '';
      form.price = item.price ?? null;
      form.originalPrice = item.originalPrice ?? null;
      form.categoryId = item.categoryId ?? null;
      form.condition = item.condition ?? 'LIKE_NEW';
      form.location = item.location ?? '';
      form.phone = item.phone ?? '';
      form.wechat = item.wechat ?? '';
      form.qq = item.qq ?? '';
      form.images = item.images || [];

      // 根据 categoryId 反推级联路径
      if (item.categoryId) {
        const path = categoryStore.getCategoryPath(item.categoryId);
        cascaderPath.value = path.map(c => c.id);
      }
    } catch (error) {
      ElMessage.error('获取物品信息失败');
    }
  }
});
</script>

<style src="../styles/pages/publish.css"></style>
