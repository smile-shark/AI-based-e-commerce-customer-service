<template>
  <div class="ai-settings">
    <div v-if="!aiRole" class="no-ai-section">
      <h2>AI客服设置</h2>
      <p>您还没有配置AI客服，请添加一个AI客服角色。</p>
      <button @click="showAddModal = true" class="add-ai-btn">添加AI客服</button>
    </div>

    <div v-else class="ai-detail-section">
      <div class="header">
        <h2>AI客服设置</h2>
        <div class="actions">
          <button @click="showEditModal = true" class="edit-btn">编辑</button>
          <button @click="deleteAIRole" class="delete-btn">删除</button>
        </div>
      </div>

      <div class="ai-info">
        <div class="info-item">
          <label>角色名称：</label>
          <span>{{ aiRole.roleName }}</span>
        </div>
        <div class="info-item">
          <label>角色描述：</label>
          <span>{{ aiRole.roleDescription }}</span>
        </div>
        <div class="info-item">
          <label>问候语：</label>
          <span>{{ aiRole.greetingMessage }}</span>
        </div>
        <div class="info-item">
          <label>解决问题的方法：</label>
          <span>{{ aiRole.problemSolvingApproach }}</span>
        </div>
        <div class="info-item">
          <label>沟通风格：</label>
          <span>{{ aiRole.communicationStyle }}</span>
        </div>
        <div class="info-item">
          <label>回复语调：</label>
          <span>{{ aiRole.responseTone }}</span>
        </div>
        <div class="info-item">
          <label>产品知识水平：</label>
          <span>{{ aiRole.productKnowledgeLevel }}</span>
        </div>
        <div class="info-item">
          <label>情商表现：</label>
          <span>{{ aiRole.emotionalIntelligence }}</span>
        </div>
        <div class="info-item">
          <label>升级处理标准：</label>
          <span>{{ aiRole.escalationCriteria }}</span>
        </div>
        <div class="info-item">
          <label>结束语：</label>
          <span>{{ aiRole.closingMessage }}</span>
        </div>
      </div>

      <div class="ai-prompt-preview">
        <h3>AI角色提示词预览</h3>
        <div class="prompt-content">
          你是一个专业的{{ aiRole.roleName }}。你的主要职责是：
          角色描述：{{ aiRole.roleDescription }}
          问候语：{{ aiRole.greetingMessage }}
          解决问题的方法：{{ aiRole.problemSolvingApproach }}
          沟通风格：{{ aiRole.communicationStyle }}
          回复语调：{{ aiRole.responseTone }}
          产品知识水平：{{ aiRole.productKnowledgeLevel }}
          情商表现：{{ aiRole.emotionalIntelligence }}
          升级处理标准：{{ aiRole.escalationCriteria }}
          结束语：{{ aiRole.closingMessage }}
        </div>
      </div>
    </div>

    <!-- 添加AI模态框 -->
    <div v-if="showAddModal" class="modal-overlay" @click="showAddModal = false">
      <div class="modal-content" @click.stop>
        <h3>添加AI客服</h3>
        <form @submit.prevent="addAIRole">
          <div class="form-row">
            <div class="form-group">
              <label>角色名称：</label>
              <input v-model="aiForm.roleName" type="text" required>
            </div>
            <div class="form-group">
              <label>问候语：</label>
              <input v-model="aiForm.greetingMessage" type="text" required>
            </div>
          </div>
          <div class="form-group">
            <label>角色描述：</label>
            <textarea v-model="aiForm.roleDescription" required></textarea>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>解决问题的方法：</label>
              <input v-model="aiForm.problemSolvingApproach" type="text" required>
            </div>
            <div class="form-group">
              <label>沟通风格：</label>
              <input v-model="aiForm.communicationStyle" type="text" required>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>回复语调：</label>
              <input v-model="aiForm.responseTone" type="text" required>
            </div>
            <div class="form-group">
              <label>产品知识水平：</label>
              <input v-model="aiForm.productKnowledgeLevel" type="text" required>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>情商表现：</label>
              <input v-model="aiForm.emotionalIntelligence" type="text" required>
            </div>
            <div class="form-group">
              <label>升级处理标准：</label>
              <input v-model="aiForm.escalationCriteria" type="text" required>
            </div>
          </div>
          <div class="form-group">
            <label>结束语：</label>
            <input v-model="aiForm.closingMessage" type="text" required>
          </div>
          <div class="modal-actions">
            <button type="button" @click="showAddModal = false">取消</button>
            <button type="submit" :disabled="loading">添加</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 编辑AI模态框 -->
    <div v-if="showEditModal" class="modal-overlay" @click="showEditModal = false">
      <div class="modal-content" @click.stop>
        <h3>编辑AI客服</h3>
        <form @submit.prevent="updateAIRole">
          <div class="form-row">
            <div class="form-group">
              <label>角色名称：</label>
              <input v-model="editForm.roleName" type="text" required>
            </div>
            <div class="form-group">
              <label>问候语：</label>
              <input v-model="editForm.greetingMessage" type="text" required>
            </div>
          </div>
          <div class="form-group">
            <label>角色描述：</label>
            <textarea v-model="editForm.roleDescription" required></textarea>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>解决问题的方法：</label>
              <input v-model="editForm.problemSolvingApproach" type="text" required>
            </div>
            <div class="form-group">
              <label>沟通风格：</label>
              <input v-model="editForm.communicationStyle" type="text" required>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>回复语调：</label>
              <input v-model="editForm.responseTone" type="text" required>
            </div>
            <div class="form-group">
              <label>产品知识水平：</label>
              <input v-model="editForm.productKnowledgeLevel" type="text" required>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>情商表现：</label>
              <input v-model="editForm.emotionalIntelligence" type="text" required>
            </div>
            <div class="form-group">
              <label>升级处理标准：</label>
              <input v-model="editForm.escalationCriteria" type="text" required>
            </div>
          </div>
          <div class="form-group">
            <label>结束语：</label>
            <input v-model="editForm.closingMessage" type="text" required>
          </div>
          <div class="modal-actions">
            <button type="button" @click="showEditModal = false">取消</button>
            <button type="submit" :disabled="loading">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: 'AISettings'
})
import { ref, onMounted, reactive, watch } from 'vue'
import aiApi, { type AIRole } from '../../api/aiApi'
import { getCtInfo } from '../../utils/storage'

