<template>
  <div class="chat">
    <div class="chat-container">
      <!-- 会话列表 -->
      <div class="session-list">
        <div class="session-header">
          <h3>客户会话</h3>
        </div>
        <div class="sessions">
          <div
            v-for="session in sessionList"
            :key="session.id"
            :class="['session-item', { active: selectedSession?.id === session.id }]"
            @click="selectSession(session)"
          >
            <div class="session-avatar">
              <div class="avatar-circle">客</div>
            </div>
            <div class="session-info">
              <div class="user-name">用户{{ session.userId }}</div>
              <div class="goods-name">咨询商品{{ session.goodsId }}</div>
              <div class="last-message">{{ getLastMessage(session) }}</div>
            </div>
            <div class="session-meta">
              <div class="message-time">{{ formatTime(session.timestamp) }}</div>
              <div v-if="getUnreadCount(session.id) > 0" class="unread-badge">
                {{ getUnreadCount(session.id) }}
              </div>
              <div class="status-indicator">
                <span :class="['status-dot', session.conversationStatus.toLowerCase()]"></span>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="sessionList.length === 0" class="empty-sessions">
            <div class="empty-icon">💬</div>
            <h4>暂无客户会话</h4>
            <p>当有客户咨询商品时，会话将显示在这里</p>
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
            <div class="user-info">
              <div class="user-avatar">用</div>
              <div class="user-details">
                <div class="user-name">用户{{ selectedSession.userId }}</div>
                <div class="goods-info">咨询商品{{ selectedSession.goodsId }}</div>
                <div class="session-status">
                  <span :class="['status-badge', selectedSession.conversationStatus.toLowerCase()]">
                    {{ selectedSession.conversationStatus === 'AI' ? 'AI客服中' : '人工服务中' }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 消息列表 -->
          <div class="messages" ref="messagesContainer">
            <div
              v-for="message in messages"
              :key="message.id"
              :class="['message', {
                'own-message': message.type === 'COMMERCIAL_TENANT',
                'user-message': message.type === 'USER',
                'ai-message': message.type === 'ASSISTANT',
                'system-message': message.type === 'SYSTEM'
              }]"
            >
              <div class="message-avatar" v-if="message.type !== 'COMMERCIAL_TENANT'">
                {{ getMessageAvatar(message.type) }}
              </div>
              <div class="message-content">
                <div
                  class="message-text"
                  v-html="message.type === 'ASSISTANT' ? renderMarkdown(message.content) : message.content"
                ></div>
                <div class="message-time">{{ formatTime(message.timestamp) }}</div>
              </div>
              <div class="message-avatar own-avatar" v-if="message.type === 'COMMERCIAL_TENANT'">
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
              :disabled="selectedSession?.conversationStatus === 'AI'"
            >
            <button
              @click="sendMessage"
              :disabled="!newMessage.trim() || selectedSession?.conversationStatus === 'AI'"
            >
              发送
            </button>
            <button
              v-if="selectedSession?.conversationStatus === 'AI'"
              @click="takeOverSession"
              class="take-over-btn"
            >
              人工接管
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getCtSessionList } from '@/api/sessionApi'
import { getWindowMessage, readUserMessage } from '@/api/sessionLogApi'
import { getCtWebSocketClient } from '@/utils/websocket'
import { marked } from 'marked'
import type { SessionItem } from '@/api/sessionApi'
import type { MessageItem } from '@/api/sessionLogApi'
import type { ChatMessage, WebSocketMessage } from '@/utils/websocket'

const router = useRouter()

// 数据
const sessionList = ref<SessionItem[]>([])
const selectedSession = ref<SessionItem | null>(null)
const messages = ref<MessageItem[]>([])
const newMessage = ref('')
const messagesContainer = ref<HTMLElement>()
const unreadCounts = ref<Record<number, number>>({})

// AI流式消息累积
const aiStreamingMessage = ref<MessageItem | null>(null)

// WebSocket客户端
let wsClient: any = null

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
    case 'USER': return '客'
    case 'ASSISTANT': return 'AI'
    case 'SYSTEM': return '系'
    default: return '?'
  }
}

// 渲染Markdown内容
const renderMarkdown = (content: string) => {
  // 配置marked选项
  marked.setOptions({
    breaks: true, // 支持换行
    gfm: true, // 支持GitHub风格的Markdown
  })

  // 渲染Markdown为HTML
  return marked.parse(content)
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
    const ctId = localStorage.getItem('ctId')
    if (!ctId) {
      router.push('/ct/login')
      return
    }

    const response = await getCtSessionList(parseInt(ctId))
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
    const ctId = localStorage.getItem('ctId')
    if (!ctId) return

    await readUserMessage(sessionId, parseInt(ctId))
    unreadCounts.value[sessionId] = 0
  } catch (error) {
    console.error('Failed to mark as read:', error)
  }
}

