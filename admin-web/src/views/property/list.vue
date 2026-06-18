<template>
  <div class="property-list-container">
    <div class="page-header">
      <h1 class="page-title">房源列表</h1>
      <p class="page-subtitle">全平台房源数据管控，支持查看所有房东房源信息</p>
    </div>

    <!-- 筛选区域 -->
    <div class="card-container filter-container">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="房源类型">
          <el-select v-model="filterForm.propertyType" placeholder="全部" clearable style="width: 120px">
            <el-option label="整租" :value="1" />
            <el-option label="合租" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item label="业务状态">
          <el-select v-model="filterForm.businessStatus" placeholder="全部" clearable style="width: 140px">
            <el-option label="正常经营" :value="1" />
            <el-option label="租期到期停用" :value="2" />
            <el-option label="主动终止经营" :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="省份">
          <el-input v-model="filterForm.province" placeholder="请输入省份" clearable style="width: 120px" />
        </el-form-item>

        <el-form-item label="城市">
          <el-input v-model="filterForm.city" placeholder="请输入城市" clearable style="width: 120px" />
        </el-form-item>

        <el-form-item label="关键词">
          <el-input
            v-model="filterForm.keyword"
            placeholder="房源名称/房源ID/房东"
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
        <el-table-column prop="propertyId" label="房源ID" width="100" />
        <el-table-column prop="propertyName" label="房源名称" min-width="150" />
        <el-table-column prop="propertyType" label="类型" width="80">
          <template #default="{ row }">
            <span :class="['type-tag', row.propertyType === 1 ? 'type-whole' : 'type-shared']">
              {{ row.propertyType === 1 ? '整租' : '合租' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="landlordName" label="房东" width="100">
          <template #default="{ row }">
            {{ maskName(row.landlordName) }}
          </template>
        </el-table-column>
        <el-table-column prop="roomCount" label="房间数" width="80" align="center" />
        <el-table-column prop="rentedCount" label="已出租" width="80" align="center">
          <template #default="{ row }">
            <span class="text-success">{{ row.rentedCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="vacantCount" label="空置" width="80" align="center">
          <template #default="{ row }">
            <span class="text-secondary">{{ row.vacantCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="businessStatus" label="状态" width="110">
          <template #default="{ row }">
            <span :class="['status-tag', getStatusClass(row.businessStatus)]">
              {{ getStatusName(row.businessStatus) }}
            </span>
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
import { PropertyBusinessStatusName } from '@/types'

const router = useRouter()

// 筛选表单
const filterForm = reactive({
  propertyType: null as number | null,
  businessStatus: null as number | null,
  province: '',
  city: '',
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

// 获取状态样式类
const getStatusClass = (status: number): string => {
  const classMap: Record<number, string> = {
    1: 'status-active',
    2: 'status-warning',
    3: 'status-inactive'
  }
  return classMap[status] || ''
}

// 获取状态名称
const getStatusName = (status: number): string => {
  return PropertyBusinessStatusName[status] || '-'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  filterForm.propertyType = null
  filterForm.businessStatus = null
  filterForm.province = ''
  filterForm.city = ''
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
    const res: any = await request.get('/property/list', params)
    tableData.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    // 模拟数据
    tableData.value = [
      {
        propertyId: 'H001',
        propertyName: '万科城A栋101',
        propertyType: 1,
        address: '广东省深圳市南山区万科城A栋',
        landlordName: '张三',
        roomCount: 1,
        rentedCount: 1,
        vacantCount: 0,
        businessStatus: 1
      },
      {
        propertyId: 'H002',
        propertyName: '龙湖小区5栋',
        propertyType: 2,
        address: '广东省广州市天河区龙湖小区5栋',
        landlordName: '李四',
        roomCount: 4,
        rentedCount: 2,
        vacantCount: 2,
        businessStatus: 1
      },
      {
        propertyId: 'H003',
        propertyName: '保利公园B单元',
        propertyType: 1,
        address: '广东省佛山市禅城区保利公园B单元',
        landlordName: '王五',
        roomCount: 1,
        rentedCount: 0,
        vacantCount: 1,
        businessStatus: 2
      }
    ]
    pagination.total = 3
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleView = (row: any) => {
  router.push(`/property/detail/${row.propertyId}`)
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

.property-list-container {
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

.type-tag {
  display: inline-block;
  padding: 2px 6px;
  border-radius: $border-radius-tag;
  font-size: $font-size-xs;

  &.type-whole {
    background-color: #eff6ff;
    color: #2563eb;
  }

  &.type-shared {
    background-color: #f0fdf4;
    color: #16a34a;
  }
}

.text-success {
  color: $color-success;
}

.text-secondary {
  color: $color-text-secondary;
}
</style>
