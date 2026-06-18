<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ isCollapsed: isCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <el-icon size="24"><House /></el-icon>
          <span v-show="!isCollapsed" class="logo-text">租赁管理系统</span>
        </div>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        background-color="#1e293b"
        text-color="#94a3b8"
        active-text-color="#ffffff"
        router
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <!-- 有子菜单 -->
          <el-sub-menu v-if="route.children && route.children.length > 0" :index="route.path">
            <template #title>
              <el-icon><component :is="route.meta?.icon" /></el-icon>
              <span>{{ route.meta?.title }}</span>
            </template>
            <el-menu-item
              v-for="child in route.children"
              :key="child.path"
              :index="route.path + '/' + child.path"
            >
              {{ child.meta?.title }}
            </el-menu-item>
          </el-sub-menu>

          <!-- 无子菜单 -->
          <el-menu-item v-else :index="route.path">
            <el-icon><component :is="route.meta?.icon" /></el-icon>
            <span>{{ route.meta?.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </aside>

    <!-- 主内容区 -->
    <div class="main-wrapper">
      <!-- 头部 -->
      <header class="header">
        <div class="header-left">
          <el-button text @click="toggleSidebar">
            <el-icon size="20"><Fold v-if="!isCollapsed" /><Expand v-else /></el-icon>
          </el-button>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" bg-color="#2563eb">
                {{ userStore.adminInfo?.username?.charAt(0) || 'A' }}
              </el-avatar>
              <span class="username">{{ userStore.adminInfo?.username || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { asyncRoutes } from '@/router'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapsed = ref(false)

// 获取菜单路由（扁平化一级菜单）
const menuRoutes = computed(() => {
  const mainRoute = asyncRoutes.find(r => r.path === '/')
  return mainRoute?.children?.filter(child => !child.meta?.hidden) || []
})

// 当前激活菜单
const activeMenu = computed(() => {
  const path = route.path
  const menuItem = menuRoutes.value.find(item =>
    item.children?.some(child => path.includes(child.path)) ||
    path.includes(item.path)
  )
  return menuItem?.path || path
})

// 切换侧边栏
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// 用户菜单命令
const handleCommand = (command: string) => {
  switch (command) {
    case 'logout':
      userStore.logout()
      router.push('/login')
      break
    case 'profile':
      // 个人中心
      break
  }
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.layout-container {
  display: flex;
  height: 100vh;
}

// 侧边栏
.sidebar {
  width: $layout-sidebar-width;
  background-color: $color-bg-sidebar;
  transition: width 0.3s ease;
  overflow: hidden;

  &.isCollapsed {
    width: 64px;
  }

  .sidebar-header {
    height: $layout-header-height;
    display: flex;
    align-items: center;
    padding: 0 $spacing-lg;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    .logo {
      display: flex;
      align-items: center;
      gap: $spacing-md;
      color: #fff;

      .logo-text {
        font-size: $font-size-lg;
        font-weight: 600;
        white-space: nowrap;
      }
    }
  }

  :deep(.el-menu) {
    border-right: none;

    .el-menu-item,
    .el-sub-menu__title {
      &:hover {
        background-color: rgba(255, 255, 255, 0.05) !important;
      }
    }

    .el-menu-item.is-active {
      background-color: $color-primary !important;
    }
  }
}

// 主内容区
.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 头部
.header {
  height: $layout-header-height;
  background-color: $color-bg-header;
  border-bottom: 1px solid $color-border;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-xl;

  .header-left {
    display: flex;
    align-items: center;
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      cursor: pointer;

      .username {
        font-size: $font-size-sm;
        color: $color-text-primary;
      }
    }
  }
}

// 内容区
.main-content {
  flex: 1;
  overflow-y: auto;
  background-color: $color-bg-page;
}

// 路由过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
