<template>
  <div class="notifications-page">
    <PageHeader title="消息通知" subtitle="查看系统通知和订单消息">
      <template #action>
        <el-button v-if="notifications.length > 0" type="primary" link @click="markAllAsRead">
          全部已读
        </el-button>
      </template>
    </PageHeader>

    <div class="notifications-list">
      <NotificationCard
        v-for="item in notifications"
        :key="item.id"
        :id="item.id"
        :type="item.notificationType"
        :title="item.title"
        :content="item.content"
        :time="formatTime(item.createdAt)"
        :isRead="item.isRead"
        @click="handleNotification(item)"
        @read="markAsRead(item.id)"
      />

      <EmptyState v-if="notifications.length === 0 && !loading" title="暂无通知" description="新的通知会显示在这里">
        <template #action>
          <el-button type="primary" @click="$router.push('/')">去浏览物品</el-button>
        </template>
      </EmptyState>
    </div>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadNotifications"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import notificationApi from '@/api/services/notification';
import { wsService } from '@/utils/websocket';
import { useUserStore } from '@/store';
import PageHeader from '@/components/user/PageHeader.vue';
import NotificationCard from '@/components/user/NotificationCard.vue';
import EmptyState from '@/components/user/EmptyState.vue';

const router = useRouter();
const userStore = useUserStore();

const notifications = ref<any[]>([]);
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);
const loading = ref(false);

const loadNotifications = async () => {
  loading.value = true;
  try {
    const res = await notificationApi.getNotifications({
      page: currentPage.value,
      size: pageSize.value
    });
    notifications.value = res.data.content || [];
    total.value = res.data.totalElements || 0;
  } catch (error) {
    console.error('加载通知失败:', error);
  } finally {
    loading.value = false;
  }
};

const markAsRead = async (id: string) => {
  try {
    await notificationApi.markAsRead(id);
    const item = notifications.value.find(n => n.id === id);
    if (item) item.isRead = true;
  } catch (error) {
    console.error('标记已读失败:', error);
  }
};

const markAllAsRead = async () => {
  try {
    await notificationApi.markAllAsRead();
    notifications.value.forEach(n => n.isRead = true);
    ElMessage.success('已全部标记为已读');
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

const handleNotification = (item: any) => {
  if (!item.isRead) markAsRead(item.id);
  if (item.relatedType === 'ORDER' && item.relatedId) {
    router.push('/user/orders');
  }
};

const formatTime = (time: string) => {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
  return date.toLocaleDateString();
};

const handleNewNotification = (notification: any) => {
  if (notifications.value.some(n => n.id === notification.id)) return;
  notifications.value.unshift(notification);
  total.value++;
};

onMounted(() => {
  loadNotifications();
  wsService.onMessage('notification', handleNewNotification);

  const userId = userStore.user?.id;
  if (userId) {
    // WebSocket 使用 access_token cookie 认证
    wsService.connect('', String(userId)).catch((err) => {
      console.error('WebSocket连接失败:', err);
    });
  }
});

onUnmounted(() => {
  wsService.disconnect();
});
</script>

<style scoped src="../../styles/pages/notifications.css"></style>
