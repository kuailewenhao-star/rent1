<template>
  <div class="room-detail-container">
    <div class="page-header">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h1 class="page-title">房间详情</h1>
    </div>

    <div v-loading="loading">
      <!-- 基本信息 -->
      <div class="card-container info-section">
        <h3 class="section-title">基本信息</h3>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="房间ID">{{ detailData.roomId }}</el-descriptions-item>
          <el-descriptions-item label="房间名称">{{ detailData.roomName }}</el-descriptions-item>
          <el-descriptions-item label="所属房源">
            <el-button type="primary" link @click="$router.push(`/property/detail/${detailData.propertyId}`)">
              {{ detailData.propertyName }}
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="房间面积">{{ detailData.roomArea }} ㎡</el-descriptions-item>
          <el-descriptions-item label="月租金额">
            <span class="text-primary">¥{{ detailData.monthlyRent }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="押金金额">¥{{ detailData.deposit }}</el-descriptions-item>
          <el-descriptions-item label="缴费周期">{{ detailData.paymentCycle }}</el-descriptions-item>
          <el-descriptions-item label="房间状态">
            <span :class="['status-tag', detailData.status === 1 ? 'status-inactive' : 'status-active']">
              {{ detailData.status === 1 ? '空置中' : '已出租' }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 计费规则 -->
      <div class="card-container info-section">
        <h3 class="section-title">计费规则</h3>
        <el-table :data="detailData.chargeRules" stripe>
          <el-table-column prop="feeType" label="费用类型" width="120" />
          <el-table-column prop="chargeType" label="计费方式" width="100">
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
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        </el-table>
      </div>

      <!-- 当前租客信息（已出租时显示） -->
      <div v-if="detailData.status === 2" class="card-container info-section">
        <h3 class="section-title">当前租客</h3>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="租客姓名">
            <el-button type="primary" link @click="$router.push(`/member/detail/${detailData.tenantId}`)">
              {{ maskName(detailData.tenantName) }}
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ maskPhone(detailData.tenantPhone) }}
          </el-descriptions-item>
          <el-descriptions-item label="关联合约">
            <el-button type="primary" link @click="$router.push(`/contract/detail/${detailData.contractId}`)">
              查看合约
            </el-button>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 房间图片 -->
      <div class="card-container info-section">
        <h3 class="section-title">房间图片</h3>
        <div class="image-grid">
          <el-image
            v-for="(img, index) in detailData.images"
            :key="index"
            :src="img"
            :preview-src-list="detailData.images"
            fit="cover"
            class="room-image"
          />
          <div v-if="!detailData.images || detailData.images.length === 0" class="empty-images">
            <el-icon size="48" color="#cbd5e1"><Picture /></el-icon>
            <span>暂无图片</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { request } from '@/api/axios'

const route = useRoute()

const loading = ref(false)
const detailData = ref<any>({
  roomId: '',
  roomName: '',
  propertyId: '',
  propertyName: '',
  roomArea: 0,
  monthlyRent: 0,
  deposit: 0,
  paymentCycle: '每月',
  status: 1,
  createTime: '',
  chargeRules: [],
  tenantId: '',
  tenantName: '',
  tenantPhone: '',
  contractId: '',
  images: []
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

// 获取详情
const fetchDetail = async () => {
  loading.value = true
  const roomId = route.params.id as string
  try {
    const res: any = await request.get(`/room/detail/${roomId}`)
    detailData.value = res.data
  } catch (error) {
    // 模拟数据
    detailData.value = {
      roomId: 'R001',
      roomName: '整套',
      propertyId: 'H001',
      propertyName: '万科城A栋101',
      roomArea: 89,
      monthlyRent: 4500,
      deposit: 9000,
      paymentCycle: '每月',
      status: 2,
      createTime: '2024-01-15 10:30:00',
      chargeRules: [
        { feeType: '租金', chargeType: 'fixed', chargeValue: 4500, chargeCycle: '每月', remark: '月度固定房租' },
        { feeType: '押金', chargeType: 'fixed', chargeValue: 9000, chargeCycle: '', remark: '一次性收取' },
        { feeType: '水费', chargeType: 'ratio', chargeValue: 0.3, chargeCycle: '每月', remark: '按30%比例分摊' },
        { feeType: '电费', chargeType: 'fixed', chargeValue: 80, chargeCycle: '每月', remark: '月度固定电费' }
      ],
      tenantId: 'U002',
      tenantName: '李四',
      tenantPhone: '13923456789',
      contractId: 'C001',
      images: []
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

.room-detail-container {
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

  .section-title {
    font-size: $font-size-lg;
    font-weight: 600;
    color: $color-text-primary;
    margin-bottom: $spacing-lg;
  }
}

.text-primary {
  color: $color-primary;
  font-weight: 500;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: $spacing-lg;

  .room-image {
    width: 200px;
    height: 150px;
    border-radius: $border-radius-button;
    overflow: hidden;
  }

  .empty-images {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 200px;
    height: 150px;
    background-color: $color-bg-page;
    border-radius: $border-radius-button;
    color: $color-text-placeholder;

    span {
      margin-top: $spacing-sm;
      font-size: $font-size-sm;
    }
  }
}
</style>
