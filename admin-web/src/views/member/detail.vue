<template>
  <div class="member-detail-container">
    <div class="page-header">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h1 class="page-title">会员详情</h1>
    </div>

    <div v-loading="loading">
      <!-- 基本信息 -->
      <div class="card-container info-section">
        <h3 class="section-title">基本信息</h3>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="用户ID">{{ detailData.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ detailData.username }}</el-descriptions-item>
          <el-descriptions-item label="手机号">
            <span class="masked">{{ maskPhone(detailData.phone) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="主体ID">{{ detailData.mainId }}</el-descriptions-item>
          <el-descriptions-item label="会员类型">
            <span :class="['member-type-tag', `type-${detailData.memberType}`]">
              {{ getMemberTypeName(detailData.memberType) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag :type="detailData.status === 1 ? 'success' : 'danger'">
              {{ getStatusName(detailData.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ detailData.createTime }}</el-descriptions-item>
          <el-descriptions-item label="最后登录时间">{{ detailData.lastLoginTime }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 扩展信息 -->
      <div class="card-container info-section">
        <h3 class="section-title">扩展信息</h3>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="项目ID">{{ detailData.projectId }}</el-descriptions-item>
          <el-descriptions-item label="通行证ID">{{ detailData.passportId }}</el-descriptions-item>
          <el-descriptions-item label="系统角色ID">{{ detailData.roleId }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 关联数据（房东/租客） -->
      <div v-if="detailData.memberType === 1" class="card-container info-section">
        <h3 class="section-title">关联房源</h3>
        <el-table :data="detailData.properties" stripe>
          <el-table-column prop="propertyId" label="房源ID" width="120" />
          <el-table-column prop="propertyName" label="房源名称" min-width="150" />
          <el-table-column prop="propertyType" label="房源类型" width="100">
            <template #default="{ row }">
              {{ row.propertyType === 1 ? '整租' : '合租' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <span :class="['status-tag', getPropertyStatusClass(row.status)]">
                {{ getPropertyStatusName(row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" link @click="$router.push(`/property/detail/${row.propertyId}`)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="detailData.memberType === 2" class="card-container info-section">
        <h3 class="section-title">关联合约</h3>
        <el-table :data="detailData.contracts" stripe>
          <el-table-column prop="contractId" label="合约ID" width="120" />
          <el-table-column prop="propertyName" label="房源" min-width="150" />
          <el-table-column prop="roomName" label="房间" min-width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <span :class="['status-tag', getContractStatusClass(row.status)]">
                {{ getContractStatusName(row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="startDate" label="开始日期" width="120" />
          <el-table-column prop="endDate" label="结束日期" width="120" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" link @click="$router.push(`/contract/detail/${row.contractId}`)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 操作日志 -->
      <div class="card-container info-section">
        <h3 class="section-title">操作日志</h3>
        <el-timeline>
          <el-timeline-item
            v-for="log in detailData.logs"
            :key="log.id"
            :timestamp="log.createTime"
            placement="top"
          >
            <el-card shadow="never">
              <p>{{ log.action }}</p>
              <p class="log-detail">{{ log.detail }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { request } from '@/api/axios'
import { MemberTypeName, AccountStatusName, PropertyBusinessStatusName, ContractStatusName } from '@/types'

const route = useRoute()

const loading = ref(false)
const detailData = ref<any>({
  userId: '',
  username: '',
  phone: '',
  mainId: '',
  memberType: 0,
  status: 1,
  createTime: '',
  lastLoginTime: '',
  projectId: '',
  passportId: '',
  roleId: '',
  properties: [],
  contracts: [],
  logs: []
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

// 获取房源状态样式
const getPropertyStatusClass = (status: number): string => {
  const classMap: Record<number, string> = {
    1: 'status-active',
    2: 'status-warning',
    3: 'status-inactive'
  }
  return classMap[status] || ''
}

// 获取房源状态名称
const getPropertyStatusName = (status: number): string => {
  return PropertyBusinessStatusName[status] || '-'
}

// 获取合约状态样式
const getContractStatusClass = (status: number): string => {
  const classMap: Record<number, string> = {
    1: 'status-active',
    2: 'status-inactive',
    3: 'status-warning',
    4: 'status-danger'
  }
  return classMap[status] || ''
}

// 获取合约状态名称
const getContractStatusName = (status: number): string => {
  return ContractStatusName[status] || '-'
}

// 获取详情
const fetchDetail = async () => {
  loading.value = true
  const userId = route.params.id as string
  try {
    const res: any = await request.get(`/member/detail/${userId}`)
    detailData.value = res.data
  } catch (error) {
    // 模拟数据
    detailData.value = {
      userId: 'U001',
      username: '张三',
      phone: '13812345678',
      mainId: 'M001',
      memberType: 1,
      status: 1,
      createTime: '2024-01-15 10:30:00',
      lastLoginTime: '2024-06-15 14:20:00',
      projectId: 'P001',
      passportId: 'PP001',
      roleId: 'R001',
      properties: [
        { propertyId: 'H001', propertyName: '万科城A栋', propertyType: 1, status: 1 },
        { propertyId: 'H002', propertyName: '龙湖小区5栋', propertyType: 2, status: 1 }
      ],
      contracts: [],
      logs: [
        { id: 1, createTime: '2024-06-15 14:20:00', action: '登录系统', detail: 'PC后台登录成功' },
        { id: 2, createTime: '2024-06-10 09:15:00', action: '更新房源', detail: '修改了万科城A栋的房间信息' },
        { id: 3, createTime: '2024-06-01 10:00:00', action: '创建合约', detail: '新建了与租客李四的租赁合约' }
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

.member-detail-container {
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

  .masked {
    font-family: monospace;
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

.log-detail {
  margin-top: $spacing-sm;
  font-size: $font-size-sm;
  color: $color-text-secondary;
}
</style>
