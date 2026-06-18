<template>
  <div class="contract-detail-container">
    <div class="page-header">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h1 class="page-title">合约详情</h1>
    </div>

    <div v-loading="loading">
      <!-- 基本信息 -->
      <div class="card-container info-section">
        <h3 class="section-title">基本信息</h3>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="合约编号">{{ detailData.contractId }}</el-descriptions-item>
          <el-descriptions-item label="合约状态">
            <span :class="['status-tag', getStatusClass(detailData.status)]">
              {{ getStatusName(detailData.status) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
          <el-descriptions-item label="所属房源">
            <el-button type="primary" link @click="$router.push(`/property/detail/${detailData.propertyId}`)">
              {{ detailData.propertyName }}
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="房间">
            <el-button type="primary" link @click="$router.push(`/room/detail/${detailData.roomId}`)">
              {{ detailData.roomName }}
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="房东">
            <el-button type="primary" link @click="$router.push(`/member/detail/${detailData.landlordId}`)">
              {{ maskName(detailData.landlordName) }}
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="租客">
            <el-button type="primary" link @click="$router.push(`/member/detail/${detailData.tenantId}`)">
              {{ maskName(detailData.tenantName) }}
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="紧急联系人">{{ maskName(detailData.emergencyContact) }}</el-descriptions-item>
          <el-descriptions-item label="紧急联系电话">{{ maskPhone(detailData.emergencyPhone) }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 租期信息 -->
      <div class="card-container info-section">
        <h3 class="section-title">租期信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="合约开始日期">{{ detailData.startDate }}</el-descriptions-item>
          <el-descriptions-item label="合约结束日期">{{ detailData.endDate }}</el-descriptions-item>
          <el-descriptions-item label="合约时长">
            {{ calculateDuration(detailData.startDate, detailData.endDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="剩余天数">
            <span :class="getRemainingClass(detailData.remainingDays)">
              {{ detailData.remainingDays }}天
            </span>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 计费规则 -->
      <div class="card-container info-section">
        <h3 class="section-title">计费规则（已锁定）</h3>
        <el-alert
          title="计费规则锁定说明"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: $spacing-lg"
        >
          该合约计费规则已锁定，不可编辑。如需修改，仅可通过退租完结后新建合约实现。
        </el-alert>
        <el-table :data="detailData.chargeRules" stripe>
          <el-table-column prop="feeType" label="费用类型" width="120" />
          <el-table-column prop="chargeType" label="计费方式" width="120">
            <template #default="{ row }">
              {{ row.chargeType === 'fixed' ? '固定值' : '比例分摊' }}
            </template>
          </el-table-column>
          <el-table-column prop="chargeValue" label="计费值" width="120" align="right">
            <template #default="{ row }">
              <span v-if="row.chargeType === 'fixed'">¥{{ row.chargeValue }}</span>
              <span v-else>{{ (row.chargeValue * 100).toFixed(0) }}%</span>
            </template>
          </el-table-column>
          <el-table-column prop="chargeCycle" label="计费周期" width="120" />
          <el-table-column prop="remark" label="备注" min-width="150" />
        </el-table>
      </div>

      <!-- 纸质合约 -->
      <div class="card-container info-section">
        <h3 class="section-title">纸质合约凭证</h3>
        <div v-if="detailData.contractImages && detailData.contractImages.length > 0" class="image-list">
          <el-image
            v-for="(img, index) in detailData.contractImages"
            :key="index"
            :src="img"
            :preview-src-list="detailData.contractImages"
            fit="contain"
            class="contract-image"
          />
        </div>
        <el-empty v-else description="暂无合约凭证" />
      </div>

      <!-- 关联账单 -->
      <div class="card-container info-section">
        <div class="section-header">
          <h3 class="section-title">关联账单</h3>
          <el-button type="primary" link @click="$router.push(`/income-bill/list?contractId=${detailData.contractId}`)">
            查看全部账单
          </el-button>
        </div>
        <el-table :data="detailData.bills" stripe>
          <el-table-column prop="billId" label="账单编号" width="160" />
          <el-table-column prop="billType" label="账单类型" width="100">
            <template #default="{ row }">
              {{ getBillTypeName(row.billType) }}
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="100" align="right">
            <template #default="{ row }">
              ¥{{ row.amount }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <span :class="['status-tag', getBillStatusClass(row.status)]">
                {{ getBillStatusName(row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="periodStart" label="账期开始" width="120" />
          <el-table-column prop="periodEnd" label="账期结束" width="120" />
          <el-table-column prop="createTime" label="生成时间" width="180" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { request } from '@/api/axios'
import { ContractStatus, ContractStatusName, BillStatus, BillStatusName, BillTypeName } from '@/types'

const route = useRoute()

const loading = ref(false)
const detailData = ref<any>({
  contractId: '',
  status: 1,
  createTime: '',
  propertyId: '',
  propertyName: '',
  roomId: '',
  roomName: '',
  landlordId: '',
  landlordName: '',
  tenantId: '',
  tenantName: '',
  emergencyContact: '',
  emergencyPhone: '',
  startDate: '',
  endDate: '',
  remainingDays: 0,
  chargeRules: [],
  contractImages: [],
  bills: []
})

// 姓名脱敏
const maskName = (name: string): string => {
  if (!name) return '-'
  return name.charAt(0) + '***'
}

// 手机号脱敏
const maskPhone = (phone: string): string => {
  if (!phone) return '-'
  return phone.substring(0, 3) + '****' + phone.substring(7)
}

// 获取合约状态样式类
const getStatusClass = (status: number): string => {
  const classMap: Record<number, string> = {
    [ContractStatus.Active]: 'status-active',
    [ContractStatus.Expired]: 'status-inactive',
    [ContractStatus.Terminated]: 'status-warning',
    [ContractStatus.Cancelled]: 'status-danger'
  }
  return classMap[status] || ''
}

// 获取合约状态名称
const getStatusName = (status: number): string => {
  return ContractStatusName[status] || '-'
}

// 计算租期时长
const calculateDuration = (startDate: string, endDate: string): string => {
  if (!startDate || !endDate) return '-'
  const start = new Date(startDate)
  const end = new Date(endDate)
  const months = Math.floor((end.getTime() - start.getTime()) / (30 * 24 * 60 * 60 * 1000))
  const years = Math.floor(months / 12)
  const remainingMonths = months % 12
  let result = ''
  if (years > 0) result += `${years}年`
  if (remainingMonths > 0) result += `${remainingMonths}个月`
  return result || months + '个月'
}

// 获取剩余天数样式类
const getRemainingClass = (days: number): string => {
  if (days <= 0) return 'text-danger'
  if (days <= 30) return 'text-warning'
  return 'text-success'
}

// 获取账单类型名称
const getBillTypeName = (type: number): string => {
  return BillTypeName[type] || '-'
}

// 获取账单状态样式类
const getBillStatusClass = (status: number): string => {
  const classMap: Record<number, string> = {
    1: 'status-warning',
    2: 'status-active',
    3: 'status-danger',
    4: 'status-info'
  }
  return classMap[status] || ''
}

// 获取账单状态名称
const getBillStatusName = (status: number): string => {
  return BillStatusName[status] || '-'
}

// 获取详情
const fetchDetail = async () => {
  loading.value = true
  const contractId = route.params.id as string
  try {
    const res: any = await request.get(`/contract/detail/${contractId}`)
    detailData.value = res.data
  } catch (error) {
    // 模拟数据
    detailData.value = {
      contractId: 'HT202406001',
      status: 1,
      createTime: '2024-06-01 10:30:00',
      propertyId: 'H001',
      propertyName: '万科城A栋101',
      roomId: 'R001',
      roomName: '整套',
      landlordId: 'U001',
      landlordName: '张三',
      tenantId: 'U002',
      tenantName: '李四',
      emergencyContact: '王小二',
      emergencyPhone: '13800001111',
      startDate: '2024-06-01',
      endDate: '2025-05-31',
      remainingDays: 348,
      chargeRules: [
        { feeType: '租金', chargeType: 'fixed', chargeValue: 4500, chargeCycle: '每月', remark: '月度固定房租' },
        { feeType: '押金', chargeType: 'fixed', chargeValue: 9000, chargeCycle: '', remark: '一次性收取' },
        { feeType: '水费', chargeType: 'ratio', chargeValue: 0.3, chargeCycle: '每月', remark: '按30%比例分摊' },
        { feeType: '电费', chargeType: 'fixed', chargeValue: 80, chargeCycle: '每月', remark: '月度固定电费' }
      ],
      contractImages: [],
      bills: [
        { billId: 'B001', billType: 2, amount: 9000, status: 4, periodStart: '2024-06-01', periodEnd: '2024-06-01', createTime: '2024-06-01 10:35:00' },
        { billId: 'B002', billType: 1, amount: 4500, status: 2, periodStart: '2024-06-01', periodEnd: '2024-06-30', createTime: '2024-06-01 10:35:00' },
        { billId: 'B003', billType: 1, amount: 4500, status: 2, periodStart: '2024-07-01', periodEnd: '2024-07-31', createTime: '2024-07-01 00:00:00' }
      ]
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

.contract-detail-container {
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

.text-success {
  color: $color-success;
}

.text-warning {
  color: $color-warning;
}

.text-danger {
  color: $color-danger;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;

  .contract-image {
    width: 300px;
    height: 200px;
    border-radius: $border-radius-button;
    overflow: hidden;
  }
}
</style>
