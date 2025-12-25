import api from './index'

// AI客服相关接口

// AI客服信息
export interface AIRole {
  id: number
  roleName: string
  roleDescription: string
  greetingMessage: string
  problemSolvingApproach: string
  communicationStyle: string
  responseTone: string
  productKnowledgeLevel: string
  emotionalIntelligence: string
  escalationCriteria: string
  closingMessage: string
  createdAt: string
  updatedAt: string
  ctId: number
}

// AI客服响应
export interface AIRoleResponse {
  code: number
  message: string
  data: AIRole | null
}

// 通用响应
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 添加AI请求
export interface AddAIRoleRequest {
  roleName: string
  roleDescription: string
  greetingMessage: string
  problemSolvingApproach: string
  communicationStyle: string
  responseTone: string
  productKnowledgeLevel: string
  emotionalIntelligence: string
  escalationCriteria: string
  closingMessage: string
  createdAt: string
  updatedAt: string
  ctId: number
}

// 修改AI请求
export interface UpdateAIRoleRequest extends AddAIRoleRequest {
  id: number
}

// 查询AI客服信息
export const getAIRoleByCtId = (ctId: number): Promise<AIRoleResponse> => {
  return api.get(`/role/detailsByCtId?ctId=${ctId}`)
}

// 添加AI客服
export const addAIRole = (data: AddAIRoleRequest): Promise<ApiResponse<number>> => {
  return api.post('/role/add', data)
}

// 修改AI客服
export const updateAIRole = (data: UpdateAIRoleRequest): Promise<ApiResponse<boolean>> => {
  return api.post('/role/update', data)
}

// 删除AI客服
export const deleteAIRole = (id: number): Promise<ApiResponse<boolean>> => {
  return api.delete(`/role/delete?id=${id}`)
}

export default {
  getAIRoleByCtId,
  addAIRole,
  updateAIRole,
  deleteAIRole
}
