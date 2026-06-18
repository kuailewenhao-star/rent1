import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 静态路由
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  }
]

// 动态路由（需要权限）
export const asyncRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/components/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页看板', icon: 'Odometer' }
      },
      {
        path: 'member',
        name: 'Member',
        component: () => import('@/components/layout/index.vue'),
        redirect: '/member/list',
        meta: { title: '会员管理', icon: 'User' },
        children: [
          {
            path: 'list',
            name: 'MemberList',
            component: () => import('@/views/member/list.vue'),
            meta: { title: '会员列表' }
          },
          {
            path: 'detail/:id',
            name: 'MemberDetail',
            component: () => import('@/views/member/detail.vue'),
            meta: { title: '会员详情', hidden: true }
          }
        ]
      },
      {
        path: 'property',
        name: 'Property',
        component: () => import('@/components/layout/index.vue'),
        redirect: '/property/list',
        meta: { title: '房源管理', icon: 'House' },
        children: [
          {
            path: 'list',
            name: 'PropertyList',
            component: () => import('@/views/property/list.vue'),
            meta: { title: '房源列表' }
          },
          {
            path: 'detail/:id',
            name: 'PropertyDetail',
            component: () => import('@/views/property/detail.vue'),
            meta: { title: '房源详情', hidden: true }
          }
        ]
      },
      {
        path: 'room',
        name: 'Room',
        component: () => import('@/components/layout/index.vue'),
        redirect: '/room/list',
        meta: { title: '房间管理', icon: 'DoorRight' },
        children: [
          {
            path: 'list',
            name: 'RoomList',
            component: () => import('@/views/room/list.vue'),
            meta: { title: '房间列表' }
          },
          {
            path: 'detail/:id',
            name: 'RoomDetail',
            component: () => import('@/views/room/detail.vue'),
            meta: { title: '房间详情', hidden: true }
          }
        ]
      },
      {
        path: 'contract',
        name: 'Contract',
        component: () => import('@/components/layout/index.vue'),
        redirect: '/contract/list',
        meta: { title: '合约管理', icon: 'Document' },
        children: [
          {
            path: 'list',
            name: 'ContractList',
            component: () => import('@/views/contract/list.vue'),
            meta: { title: '合约列表' }
          },
          {
            path: 'detail/:id',
            name: 'ContractDetail',
            component: () => import('@/views/contract/detail.vue'),
            meta: { title: '合约详情', hidden: true }
          }
        ]
      },
      {
        path: 'income-bill',
        name: 'IncomeBill',
        component: () => import('@/components/layout/index.vue'),
        redirect: '/income-bill/list',
        meta: { title: '收入账单', icon: 'Money' },
        children: [
          {
            path: 'list',
            name: 'IncomeBillList',
            component: () => import('@/views/income-bill/list.vue'),
            meta: { title: '收入账单列表' }
          }
        ]
      },
      {
        path: 'expense-bill',
        name: 'ExpenseBill',
        component: () => import('@/components/layout/index.vue'),
        redirect: '/expense-bill/list',
        meta: { title: '支出账单', icon: 'Coin' },
        children: [
          {
            path: 'list',
            name: 'ExpenseBillList',
            component: () => import('@/views/expense-bill/list.vue'),
            meta: { title: '支出账单列表' }
          }
        ]
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/components/layout/index.vue'),
        redirect: '/role/list',
        meta: { title: '角色权限', icon: 'Key' },
        children: [
          {
            path: 'list',
            name: 'RoleList',
            component: () => import('@/views/role/list.vue'),
            meta: { title: '角色列表' }
          }
        ]
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('@/components/layout/index.vue'),
        redirect: '/logs/list',
        meta: { title: '日志溯源', icon: 'Clock' },
        children: [
          {
            path: 'list',
            name: 'LogList',
            component: () => import('@/views/logs/list.vue'),
            meta: { title: '操作日志' }
          }
        ]
      }
    ]
  }
]

// 创建路由实例 — 注册所有路由（静态 + 动态）
const router = createRouter({
  history: createWebHistory(),
  routes: [...constantRoutes, ...asyncRoutes]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 房东租赁管理系统`
  }

  // 公开路由直接通过
  if (to.meta.public) {
    return next()
  }

  // 检查登录状态
  if (!userStore.token) {
    return next('/login')
  }

  next()
})

export default router
