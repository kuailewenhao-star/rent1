// 数据看板接口封装

import { request } from './axios'

// 看板数据查询
export interface DashboardParams {
  timeRange?: string  // TODAY / THIS_MONTH / THIS_QUARTER / THIS_YEAR / CUSTOM
  startDate?: string  // timeRange=CUSTOM 时必填
  endDate?: string    // timeRange=CUSTOM 时必填
}

export interface DashboardResponse {
  pendingBills: BillSummary
  overdueBills: BillSummary
  effectiveDepositAmount: number
  rooms: RoomSummary
  profit: ProfitSummary
  timeRange: string
}

export interface BillSummary {
  count: number
  amount: number
}

export interface RoomSummary {
  total: number
  vacant: number
  occupied: number
  expiringSoon: number
}

export interface ProfitSummary {
  income: number
  expense: number
  profit: number
}

export const getDashboard = (params?: DashboardParams) => {
  return request.get<DashboardResponse>('/landlord/dashboard', params)
}

// 管理员看板（全平台数据）
export interface AdminDashboardResponse {
  totalMembers: number
  landlordCount: number
  tenantCount: number
  adminCount: number
  totalProperties: number
  totalRooms: number
  vacantRooms: number
  occupiedRooms: number
  totalContracts: number
  activeContracts: number
  expiredContracts: number
  totalIncome: number
  totalExpense: number
  profit: number
  totalDeposit: number
}

export const getAdminDashboard = () => {
  return request.get<AdminDashboardResponse>('/admin/dashboard')
}