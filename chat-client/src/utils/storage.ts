// 本地存储工具函数
import type { UserData as ApiUserData } from '../api/userApi'
import type { CtData as ApiCtData } from '../api/ctApi'

export type UserData = ApiUserData
export type CtData = ApiCtData

// 存储用户登录信息
export const setUserInfo = (userData: UserData) => {
  localStorage.setItem('userInfo', JSON.stringify(userData))
}

// 获取用户登录信息
export const getUserInfo = (): UserData | null => {
  const userInfo = localStorage.getItem('userInfo')
  return userInfo ? JSON.parse(userInfo) : null
}

// 清除用户登录信息
export const clearUserInfo = () => {
  localStorage.removeItem('userInfo')
}

// 存储商户登录信息
export const setCtInfo = (ctData: CtData) => {
  localStorage.setItem('ctInfo', JSON.stringify(ctData))
}

// 获取商户登录信息
export const getCtInfo = (): CtData | null => {
  const ctInfo = localStorage.getItem('ctInfo')
  return ctInfo ? JSON.parse(ctInfo) : null
}

// 清除商户登录信息
export const clearCtInfo = () => {
  localStorage.removeItem('ctInfo')
}
