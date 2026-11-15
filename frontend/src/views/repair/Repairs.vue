<template>
  <el-card>
    <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px">
      <div style="font-size: 16px; font-weight: 600">报修管理</div>
      <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap; justify-content: flex-end">
        <el-select v-model="status" placeholder="状态" clearable style="width: 150px" @change="onFilterChange">
          <el-option label="已提交" value="SUBMITTED" />
          <el-option label="已派单" value="ASSIGNED" />
          <el-option label="处理中" value="IN_PROGRESS" />
          <el-option label="已完成" value="FINISHED" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
        <el-input v-model="keyword" placeholder="单号/标题" style="width: 220px" clearable @keyup.enter="load" />
        <el-button @click="load">刷新</el-button>
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
      <el-table-column prop="priority" label="优先级" width="110" />
      <el-table-column prop="roomId" label="房间ID" width="110" />
      <el-table-column prop="studentId" label="学生ID" width="110" />
      <el-table-column prop="assignedWorkerUserId" label="维修工ID" width="110" />
      <el-table-column prop="assignedAt" label="派单时间" width="170" />
      <el-table-column prop="finishedAt" label="完成时间" width="170" />
      <el-table-column prop="rating" label="评分" width="90" />
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="openLogs(scope.row)">日志</el-button>

          <el-button
            v-if="canAssign"
            size="small"
            type="primary"
            :disabled="scope.row.status !== 'SUBMITTED'"
            @click="openAssign(scope.row)"
          >
            派单
          </el-button>

          <el-button
            v-if="canWorker"
            size="small"
            type="primary"
            :disabled="scope.row.status !== 'ASSIGNED'"
            @click="onStart(scope.row)"
          >
            开始
          </el-button>
          <el-button
            v-if="canWorker"
            size="small"
            type="success"
            :disabled="scope.row.status !== 'IN_PROGRESS'"
            @click="onFinish(scope.row)"
          >
            完成
          </el-button>

          <el-button
            v-if="canClose"
            size="small"
            type="danger"
            :disabled="scope.row.status === 'CLOSED'"
            @click="openClose(scope.row)"
          >
            关闭
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

  <el-dialog v-model="assignVisible" title="派单" width="520px">
    <el-form :model="assignForm" label-width="90px">
      <el-form-item label="维修工">
        <el-select v-model="assignForm.workerUserId" filterable style="width: 100%">
          <el-option v-for="u in workerOptions" :key="u.id" :label="`${u.displayName}(${u.username})`" :value="u.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="assignForm.note" placeholder="可选" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="assignVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onAssign">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="closeVisible" title="关闭工单" width="520px">
    <el-form :model="closeForm" label-width="90px">
      <el-form-item label="备注">
        <el-input v-model="closeForm.note" placeholder="可选" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="closeVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onClose">确认关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="logsVisible" title="流转日志" width="720px">
    <el-table :data="logs" style="width: 100%" v-loading="logsLoading">
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column prop="action" label="动作" width="120" />
      <el-table-column prop="fromStatus" label="原状态" width="120" />
      <el-table-column prop="toStatus" label="新状态" width="120" />
      <el-table-column prop="note" label="备注" min-width="200" />
      <el-table-column prop="operatorUserId" label="操作人ID" width="110" />
    </el-table>
    <template #footer>
      <el-button @click="logsVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../store/auth'
import { fetchRepairPage, assignRepair, startRepair, finishRepair, closeRepair, fetchRepairLogs } from '../../api/repair'
import { request } from '../../api/request'

const auth = useAuthStore()

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const status = ref(null)

const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rows = ref([])

const canAssign = computed(() => auth.roles.includes('ADMIN') || auth.roles.includes('DORM_MANAGER'))
const canClose = computed(() => auth.roles.includes('ADMIN') || auth.roles.includes('DORM_MANAGER'))
const canWorker = computed(() => auth.roles.includes('WORKER'))

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
    const data = await fetchRepairPage({
      page: pageNo.value,
      size: pageSize.value,
      status: status.value || undefined,
      keyword: keyword.value || undefined
    })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const onFilterChange = () => {
  pageNo.value = 1
  load()
}

const workerOptions = ref([])
const assignVisible = ref(false)
const assignForm = reactive({
  id: null,
  workerUserId: null,
  note: ''
})

const loadWorkers = async () => {
  // /api/users/options?roleKey=WORKER
  workerOptions.value = await request({ url: '/api/users/options', method: 'GET', params: { roleKey: 'WORKER' } })
}

const openAssign = async (row) => {
  await loadWorkers()
  assignForm.id = row.id
  assignForm.workerUserId = workerOptions.value[0]?.id ?? null
  assignForm.note = ''
  assignVisible.value = true
}

const onAssign = async () => {
  if (!assignForm.workerUserId) {
    ElMessage.error('请选择维修工')
    return
  }
  saving.value = true
  try {
    await assignRepair(assignForm.id, { workerUserId: assignForm.workerUserId, note: assignForm.note || undefined })
    ElMessage.success('派单成功')
    assignVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const onStart = async (row) => {
  saving.value = true
  try {
    await startRepair(row.id, { note: '开始处理' })
    ElMessage.success('已开始')
    await load()
  } finally {
    saving.value = false
  }
}

const onFinish = async (row) => {
  saving.value = true
  try {
    await finishRepair(row.id, { note: '已处理完成' })
    ElMessage.success('已完成')
    await load()
  } finally {
    saving.value = false
  }
}

const closeVisible = ref(false)
const closeForm = reactive({ id: null, note: '' })

const openClose = (row) => {
  closeForm.id = row.id
  closeForm.note = ''
  closeVisible.value = true
}

const onClose = async () => {
  saving.value = true
  try {
    await closeRepair(closeForm.id, { note: closeForm.note || undefined })
    ElMessage.success('已关闭')
    closeVisible.value = false
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
