<template>
  <div class="goods-management">
    <div class="header">
      <h2>商品管理</h2>
      <button @click="showAddModal = true" class="add-btn">添加商品</button>
    </div>

    <div class="goods-list">
      <div v-for="goods in goodsList" :key="goods.id" class="goods-item">
        <div class="goods-info">
          <h3>{{ goods.name }}</h3>
          <p>ID: {{ goods.id }}</p>
        </div>
        <div class="goods-actions">
          <button @click="viewDetail(goods.id)" class="detail-btn">详情</button>
          <button @click="editGoods(goods)" class="edit-btn">编辑</button>
          <button @click="deleteGoods(goods.id)" class="delete-btn">删除</button>
        </div>
      </div>
    </div>

    <!-- 添加商品模态框 -->
    <div v-if="showAddModal" class="modal-overlay" @click="showAddModal = false">
      <div class="modal-content" @click.stop>
        <h3>添加商品</h3>
        <form @submit.prevent="addGoods">
          <div class="form-group">
            <label>商品名称：</label>
            <input v-model="newGoodsName" type="text" required>
          </div>
          <div class="modal-actions">
            <button type="button" @click="showAddModal = false">取消</button>
            <button type="submit" :disabled="loading">添加</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 编辑商品模态框 -->
    <div v-if="showEditModal" class="modal-overlay" @click="showEditModal = false">
      <div class="modal-content" @click.stop>
        <h3>编辑商品</h3>
        <form @submit.prevent="updateGoods">
          <div class="form-group">
            <label>商品名称：</label>
            <input v-model="editGoodsName" type="text" required>
          </div>
          <div class="modal-actions">
            <button type="button" @click="showEditModal = false">取消</button>
            <button type="submit" :disabled="loading">保存</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 商品详情模态框 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="showDetailModal = false">
      <div class="modal-content detail-modal" @click.stop>
        <h3>{{ currentGoods?.name }}</h3>

        <div class="documents-section">
          <h4>商品文档</h4>
          <div class="upload-section">
            <input
              type="file"
              ref="fileInput"
              @change="handleFileSelect"
              accept=".md,.pdf,.txt"
              :disabled="uploading"
            >
            <div v-if="uploading" class="upload-status">
              正在上传...
            </div>
          </div>

          <div class="documents-list">
            <div v-for="doc in currentGoods?.documents" :key="doc.id" class="document-item">
              <span>{{ doc.name }}</span>
              <button @click="deleteDocument(doc.id)" class="delete-doc-btn">删除</button>
            </div>
          </div>
        </div>

        <div class="modal-actions">
          <button @click="showDetailModal = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: 'GoodsManagement'
})
import { ref, onMounted } from 'vue'
import goodsApi, { type GoodsItem, type GoodsDetail } from '../../api/goodsApi'
import { getCtInfo } from '../../utils/storage'

const goodsList = ref<GoodsItem[]>([])
const loading = ref(false)
const uploading = ref(false)

const showAddModal = ref(false)
const showEditModal = ref(false)
const showDetailModal = ref(false)

const newGoodsName = ref('')
const editGoodsName = ref('')
const currentGoodsId = ref<number | null>(null)
const currentGoods = ref<GoodsDetail | null>(null)
const selectedFile = ref<File | null>(null)
const fileInput = ref<HTMLInputElement>()

const ctInfo = getCtInfo()

// 加载商品列表
const loadGoodsList = async () => {
  try {
    const response = await goodsApi.getGoodsList()
    if (response.code === 200) {
      goodsList.value = response.data.records
    }
  } catch (error) {
    console.error('加载商品列表失败:', error)
  }
}

// 添加商品
const addGoods = async () => {
  if (!newGoodsName.value.trim() || !ctInfo) return

  loading.value = true
  try {
    const response = await goodsApi.addGoods({
      name: newGoodsName.value.trim(),
      ctId: ctInfo.id
    })

    if (response.code === 200) {
      showAddModal.value = false
      newGoodsName.value = ''
      loadGoodsList()
    } else {
      alert(response.message)
    }
  } catch (error) {
    console.error('添加商品失败:', error)
  } finally {
    loading.value = false
  }
}

// 编辑商品
const editGoods = (goods: GoodsItem) => {
  currentGoodsId.value = goods.id
  editGoodsName.value = goods.name
  showEditModal.value = true
}

