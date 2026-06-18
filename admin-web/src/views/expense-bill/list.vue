<template>
  <div class="expense-bill-list-container">
    <div class="page-header">
      <h1 class="page-title">支出账单列表</h1>
      <p class="page-subtitle">全平台支出账单溯源，支持查看所有房东成本支出台账</p>
    </div>

    <!-- 筛选区域 -->
    <div class="card-container filter-container">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="支出类型">
          <el-select v-model="filterForm.costType" placeholder="全部" clearable style="width: 160px">
            <el-option label="房源租金支出" :value="1" />
            <el-option label="房源押金支出" :value="2" />
            <el-option label="水费支出" :value="3" />
            <el-option label="电费支出" :value="4" />
            <el-option label="燃气费支出" :value="5" />
            <el-option label="宽带费支出" :value="6" />
            <el-option label="物业费支出" :value="7" />
            <el-option label="垃圾清运费支出" :value="8" />
            <el-option label="房屋维修支出" :value="9" />
            <el-option label="其他支出" :value="10" />
          </el-select>
        </el-form-item>

        <el-form-item label="房源">
          <el-input v-model="filterForm.propertyName" placeholder="房源名称" clearable style="width: 160px" />
        </el-form-item>

        <el-form-item label="房东">
          <el-input v-model="filterForm.landlordName" placeholder="房东姓名" clearable style="width: 120px" />
        </el-form-item>

        <el-form-item label="发生时间">
          <el-date-picker
            v-model="filterForm.costDate"
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
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-value text-danger">¥{{ formatNumber(stats.totalExpense) }}</div>
          <div class="stat-label">支出总额</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-value">¥{{ formatNumber(stats.propertyRentExpense) }}</div>
          <div class="stat-label">房源租金支出</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-value">¥{{ formatNumber(stats.otherExpense) }}</div>
          <div class="stat-label">其他支出</div>
        </div>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <div class="card-container table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="billId" label="支出账单ID" width="140" />
        <el-table-column prop="propertyName" label="关联房源" min-width="150" show-overflow-tooltip />
        <el-table-column prop="landlordName" label="房东" width="120">
          <template #default="{ row }">
            {{ maskName(row.landlordName) }}
          </template>
        </el-table-column>
        <el-table-column prop="costType" label="支出类型" width="130">
          <template #default="{ row }">
            <span :class="['expense-type-tag', `type-${row.costType}`]">
              {{ getExpenseTypeName(row.costType) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="costAmount" label="支出金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-value">¥{{ row.costAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="costDate" label="发生时间" width="120" />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
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
import { ExpenseTypeName } from '@/types'

// 筛选表单
const filterForm = reactive({
  costType: null as number | null,
  propertyName: '',
  landlordName: '',
  costDate: null as string[] | null
})

// 统计数据
const stats = reactive({
  totalExpense: 0,
  propertyRentExpense: 0,
  otherExpense: 0
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

// 获取支出类型名称
const getExpenseTypeName = (type: number): string => {
  return ExpenseTypeName[type] || '-'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  filterForm.costType = null
  filterForm.propertyName = ''
  filterForm.landlordName = ''
  filterForm.costDate = null
  handleSearch()
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const res: any = await request.get('/expense-bill/stats', filterForm)
    Object.assign(stats, res.data)
  } catch (error) {
    stats.totalExpense = 456200
    stats.propertyRentExpense = 320000
    stats.otherExpense = 136200
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
    const res: any = await request.get('/expense-bill/list', params)
    tableData.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    // 模拟数据
    tableData.value = [
      {
        billId: 'E001',
        propertyName: '万科城A栋101',
        landlordName: '张三',
        costType: 1,
        costAmount: 24000,
        costDate: '2024-06-01',
        remark: '2024年下半年房源租金',
        createTime: '2024-06-01 10:00:00'
      },
      {
        billId: 'E002',
        propertyName: '万科城A栋101',
        landlordName: '张三',
        costType: 7,
        costAmount: 3600,
        costDate: '2024-06-15',
        remark: '2024年度物业费',
        createTime: '2024-06-15 14:30:00'
      },
      {
        billId: 'E003',
        propertyName: '龙湖小区5栋',
        landlordName: '王五',
        costType: 1,
        costAmount: 36000,
        costDate: '2024-06-01',
        remark: '2024年下半年房源租金',
        createTime: '2024-06-01 09:00:00'
      },
      {
        billId: 'E004',
        propertyName: '龙湖小区5栋',
        landlordName: '王五',
        costType: 9,
        costAmount: 500,
        costDate: '2024-06-20',
        remark: '更换水龙头维修费',
        createTime: '2024-06-20 16:00:00'
      },
      {
        billId: 'E005',
        propertyName: '保利公园B单元',
        landlordName: '孙七',
        costType: 4,
        costAmount: 280,
        costDate: '2024-05-20',
        remark: '5月份电费公摊',
        createTime: '2024-05-20 11:00:00'
      }
    ]
    pagination.total = 5
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

.expense-bill-list-container {
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
    color: $color-text-primary;
  }

  .stat-label {
    margin-top: $spacing-sm;
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}

.text-danger {
  color: $color-danger;
}

.expense-type-tag {
  display: inline-block;
  padding: 2px 6px;
  border-radius: $border-radius-tag;
  font-size: $font-size-xs;

  &.type-1 { background-color: #fef2f2; color: #dc2626; }
  &.type-2 { background-color: #fef3c7; color: #d97706; }
  &.type-3 { background-color: #ecfeff; color: #0891b2; }
  &.type-4 { background-color: #fef3c7; color: #d97706; }
  &.type-5 { background-color: #fce7f3; color: #db2777; }
  &.type-6 { background-color: #f1f5f9; color: #64748b; }
  &.type-7 { background-color: #dcfce7; color: #16a34a; }
  &.type-8 { background-color: #f0fdf4; color: #15803d; }
  &.type-9 { background-color: #fffbeb; color: #b45309; }
  &.type-10 { background-color: #f5f5f5; color: #737373; }
}

.amount-value {
  color: $color-danger;
  font-weight: 500;
}
</style>
