<template>
  <div class="property-detail-container">
    <div class="page-header">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h1 class="page-title">房源详情</h1>
    </div>

    <div v-loading="loading">
      <!-- 基本信息 -->
      <div class="card-container info-section">
        <h3 class="section-title">基本信息</h3>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="房源ID">{{ detailData.propertyId }}</el-descriptions-item>
          <el-descriptions-item label="房源名称">{{ detailData.propertyName }}</el-descriptions-item>
          <el-descriptions-item label="房源类型">
            <span :class="['type-tag', detailData.propertyType === 1 ? 'type-whole' : 'type-shared']">
              {{ detailData.propertyType === 1 ? '整租' : '合租' }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="总户型">{{ detailData.roomLayout }}</el-descriptions-item>
          <el-descriptions-item label="业务状态">
            <span :class="['status-tag', getStatusClass(detailData.businessStatus)]">
              {{ getStatusName(detailData.businessStatus) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 地址信息 -->
      <div class="card-container info-section">
        <h3 class="section-title">地址信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="省份">{{ detailData.province }}</el-descriptions-item>
          <el-descriptions-item label="城市">{{ detailData.city }}</el-descriptions-item>
          <el-descriptions-item label="区县">{{ detailData.district }}</el-descriptions-item>
          <el-descriptions-item label="详细地址">{{ detailData.address }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 承租信息 -->
      <div class="card-container info-section">
        <h3 class="section-title">承租信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="承租起始时间">{{ detailData.leaseStartDate }}</el-descriptions-item>
          <el-descriptions-item label="承租结束时间">{{ detailData.leaseEndDate }}</el-descriptions-item>
          <el-descriptions-item label="房东">
            <el-button type="primary" link @click="$router.push(`/member/detail/${detailData.landlordId}`)">
              {{ maskName(detailData.landlordName) }}
            </el-button>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 房间列表 -->
      <div class="card-container info-section">
        <div class="section-header">
          <h3 class="section-title">房间列表</h3>
          <el-button type="primary" link @click="$router.push(`/room/list?propertyId=${detailData.propertyId}`)">
            查看全部房间
          </el-button>
        </div>
        <el-table :data="detailData.rooms" stripe>
          <el-table-column prop="roomId" label="房间ID" width="100" />
          <el-table-column prop="roomName" label="房间名称" min-width="120" />
          <el-table-column prop="roomArea" label="面积(㎡)" width="100" align="center" />
          <el-table-column prop="monthlyRent" label="月租(元)" width="100" align="right">
            <template #default="{ row }">
              ¥{{ row.monthlyRent }}
            </template>
          </el-table-column>
          <el-table-column prop="deposit" label="押金(元)" width="100" align="right">
            <template #default="{ row }">
              ¥{{ row.deposit }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <span :class="['status-tag', row.status === 1 ? 'status-active' : 'status-warning']">
                {{ row.status === 1 ? '空置中' : '已出租' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" link @click="$router.push(`/room/detail/${row.roomId}`)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 经营数据 -->
      <div class="card-container info-section">
        <h3 class="section-title">经营数据</h3>
        <el-row :gutter="24">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-label">总收入</div>
              <div class="stat-value text-success">¥{{ formatNumber(detailData.totalIncome) }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-label">总支出</div>
              <div class="stat-value text-danger">¥{{ formatNumber(detailData.totalExpense) }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-label">盈利</div>
              <div class="stat-value" :class="detailData.profit >= 0 ? 'text-success' : 'text-danger'">
                ¥{{ formatNumber(detailData.profit) }}
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-label">当前押金</div>
              <div class="stat-value text-primary">¥{{ formatNumber(detailData.currentDeposit) }}</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { request } from '@/api/axios'
import { PropertyBusinessStatusName } from '@/types'

const route = useRoute()

const loading = ref(false)
const detailData = ref<any>({
  propertyId: '',
  propertyName: '',
  propertyType: 1,
  roomLayout: '',
  businessStatus: 1,
  createTime: '',
  province: '',
  city: '',
  district: '',
  address: '',
  leaseStartDate: '',
  leaseEndDate: '',
  landlordId: '',
  landlordName: '',
  rooms: [],
  totalIncome: 0,
  totalExpense: 0,
  profit: 0,
  currentDeposit: 0
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

// 数字格式化
const formatNumber = (num: number): string => {
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 获取详情
const fetchDetail = async () => {
  loading.value = true
  const propertyId = route.params.id as string
  try {
    const res: any = await request.get(`/property/detail/${propertyId}`)
    detailData.value = res.data
  } catch (error) {
    // 模拟数据
    detailData.value = {
      propertyId: 'H001',
      propertyName: '万科城A栋101',
      propertyType: 1,
      roomLayout: '3室2厅',
      businessStatus: 1,
      createTime: '2024-01-15 10:30:00',
      province: '广东省',
      city: '深圳市',
      district: '南山区',
      address: '万科城A栋101室',
      leaseStartDate: '2023-01-01',
      leaseEndDate: '2025-12-31',
      landlordId: 'U001',
      landlordName: '张三',
      rooms: [
        {
          roomId: 'R001',
          roomName: '整套',
          roomArea: 89,
          monthlyRent: 4500,
          deposit: 9000,
          status: 2
        }
      ],
      totalIncome: 99000,
      totalExpense: 36000,
      profit: 63000,
      currentDeposit: 9000
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.property-detail-container {
  padding: $spacing-xl;
}

.page-header {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  margin-bottom: $spacing-xl;

  .page-title {
    font-size: $font-size-xxl;
    font-weight: 600;
    color: $color-text-primary;
  }
}

.info-section {
  margin-bottom: $spacing-xl;

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-lg;

    .section-title {
      margin: 0;
    }
  }

  .section-title {
    font-size: $font-size-lg;
    font-weight: 600;
    color: $color-text-primary;
    margin-bottom: $spacing-lg;
  }
}

.type-tag {
  display: inline-block;
  padding: 2px 8px;
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

.stat-item {
  padding: $spacing-lg;
  background-color: $color-bg-page;
  border-radius: $border-radius-button;
  text-align: center;

  .stat-label {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    margin-bottom: $spacing-sm;
  }

  .stat-value {
    font-size: $font-size-xxl;
    font-weight: 600;
  }
}

.text-success {
  color: $color-success;
}

.text-danger {
  color: $color-danger;
}

.text-primary {
  color: $color-primary;
}
</style>
