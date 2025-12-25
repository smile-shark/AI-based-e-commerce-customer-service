import api from './index'

// 商品相关接口

// 商品列表响应
export interface GoodsListResponse {
  code: number
  message: string
  data: {
    records: GoodsItem[]
    total: number
    size: number
    current: number
    pages: number
  }
}

// 商品项
export interface GoodsItem {
  id: number
  name: string
  ctId: number
}

// 商品详情响应
export interface GoodsDetailResponse {
  code: number
  message: string
  data: GoodsDetail
}

// 商品详情
export interface GoodsDetail {
  id: number
  name: string
  ctId: number
  documents: DocumentItem[]
}

// 文档项
export interface DocumentItem {
  id: number
  name: string
  goodsId: number
}

// 商品添加请求
export interface AddGoodsRequest {
  name: string
  ctId: number
}

// 商品修改请求
export interface UpdateGoodsRequest {
  id: number
  name: string
}

// 通用响应
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 获取商品列表
export const getGoodsList = (pageNum: number = 1, pageSize: number = 10): Promise<GoodsListResponse> => {
  return api.get(`/goods/pageInfo?pageNum=${pageNum}&pageSize=${pageSize}`)
}

// 添加商品
export const addGoods = (data: AddGoodsRequest): Promise<ApiResponse<number>> => {
  return api.post('/goods/add', data)
}

// 修改商品
export const updateGoods = (data: UpdateGoodsRequest): Promise<ApiResponse<boolean>> => {
  return api.put('/goods/update', data)
}

// 删除商品
export const deleteGoods = (id: number): Promise<ApiResponse<boolean>> => {
  return api.delete(`/goods/delete?id=${id}`)
}

// 获取商品详情
export const getGoodsDetail = (id: number): Promise<GoodsDetailResponse> => {
  return api.get(`/goods/detailById?id=${id}`)
}

// 上传文档
export const uploadDocument = (file: File, goodsId: number): Promise<ApiResponse<any>> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('goodsId', goodsId.toString())

  return api.post('/goodsDocument/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除文档
export const deleteDocument = (id: number): Promise<ApiResponse<boolean>> => {
  return api.delete(`/goodsDocument/delete?id=${id}`)
}

export default {
  getGoodsList,
  addGoods,
  updateGoods,
  deleteGoods,
  getGoodsDetail,
  uploadDocument,
  deleteDocument
}
