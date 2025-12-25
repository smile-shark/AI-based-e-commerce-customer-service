import api from './index'

// 商户登录接口
export interface LoginRequest {
  account: string
  password: string
}

export interface LoginResponse {
  code: number
  message: string
  data: CtData | null
}

export interface CtData {
  id: number
  name: string
  account: string
  password: null
}

export const ctLogin = (data: LoginRequest): Promise<LoginResponse> => {
  return api.post('/commercialTenant/login', data)
}

export default {
  ctLogin
}
