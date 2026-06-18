<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h1 class="page-title">首页看板</h1>
      <p class="page-subtitle">全平台运营数据概览</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="24" class="stat-cards">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #eff6ff;">
            <el-icon size="28" color="#2563eb"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalMembers }}</div>
            <div class="stat-label">会员总数</div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #ecfdf5;">
            <el-icon size="28" color="#10b981"><House /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalProperties }}</div>
            <div class="stat-label">房源总数</div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #fffbeb;">
            <el-icon size="28" color="#f59e0b"><DoorRight /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalRooms }}</div>
            <div class="stat-label">房间总数</div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #fef2f2;">
            <el-icon size="28" color="#ef4444"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalContracts }}</div>
            <div class="stat-label">合约总数</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第二行统计 -->
    <el-row :gutter="24" class="stat-cards">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #f1f5f9;">
            <el-icon size="28" color="#64748b"><Coin /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">¥{{ formatNumber(stats.totalIncome) }}</div>
            <div class="stat-label">收入总额</div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #fef2f2;">
            <el-icon size="28" color="#ef4444"><Money /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">¥{{ formatNumber(stats.totalExpense) }}</div>
            <div class="stat-label">支出总额</div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #ecfdf5;">
            <el-icon size="28" color="#10b981"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value" :class="{ 'is-profit': stats.profit >= 0, 'is-loss': stats.profit < 0 }">
              ¥{{ formatNumber(stats.profit) }}
            </div>
            <div class="stat-label">盈利总额</div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #eff6ff;">
            <el-icon size="28" color="#2563eb"><Wallet /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">¥{{ formatNumber(stats.totalDeposit) }}</div>
            <div class="stat-label">押金总额</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="24" class="chart-row">
      <el-col :span="12">
        <div class="card-container chart-card">
          <div class="chart-header">
            <h3 class="chart-title">会员类型分布</h3>
          </div>
          <div class="chart-content">
            <div ref="memberChartRef" class="chart-container"></div>
          </div>
        </div>
      </el-col>

      <el-col :span="12">
        <div class="card-container chart-card">
          <div class="chart-header">
            <h3 class="chart-title">房间状态分布</h3>
          </div>
          <div class="chart-content">
            <div ref="roomChartRef" class="chart-container"></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 近期合约 -->
    <div class="card-container recent-section">
      <div class="section-header">
        <h3 class="section-title">近期合约</h3>
        <el-button type="primary" link @click="$router.push('/contract/list')">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <el-table :data="recentContracts" stripe>
        <el-table-column prop="contractId" label="合约编号" width="180" />
        <el-table-column prop="roomName" label="房间" min-width="150" />
        <el-table-column prop="tenantName" label="租客" min-width="120">
          <template #default="{ row }">
            {{ maskName(row.tenantName) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-tag', getStatusClass(row.status)]">
              {{ getStatusName(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import * as echarts from 'echarts'
import { request } from '@/api/axios'
import { ContractStatusName } from '@/types'

// 统计数据
const stats = reactive({
  totalMembers: 0,
  totalProperties: 0,
  totalRooms: 0,
  totalContracts: 0,
  totalIncome: 0,
  totalExpense: 0,
  profit: 0,
  totalDeposit: 0
})

// 图表引用
const memberChartRef = ref<HTMLElement>()
const roomChartRef = ref<HTMLElement>()

// 近期合约
const recentContracts = ref<any[]>([])

// 数字格式化
const formatNumber = (num: number): string => {
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 姓名脱敏
const maskName = (name: string): string => {
  if (!name) return '-'
  return name.charAt(0) + '***'
}

// 状态样式类
const getStatusClass = (status: number): string => {
  const classMap: Record<number, string> = {
    1: 'status-active',
    2: 'status-inactive',
    3: 'status-warning',
    4: 'status-danger'
  }
  return classMap[status] || ''
}

// 状态名称
const getStatusName = (status: number): string => {
  return ContractStatusName[status] || '-'
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const res: any = await request.get('/dashboard/stats')
    Object.assign(stats, res.data)
  } catch (error) {
    // 使用模拟数据
    stats.totalMembers = 156
    stats.totalProperties = 42
    stats.totalRooms = 128
    stats.totalContracts = 89
    stats.totalIncome = 1256800
    stats.totalExpense = 456200
    stats.profit = 800600
    stats.totalDeposit = 356000
  }
}

// 获取近期合约
const fetchRecentContracts = async () => {
  try {
    const res: any = await request.get('/contract/recent')
    recentContracts.value = res.data || []
  } catch (error) {
    // 使用模拟数据
    recentContracts.value = [
      { contractId: 'HT202406001', roomName: '万科城A栋101', tenantName: '张三', status: 1, startDate: '2024-06-01', endDate: '2025-05-31' },
      { contractId: 'HT202406002', roomName: '万科城A栋201', tenantName: '李四', status: 1, startDate: '2024-06-15', endDate: '2025-06-14' },
      { contractId: 'HT202405001', roomName: '龙湖小区5栋302', tenantName: '王五', status: 2, startDate: '2023-06-01', endDate: '2024-05-31' },
      { contractId: 'HT202404001', roomName: '保利公园B单元', tenantName: '赵六', status: 3, startDate: '2024-04-01', endDate: '2025-03-31' }
    ]
  }
}

// 初始化会员分布图表
const initMemberChart = () => {
  if (!memberChartRef.value) return

  const chart = echarts.init(memberChartRef.value)
  const option = {
    tooltip: { trigger: 'item' },
    legend: { bottom: '5%', left: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}: {c}人' },
      data: [
        { value: 42, name: '房东', itemStyle: { color: '#2563eb' } },
        { value: 108, name: '租客', itemStyle: { color: '#10b981' } },
        { value: 6, name: '管理员', itemStyle: { color: '#f59e0b' } }
      ]
    }]
  }
  chart.setOption(option)
}

// 初始化房间状态图表
const initRoomChart = () => {
  if (!roomChartRef.value) return

  const chart = echarts.init(roomChartRef.value)
  const option = {
    tooltip: { trigger: 'item' },
    legend: { bottom: '5%', left: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}: {c}间' },
      data: [
        { value: 45, name: '空置中', itemStyle: { color: '#94a3b8' } },
        { value: 83, name: '已出租', itemStyle: { color: '#10b981' } }
      ]
    }]
  }
  chart.setOption(option)
}

onMounted(() => {
  fetchStats()
  fetchRecentContracts()
  initMemberChart()
  initRoomChart()

  // 响应窗口变化
  window.addEventListener('resize', () => {
    echarts.getInstanceByDom(memberChartRef.value!)?.resize()
    echarts.getInstanceByDom(roomChartRef.value!)?.resize()
  })
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.dashboard-container {
  padding: $spacing-xl;
}

.page-header {
  margin-bottom: $spacing-xl;

  .page-title {
    font-size: $font-size-xxxl;
    font-weight: 600;
    color: $color-text-primary;
  }

  .page-subtitle {
    margin-top: $spacing-xs;
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}

// 统计卡片
.stat-cards {
  margin-bottom: $spacing-xl;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  padding: $spacing-xl;
  background-color: $color-bg-card;
  border-radius: $border-radius-card;
  box-shadow: $shadow-sm;

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: $border-radius-button;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .stat-content {
    .stat-value {
      font-size: $font-size-xxxl;
      font-weight: 600;
      color: $color-text-primary;
      line-height: 1.2;

      &.is-profit {
        color: $color-success;
      }

      &.is-loss {
        color: $color-danger;
      }
    }

    .stat-label {
      margin-top: $spacing-xs;
      font-size: $font-size-sm;
      color: $color-text-secondary;
    }
  }
}

// 图表区域
.chart-row {
  margin-bottom: $spacing-xl;
}

.chart-card {
  height: 320px;

  .chart-header {
    margin-bottom: $spacing-lg;

    .chart-title {
      font-size: $font-size-lg;
      font-weight: 600;
      color: $color-text-primary;
    }
  }

  .chart-content {
    height: 260px;

    .chart-container {
      width: 100%;
      height: 100%;
    }
  }
}

// 近期合约
.recent-section {
  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-lg;

    .section-title {
      font-size: $font-size-lg;
      font-weight: 600;
      color: $color-text-primary;
    }
  }
}
</style>
