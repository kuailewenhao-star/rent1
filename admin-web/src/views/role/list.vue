<template>
  <div class="role-list-container">
    <div class="page-header">
      <h1 class="page-title">角色权限配置</h1>
      <p class="page-subtitle">系统角色与权限管理，支持查看/配置角色权限</p>
    </div>

    <!-- 角色列表 -->
    <div class="card-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="roleId" label="角色ID" width="100" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleType" label="角色类型" width="120">
          <template #default="{ row }">
            <span :class="['role-type-tag', `type-${row.roleType}`]">
              {{ getRoleTypeName(row.roleType) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="角色描述" min-width="200" />
        <el-table-column prop="memberCount" label="成员数量" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewPermissions(row)">查看权限</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 权限详情弹窗 -->
    <el-dialog
      v-model="permissionDialogVisible"
      :title="`${currentRole?.roleName || ''} - 权限配置`"
      width="700px"
    >
      <div v-if="currentRole" class="permission-tree-container">
        <el-tree
          ref="permissionTreeRef"
          :data="permissionTree"
          :props="{ label: 'permissionName', children: 'children' }"
          node-key="permissionId"
          show-checkbox
          default-expand-all
        />
      </div>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElTree } from 'element-plus'
import { request } from '@/api/axios'

// 表格数据
const tableData = ref<any[]>([])
const loading = ref(false)

// 权限弹窗
const permissionDialogVisible = ref(false)
const currentRole = ref<any>(null)
const permissionTreeRef = ref<InstanceType<typeof ElTree>>()
const permissionTree = ref<any[]>([])

// 角色类型名称
const getRoleTypeName = (type: number): string => {
  const typeMap: Record<number, string> = {
    1: '平台管理员',
    2: '房东',
    3: '租客'
  }
  return typeMap[type] || '-'
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/role/list')
    tableData.value = res.data
  } catch (error) {
    // 模拟数据
    tableData.value = [
      {
        roleId: 'R001',
        roleName: '超级管理员',
        roleType: 1,
        description: '系统最高权限，负责项目管理、角色权限配置、系统运维',
        memberCount: 3,
        status: 1
      },
      {
        roleId: 'R002',
        roleName: '运营管理员',
        roleType: 1,
        description: '负责日常运营管理、数据监控、问题处理',
        memberCount: 5,
        status: 1
      },
      {
        roleId: 'R003',
        roleName: '房东',
        roleType: 2,
        description: '负责房源管理、房间管理、合约管理、账单管理',
        memberCount: 42,
        status: 1
      },
      {
        roleId: 'R004',
        roleName: '租客',
        roleType: 3,
        description: '负责查看房间、合约、账单、缴费',
        memberCount: 108,
        status: 1
      }
    ]
  } finally {
    loading.value = false
  }
}

// 获取权限树
const fetchPermissionTree = async () => {
  try {
    const res: any = await request.get('/role/permissions')
    permissionTree.value = res.data
  } catch (error) {
    // 模拟权限树数据
    permissionTree.value = [
      {
        permissionId: 'P001',
        permissionName: '会员管理',
        children: [
          { permissionId: 'P001-1', permissionName: '查看会员列表' },
          { permissionId: 'P001-2', permissionName: '查看会员详情' },
          { permissionId: 'P001-3', permissionName: '禁用/启用账号' },
          { permissionId: 'P001-4', permissionName: '重置密码' }
        ]
      },
      {
        permissionId: 'P002',
        permissionName: '房源管理',
        children: [
          { permissionId: 'P002-1', permissionName: '查看房源列表' },
          { permissionId: 'P002-2', permissionName: '查看房源详情' }
        ]
      },
      {
        permissionId: 'P003',
        permissionName: '房间管理',
        children: [
          { permissionId: 'P003-1', permissionName: '查看房间列表' },
          { permissionId: 'P003-2', permissionName: '查看房间详情' }
        ]
      },
      {
        permissionId: 'P004',
        permissionName: '合约管理',
        children: [
          { permissionId: 'P004-1', permissionName: '查看合约列表' },
          { permissionId: 'P004-2', permissionName: '查看合约详情' }
        ]
      },
      {
        permissionId: 'P005',
        permissionName: '账单管理',
        children: [
          { permissionId: 'P005-1', permissionName: '查看收入账单' },
          { permissionId: 'P005-2', permissionName: '查看支出账单' }
        ]
      },
      {
        permissionId: 'P006',
        permissionName: '系统管理',
        children: [
          { permissionId: 'P006-1', permissionName: '角色权限配置' },
          { permissionId: 'P006-2', permissionName: '操作日志查看' }
        ]
      }
    ]
  }
}

// 查看权限
const handleViewPermissions = async (row: any) => {
  currentRole.value = row
  permissionDialogVisible.value = true

  if (permissionTree.value.length === 0) {
    await fetchPermissionTree()
  }

  // 设置已选中的权限（模拟）
  setTimeout(() => {
    if (permissionTreeRef.value && row.roleId === 'R001') {
      // 超级管理员选中所有权限
      const allPermissionIds = getAllPermissionIds(permissionTree.value)
      permissionTreeRef.value.setCheckedKeys(allPermissionIds)
    }
  }, 100)
}

// 递归获取所有权限ID
const getAllPermissionIds = (permissions: any[]): string[] => {
  const ids: string[] = []
  permissions.forEach(p => {
    ids.push(p.permissionId)
    if (p.children) {
      ids.push(...getAllPermissionIds(p.children))
    }
  })
  return ids
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.role-list-container {
  padding: $spacing-xl;
}

.page-header {
  margin-bottom: $spacing-xl;

  .page-title {
    font-size: $font-size-xxl;
    font-weight: 600;
    color: $color-text-primary;
  }

  .page-subtitle {
    margin-top: $spacing-sm;
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}

.role-type-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: $border-radius-tag;
  font-size: $font-size-xs;

  &.type-1 {
    background-color: #fffbeb;
    color: #f59e0b;
  }

  &.type-2 {
    background-color: #eff6ff;
    color: #2563eb;
  }

  &.type-3 {
    background-color: #ecfdf5;
    color: #10b981;
  }
}

.permission-tree-container {
  max-height: 500px;
  overflow-y: auto;

  :deep(.el-tree-node__content) {
    height: 36px;
  }
}
</style>
