import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminLogin, getAdminProfile } from '@/api/auth'

export interface AdminInfo {
  adminId: string
  username: string
  permissions: string[]
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('admin_token') || '')
  const adminInfo = ref<AdminInfo | null>(null)

  // 登录
  const login = async (username: string, password: string) => {
    const data = await adminLogin({ username, password })
    token.value = data.token
    adminInfo.value = {
      adminId: data.adminId,
      username: username,
      permissions: data.permissions
    }
    localStorage.setItem('admin_token', data.token)
    return data
  }

  // 登出
  const logout = () => {
    token.value = ''
    adminInfo.value = null
    localStorage.removeItem('admin_token')
  }

  // 获取管理员信息
  const getProfile = async () => {
    const data = await getAdminProfile()
    adminInfo.value = data
    return data
  }

  return {
    token,
    adminInfo,
    login,
    logout,
    getProfile
  }
})
