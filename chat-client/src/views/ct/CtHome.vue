<script setup lang="ts">
import { ref } from 'vue'
import GoodsManagement from './GoodsManagement.vue'
import AISettings from './AISettings.vue'
import Chat from './Chat.vue'
import { getCtInfo } from '../../utils/storage'

type MenuItem = 'goods' | 'ai' | 'chat'

const activeMenu = ref<MenuItem>('goods')
const ctInfo = getCtInfo()

const menuItems = [
  { key: 'goods' as MenuItem, label: '商品管理', icon: '📦' },
  { key: 'ai' as MenuItem, label: 'AI客服设置', icon: '🤖' },
  { key: 'chat' as MenuItem, label: '聊天', icon: '💬' }
]

const setActiveMenu = (menu: MenuItem) => {
  activeMenu.value = menu
}
</script>

<template>
  <div class="ct-home">
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>商户后台</h2>
        <div class="user-info">
          <div class="user-avatar">{{ ctInfo?.name?.charAt(0) || '商' }}</div>
          <div class="user-details">
            <div class="user-name">{{ ctInfo?.name || '商户' }}</div>
            <div class="user-account">{{ ctInfo?.account || '' }}</div>
          </div>
        </div>
      </div>

      <nav class="menu">
        <div
          v-for="item in menuItems"
          :key="item.key"
          :class="['menu-item', { active: activeMenu === item.key }]"
          @click="setActiveMenu(item.key)"
        >
          <span class="menu-icon">{{ item.icon }}</span>
          <span class="menu-label">{{ item.label }}</span>
        </div>
      </nav>
    </div>

    <div class="main-content">
      <GoodsManagement v-if="activeMenu === 'goods'" />
      <AISettings v-else-if="activeMenu === 'ai'" />
      <Chat v-else-if="activeMenu === 'chat'" />
    </div>
  </div>
</template>

<style scoped>
.ct-home {
  display: flex;
  height: 100vh;
  background-color: #f5f5f5;
}

.sidebar {
  width: 240px;
  background: white;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.sidebar-header h2 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-weight: 500;
  margin-bottom: 2px;
  font-size: 14px;
}

.user-account {
  font-size: 12px;
  opacity: 0.8;
}

.menu {
  flex: 1;
  padding: 16px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 3px solid transparent;
}

.menu-item:hover {
  background-color: #f8f9fa;
}

.menu-item.active {
  background-color: #e3f2fd;
  border-left-color: #2196f3;
  color: #2196f3;
}

.menu-icon {
  margin-right: 12px;
  font-size: 18px;
}

.menu-label {
  font-weight: 500;
}

.main-content {
  flex: 1;
  overflow: hidden;
}
</style>
