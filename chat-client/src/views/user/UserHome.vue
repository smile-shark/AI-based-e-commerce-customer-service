<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUserSessionList } from '@/api/sessionApi'
import type { SessionItem } from '@/api/sessionApi'

const router = useRouter()

// 数据
const sessionList = ref<SessionItem[]>([])

// 获取商品名称（暂时用ID代替）
const getGoodsName = (goodsId: number) => {
  return `商品${goodsId}`
}

// 格式化时间
const formatTime = (timestamp: string) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

// 加载会话列表
const loadSessionList = async () => {
  try {
    const userId = localStorage.getItem('userId')
    if (!userId) {
      router.push('/user/login')
      return
    }

    const response = await getUserSessionList(parseInt(userId))
    if (response.code === 200) {
      sessionList.value = response.data.slice(0, 5) // 只显示最近5个会话
    }
  } catch (error) {
    console.error('Failed to load session list:', error)
  }
}

// 导航到商品列表
const goToGoods = () => {
  router.push('/user/goods')
}

// 导航到聊天
const goToChat = (session: SessionItem) => {
  router.push({
    path: '/user/chat',
    query: {
      goodsId: session.goodsId.toString(),
      ctId: session.ctId.toString()
    }
  })
}

// 退出登录
const logout = () => {
  localStorage.removeItem('userId')
  router.push('/user/login')
}

onMounted(() => {
  loadSessionList()
})
</script>

<template>
  <div class="user-home">
    <header class="header">
      <h1>用户中心</h1>
      <button @click="logout" class="logout-btn">退出登录</button>
    </header>

    <div class="content">
      <!-- 快捷操作 -->
      <div class="actions">
        <div class="action-card" @click="goToGoods">
          <div class="action-icon">🛒</div>
          <h3>浏览商品</h3>
          <p>查看所有商品，寻找心仪之选</p>
        </div>
      </div>

      <!-- 最近会话 -->
      <div class="recent-sessions" v-if="sessionList.length > 0">
        <h2>最近咨询</h2>
        <div class="session-list">
          <div
            v-for="session in sessionList"
            :key="session.id"
            class="session-item"
            @click="goToChat(session)"
          >
            <div class="session-info">
              <div class="goods-name">{{ getGoodsName(session.goodsId) }}</div>
              <div class="session-time">{{ formatTime(session.timestamp) }}</div>
            </div>
            <div class="session-status">
              <span :class="['status-badge', session.conversationStatus.toLowerCase()]">
                {{ session.conversationStatus === 'AI' ? 'AI客服' : '人工客服' }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">💬</div>
        <h3>还没有咨询记录</h3>
        <p>开始浏览商品，进行咨询吧！</p>
        <button @click="goToGoods" class="primary-btn">开始浏览</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-home {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: white;
  padding: 20px 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.logout-btn {
  padding: 8px 16px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background: #c82333;
}

.content {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
}

.actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.action-card {
  background: white;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.action-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.action-card h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 18px;
}

.action-card p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.recent-sessions h2 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 20px;
}

.session-list {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 25px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.2s;
}

.session-item:last-child {
  border-bottom: none;
}

.session-item:hover {
  background: #f8f9fa;
}

.session-info {
  flex: 1;
}

.goods-name {
  font-weight: 500;
  font-size: 16px;
  color: #333;
  margin-bottom: 5px;
}

.session-time {
  color: #999;
  font-size: 14px;
}

.session-status {
  flex-shrink: 0;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.ai {
  background: #e3f2fd;
  color: #1976d2;
}

.status-badge.human {
  background: #fff3e0;
  color: #f57c00;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-state h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 20px;
}

.empty-state p {
  margin: 0 0 30px 0;
  color: #666;
}

.primary-btn {
  padding: 12px 30px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.primary-btn:hover {
  background: #0056b3;
}
</style>
