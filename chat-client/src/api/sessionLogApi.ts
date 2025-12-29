import api from './index'

// 消息相关接口

// 消息列表响应
export interface MessageListResponse {
  code: number
  message: string
  data: MessageItem[]
}

// 消息项
export interface MessageItem {
  id: number
  content: string
  type: string
  timestamp: string
  sessionId: number
  readStatus: string
}

// 通用响应
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 用户阅读商户的消息（用户端使用）
export const readCtMessage = (sessionId: number, userId: number): Promise<ApiResponse> => {
  return api.put(`/sessionLog/readCtMessage?sessionId=${sessionId}&userId=${userId}`)
}

// 商户阅读用户的消息（商户端使用）
export const readUserMessage = (sessionId: number, ctId: number): Promise<ApiResponse> => {
  return api.put(`/sessionLog/readUserMessage?sessionId=${sessionId}&ctId=${ctId}`)
}

// 获取会话窗口消息
export const getWindowMessage = (sessionId: number): Promise<MessageListResponse> => {
  return api.get(`/sessionLog/getWindowMessage?sessionId=${sessionId}`)
}

// 获取用户未读消息数量
export const getUserUnreadMessageCount = (sessionId: number): Promise<ApiResponse<number>> => {
  return api.get(`/sessionLog/userGetUnreadMessageCount?sessionId=${sessionId}`)
}

// 获取商户未读消息数量
export const getCtUnreadMessageCount = (sessionId: number): Promise<ApiResponse<number>> => {
  return api.get(`/sessionLog/ctGetUnreadMessageCount?sessionId=${sessionId}`)
}

export default {
  readUserMessage,
  readCtMessage,
  getWindowMessage,
  getUserUnreadMessageCount,
  getCtUnreadMessageCount
}
