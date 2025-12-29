<template>
  <div class="user-chat">
    <div class="chat-container">
      <!-- 会话列表 -->
      <div class="session-list">
        <div class="session-header">
          <h3>我的会话</h3>
          <button @click="goBack" class="back-btn">返回</button>
        </div>
        <div class="sessions">
          <div
            v-for="session in sessionList"
            :key="session.id"
            :class="['session-item', { active: selectedSession?.id === session.id }]"
            @click="selectSession(session)"
          >
            <div class="session-info">
              <div class="goods-name">商品: {{ getGoodsName(session.goodsId) }}</div>
              <div class="last-message">{{ getLastMessage(session) }}</div>
            </div>
            <div class="session-meta">
              <div class="message-time">{{ formatTime(session.timestamp) }}</div>
              <div v-if="getUnreadCount(session.id) > 0" class="unread-badge">
                {{ getUnreadCount(session.id) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 聊天窗口 -->
      <div class="chat-window">
        <div v-if="!selectedSession" class="no-session-selected">
          <div class="placeholder">
            <h3>选择会话开始聊天</h3>
            <p>从左侧列表中选择一个会话来查看聊天记录</p>
          </div>
        </div>

        <div v-else class="chat-content">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <div class="goods-info">
              <div class="goods-avatar">商</div>
              <div class="goods-details">
                <div class="goods-name">{{ getGoodsName(selectedSession.goodsId) }}</div>
                <div class="goods-status">正在咨询中</div>
              </div>
            </div>
          </div>

          <!-- 消息列表 -->
          <div class="messages" ref="messagesContainer">
            <div
              v-for="message in messages"
              :key="message.id"
              :class="['message', {
                'own-message': message.type === 'USER',
                'ai-message': message.type === 'ASSISTANT',
                'system-message': message.type === 'SYSTEM'
              }]"
            >
              <div class="message-avatar" v-if="message.type !== 'USER'">
                {{ getMessageAvatar(message.type) }}
              </div>
              <div class="message-content">
                <div class="message-text">{{ message.content }}</div>
                <div class="message-time">{{ formatTime(message.timestamp) }}</div>
              </div>
              <div class="message-avatar own-avatar" v-if="message.type === 'USER'">
                我
              </div>
            </div>
          </div>

          <!-- 消息输入框 -->
          <div class="message-input">
            <input
              v-model="newMessage"
              @keyup.enter="sendMessage"
              type="text"
              placeholder="输入消息..."
              :disabled="isTyping"
            >
            <button @click="sendMessage" :disabled="!newMessage.trim() || isTyping">
              {{ isTyping ? 'AI回复中...' : '发送' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getUserSessionList } from '@/api/sessionApi'
import { getWindowMessage, readCtMessage } from '@/api/sessionLogApi'
import { getUserWebSocketClient } from '@/utils/websocket'
import type { SessionItem } from '@/api/sessionApi'
import type { MessageItem } from '@/api/sessionLogApi'
import type { ChatMessage, WebSocketMessage } from '@/utils/websocket'

const router = useRouter()
const route = useRoute()

// 数据
const sessionList = ref<SessionItem[]>([])
const selectedSession = ref<SessionItem | null>(null)
const messages = ref<MessageItem[]>([])
const newMessage = ref('')
const messagesContainer = ref<HTMLElement>()
const isTyping = ref(false)
const unreadCounts = ref<Record<number, number>>({})

// AI流式消息累积
const aiStreamingMessage = ref<MessageItem | null>(null)

// AI回复超时定时器
let aiReplyTimeout: ReturnType<typeof setTimeout> | null = null

// WebSocket客户端
let wsClient: any = null

// 商品名称映射
const goodsNameMap = ref<Record<number, string>>({})

// 获取商品名称
const getGoodsName = (goodsId: number) => {
  return goodsNameMap.value[goodsId] || `商品${goodsId}`
}

// 获取最后消息
const getLastMessage = (session: SessionItem) => {
  const lastMsg = messages.value.find(m => m.sessionId === session.id)
  return lastMsg ? lastMsg.content : '暂无消息'
}

// 获取未读数量
const getUnreadCount = (sessionId: number) => {
  return unreadCounts.value[sessionId] || 0
}

// 获取消息头像
const getMessageAvatar = (type: string) => {
  switch (type) {
    case 'ASSISTANT': return 'AI'
    case 'SYSTEM': return '系'
    case 'COMMERCIAL_TENANT': return '商'
    default: return '?'
  }
}

// 格式化时间
const formatTime = (timestamp: string) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

// 加载会话列表
const loadSessionList = async () => {
  try {
    // 从本地存储获取用户ID（实际应该从登录状态获取）
    const userId = localStorage.getItem('userId')
    if (!userId) {
      router.push('/user/login')
      return
    }

    const response = await getUserSessionList(parseInt(userId))
    if (response.code === 200) {
      sessionList.value = response.data
    }
  } catch (error) {
    console.error('Failed to load session list:', error)
  }
}

// 选择会话
const selectSession = async (session: SessionItem) => {
  selectedSession.value = session

  // 加载消息
  await loadMessages(session.id)

  // 标记为已读
  await markAsRead(session.id)

  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
}

// 加载消息
const loadMessages = async (sessionId: number) => {
  try {
    const response = await getWindowMessage(sessionId)
    if (response.code === 200) {
      messages.value = response.data
    }
  } catch (error) {
    console.error('Failed to load messages:', error)
  }
}

// 标记为已读
const markAsRead = async (sessionId: number) => {
  try {
    const userId = localStorage.getItem('userId')
    if (!userId) return

    await readCtMessage(sessionId, parseInt(userId))
    unreadCounts.value[sessionId] = 0
  } catch (error) {
    console.error('Failed to mark as read:', error)
  }
}

// 发送消息
const sendMessage = () => {
  if (!newMessage.value.trim() || !selectedSession.value || !wsClient) return

  // 检查是否是临时session（前端生成的ID，通常是时间戳）
  const isTempSession = selectedSession.value.id > 1000000000000 // 时间戳ID通常大于这个值

  const message: ChatMessage = {
    // 临时session不发送sessionId，让后端创建新的session
    ...(isTempSession ? {} : { sessionId: selectedSession.value.id }),
    goodsId: selectedSession.value.goodsId,
    ctId: selectedSession.value.ctId,
    type: 'USER',
    message: newMessage.value.trim()
  }

  wsClient.sendMessage(message)
  newMessage.value = ''

  // 暂时添加到本地消息列表
  const tempMessage: MessageItem = {
    id: Date.now(),
    content: message.message,
    type: 'USER',
    timestamp: new Date().toISOString(),
    sessionId: selectedSession.value.id,
    readStatus: 'READ'
  }
  messages.value.push(tempMessage)

  // 清除之前的超时定时器
  if (aiReplyTimeout) {
    clearTimeout(aiReplyTimeout)
  }

  // 设置AI回复超时保护（30秒后自动启用输入框）
  aiReplyTimeout = setTimeout(() => {
    console.log('AI reply timeout, enabling input')
    isTyping.value = false
    aiStreamingMessage.value = null
  }, 30000)

  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
}

// 接收消息
const handleMessage = (message: WebSocketMessage) => {
  // 处理SURE状态：后端返回新创建的sessionId
  if (message.state === 'SURE' && message.sessionId) {
    console.log('Received SURE response with new sessionId:', message.sessionId)

    // 更新临时session为真实session
    if (selectedSession.value) {
      const oldSessionId = selectedSession.value.id
      const newSessionId = message.sessionId

      // 更新selectedSession
      selectedSession.value.id = newSessionId

      // 更新消息列表中的sessionId
      messages.value.forEach(msg => {
        if (msg.sessionId === oldSessionId) {
          msg.sessionId = newSessionId
        }
      })

      // 更新sessionList中的session
      const sessionIndex = sessionList.value.findIndex((s: SessionItem) => s.id === oldSessionId)
      if (sessionIndex !== -1) {
        sessionList.value[sessionIndex]!.id = newSessionId
      }

      console.log('Updated temp session', oldSessionId, 'to real session', newSessionId)
    }
    return
  }

  // 处理END状态：AI流式输出完成
  if (message.state === 'END') {
    console.log('AI streaming completed, received END state')

    // 停止打字效果
    isTyping.value = false

    // 清除流式消息累积
    aiStreamingMessage.value = null

    // 清除超时定时器
    if (aiReplyTimeout) {
      clearTimeout(aiReplyTimeout)
      aiReplyTimeout = null
    }

    // 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
    return
  }

  // 处理普通消息
  if (message.sessionId === selectedSession.value?.id) {
    // 处理流式AI消息
    if (message.type === 'ASSISTANT') {
      // 如果还没有流式消息，开始新的流式消息
      if (!aiStreamingMessage.value) {
        aiStreamingMessage.value = {
          id: Date.now(),
          content: message.message,
          type: message.type,
          timestamp: message.timestamp || new Date().toISOString(),
          sessionId: message.sessionId!,
          readStatus: 'READ'
        }
        messages.value.push(aiStreamingMessage.value)

        // 开始打字效果
        isTyping.value = true
      } else {
        // 累积流式内容
        aiStreamingMessage.value.content += message.message
      }

      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
      return
    }

    // 处理其他类型的消息
    const newMsg: MessageItem = {
      id: Date.now(),
      content: message.message,
      type: message.type,
      timestamp: message.timestamp || new Date().toISOString(),
      sessionId: message.sessionId!,
      readStatus: 'READ'
    }

    messages.value.push(newMsg)

    // 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 返回
const goBack = () => {
  router.push('/user/home')
}

// 初始化WebSocket
const initWebSocket = async () => {
  try {
    const userId = localStorage.getItem('userId')
    if (!userId) return

    wsClient = getUserWebSocketClient(parseInt(userId))

    // 检查是否已经连接，如果未连接则建立连接
    if (!wsClient.isConnected) {
      await wsClient.connect()
      wsClient.onMessage(handleMessage)
      console.log('WebSocket connected for user:', userId)
    } else {
      // 如果已经连接，直接设置消息处理器
      wsClient.onMessage(handleMessage)
      console.log('WebSocket already connected for user:', userId)
    }
  } catch (error) {
    console.error('Failed to connect WebSocket:', error)
  }
}

// 检查是否从商品页面跳转过来
const checkInitialSession = () => {
  const { goodsId, ctId, goodsName } = route.query
  if (goodsId && ctId) {
    // 保存商品名称到映射
    if (goodsName) {
      goodsNameMap.value[parseInt(goodsId as string)] = goodsName as string
    }

    // 创建新会话或找到现有会话
    const existingSession = sessionList.value.find(
      (s: SessionItem) => s.goodsId === parseInt(goodsId as string) && s.ctId === parseInt(ctId as string)
    )

    if (existingSession) {
      selectSession(existingSession)
    } else {
      // 如果没有现有会话，创建一个临时的前端会话显示（后端不会存储）
      const tempSession: SessionItem = {
        id: Date.now(), // 使用时间戳作为临时ID
        ctId: parseInt(ctId as string),
        userId: parseInt(localStorage.getItem('userId') || '0'),
        goodsId: parseInt(goodsId as string),
        conversationStatus: 'AI',
        timestamp: new Date().toISOString()
      }

      // 将临时会话添加到会话列表前端显示
      sessionList.value.unshift(tempSession)
      selectedSession.value = tempSession
      messages.value = []
    }
  }
}

onMounted(async () => {
  await loadSessionList()
  await initWebSocket()
  checkInitialSession()
})

onUnmounted(() => {
  if (wsClient) {
    wsClient.disconnect()
  }
})
</script>

<style scoped>
.user-chat {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.chat-container {
  flex: 1;
  display: flex;
  padding: 20px;
  gap: 20px;
  max-height: calc(100vh - 80px);
}

.session-list {
  width: 300px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.session-header {
  padding: 16px;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session-header h3 {
  margin: 0;
}

.back-btn {
  padding: 6px 12px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  background: #5a6268;
}

.sessions {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
  position: relative;
}

.session-item:hover {
  background-color: #f8f9fa;
}

.session-item.active {
  background-color: #e3f2fd;
  border-left: 3px solid #2196f3;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.goods-name {
  font-weight: 500;
  margin-bottom: 4px;
  font-size: 14px;
  color: #333;
}

.last-message {
  color: #666;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.message-time {
  color: #999;
  font-size: 11px;
}

.unread-badge {
  background: #dc3545;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 11px;
  font-weight: bold;
  min-width: 18px;
  text-align: center;
}

.chat-window {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.no-session-selected {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder {
  text-align: center;
  color: #666;
}

.placeholder h3 {
  margin-bottom: 8px;
  color: #333;
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 16px;
  border-bottom: 1px solid #ddd;
  background: white;
  display: flex;
  align-items: center;
}

.goods-info {
  display: flex;
  align-items: center;
}

.goods-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #28a745;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 12px;
}

.goods-details {
  flex: 1;
}

.goods-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.goods-status {
  color: #28a745;
  font-size: 14px;
}

.messages {
  flex: 1;
  max-height: calc(100vh - 200px);
  padding: 16px;
  overflow-y: auto;
  background: #f8f9fa;
}

.message {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.message.own-message {
  flex-direction: row-reverse;
}

.message.ai-message .message-avatar {
  background-color: #17a2b8;
}

.message.system-message .message-avatar {
  background-color: #ffc107;
  color: #000;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #28a745;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  margin: 0 8px;
}

.own-avatar {
  background-color: #007bff;
}

.message-content {
  max-width: 60%;
  background: white;
  padding: 8px 12px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.own-message .message-content {
  background: #e3f2fd;
  border: 1px solid #bbdefb;
  color: #333;
}

.ai-message .message-content {
  background: #e8f5e8;
  border: 1px solid #c8e6c9;
  color: #333;
}

.system-message .message-content {
  background: #ffc107;
  color: #000;
}

.message-text {
  margin-bottom: 4px;
  line-height: 1.4;
  word-wrap: break-word;
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
  text-align: right;
}

.message-input {
  padding: 16px;
  border-top: 1px solid #ddd;
  background: white;
  display: flex;
  gap: 12px;
}

.message-input input {
  flex: 1;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 24px;
  outline: none;
}

.message-input input:focus {
  border-color: #007bff;
}

.message-input input:disabled {
  background: #f8f9fa;
  cursor: not-allowed;
}

.message-input button {
  padding: 12px 24px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 24px;
  cursor: pointer;
}

.message-input button:hover:not(:disabled) {
  background-color: #0056b3;
}

.message-input button:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}
</style>
