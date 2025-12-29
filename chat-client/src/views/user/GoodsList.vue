<template>
  <div class="goods-list">
    <div class="header">
      <h1>商品列表</h1>
      <button @click="goBack" class="back-btn">返回</button>
    </div>

    <div class="goods-container">
      <div
        v-for="goods in goodsList"
        :key="goods.id"
        class="goods-card"
      >
        <div class="goods-info">
          <h3>{{ goods.name }}</h3>
          <p>商品ID: {{ goods.id }}</p>
          <p>商户ID: {{ goods.ctId }}</p>
        </div>
        <button
          @click="startChat(goods)"
          class="consult-btn"
        >
          咨询商品
        </button>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="totalPages > 1">
      <button
        @click="changePage(currentPage - 1)"
        :disabled="currentPage <= 1"
        class="page-btn"
      >
        上一页
      </button>

      <span class="page-info">
        第 {{ currentPage }} 页 / 共 {{ totalPages }} 页
      </span>

      <button
        @click="changePage(currentPage + 1)"
        :disabled="currentPage >= totalPages"
        class="page-btn"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getGoodsList } from '@/api/goodsApi'
import type { GoodsItem } from '@/api/goodsApi'

const router = useRouter()

// 数据
const goodsList = ref<GoodsItem[]>([])
const currentPage = ref(1)
const totalPages = ref(1)
const pageSize = 10

// 获取商品列表
const loadGoodsList = async (page: number = 1) => {
  try {
    const response = await getGoodsList(page, pageSize)
    if (response.code === 200) {
      goodsList.value = response.data.records
      totalPages.value = response.data.pages
      currentPage.value = page
    }
  } catch (error) {
    console.error('Failed to load goods list:', error)
  }
}

// 换页
const changePage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    loadGoodsList(page)
  }
}

// 开始咨询
const startChat = (goods: GoodsItem) => {
  // 跳转到聊天页面，传递商品信息
  router.push({
    path: '/user/chat',
    query: {
      goodsId: goods.id.toString(),
      ctId: goods.ctId.toString(),
      goodsName: goods.name
    }
  })
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(() => {
  loadGoodsList()
})
</script>

<style scoped>
.goods-list {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header h1 {
  margin: 0;
  color: #333;
}

.back-btn {
  padding: 10px 20px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.back-btn:hover {
  background: #5a6268;
}

.goods-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.goods-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: transform 0.2s, box-shadow 0.2s;
}

.goods-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.goods-info h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.goods-info p {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}

.consult-btn {
  margin-top: 15px;
  padding: 12px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.consult-btn:hover {
  background: #0056b3;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-btn {
  padding: 10px 20px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: #0056b3;
}

.page-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-weight: 500;
}
</style>
