<template>
  <div class="income-bill-list-container">
    <div class="page-header">
      <h1 class="page-title">收入账单列表</h1>
      <p class="page-subtitle">全平台收入账单管控，支持查看所有租客缴费账单</p>
    </div>

    <!-- 筛选区域 -->
    <div class="card-container filter-container">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="账单状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待支付" :value="1" />
            <el-option label="已支付" :value="2" />
            <el-option label="逾期未付" :value="3" />
            <el-option label="押金已收" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item label="账单类型">
          <el-select v-model="filterForm.billType" placeholder="全部" clearable style="width: 120px">
            <el-option label="租金" :value="1" />
            <el-option label="押金" :value="2" />
            <el-option label="水费" :value="3" />
            <el-option label="电费" :value="4" />
            <el-option label="燃气费" :value="5" />
            <el-option label="宽带费" :value="6" />
            <el-option label="物业费" :value="7" />
            <el-option label="垃圾清运费" :value="8" />
            <el-option label="其他杂费" :value="9" />
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

        <el-form-item label="账期">
          <el-date-picker
            v-model="filterForm.period"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
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

    <!-- 统计卡片 -->
    <el-row :gutter="24" class="stat-cards">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value text-primary">¥{{ formatNumber(stats.totalAmount) }}</div>
          <div class="stat-label">账单总额</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value text-warning">¥{{ formatNumber(stats.pendingAmount) }}</div>
          <div class="stat-label">待支付</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value text-danger">¥{{ formatNumber(stats.overdueAmount) }}</div>
          <div class="stat-label">逾期未付</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value text-success">¥{{ formatNumber(stats.paidAmount) }}</div>
          <div class="stat-label">已支付</div>
        </div>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <div class="card-container table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="billId" label="账单编号" width="160" />
        <el-table-column prop="propertyName" label="房源" min-width="150" show-overflow-tooltip />
        <el-table-column prop="roomName" label="房间" min-width="100" />
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
        <el-table-column prop="billType" label="账单类型" width="100">
          <template #default="{ row }">
            <span :class="['bill-type-tag', `type-${row.billType}`]">
              {{ getBillTypeName(row.billType) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100" align="right">
          <template #default="{ row }">
            <span class="amount-value">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-tag', getStatusClass(row.status)]">
              {{ getStatusName(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="periodStart" label="账期" width="200">
          <template #default="{ row }">
            {{ row.periodStart }} 至 {{ row.periodEnd }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" width="180" />
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
import { request } from '@/api/axios'
import { BillStatus, BillStatusName, BillTypeName } from '@/types'

// 筛选表单
const filterForm = reactive({
  status: null as number | null,
  billType: null as number | null,
  propertyName: '',
  landlordName: '',
  tenantName: '',
  period: null as string[] | null
})

// 统计数据
const stats = reactive({
  totalAmount: 0,
  pendingAmount: 0,
  overdueAmount: 0,
  paidAmount: 0
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

// 数字格式化
const formatNumber = (num: number): string => {
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 获取账单类型名称
const getBillTypeName = (type: number): string => {
  return BillTypeName[type] || '-'
}

// 获取状态样式类
const getStatusClass = (status: number): string => {
  const classMap: Record<number, string> = {
    1: 'status-warning',
    2: 'status-active',
    3: 'status-danger',
    4: 'status-info'
  }
  return classMap[status] || ''
}

// 获取状态名称
const getStatusName = (status: number): string => {
  return BillStatusName[status] || '-'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  filterForm.status = null
  filterForm.billType = null
  filterForm.propertyName = ''
  filterForm.landlordName = ''
  filterForm.tenantName = ''
  filterForm.period = null
  handleSearch()
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const res: any = await request.get('/income-bill/stats', filterForm)
    Object.assign(stats, res.data)
  } catch (error) {
    stats.totalAmount = 1256800
    stats.pendingAmount = 45600
    stats.overdueAmount = 12300
    stats.paidAmount = 1198900
  }
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
    const res: any = await request.get('/income-bill/list', params)
    tableData.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    // 模拟数据
    tableData.value = [
      {
        billId: 'B001',
        propertyName: '万科城A栋101',
        roomName: '整套',
        landlordName: '张三',
        tenantName: '李四',
        billType: 2,
        amount: 9000,
        status: 4,
        periodStart: '2024-06-01',
        periodEnd: '2024-06-01',
        createTime: '2024-06-01 10:35:00'
      },
      {
        billId: 'B002',
        propertyName: '万科城A栋101',
        roomName: '整套',
        landlordName: '张三',
        tenantName: '李四',
        billType: 1,
        amount: 4500,
        status: 2,
        periodStart: '2024-06-01',
        periodEnd: '2024-06-30',
        createTime: '2024-06-01 10:35:00'
      },
      {
        billId: 'B003',
        propertyName: '龙湖小区5栋',
        roomName: '主卧',
        landlordName: '王五',
        tenantName: '赵六',
        billType: 1,
        amount: 1800,
        status: 1,
        periodStart: '2024-06-15',
        periodEnd: '2024-07-14',
        createTime: '2024-06-15 00:00:00'
      },
      {
        billId: 'B004',
        propertyName: '龙湖小区5栋',
        roomName: '次卧',
        landlordName: '王五',
        tenantName: '孙七',
        billType: 1,
        amount: 1500,
        status: 3,
        periodStart: '2024-05-15',
        periodEnd: '2024-06-14',
        createTime: '2024-05-15 00:00:00'
      }
    ]
    pagination.total = 4
  } finally {
    loading.value = false
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
  fetchStats()
  fetchData()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.income-bill-list-container {
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

.stat-cards {
  margin-bottom: $spacing-xl;
}

.stat-card {
  padding: $spacing-xl;
  background-color: $color-bg-card;
  border-radius: $border-radius-card;
  box-shadow: $shadow-sm;
  text-align: center;

  .stat-value {
    font-size: $font-size-xxxl;
    font-weight: 600;
  }

  .stat-label {
    margin-top: $spacing-sm;
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}

.text-primary {
  color: $color-primary;
}

.text-success {
  color: $color-success;
}

.text-warning {
  color: $color-warning;
}

.text-danger {
  color: $color-danger;
}

.bill-type-tag {
  display: inline-block;
  padding: 2px 6px;
  border-radius: $border-radius-tag;
  font-size: $font-size-xs;

  &.type-1 { background-color: #eff6ff; color: #2563eb; }
  &.type-2 { background-color: #fdf4ff; color: #9333ea; }
  &.type-3 { background-color: #ecfeff; color: #0891b2; }
  &.type-4 { background-color: #fef3c7; color: #d97706; }
  &.type-5 { background-color: #fce7f3; color: #db2777; }
  &.type-6 { background-color: #f1f5f9; color: #64748b; }
  &.type-7 { background-color: #dcfce7; color: #16a34a; }
  &.type-8 { background-color: #f0fdf4; color: #15803d; }
  &.type-9 { background-color: #f5f5f5; color: #737373; }
}

.amount-value {
  color: $color-primary;
  font-weight: 500;
}
</style>
