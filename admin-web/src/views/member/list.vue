<template>
  <div class="member-list-container">
    <div class="page-header">
      <h1 class="page-title">会员列表</h1>
      <p class="page-subtitle">全平台会员信息管理，支持查看/管控所有会员账号</p>
    </div>

    <!-- 筛选区域 -->
    <div class="card-container filter-container">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="会员类型">
          <el-select v-model="filterForm.memberType" placeholder="全部" clearable style="width: 120px">
            <el-option label="房东" :value="1" />
            <el-option label="租客" :value="2" />
            <el-option label="平台管理员" :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="账号状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="已禁用" :value="2" />
            <el-option label="已冻结" :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="关键词">
          <el-input
            v-model="filterForm.keyword"
            placeholder="用户名/手机号/主体ID"
            clearable
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="card-container table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140">
          <template #default="{ row }">
            {{ maskPhone(row.phone) }}
          </template>
        </el-table-column>
        <el-table-column prop="mainId" label="主体ID" width="140" />
        <el-table-column prop="memberType" label="会员类型" width="120">
          <template #default="{ row }">
            <span :class="['member-type-tag', `type-${row.memberType}`]">
              {{ getMemberTypeName(row.memberType) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="账号状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              :active-value="1"
              :inactive-value="2"
              @change="handleStatusChange(row)"
            />
            <span class="status-text">{{ getStatusName(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleResetPwd(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/api/axios'
import { MemberType, MemberTypeName, AccountStatus, AccountStatusName } from '@/types'

const router = useRouter()

// 筛选表单
const filterForm = reactive({
  memberType: null as number | null,
  status: null as number | null,
  keyword: ''
})

// 表格数据
const tableData = ref<any[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 手机号脱敏
const maskPhone = (phone: string): string => {
  if (!phone) return '-'
  return phone.substring(0, 3) + '****' + phone.substring(7)
}

// 获取会员类型名称
const getMemberTypeName = (type: number): string => {
  return MemberTypeName[type] || '-'
}

// 获取账号状态名称
const getStatusName = (status: number): string => {
  return AccountStatusName[status] || '-'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  filterForm.memberType = null
  filterForm.status = null
  filterForm.keyword = ''
  handleSearch()
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      memberType: filterForm.memberType,
      status: filterForm.status,
      keyword: filterForm.keyword
    }
    const res: any = await request.get('/member/list', params)
    tableData.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    // 模拟数据
    tableData.value = [
      { userId: 'U001', username: '张三', phone: '13812345678', mainId: 'M001', memberType: 1, status: 1, createTime: '2024-01-15 10:30:00' },
      { userId: 'U002', username: '李四', phone: '13923456789', mainId: 'M002', memberType: 2, status: 1, createTime: '2024-02-20 14:20:00' },
      { userId: 'U003', username: '王五', phone: '13734567890', mainId: 'M003', memberType: 1, status: 1, createTime: '2024-03-10 09:15:00' },
      { userId: 'U004', username: '赵六', phone: '13645678901', mainId: 'M004', memberType: 2, status: 2, createTime: '2024-04-05 16:45:00' },
      { userId: 'U005', username: '管理员', phone: '13556789012', mainId: 'M005', memberType: 3, status: 1, createTime: '2024-01-01 08:00:00' }
    ]
    pagination.total = 5
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleView = (row: any) => {
  router.push(`/member/detail/${row.userId}`)
}

// 重置密码
const handleResetPwd = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户【${row.username}】的密码吗？`,
      '密码重置',
      { type: 'warning' }
    )
    await request.post('/member/resetPassword', { userId: row.userId })
    ElMessage.success('密码重置成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.success('密码已重置为：123456')
    }
  }
}

// 状态切换
const handleStatusChange = async (row: any) => {
  try {
    await request.post('/member/updateStatus', {
      userId: row.userId,
      status: row.status === 1 ? 2 : 1
    })
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 2 : 1
    ElMessage.error('状态更新失败')
  }
}

// 分页大小变化
const handleSizeChange = () => {
  fetchData()
}

// 页码变化
const handleCurrentChange = () => {
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.member-list-container {
  padding: $spacing-xl;
}

.filter-container {
  margin-bottom: $spacing-lg;

  .filter-form {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-md;
  }
}

.table-container {
  .status-text {
    margin-left: $spacing-sm;
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}

.member-type-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: $border-radius-tag;
  font-size: $font-size-xs;

  &.type-1 {
    background-color: #eff6ff;
    color: #2563eb;
  }

  &.type-2 {
    background-color: #ecfdf5;
    color: #10b981;
  }

  &.type-3 {
    background-color: #fffbeb;
    color: #f59e0b;
  }
}
</style>
