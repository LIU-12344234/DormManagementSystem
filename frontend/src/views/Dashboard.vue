<template>
  <div style="display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px">
    <el-card>
      <div style="font-size: 14px; color: #666">床位总数</div>
      <div style="font-size: 26px; font-weight: 700; margin-top: 6px">{{ overview.totalBeds }}</div>
    </el-card>
    <el-card>
      <div style="font-size: 14px; color: #666">已入住</div>
      <div style="font-size: 26px; font-weight: 700; margin-top: 6px">{{ overview.occupiedBeds }}</div>
    </el-card>
    <el-card>
      <div style="font-size: 14px; color: #666">空床</div>
      <div style="font-size: 26px; font-weight: 700; margin-top: 6px">{{ overview.emptyBeds }}</div>
    </el-card>
    <el-card>
      <div style="font-size: 14px; color: #666">入住率</div>
      <div style="font-size: 26px; font-weight: 700; margin-top: 6px">
        {{ (overview.occupancyRate * 100).toFixed(1) }}%
      </div>
    </el-card>
  </div>

  <div style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-top: 12px">
    <el-card>
      <div style="font-size: 14px; color: #666">报修总数</div>
      <div style="font-size: 26px; font-weight: 700; margin-top: 6px">{{ overview.totalRepairs }}</div>
    </el-card>
    <el-card>
      <div style="font-size: 14px; color: #666">处理中</div>
      <div style="font-size: 26px; font-weight: 700; margin-top: 6px">
        {{ overview.repairsAssigned + overview.repairsInProgress }}
      </div>
      <div style="font-size: 12px; color: #999; margin-top: 6px">
        已派单 {{ overview.repairsAssigned }} / 处理中 {{ overview.repairsInProgress }}
      </div>
    </el-card>
    <el-card>
      <div style="font-size: 14px; color: #666">今日晚归</div>
      <div style="font-size: 26px; font-weight: 700; margin-top: 6px">{{ overview.lateReturnsToday }}</div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { fetchOverview } from '../api/report'

const overview = reactive({
  totalBeds: 0,
  occupiedBeds: 0,
  emptyBeds: 0,
  occupancyRate: 0,
  totalRepairs: 0,
  repairsSubmitted: 0,
  repairsAssigned: 0,
  repairsInProgress: 0,
  repairsFinished: 0,
  repairsClosed: 0,
  lateReturnsToday: 0
})

const load = async () => {
  const data = await fetchOverview()
  Object.assign(overview, data)
}

onMounted(() => {
  load()
})
</script>
