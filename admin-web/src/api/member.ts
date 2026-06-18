// 会员管理接口封装（管理员专用）

import { request } from './axios'
import { PageParams, PageResult } from '@/types'

// 会员列表查询
export interface MemberListParams extends PageParams {
  memberType?: number | null  // 会员类型筛选
  status?: number | null      // 账号状态筛选
  keyword?: string            // 关键词搜索
}

export interface MemberListItem {
  userId: string
  username: string
  phone: string
  mainId: string
  memberType: number
  memberTypeName: string
  status: number
  statusName: string
  createTime: string
  lastLoginTime: string
}

export const getMemberList = (params: MemberListParams) => {
  return request.get<PageResult<MemberListItem>>('/admin/members', params)
}

// 会员详情
export interface MemberDetailResponse {
  userId: string
  username: string
  phone: string
  mainId: string
  memberType: number
  memberTypeName: string
  status: number
  statusName: string
  createTime: string
  lastLoginTime: string
  projectId: string
  passportId: string
  roleId: string
  properties?: MemberProperty[]
  contracts?: MemberContract[]
  logs?: MemberLog[]
}

export interface MemberProperty {
  propertyId: string
  propertyName: string
  propertyType: number
  status: number
}

export interface MemberContract {
  contractId: string
  propertyName: string
  roomName: string
  status: number
  startDate: string
  endDate: string
}

export interface MemberLog {
  id: number
  createTime: string
  action: string
  detail: string
}

export const getMemberDetail = (memberId: string) => {
  return request.get<MemberDetailResponse>(`/admin/members/${memberId}`)
}

// 更新会员状态
export interface UpdateMemberStatusRequest {
  memberId: string
  status: number
}

export const updateMemberStatus = (memberId: string, status: number) => {
  return request.put(`/admin/members/${memberId}/status`, { status })
}

// 重置密码
export const resetMemberPassword = (memberId: string) => {
  return request.post(`/admin/members/${memberId}/reset-password`)
}