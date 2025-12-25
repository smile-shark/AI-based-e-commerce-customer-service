import api from './index'

// 用户登录接口
export interface LoginRequest {
  account: string
  password: string
}

export interface LoginResponse {
  code: number
  message: string
  data: UserData | null
}

export interface UserData {
  id: number
  name: string
  account: string
  password: null
}

export const userLogin = (data: LoginRequest): Promise<LoginResponse> => {
  return api.post('/user/login', data)
}

export default {
  userLogin
}
