<template>
  <el-card>
    <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px">
      <div style="font-size: 16px; font-weight: 600">访客管理</div>
      <div style="display: flex; gap: 8px; align-items: center">
        <el-switch v-model="activeOnly" active-text="只看在访" @change="onFilterChange" />
        <el-button type="primary" @click="openCreate">来访登记</el-button>
      </div>
    </div>

    <el-table :data="rows" style="width: 100%; margin-top: 12px" v-loading="loading">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="visitorName" label="访客姓名" width="140" />
      <el-table-column prop="visitorPhone" label="电话" width="140" />
      <el-table-column prop="buildingId" label="楼栋ID" width="110" />
      <el-table-column prop="roomId" label="房间ID" width="110" />
      <el-table-column prop="inTime" label="进入时间" width="180" />
      <el-table-column prop="outTime" label="离开时间" width="180" />
      <el-table-column prop="visitReason" label="来访原因" min-width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button size="small" type="success" :disabled="!!scope.row.outTime" @click="onCheckout(scope.row)">登记离开</el-button>
          <el-button size="small" type="danger" v-if="isAdmin" @click="onDelete(scope.row)">删除</el-button>
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

  <el-dialog v-model="createVisible" title="来访登记" width="620px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="楼栋">
        <el-select v-model="form.buildingId" style="width: 100%" @change="onBuildingChange">
          <el-option v-for="b in buildingOptions" :key="b.id" :label="`${b.buildingCode}-${b.buildingName}`" :value="b.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="房间">
        <el-select v-model="form.roomId" filterable style="width: 100%">
          <el-option v-for="r in roomOptions" :key="r.id" :label="`${r.roomNo}`" :value="r.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="访客姓名"><el-input v-model="form.visitorName" /></el-form-item>
      <el-form-item label="电话"><el-input v-model="form.visitorPhone" /></el-form-item>
      <el-form-item label="证件号"><el-input v-model="form.visitorIdCard" /></el-form-item>
      <el-form-item label="来访原因"><el-input v-model="form.visitReason" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onCreate">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../../store/auth'
import { fetchBuildingOptions } from '../../api/building'
import { fetchRoomOptions } from '../../api/room'
import { fetchVisitorPage, createVisitor, checkoutVisitor, deleteVisitor } from '../../api/visitor'

const auth = useAuthStore()
const isAdmin = computed(() => auth.roles.includes('ADMIN'))

const loading = ref(false)
const saving = ref(false)

const activeOnly = ref(true)
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rows = ref([])

const buildingOptions = ref([])
const roomOptions = ref([])

const load = async () => {
  loading.value = true
  try {
    const data = await fetchVisitorPage({ page: pageNo.value, size: pageSize.value, activeOnly: activeOnly.value })
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

const createVisible = ref(false)
const form = reactive({ buildingId: null, roomId: null, visitorName: '', visitorPhone: '', visitorIdCard: '', visitReason: '' })

const openCreate = async () => {
  buildingOptions.value = await fetchBuildingOptions()
  form.buildingId = buildingOptions.value[0]?.id ?? null
  await onBuildingChange(form.buildingId)
  form.roomId = roomOptions.value[0]?.id ?? null
  form.visitorName = ''
  form.visitorPhone = ''
  form.visitorIdCard = ''
  form.visitReason = ''
  createVisible.value = true
}

const onBuildingChange = async (bid) => {
  roomOptions.value = await fetchRoomOptions({ buildingId: bid })
}

const onCreate = async () => {
  if (!form.buildingId || !form.visitorName) {
    ElMessage.error('请选择楼栋并填写访客姓名')
    return
  }
  saving.value = true
  try {
    await createVisitor({
      buildingId: form.buildingId,
      roomId: form.roomId || undefined,
      visitorName: form.visitorName,
      visitorPhone: form.visitorPhone || undefined,
      visitorIdCard: form.visitorIdCard || undefined,
      visitReason: form.visitReason || undefined
    })
    ElMessage.success('登记成功')
    createVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const onCheckout = async (row) => {
  saving.value = true
  try {
    await checkoutVisitor(row.id)
    ElMessage.success('已登记离开')
    await load()
  } finally {
    saving.value = false
  }
}

const onDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该来访记录？', '提示', { type: 'warning' })
  await deleteVisitor(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(() => load())
</script>
