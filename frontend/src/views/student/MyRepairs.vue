<template>
  <el-card>
    <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px">
      <div style="font-size: 16px; font-weight: 600">我的报修</div>
      <div style="display: flex; gap: 8px; align-items: center">
        <el-button type="primary" @click="openSubmit">提交报修</el-button>
      </div>
    </div>

    <el-table :data="rows" style="width: 100%; margin-top: 12px" v-loading="loading">
      <el-table-column prop="orderNo" label="工单号" width="180" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="roomId" label="房间ID" width="110" />
      <el-table-column prop="createdAt" label="提交时间" width="170" />
      <el-table-column prop="rating" label="评分" width="90" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="openLogs(scope.row)">日志</el-button>
          <el-button
            size="small"
            type="primary"
            :disabled="scope.row.rating != null || (scope.row.status !== 'FINISHED' && scope.row.status !== 'CLOSED')"
            @click="openRate(scope.row)"
          >
            评分
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="display: flex; justify-content: flex-end; margin-top: 12px">
      <el-pagination
        background
        layout="prev, pager, next, sizes, total"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNo"
        @current-change="(p) => (pageNo = p) && load()"
        @size-change="(s) => (pageSize = s) && (pageNo = 1) && load()"
      />
    </div>
  </el-card>

  <el-dialog v-model="submitVisible" title="提交报修" width="620px">
    <el-form :model="submitForm" label-width="90px">
      <el-form-item label="房间">
        <el-select v-model="submitForm.roomId" filterable style="width: 100%">
          <el-option v-for="r in roomOptions" :key="r.id" :label="`${r.roomNo}`" :value="r.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题">
        <el-input v-model="submitForm.title" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="submitForm.description" type="textarea" :rows="4" />
      </el-form-item>
      <el-form-item label="优先级">
        <el-select v-model="submitForm.priority" style="width: 100%">
          <el-option label="普通" value="NORMAL" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSubmit">提交</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="rateVisible" title="评分" width="420px">
    <el-form :model="rateForm" label-width="90px">
      <el-form-item label="评分">
        <el-rate v-model="rateForm.rating" :max="5" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="rateVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onRate">提交评分</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="logsVisible" title="流转日志" width="720px">
    <el-table :data="logs" style="width: 100%" v-loading="logsLoading">
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column prop="action" label="动作" width="120" />
      <el-table-column prop="fromStatus" label="原状态" width="120" />
      <el-table-column prop="toStatus" label="新状态" width="120" />
      <el-table-column prop="note" label="备注" min-width="200" />
    </el-table>
    <template #footer>
      <el-button @click="logsVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchMyRepairPage, submitRepair, rateRepair, fetchRepairLogs } from '../../api/repair'
import { fetchRoomOptions } from '../../api/room'

const loading = ref(false)
const saving = ref(false)

const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rows = ref([])

const roomOptions = ref([])

const statusText = (s) => {
  if (s === 'SUBMITTED') return '已提交'
  if (s === 'ASSIGNED') return '已派单'
  if (s === 'IN_PROGRESS') return '处理中'
  if (s === 'FINISHED') return '已完成'
  if (s === 'CLOSED') return '已关闭'
  return s
}

const statusTag = (s) => {
  if (s === 'SUBMITTED') return 'info'
  if (s === 'ASSIGNED') return 'warning'
  if (s === 'IN_PROGRESS') return 'primary'
  if (s === 'FINISHED') return 'success'
  if (s === 'CLOSED') return 'danger'
  return ''
}

const load = async () => {
  loading.value = true
  try {
    const data = await fetchMyRepairPage({ page: pageNo.value, size: pageSize.value })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const submitVisible = ref(false)
const submitForm = reactive({
  roomId: null,
  title: '',
  description: '',
  priority: 'NORMAL'
})

const openSubmit = async () => {
  roomOptions.value = await fetchRoomOptions({})
  submitForm.roomId = roomOptions.value[0]?.id ?? null
  submitForm.title = ''
  submitForm.description = ''
  submitForm.priority = 'NORMAL'
  submitVisible.value = true
}

const onSubmit = async () => {
  if (!submitForm.roomId || !submitForm.title) {
    ElMessage.error('请选择房间并填写标题')
    return
  }
  saving.value = true
  try {
    await submitRepair({
      roomId: submitForm.roomId,
      title: submitForm.title,
      description: submitForm.description || undefined,
      priority: submitForm.priority
    })
    ElMessage.success('提交成功')
    submitVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const rateVisible = ref(false)
const rateForm = reactive({ id: null, rating: 5 })

const openRate = (row) => {
  rateForm.id = row.id
  rateForm.rating = 5
  rateVisible.value = true
}

const onRate = async () => {
  saving.value = true
  try {
    await rateRepair(rateForm.id, { rating: rateForm.rating })
    ElMessage.success('评分成功')
    rateVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const logsVisible = ref(false)
const logsLoading = ref(false)
const logs = ref([])

const openLogs = async (row) => {
  logsVisible.value = true
  logsLoading.value = true
  try {
    logs.value = await fetchRepairLogs(row.id)
  } finally {
    logsLoading.value = false
  }
}

onMounted(() => {
  load()
})
</script>
