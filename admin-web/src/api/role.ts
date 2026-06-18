// 角色权限接口封装

import { request } from './axios'

// 角色列表
export interface RoleListItem {
  roleId: string
  roleName: string
  roleType: number
  description: string
  memberCount: number
  status: number
}

export const getRoleList = () => {
  return request.get<RoleListItem[]>('/admin/roles')
}

// 角色权限详情
export interface PermissionNode {
  permissionId: string
  permissionName: string
  children?: PermissionNode[]
}

export interface RolePermissionResponse {
  roleId: string
  roleName: string
  permissions: PermissionNode[]
}

export const getRolePermissions = (roleId: string) => {
  return request.get<RolePermissionResponse>(`/admin/roles/${roleId}/permissions`)
}