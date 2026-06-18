// 收入账单接口封装

import { request } from './axios'
import { PageParams, PageResult } from '@/types'

// 收入账单列表查询
export interface IncomeBillListParams extends PageParams {
  status?: string | null
  billType?: string | null
  propertyName?: string
  landlordName?: string
  tenantName?: string
  period?: string[]
}

export interface IncomeBillListItem {
  invoiceId: string
  propertyName: string
  roomName: string
  landlordName: string
  tenantName: string
  billType: string
  billTypeName: string
  amount: number
  status: string
  statusName: string
  periodStart: string
  periodEnd: string
  createTime: string
  isManual: boolean
  isOverdue: boolean
  overdueDays: number
}

export const getIncomeBillList = (params: IncomeBillListParams) => {
  return request.get<PageResult<IncomeBillListItem>>('/income-invoices', params)
}

// 收入账单统计
export interface IncomeBillStatsResponse {
  totalAmount: number
  pendingAmount: number
  overdueAmount: number
  paidAmount: number
}

export const getIncomeBillStats = (params?: IncomeBillListParams) => {
  return request.get<IncomeBillStatsResponse>('/income-invoices/stats', params)
}

// 手动录入杂费账单
export interface ManualInvoiceRequest {
  feeType: string
  amount: number
  roomId: string
  billMonth: string
  remark?: string
}

export interface ManualInvoiceResponse {
  invoiceId: string
  contractId: string
  tenantName: string
  roomName: string
  houseSourceName: string
  feeType: string
  feeTypeName: string
  amount: number
  cycleStart: string
  cycleEnd: string
  status: string
  statusName: string
  isManual: boolean
}

export const createManualInvoice = (data: ManualInvoiceRequest) => {
  return request.post<ManualInvoiceResponse>('/income-invoices/manual', data)
}

// 公摊费用录入
export interface SharedInvoiceRequest {
  feeType: string
  totalAmount: number
  houseSourceId: string
  billMonth: string
}

export interface SharedInvoiceResponse {
  splitCount: number
  invoices: SharedInvoiceItem[]
}

export interface SharedInvoiceItem {
  roomId: string
  amount: number
  ratio: number
  tenantName: string
}

export const createSharedInvoice = (data: SharedInvoiceRequest) => {
  return request.post<SharedInvoiceResponse>('/income-invoices/shared', data)
}

// 账单核销
export interface InvoicePayRequest {
  paidTime?: string
}

export const payInvoice = (invoiceId: string, data?: InvoicePayRequest) => {
  return request.put(`/income-invoices/${invoiceId}/pay`, data)
}