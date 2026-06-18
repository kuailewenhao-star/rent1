// 路由认证守卫 — 未登录重定向到 /login
import { Navigate, useLocation } from 'react-router-dom'

const PROTECTED_ROUTES = ['/home', '/archive', '/bill', '/contracts', '/messages', '/profile', '/tenant-home']

export default function AuthGuard({ children }) {
  const location = useLocation()
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true'

  // 登录/角色选择页面不需要守卫
  if (!PROTECTED_ROUTES.some(route => location.pathname.startsWith(route)) && location.pathname !== '/' && location.pathname !== '/login' && location.pathname !== '/role-select') {
    return children
  }

  if (!isLoggedIn && PROTECTED_ROUTES.some(route => location.pathname.startsWith(route))) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return children
}
