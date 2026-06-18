// 合约管理接口封装

import { request } from './axios'
import { PageParams, PageResult } from '@/types'

// 合约列表查询
export interface ContractListParams extends PageParams {
  status?: number | null
  propertyName?: string
  landlordName?: string
  tenantName?: string
  contractId?: string
}

export interface ContractListItem {
  contractId: string
  propertyName: string
  roomName: string
  landlordName: string
  tenantName: string
  monthlyRent: number
  status: number
  startDate: string
  endDate: string
}

export const getContractList = (params: ContractListParams) => {
  return request.get<PageResult<ContractListItem>>('/contracts', params)
}

// 合约详情
export interface ContractDetailResponse {
  contractId: string
  status: number
  createTime: string
  propertyId: string
  propertyName: string
  roomId: string
  roomName: string
  landlordId: string
  landlordName: string
  tenantId: string
  tenantName: string
  emergencyContact: string
  emergencyPhone: string
  startDate: string
  endDate: string
  remainingDays: number
  chargeRules: ContractChargeRule[]
  contractImages: string[]
  bills: ContractBill[]
}

export interface ContractChargeRule {
  feeType: string
  feeTypeName: string
  chargeType: string
  chargeValue: number
  chargeCycle: string
  remark: string
}

export interface ContractBill {
  billId: string
  billType: number
  amount: number
  status: number
  periodStart: string
  periodEnd: string
  createTime: string
}

export const getContractDetail = (contractId: string) => {
  return request.get<ContractDetailResponse>(`/contracts/${contractId}`)
}

// 创建合约
export interface CreateContractRequest {
  roomId: string
  tenantId: string
  startDate: string
  endDate: string
  emergencyContactName: string
  emergencyContactPhone: string
}

export interface CreateContractResponse {
  contractId: string
  status: number
}

export const createContract = (data: CreateContractRequest) => {
  return request.post<CreateContractResponse>('/contracts', data)
}

// 提前解约
export interface TerminateContractRequest {
  terminationType: string
  terminationReason: string
}

export const terminateContract = (contractId: string, data: TerminateContractRequest) => {
  return request.post(`/contracts/${contractId}/terminate`, data)
}

// 作废合约
export interface VoidContractRequest {
  voidReason: string
}

export const voidContract = (contractId: string, data: VoidContractRequest) => {
  return request.post(`/contracts/${contractId}/void`, data)
}