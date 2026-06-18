<template>
  <div class="room-list-container">
    <div class="page-header">
      <h1 class="page-title">房间列表</h1>
      <p class="page-subtitle">全平台房间数据管控，支持查看所有房间状态与信息</p>
    </div>

    <!-- 筛选区域 -->
    <div class="card-container filter-container">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="房间状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="空置中" :value="1" />
            <el-option label="已出租" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item label="所属房源">
          <el-input v-model="filterForm.propertyName" placeholder="房源名称" clearable style="width: 160px" />
        </el-form-item>

        <el-form-item label="房东">
          <el-input v-model="filterForm.landlordName" placeholder="房东姓名" clearable style="width: 120px" />
        </el-form-item>

        <el-form-item label="租金范围">
          <el-input v-model="filterForm.minRent" placeholder="最低" clearable style="width: 100px" />
          <span class="separator">-</span>
          <el-input v-model="filterForm.maxRent" placeholder="最高" clearable style="width: 100px" />
        </el-form-item>

        <el-form-item label="关键词">
          <el-input
            v-model="filterForm.keyword"
            placeholder="房间ID/房间号"
            clearable
            style="width: 160px"
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
        <el-table-column prop="roomId" label="房间ID" width="100" />
        <el-table-column prop="roomName" label="房间名称" min-width="120" />
        <el-table-column prop="propertyName" label="所属房源" min-width="150" show-overflow-tooltip />
        <el-table-column prop="propertyType" label="房源类型" width="100">
          <template #default="{ row }">
            {{ row.propertyType === 1 ? '整租' : '合租' }}
          </template>
        </el-table-column>
        <el-table-column prop="landlordName" label="房东" width="100">
          <template #default="{ row }">
            {{ maskName(row.landlordName) }}
          </template>
        </el-table-column>
        <el-table-column prop="roomArea" label="面积(㎡)" width="90" align="center" />
        <el-table-column prop="monthlyRent" label="月租(元)" width="100" align="right">
          <template #default="{ row }">
            <span class="rent-value">¥{{ row.monthlyRent }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deposit" label="押金(元)" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.deposit }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-tag', row.status === 1 ? 'status-inactive' : 'status-active']">
              {{ row.status === 1 ? '空置中' : '已出租' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="tenantName" label="当前租客" width="120">
          <template #default="{ row }">
            {{ row.tenantName ? maskName(row.tenantName) : '-' }}
          </template>
        </el-table-column>
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

const router = useRouter()

// 筛选表单
const filterForm = reactive({
  status: null as number | null,
  propertyName: '',
  landlordName: '',
  minRent: '',
  maxRent: '',
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

// 姓名脱敏
const maskName = (name: string): string => {
  if (!name) return '-'
  return name.charAt(0) + '***'
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
  filterForm.minRent = ''
  filterForm.maxRent = ''
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
      ...filterForm
    }
    const res: any = await request.get('/room/list', params)
    tableData.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    // 模拟数据
    tableData.value = [
      {
        roomId: 'R001',
        roomName: '整套',
        propertyName: '万科城A栋101',
        propertyType: 1,
        landlordName: '张三',
        roomArea: 89,
        monthlyRent: 4500,
        deposit: 9000,
        status: 2,
        tenantName: '李四'
      },
      {
        roomId: 'R002',
        roomName: '主卧',
        propertyName: '龙湖小区5栋',
        propertyType: 2,
        landlordName: '王五',
        roomArea: 25,
        monthlyRent: 1800,
        deposit: 3600,
        status: 2,
        tenantName: '赵六'
      },
      {
        roomId: 'R003',
        roomName: '次卧',
        propertyName: '龙湖小区5栋',
        propertyType: 2,
        landlordName: '王五',
        roomArea: 18,
        monthlyRent: 1500,
        deposit: 3000,
        status: 1,
        tenantName: ''
      },
      {
        roomId: 'R004',
        roomName: '整套',
        propertyName: '保利公园B单元',
        propertyType: 1,
        landlordName: '孙七',
        roomArea: 75,
        monthlyRent: 3800,
        deposit: 7600,
        status: 1,
        tenantName: ''
      }
    ]
    pagination.total = 4
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleView = (row: any) => {
  router.push(`/room/detail/${row.roomId}`)
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

.room-list-container {
  padding: $spacing-xl;
}

.filter-container {
  margin-bottom: $spacing-lg;

  .filter-form {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-md;
  }

  .separator {
    margin: 0 $spacing-sm;
    color: $color-text-secondary;
  }
}

.rent-value {
  color: $color-primary;
  font-weight: 500;
}
</style>
