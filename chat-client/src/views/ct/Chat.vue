<template>
  <div class="chat">
    <div class="chat-container">
      <!-- 客户列表 -->
      <div class="customer-list">
        <h3>客户列表</h3>
        <div class="customers">
          <div
            v-for="customer in customers"
            :key="customer.id"
            :class="['customer-item', { active: selectedCustomer?.id === customer.id }]"
            @click="selectCustomer(customer)"
          >
            <div class="customer-avatar">{{ customer.name.charAt(0) }}</div>
            <div class="customer-info">
              <div class="customer-name">{{ customer.name }}</div>
              <div class="last-message">{{ customer.lastMessage }}</div>
            </div>
            <div class="message-time">{{ customer.lastMessageTime }}</div>
          </div>
        </div>
      </div>

      <!-- 聊天窗口 -->
      <div class="chat-window">
        <div v-if="!selectedCustomer" class="no-customer-selected">
          <div class="placeholder">
            <h3>选择客户开始聊天</h3>
            <p>从左侧列表中选择一个客户来查看聊天记录</p>
          </div>
        </div>

        <div v-else class="chat-content">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <div class="customer-info">
              <div class="customer-avatar">{{ selectedCustomer.name.charAt(0) }}</div>
              <div class="customer-details">
                <div class="customer-name">{{ selectedCustomer.name }}</div>
                <div class="customer-status">在线</div>
              </div>
            </div>
          </div>

          <!-- 消息列表 -->
          <div class="messages" ref="messagesContainer">
            <div
              v-for="message in messages"
              :key="message.id"
              :class="['message', { 'own-message': message.isOwn }]"
            >
              <div class="message-avatar" v-if="!message.isOwn">
                {{ selectedCustomer?.name.charAt(0) }}
              </div>
              <div class="message-content">
                <div class="message-text">{{ message.text }}</div>
                <div class="message-time">{{ message.time }}</div>
              </div>
              <div class="message-avatar own-avatar" v-if="message.isOwn">
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
            >
            <button @click="sendMessage" :disabled="!newMessage.trim()">发送</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: 'Chat'
})
import { ref, onMounted, nextTick } from 'vue'

interface Customer {
  id: number
  name: string
  lastMessage: string
  lastMessageTime: string
}

interface Message {
  id: number
  text: string
  time: string
  isOwn: boolean
}

const customers = ref<Customer[]>([
  {
    id: 1,
    name: '张三',
    lastMessage: '你好，请问这个商品怎么购买？',
    lastMessageTime: '10:30'
  },
  {
    id: 2,
    name: '李四',
    lastMessage: '我想咨询一下售后服务',
    lastMessageTime: '09:45'
  },
  {
    id: 3,
    name: '王五',
    lastMessage: '商品质量怎么样？',
    lastMessageTime: '昨天'
  }
])

const selectedCustomer = ref<Customer | null>(null)
const messages = ref<Message[]>([])
const newMessage = ref('')
const messagesContainer = ref<HTMLElement>()

// 选择客户
const selectCustomer = (customer: Customer) => {
  selectedCustomer.value = customer
  // 加载该客户的聊天记录
  loadMessages(customer.id)
}

// 加载消息
const loadMessages = (customerId: number) => {
  // 这里是模拟数据，实际应该从API获取
  messages.value = [
    {
      id: 1,
      text: '你好，我想咨询一下商品信息',
      time: '10:00',
      isOwn: false
    },
    {
      id: 2,
      text: '您好！请问您想了解哪个商品的信息？',
      time: '10:01',
      isOwn: true
    },
    {
      id: 3,
      text: '就是你们网站上的那个华硕主板',
      time: '10:02',
      isOwn: false
    },
    {
      id: 4,
      text: '好的，这个主板性能很不错，兼容性也好。您有什么具体问题吗？',
      time: '10:03',
      isOwn: true
    }
  ]

  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
}

// 发送消息
const sendMessage = () => {
  if (!newMessage.value.trim() || !selectedCustomer.value) return

  const message: Message = {
    id: Date.now(),
    text: newMessage.value.trim(),
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    isOwn: true
  }

  messages.value.push(message)
  newMessage.value = ''

  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })

  // 更新客户的最后消息
  if (selectedCustomer.value) {
    const customer = customers.value.find(c => c.id === selectedCustomer.value.id)
    if (customer) {
      customer.lastMessage = message.text
      customer.lastMessageTime = message.time
    }
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

onMounted(() => {
  // 默认选择第一个客户
  if (customers.value.length > 0) {
    selectCustomer(customers.value[0])
  }
})
</script>

<style scoped>
.chat {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.chat-container {
  flex: 1;
  display: flex;
}

.customer-list {
  width: 300px;
  border-right: 1px solid #ddd;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
}

.customer-list h3 {
  padding: 16px;
  margin: 0;
  border-bottom: 1px solid #ddd;
  background: white;
}

.customers {
  flex: 1;
  overflow-y: auto;
}

.customer-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
}

.customer-item:hover {
  background-color: #e9ecef;
}

.customer-item.active {
  background-color: #e3f2fd;
  border-left: 3px solid #2196f3;
}

.customer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #2196f3;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 12px;
}

.customer-info {
  flex: 1;
  min-width: 0;
}

.customer-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.last-message {
  color: #666;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-time {
  color: #999;
  font-size: 12px;
  align-self: flex-start;
}

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.no-customer-selected {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
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

.chat-header .customer-avatar {
  width: 40px;
  height: 40px;
  margin-right: 12px;
}

.customer-details {
  flex: 1;
}

.customer-status {
  color: #4caf50;
  font-size: 14px;
}

.messages {
  flex: 1;
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

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #2196f3;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  margin: 0 8px;
}

.own-avatar {
  background-color: #4caf50;
}

.message-content {
  max-width: 60%;
  background: white;
  padding: 8px 12px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.own-message .message-content {
  background: #dcf8c6;
}

.message-text {
  margin-bottom: 4px;
  line-height: 1.4;
}

.message-time {
  font-size: 12px;
  color: #999;
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
  border-color: #2196f3;
}

.message-input button {
  padding: 12px 24px;
  background-color: #2196f3;
  color: white;
  border: none;
  border-radius: 24px;
  cursor: pointer;
}

.message-input button:hover:not(:disabled) {
  background-color: #1976d2;
}

.message-input button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
</style>
