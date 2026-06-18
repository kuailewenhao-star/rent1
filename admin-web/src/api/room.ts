// 房间管理接口封装

import { request } from './axios'
import { PageParams, PageResult } from '@/types'

// 房间列表查询
export interface RoomListParams extends PageParams {
  status?: number | null
  propertyName?: string
  landlordName?: string
  minRent?: string
  maxRent?: string
  keyword?: string
}

export interface RoomListItem {
  roomId: string
  roomName: string
  propertyName: string
  propertyType: number
  landlordName: string
  roomArea: number
  monthlyRent: number
  deposit: number
  status: number
  tenantName: string
}

export const getRoomList = (params: RoomListParams) => {
  return request.get<PageResult<RoomListItem>>('/rooms', params)
}

// 房间详情
export interface RoomDetailResponse {
  roomId: string
  roomName: string
  propertyId: string
  propertyName: string
  roomArea: number
  monthlyRent: number
  deposit: number
  paymentCycle: string
  status: number
  createTime: string
  chargeRules: ChargeRuleItem[]
  tenantId: string
  tenantName: string
  tenantPhone: string
  contractId: string
  images: string[]
}

export interface ChargeRuleItem {
  feeType: string
  feeTypeName: string
  chargeType: string
  chargeValue: number
  chargeCycle: string
  remark: string
}

export const getRoomDetail = (roomId: string) => {
  return request.get<RoomDetailResponse>(`/rooms/${roomId}`)
}

// 计费规则查询
export interface BillingRulesResponse {
  rulesId: string
  roomId: string
  locked: boolean
  lockedAt: string | null
  items: BillingRuleItem[]
  createTime: string
  updateTime: string
}

export interface BillingRuleItem {
  feeType: string
  feeTypeName: string
  chargeType: string
  chargeValue: number
  chargeCycle: string
  remark: string
  immutable: boolean
}

export const getBillingRules = (roomId: string, contractId?: string) => {
  const params = contractId ? { contractId } : {}
  return request.get<BillingRulesResponse>(`/rooms/${roomId}/billing-rules`, params)
}

// 新增费用项
export interface AddBillingItemRequest {
  feeType: string
  chargeType: string
  chargeValue: number
  chargeCycle?: string
  remark?: string
}

export const addBillingItem = (roomId: string, data: AddBillingItemRequest) => {
  return request.post<BillingRulesResponse>(`/rooms/${roomId}/billing-rules`, data)
}

// 编辑费用项
export interface UpdateBillingItemRequest {
  chargeType?: string
  chargeValue?: number
  chargeCycle?: string
  remark?: string
}

export const updateBillingItem = (roomId: string, feeType: string, data: UpdateBillingItemRequest) => {
  return request.put<BillingRulesResponse>(`/rooms/${roomId}/billing-rules/${feeType}`, data)
}

// 删除费用项
export const deleteBillingItem = (roomId: string, feeType: string) => {
  return request.delete<BillingRulesResponse>(`/rooms/${roomId}/billing-rules/${feeType}`)
}