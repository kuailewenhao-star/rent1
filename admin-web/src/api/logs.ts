// 操作日志接口封装

import { request } from './axios'
import { PageParams, PageResult } from '@/types'

// 操作日志列表查询
export interface LogListParams extends PageParams {
  operatorName?: string
  actionType?: number | null
  module?: number | null
  dateRange?: string[]
}

export interface LogListItem {
  logId: string
  operatorName: string
  operatorRole: number
  actionType: number
  module: number
  description: string
  ipAddress: string
  userAgent: string
  createTime: string
}

export const getLogList = (params: LogListParams) => {
  return request.get<PageResult<LogListItem>>('/admin/logs', params)
}

// 导出日志
export const exportLogs = (params?: LogListParams) => {
  return request.get('/admin/logs/export', params)
}