const aiRole = ref<AIRole | null>(null)
const loading = ref(false)
const showAddModal = ref(false)
const showEditModal = ref(false)

const aiForm = reactive({
  roleName: '',
  roleDescription: '',
  greetingMessage: '',
  problemSolvingApproach: '',
  communicationStyle: '',
  responseTone: '',
  productKnowledgeLevel: '',
  emotionalIntelligence: '',
  escalationCriteria: '',
  closingMessage: '',
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
  ctId: 0
})

const editForm = reactive({
  id: 0,
  roleName: '',
  roleDescription: '',
  greetingMessage: '',
  problemSolvingApproach: '',
  communicationStyle: '',
  responseTone: '',
  productKnowledgeLevel: '',
  emotionalIntelligence: '',
  escalationCriteria: '',
  closingMessage: '',
  createdAt: '',
  updatedAt: '',
  ctId: 0
})

const ctInfo = getCtInfo()

// 加载AI角色信息
const loadAIRole = async () => {
  if (!ctInfo) return

  try {
    const response = await aiApi.getAIRoleByCtId(ctInfo.id)
    if (response.code === 200 && response.data) {
      aiRole.value = response.data
    }
  } catch (error) {
    console.error('加载AI角色信息失败:', error)
  }
}

// 添加AI角色
const addAIRole = async () => {
  if (!ctInfo) return

  loading.value = true
  try {
    aiForm.ctId = ctInfo.id
    aiForm.createdAt = new Date().toISOString()
    aiForm.updatedAt = new Date().toISOString()

    const response = await aiApi.addAIRole(aiForm)
    if (response.code === 200) {
      showAddModal.value = false
      resetForm()
      loadAIRole()
    } else {
      alert(response.message)
    }
  } catch (error) {
    console.error('添加AI角色失败:', error)
  } finally {
    loading.value = false
  }
}

// 更新AI角色
const updateAIRole = async () => {
  loading.value = true
  try {
    editForm.updatedAt = new Date().toISOString()

    const response = await aiApi.updateAIRole(editForm)
    if (response.code === 200) {
      showEditModal.value = false
      loadAIRole()
    } else {
      alert(response.message)
    }
  } catch (error) {
    console.error('更新AI角色失败:', error)
  } finally {
    loading.value = false
  }
}

// 删除AI角色
const deleteAIRole = async () => {
  if (!aiRole.value) return
  if (!confirm('确定要删除这个AI客服吗？')) return

  try {
    const response = await aiApi.deleteAIRole(aiRole.value.id)
    if (response.code === 200) {
      aiRole.value = null
    } else {
      alert(response.message)
    }
  } catch (error) {
    console.error('删除AI角色失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(aiForm, {
    roleName: '',
    roleDescription: '',
    greetingMessage: '',
    problemSolvingApproach: '',
    communicationStyle: '',
    responseTone: '',
    productKnowledgeLevel: '',
    emotionalIntelligence: '',
    escalationCriteria: '',
    closingMessage: '',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    ctId: ctInfo?.id || 0
  })
}

// 打开编辑模态框时填充数据
const openEditModal = () => {
  if (!aiRole.value) return

  Object.assign(editForm, {
    ...aiRole.value,
    updatedAt: new Date().toISOString()
  })
  showEditModal.value = true
}

// 监听showEditModal变化
watch(showEditModal, (newVal: boolean) => {
  if (newVal) {
    openEditModal()
  }
})

onMounted(() => {
  loadAIRole()
})
</script>

<style scoped>
.ai-settings {
  padding: 20px;
}

.no-ai-section {
  text-align: center;
  padding: 40px 20px;
}

.add-ai-btn {
  padding: 12px 24px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.add-ai-btn:hover {
  background-color: #66b1ff;
}

.ai-detail-section .header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.actions {
  display: flex;
  gap: 12px;
}

.edit-btn, .delete-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.edit-btn {
  background-color: #e6a23c;
  color: white;
}

.delete-btn {
  background-color: #f56c6c;
  color: white;
}

.ai-info {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  margin-bottom: 12px;
  align-items: flex-start;
}

.info-item label {
  min-width: 120px;
  font-weight: 500;
  color: #666;
  margin-right: 16px;
}

.info-item span {
  flex: 1;
  line-height: 1.5;
}

.ai-prompt-preview {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.ai-prompt-preview h3 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #333;
}

.prompt-content {
  background-color: #f8f9fa;
  padding: 16px;
  border-radius: 4px;
  border-left: 4px solid #409eff;
  white-space: pre-line;
  font-family: monospace;
  font-size: 14px;
  line-height: 1.6;
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
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.form-row .form-group {
  flex: 1;
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

.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-height: 60px;
  resize: vertical;
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
</style>
