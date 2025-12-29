<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import userApi from '../../api/userApi'
import { setUserInfo } from '../../utils/storage'
import { getUserWebSocketClient } from '../../utils/websocket'

const router = useRouter()
const account = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
  if (!account.value || !password.value) {
    error.value = '请输入账号和密码'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const response = await userApi.userLogin({
      account: account.value,
      password: password.value
    })

    if (response.code === 200) {
      // 登录成功，存储用户信息并跳转到用户主页
      if (response.data) {
        setUserInfo(response.data)
        // 同时存储用户ID用于API调用
        localStorage.setItem('userId', response.data.id.toString())

        // 建立WebSocket连接
        try {
          const wsClient = getUserWebSocketClient(response.data.id)
          await wsClient.connect()
          console.log('User WebSocket connected after login')
        } catch (wsError) {
          console.error('Failed to establish WebSocket connection:', wsError)
        }
      }
      router.push('/user/home')
    } else {
      error.value = response.message || '登录失败'
    }
  } catch (err) {
    error.value = '网络错误，请稍后重试'
    console.error('Login error:', err)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-form">
      <h2>用户登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="account">账号:</label>
          <input
            id="account"
            v-model="account"
            type="text"
            placeholder="请输入账号"
            :disabled="loading"
          />
        </div>
        
        <div class="form-group">
          <label for="password">密码:</label>
          <input
            id="password"
            v-model="password"
            type="password"
            placeholder="请输入密码"
            :disabled="loading"
          />
        </div>
        
        <div v-if="error" class="error-message">
          {{ error }}
        </div>
        
        <button type="submit" :disabled="loading" class="login-button">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.login-form {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-form h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #333;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #666;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.form-group input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.error-message {
  color: #f56c6c;
  font-size: 0.875rem;
  margin-bottom: 1rem;
  text-align: center;
}

.login-button {
  width: 100%;
  padding: 0.75rem;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-button:hover:not(:disabled) {
  background-color: #66b1ff;
}

.login-button:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}
</style>
