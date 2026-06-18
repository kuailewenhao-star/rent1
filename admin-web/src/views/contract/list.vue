<template>
  <div class="contract-list-container">
    <div class="page-header">
      <h1 class="page-title">合约列表</h1>
      <p class="page-subtitle">全平台合约数据管控，支持查看所有租赁合约</p>
    </div>

    <!-- 筛选区域 -->
    <div class="card-container filter-container">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="合约状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="履约中" :value="1" />
            <el-option label="已到期完结" :value="2" />
            <el-option label="提前解约" :value="3" />
            <el-option label="作废" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item label="房源">
          <el-input v-model="filterForm.propertyName" placeholder="房源名称" clearable style="width: 160px" />
        </el-form-item>

        <el-form-item label="房东">
          <el-input v-model="filterForm.landlordName" placeholder="房东姓名" clearable style="width: 120px" />
        </el-form-item>

        <el-form-item label="租客">
          <el-input v-model="filterForm.tenantName" placeholder="租客姓名" clearable style="width: 120px" />
        </el-form-item>

        <el-form-item label="合约编号">
          <el-input v-model="filterForm.contractId" placeholder="请输入" clearable style="width: 160px" />
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
        <el-table-column prop="contractId" label="合约编号" width="160" />
        <el-table-column prop="propertyName" label="房源" min-width="150" show-overflow-tooltip />
        <el-table-column prop="roomName" label="房间" min-width="120" />
        <el-table-column prop="landlordName" label="房东" width="100">
          <template #default="{ row }">
            {{ maskName(row.landlordName) }}
          </template>
        </el-table-column>
        <el-table-column prop="tenantName" label="租客" width="100">
          <template #default="{ row }">
            {{ maskName(row.tenantName) }}
          </template>
        </el-table-column>
        <el-table-column prop="monthlyRent" label="月租(元)" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.monthlyRent }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <span :class="['status-tag', getStatusClass(row.status)]">
              {{ getStatusName(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
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
import { request } from '@/api/axios'
import { ContractStatus, ContractStatusName } from '@/types'

const router = useRouter()

// 筛选表单
const filterForm = reactive({
  status: null as number | null,
  propertyName: '',
  landlordName: '',
  tenantName: '',
  contractId: ''
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

// 姓名脱敏
const maskName = (name: string): string => {
  if (!name) return '-'
  return name.charAt(0) + '***'
}

// 获取状态样式类
const getStatusClass = (status: number): string => {
  const classMap: Record<number, string> = {
    1: 'status-active',
    2: 'status-inactive',
    3: 'status-warning',
    4: 'status-danger'
  }
  return classMap[status] || ''
}

// 获取状态名称
const getStatusName = (status: number): string => {
  return ContractStatusName[status] || '-'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  filterForm.status = null
  filterForm.propertyName = ''
  filterForm.landlordName = ''
  filterForm.tenantName = ''
  filterForm.contractId = ''
  handleSearch()
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...filterForm
    }
    const res: any = await request.get('/contract/list', params)
    tableData.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    // 模拟数据
    tableData.value = [
      {
        contractId: 'HT202406001',
        propertyName: '万科城A栋101',
        roomName: '整套',
        landlordName: '张三',
        tenantName: '李四',
        monthlyRent: 4500,
        status: 1,
        startDate: '2024-06-01',
        endDate: '2025-05-31'
      },
      {
        contractId: 'HT202406002',
        propertyName: '龙湖小区5栋',
        roomName: '主卧',
        landlordName: '王五',
        tenantName: '赵六',
        monthlyRent: 1800,
        status: 1,
        startDate: '2024-06-15',
        endDate: '2025-06-14'
      },
      {
        contractId: 'HT202405001',
        propertyName: '保利公园B单元',
        roomName: '整套',
        landlordName: '孙七',
        tenantName: '周八',
        monthlyRent: 3800,
        status: 2,
        startDate: '2023-06-01',
        endDate: '2024-05-31'
      },
      {
        contractId: 'HT202404001',
        propertyName: '华润小区3栋',
        roomName: '次卧',
        landlordName: '吴九',
        tenantName: '郑十',
        monthlyRent: 1500,
        status: 3,
        startDate: '2024-04-01',
        endDate: '2025-03-31'
      }
    ]
    pagination.total = 4
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleView = (row: any) => {
  router.push(`/contract/detail/${row.contractId}`)
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

.contract-list-container {
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
</style>
