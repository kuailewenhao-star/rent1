// 支出账单接口封装

import { request } from './axios'
import { PageParams, PageResult } from '@/types'

// 支出账单列表查询
export interface ExpenseBillListParams extends PageParams {
  costType?: string | null
  propertyName?: string
  landlordName?: string
  costDate?: string[]
}

export interface ExpenseBillListItem {
  expenseId: string
  propertyName: string
  landlordName: string
  costType: string
  costTypeName: string
  amount: number
  costDate: string
  remark: string
  createTime: string
  updateTime: string
}

export const getExpenseBillList = (params: ExpenseBillListParams) => {
  return request.get<PageResult<ExpenseBillListItem>>('/expense-invoices', params)
}

// 支出账单统计
export interface ExpenseBillStatsResponse {
  totalExpense: number
  propertyRentExpense: number
  otherExpense: number
}

export const getExpenseBillStats = (params?: ExpenseBillListParams) => {
  return request.get<ExpenseBillStatsResponse>('/expense-invoices/stats', params)
}

// 新增支出账单
export interface CreateExpenseInvoiceRequest {
  costType: string
  houseSourceId: string
  amount: number
  costDate: string
  remark?: string
}

export interface CreateExpenseInvoiceResponse {
  expenseId: string
  houseSourceName: string
  costType: string
  costTypeName: string
  amount: number
  costDate: string
  remark: string
  createTime: string
}

export const createExpenseInvoice = (data: CreateExpenseInvoiceRequest) => {
  return request.post<CreateExpenseInvoiceResponse>('/expense-invoices', data)
}

// 编辑支出账单
export interface UpdateExpenseInvoiceRequest {
  costType?: string
  amount?: number
  costDate?: string
  remark?: string
}

export const updateExpenseInvoice = (expenseId: string, data: UpdateExpenseInvoiceRequest) => {
  return request.put(`/expense-invoices/${expenseId}`, data)
}

// 删除支出账单
export const deleteExpenseInvoice = (expenseId: string) => {
  return request.delete(`/expense-invoices/${expenseId}`)
}