// 管理员认证接口封装

import { request } from './axios'

// 管理员登录
export interface AdminLoginRequest {
  username: string
  password: string
}

export interface AdminLoginResponse {
  token: string
  adminId: string
  permissions: string[]
}

export const adminLogin = (data: AdminLoginRequest): Promise<AdminLoginResponse> => {
  return request.post<AdminLoginResponse>('/admin/auth/login', data)
}

// 获取管理员信息
export interface AdminProfileResponse {
  adminId: string
  username: string
  permissions: string[]
}

export const getAdminProfile = (): Promise<AdminProfileResponse> => {
  return request.get<AdminProfileResponse>('/admin/auth/profile')
}

// 管理员登出
export const adminLogout = (): Promise<void> => {
  return request.post<void>('/admin/auth/logout')
}