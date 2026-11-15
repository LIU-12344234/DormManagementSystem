<template>
  <el-card>
    <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px">
      <div style="font-size: 16px; font-weight: 600">床位分配</div>
      <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap; justify-content: flex-end">
        <el-switch v-model="activeOnly" active-text="只看在住" @change="onFilterChange" />
        <el-button type="primary" @click="openAssign">分配床位</el-button>
      </div>
    </div>

    <el-table :data="rows" style="width: 100%; margin-top: 12px" v-loading="loading">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="studentId" label="学生ID" width="110" />
      <el-table-column prop="bedId" label="床位ID" width="110" />
      <el-table-column prop="checkInAt" label="入住时间" min-width="170" />
      <el-table-column prop="checkOutAt" label="退宿时间" min-width="170" />
      <el-table-column prop="reason" label="备注" min-width="160" />
      <el-table-column label="状态" width="110">
        <template #default="scope">
          <el-tag :type="scope.row.checkOutAt ? 'info' : 'success'">
            {{ scope.row.checkOutAt ? '已退宿' : '在住' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="scope">
          <el-button size="small" :disabled="!!scope.row.checkOutAt" @click="openCheckout(scope.row)">退宿</el-button>
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

  <el-dialog v-model="assignVisible" title="分配床位" width="620px">
    <el-form :model="assignForm" label-width="100px">
      <el-form-item label="学生">
        <el-select v-model="assignForm.studentId" filterable placeholder="请选择学生" style="width: 100%">
          <el-option v-for="s in studentOptions" :key="s.id" :label="`${s.studentNo}-${s.name}`" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="房间">
        <el-select v-model="assignForm.roomId" filterable placeholder="请选择房间" style="width: 100%" @change="onRoomChange">
          <el-option v-for="r in roomOptions" :key="r.id" :label="`${r.roomNo}`" :value="r.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="空床位">
        <el-select v-model="assignForm.bedId" filterable placeholder="请选择空床位" style="width: 100%">
          <el-option v-for="b in emptyBedOptions" :key="b.id" :label="`床位${b.bedNo}`" :value="b.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="assignForm.reason" placeholder="可选" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="assignVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onAssign">确定分配</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="checkoutVisible" title="退宿" width="520px">
    <el-form :model="checkoutForm" label-width="90px">
      <el-form-item label="学生ID">
        <el-input v-model="checkoutForm.studentId" disabled />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="checkoutForm.reason" placeholder="可选" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="checkoutVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onCheckout">确认退宿</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAssignmentPage, assignBed, checkoutStudent } from '../../api/assignment'
import { fetchStudentOptions } from '../../api/student'
import { fetchRoomOptions } from '../../api/room'
import { fetchBedOptions } from '../../api/bed'

const loading = ref(false)
const saving = ref(false)

const activeOnly = ref(true)

const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rows = ref([])

const studentOptions = ref([])
const roomOptions = ref([])
const emptyBedOptions = ref([])

const assignVisible = ref(false)
const assignForm = reactive({
  studentId: null,
  roomId: null,
  bedId: null,
  reason: ''
})

const checkoutVisible = ref(false)
const checkoutForm = reactive({
  studentId: null,
  reason: ''
})

const load = async () => {
  loading.value = true
  try {
    const data = await fetchAssignmentPage({
      page: pageNo.value,
      size: pageSize.value,
      activeOnly: activeOnly.value
    })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  studentOptions.value = await fetchStudentOptions({})
  roomOptions.value = await fetchRoomOptions({})
}

const onFilterChange = () => {
  pageNo.value = 1
  load()
}

const openAssign = async () => {
  assignForm.studentId = null
  assignForm.roomId = roomOptions.value[0]?.id ?? null
  assignForm.bedId = null
  assignForm.reason = ''
  assignVisible.value = true
  if (assignForm.roomId) {
    await onRoomChange(assignForm.roomId)
  }
}

const onRoomChange = async (rid) => {
  assignForm.bedId = null
  emptyBedOptions.value = await fetchBedOptions({ roomId: rid, status: 'EMPTY' })
}

const onAssign = async () => {
  if (!assignForm.studentId || !assignForm.bedId) {
    ElMessage.error('请选择学生与空床位')
    return
  }
  saving.value = true
  try {
    await assignBed({
      studentId: assignForm.studentId,
      bedId: assignForm.bedId,
      reason: assignForm.reason || undefined
    })
    ElMessage.success('分配成功')
    assignVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const openCheckout = (row) => {
  checkoutForm.studentId = row.studentId
  checkoutForm.reason = ''
  checkoutVisible.value = true
}

const onCheckout = async () => {
  saving.value = true
  try {
    await checkoutStudent({
      studentId: checkoutForm.studentId,
      reason: checkoutForm.reason || undefined
    })
    ElMessage.success('退宿成功')
    checkoutVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadOptions()
  await load()
})
</script>
