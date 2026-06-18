// 房源管理接口封装

import { request } from './axios'
import { PageParams, PageResult } from '@/types'

// 房源列表查询
export interface PropertyListParams extends PageParams {
  propertyType?: number | null
  businessStatus?: number | null
  province?: string
  city?: string
  keyword?: string
}

export interface PropertyListItem {
  propertyId: string
  propertyName: string
  propertyType: number
  address: string
  landlordName: string
  roomCount: number
  rentedCount: number
  vacantCount: number
  businessStatus: number
}

export const getPropertyList = (params: PropertyListParams) => {
  return request.get<PageResult<PropertyListItem>>('/house-sources', params)
}

// 房源详情
export interface PropertyDetailResponse {
  propertyId: string
  propertyName: string
  propertyType: number
  roomLayout: string
  businessStatus: number
  createTime: string
  province: string
  city: string
  district: string
  address: string
  leaseStartDate: string
  leaseEndDate: string
  landlordId: string
  landlordName: string
  rooms: PropertyRoom[]
  totalIncome: number
  totalExpense: number
  profit: number
  currentDeposit: number
}

export interface PropertyRoom {
  roomId: string
  roomName: string
  roomArea: number
  monthlyRent: number
  deposit: number
  status: number
}

export const getPropertyDetail = (propertyId: string) => {
  return request.get<PropertyDetailResponse>(`/house-sources/${propertyId}`)
}

// 房源盈利详情
export interface ProfitDetailParams {
  timeRange?: string
  startDate?: string
  endDate?: string
}

export interface ProfitDetailResponse {
  incomeTotal: number
  expenseTotal: number
  profit: number
  incomeList: IncomeItem[]
  expenseList: ExpenseItem[]
}

export interface IncomeItem {
  invoiceId: string
  feeTypeCode: string
  feeTypeName: string
  roomId: string
  roomName: string
  cycleStart: string
  cycleEnd: string
  amount: number
  isManual: boolean
  status: string
}

export interface ExpenseItem {
  expenseId: string
  costTypeCode: string
  costTypeName: string
  costDate: string
  amount: number
  remark: string
}

export const getPropertyProfitDetail = (propertyId: string, params?: ProfitDetailParams) => {
  return request.get<ProfitDetailResponse>(`/landlord/house-sources/${propertyId}/profit-detail`, params)
}