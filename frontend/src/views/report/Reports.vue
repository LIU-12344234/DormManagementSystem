<template>
  <el-card>
    <div style="font-size: 16px; font-weight: 600; margin-bottom: 12px">统计报表（概览）</div>
    <el-descriptions :column="3" border>
      <el-descriptions-item label="床位总数">{{ overview.totalBeds }}</el-descriptions-item>
      <el-descriptions-item label="已入住">{{ overview.occupiedBeds }}</el-descriptions-item>
      <el-descriptions-item label="空床">{{ overview.emptyBeds }}</el-descriptions-item>
      <el-descriptions-item label="入住率">{{ (overview.occupancyRate * 100).toFixed(1) }}%</el-descriptions-item>
      <el-descriptions-item label="报修总数">{{ overview.totalRepairs }}</el-descriptions-item>
      <el-descriptions-item label="今日晚归">{{ overview.lateReturnsToday }}</el-descriptions-item>
    </el-descriptions>

    <div style="margin-top: 12px">
      <el-descriptions :column="5" border>
        <el-descriptions-item label="已提交">{{ overview.repairsSubmitted }}</el-descriptions-item>
        <el-descriptions-item label="已派单">{{ overview.repairsAssigned }}</el-descriptions-item>
        <el-descriptions-item label="处理中">{{ overview.repairsInProgress }}</el-descriptions-item>
        <el-descriptions-item label="已完成">{{ overview.repairsFinished }}</el-descriptions-item>
        <el-descriptions-item label="已关闭">{{ overview.repairsClosed }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { fetchOverview } from '../../api/report'

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

onMounted(async () => {
  const data = await fetchOverview()
  Object.assign(overview, data)
})
</script>