// 更新商品
const updateGoods = async () => {
  if (!currentGoodsId.value || !editGoodsName.value.trim()) return

  loading.value = true
  try {
    const response = await goodsApi.updateGoods({
      id: currentGoodsId.value,
      name: editGoodsName.value.trim()
    })

    if (response.code === 200) {
      showEditModal.value = false
      loadGoodsList()
    } else {
      alert(response.message)
    }
  } catch (error) {
    console.error('更新商品失败:', error)
  } finally {
    loading.value = false
  }
}

// 删除商品
const deleteGoods = async (id: number) => {
  if (!confirm('确定要删除这个商品吗？')) return

  try {
    const response = await goodsApi.deleteGoods(id)
    if (response.code === 200) {
      loadGoodsList()
    } else {
      alert(response.message)
    }
  } catch (error) {
    console.error('删除商品失败:', error)
  }
}

// 查看详情
const viewDetail = async (id: number) => {
  try {
    const response = await goodsApi.getGoodsDetail(id)
    if (response.code === 200) {
      currentGoods.value = response.data
      showDetailModal.value = true
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
  }
}

// 处理文件选择
const handleFileSelect = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  // 检查文件类型
  const allowedTypes = ['text/markdown', 'application/pdf', 'text/plain']
  const allowedExtensions = ['.md', '.pdf', '.txt']

  const isAllowedType = allowedTypes.includes(file.type)
  const hasAllowedExtension = allowedExtensions.some(ext => file.name.toLowerCase().endsWith(ext))

  if (!isAllowedType && !hasAllowedExtension) {
    alert('只允许上传 .md、.pdf 或 .txt 格式的文件')
    // 清空文件输入
    if (fileInput.value) {
      fileInput.value.value = ''
    }
    return
  }

  selectedFile.value = file

  // 自动上传
  await uploadDocument()
}

// 上传文档
const uploadDocument = async () => {
  if (!selectedFile.value || !currentGoods.value) return

  uploading.value = true
  try {
    const response = await goodsApi.uploadDocument(selectedFile.value, currentGoods.value.id)
    if (response.code === 200) {
      selectedFile.value = null
      if (fileInput.value) {
        fileInput.value.value = ''
      }
      // 重新加载商品详情
      await viewDetail(currentGoods.value.id)
    } else {
      alert(response.message || '上传失败')
    }
  } catch (error) {
    console.error('上传文档失败:', error)
    alert('上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

// 删除文档
const deleteDocument = async (id: number) => {
  if (!confirm('确定要删除这个文档吗？')) return

  try {
    const response = await goodsApi.deleteDocument(id)
    if (response.code === 200) {
      // 重新加载商品详情
      if (currentGoods.value) {
        await viewDetail(currentGoods.value.id)
      }
    } else {
      alert(response.message)
    }
  } catch (error) {
    console.error('删除文档失败:', error)
  }
}

onMounted(() => {
  loadGoodsList()
})
</script>

<style scoped>
.goods-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.add-btn {
  padding: 8px 16px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.add-btn:hover {
  background-color: #66b1ff;
}

.goods-list {
  display: grid;
  gap: 16px;
}

.goods-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
}

.goods-info h3 {
  margin: 0 0 8px 0;
}

.goods-info p {
  margin: 0;
  color: #666;
}

.goods-actions {
  display: flex;
  gap: 8px;
}

.detail-btn, .edit-btn, .delete-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.detail-btn {
  background-color: #67c23a;
  color: white;
}

.edit-btn {
  background-color: #e6a23c;
  color: white;
}

.delete-btn {
  background-color: #f56c6c;
  color: white;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 24px;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
}

.detail-modal {
  max-width: 700px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.modal-actions button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  background: white;
}

.modal-actions button[type="submit"] {
  background-color: #409eff;
  color: white;
  border-color: #409eff;
}

.documents-section {
  margin-top: 20px;
}

.upload-section {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.upload-btn {
  padding: 8px 16px;
  background-color: #67c23a;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.documents-list {
  border: 1px solid #ddd;
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;
}

.document-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #eee;
}

.document-item:last-child {
  border-bottom: none;
}

.delete-doc-btn {
  padding: 4px 8px;
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}
</style>