// 发送消息
const sendMessage = () => {
  if (!newMessage.value.trim() || !selectedSession.value) return

  const message: ChatMessage = {
    sessionId: selectedSession.value.id,
    goodsId: selectedSession.value.goodsId,
    ctId: selectedSession.value.ctId,
    type: 'COMMERCIAL_TENANT',
    message: newMessage.value.trim()
  }

  wsClient.sendMessage(message)
  newMessage.value = ''

  // 暂时添加到本地消息列表
  const tempMessage: MessageItem = {
    id: Date.now(),
    content: message.message,
    type: 'COMMERCIAL_TENANT',
    timestamp: new Date().toISOString(),
    sessionId: selectedSession.value.id,
    readStatus: 'READ'
  }
  messages.value.push(tempMessage)

  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
}

// 人工接管会话
const takeOverSession = () => {
  if (!selectedSession.value) return

  // 发送切换到人工的消息
  const message: ChatMessage = {
    sessionId: selectedSession.value.id,
    goodsId: selectedSession.value.goodsId,
    ctId: selectedSession.value.ctId,
    type: 'COMMERCIAL_TENANT',
    message: '[人工接管会话]'
  }

  wsClient.sendMessage(message)

  // 更新会话状态为人工
  selectedSession.value.conversationStatus = 'HUMAN'
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
  if (message.state === 'END' && message.sessionId === selectedSession.value?.id) {
    console.log('AI streaming completed')

    // 清除流式消息累积
    aiStreamingMessage.value = null

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

  // 更新会话列表中的最后消息
  const session = sessionList.value.find(s => s.id === message.sessionId)
  if (session) {
    // 这里可以更新最后消息时间等
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 移除返回函数，不需要返回商户中心按钮

// 初始化WebSocket
const initWebSocket = async () => {
  try {
    const ctId = localStorage.getItem('ctId')
    if (!ctId) return

    wsClient = getCtWebSocketClient(parseInt(ctId))
    await wsClient.connect()

    wsClient.onMessage(handleMessage)
    console.log('WebSocket connected for CT:', ctId)
  } catch (error) {
    console.error('Failed to connect WebSocket:', error)
  }
}

onMounted(async () => {
  await loadSessionList()
  await initWebSocket()
})

onUnmounted(() => {
  if (wsClient) {
    wsClient.disconnect()
  }
})
</script>

<style scoped>
.chat {
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

/* 会话列表样式 */
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
  background: white;
  border-radius: 8px 8px 0 0;
}

.session-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.sessions {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.2s ease;
  position: relative;
  margin: 0 8px;
  border-radius: 6px;
}

.session-item:hover {
  background-color: #f8f9fa;
  transform: translateX(2px);
}

.session-item.active {
  background-color: #e3f2fd;
  border-left: 3px solid #2196f3;
  box-shadow: 0 1px 3px rgba(33, 150, 243, 0.2);
}

.session-avatar {
  margin-right: 12px;
}

.avatar-circle {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.session-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-weight: 600;
  font-size: 15px;
  color: #333;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
}

.goods-name {
  color: #666;
  font-size: 13px;
  margin-bottom: 2px;
  opacity: 0.8;
}

.last-message {
  color: #888;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 180px;
}

.session-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  min-width: 60px;
}

.message-time {
  color: #999;
  font-size: 11px;
  white-space: nowrap;
}

.unread-badge {
  background: #dc3545;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 10px;
  font-weight: bold;
  min-width: 16px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(220, 53, 69, 0.3);
}

.status-indicator {
  display: flex;
  justify-content: flex-end;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  position: relative;
}

.status-dot.ai {
  background-color: #28a745;
  box-shadow: 0 0 6px rgba(40, 167, 69, 0.4);
}

.status-dot.human {
  background-color: #ffc107;
  box-shadow: 0 0 6px rgba(255, 193, 7, 0.4);
}

/* 聊天窗口样式 */
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
  font-size: 18px;
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
  border-radius: 8px 8px 0 0;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  margin-right: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-details {
  flex: 1;
}

.user-name {
  font-weight: 600;
  font-size: 16px;
  color: #333;
  margin-bottom: 4px;
}

.goods-info {
  color: #666;
  font-size: 14px;
  margin-bottom: 6px;
}

.session-status {
  display: flex;
  align-items: center;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-badge.ai {
  background-color: #e8f5e8;
  color: #28a745;
  border: 1px solid #c3e6c3;
}

.status-badge.human {
  background-color: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.messages {
  flex: 1;
  max-height: calc(100vh - 300px);
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

.message.user-message .message-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.message.ai-message .message-avatar {
  background: linear-gradient(135deg, #17a2b8 0%, #03c4eb 100%);
}

.message.system-message .message-avatar {
  background: linear-gradient(135deg, #ffc107 0%, #ff8c00 100%);
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
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.own-avatar {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
}

.message-content {
  max-width: 60%;
  background: white;
  padding: 10px 14px;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  position: relative;
}

.own-message .message-content {
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
  color: white;
}

.user-message .message-content {
  background: #e3f2fd;
  border: 1px solid #bbdefb;
}

.ai-message .message-content {
  background: #e8f5e8;
  border: 1px solid #c8e6c9;
}

.system-message .message-content {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  color: #856404;
}

.message-text {
  margin-bottom: 6px;
  line-height: 1.5;
  word-wrap: break-word;
  font-size: 14px;
}

.message-time {
  font-size: 11px;
  opacity: 0.7;
  text-align: right;
  font-weight: 400;
}

.message-input {
  padding: 16px;
  border-top: 1px solid #ddd;
  background: white;
  display: flex;
  gap: 12px;
  border-radius: 0 0 8px 8px;
}

.message-input input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e9ecef;
  border-radius: 24px;
  outline: none;
  font-size: 14px;
  transition: border-color 0.2s;
}

.message-input input:focus {
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.message-input input:disabled {
  background: #f8f9fa;
  cursor: not-allowed;
  opacity: 0.6;
}

.message-input button {
  padding: 12px 24px;
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
  color: white;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0, 123, 255, 0.2);
}

.message-input button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 123, 255, 0.3);
}

.message-input button:disabled {
  background: #6c757d;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.take-over-btn {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%) !important;
  box-shadow: 0 2px 4px rgba(40, 167, 69, 0.2) !important;
}

.take-over-btn:hover:not(:disabled) {
  box-shadow: 0 4px 8px rgba(40, 167, 69, 0.3) !important;
}

/* Markdown 样式 */
.message-text :deep(h1), .message-text :deep(h2), .message-text :deep(h3) {
  margin: 8px 0 4px 0;
  font-weight: 600;
  color: #333;
}

.message-text :deep(h1) { font-size: 18px; }
.message-text :deep(h2) { font-size: 16px; }
.message-text :deep(h3) { font-size: 14px; }

.message-text :deep(p) {
  margin: 4px 0;
  line-height: 1.5;
}

.message-text :deep(ul), .message-text :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.message-text :deep(li) {
  margin: 4px 0;
  line-height: 1.4;
}

.message-text :deep(code) {
  background: #f6f8fa;
  border-radius: 3px;
  padding: 2px 4px;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: #d73a49;
}

.message-text :deep(pre) {
  background: #f6f8fa;
  border-radius: 6px;
  padding: 12px;
  margin: 8px 0;
  overflow-x: auto;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.4;
}

.message-text :deep(pre code) {
  background: none;
  padding: 0;
  color: inherit;
}

.message-text :deep(blockquote) {
  border-left: 4px solid #ddd;
  padding-left: 12px;
  margin: 8px 0;
  color: #666;
  font-style: italic;
}

.message-text :deep(a) {
  color: #0366d6;
  text-decoration: none;
}

.message-text :deep(a:hover) {
  text-decoration: underline;
}

.message-text :deep(strong), .message-text :deep(b) {
  font-weight: 600;
  color: #333;
}

.message-text :deep(em), .message-text :deep(i) {
  font-style: italic;
  color: #666;
}

.message-text :deep(table) {
  border-collapse: collapse;
  margin: 8px 0;
  width: 100%;
}

.message-text :deep(th), .message-text :deep(td) {
  border: 1px solid #ddd;
  padding: 6px 12px;
  text-align: left;
}

.message-text :deep(th) {
  background: #f6f8fa;
  font-weight: 600;
}

/* 空状态样式 */
.empty-sessions {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #666;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.3;
}

.empty-sessions h4 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 18px;
  font-weight: 500;
}

.empty-sessions p {
  margin: 0;
  font-size: 14px;
  text-align: center;
  max-width: 200px;
}
</style>
