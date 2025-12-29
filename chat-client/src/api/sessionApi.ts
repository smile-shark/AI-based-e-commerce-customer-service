import api from './index'

// 会话相关接口

// 会话列表响应
export interface SessionListResponse {
  code: number
  message: string
  data: SessionItem[]
}

// 会话项
export interface SessionItem {
  id: number
  ctId: number
  userId: number
  goodsId: number
  conversationStatus: string
  timestamp: string
}

// 通用响应
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 获取用户会话列表
export const getUserSessionList = (userId: number): Promise<SessionListResponse> => {
  return api.get(`/session/userGetLastSessionList?userId=${userId}`)
}

// 获取商户会话列表
export const getCtSessionList = (ctId: number): Promise<SessionListResponse> => {
  return api.get(`/session/ctGetLastSessionList?ctId=${ctId}`)
}

export default {
  getUserSessionList,
  getCtSessionList
}
