<template>
  <el-card>
    <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px">
      <div style="font-size: 16px; font-weight: 600">床位管理</div>
      <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap; justify-content: flex-end">
        <el-select v-model="roomId" placeholder="全部房间" clearable filterable style="width: 220px" @change="onFilterChange">
          <el-option v-for="r in roomOptions" :key="r.id" :label="`${r.roomNo}`" :value="r.id" />
        </el-select>
        <el-select v-model="status" placeholder="状态" clearable style="width: 140px" @change="onFilterChange">
          <el-option label="空床" value="EMPTY" />
          <el-option label="已入住" value="OCCUPIED" />
          <el-option label="停用" value="DISABLED" />
        </el-select>
        <el-button type="primary" @click="openCreate">新增</el-button>
      </div>
    </div>

    <el-table :data="rows" style="width: 100%; margin-top: 12px" v-loading="loading">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="roomId" label="房间ID" width="110" />
      <el-table-column prop="bedNo" label="床位号" width="100" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="tagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="onDelete(scope.row)">删除</el-button>
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

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="房间">
        <el-select v-model="form.roomId" filterable style="width: 100%">
          <el-option v-for="r in roomOptions" :key="r.id" :label="`${r.roomNo}`" :value="r.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="床位号">
        <el-input v-model="form.bedNo" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status" style="width: 100%">
          <el-option label="空床" value="EMPTY" />
          <el-option label="已入住" value="OCCUPIED" />
          <el-option label="停用" value="DISABLED" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchRoomOptions } from '../../api/room'
import { fetchBedPage, createBed, updateBed, deleteBed } from '../../api/bed'

const loading = ref(false)
const saving = ref(false)

const roomOptions = ref([])
const roomId = ref(null)
const status = ref(null)

const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rows = ref([])

const dialogVisible = ref(false)
const dialogMode = ref('create')
const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增床位' : '编辑床位'))

const form = reactive({
  id: null,
  roomId: null,
  bedNo: '',
  status: 'EMPTY'
})

const tagType = (s) => {
  if (s === 'EMPTY') return 'success'
  if (s === 'OCCUPIED') return 'warning'
  if (s === 'DISABLED') return 'info'
  return ''
}

const statusText = (s) => {
  if (s === 'EMPTY') return '空床'
  if (s === 'OCCUPIED') return '已入住'
  if (s === 'DISABLED') return '停用'
  return s
}

const loadRoomOptions = async () => {
  roomOptions.value = await fetchRoomOptions({})
}

const load = async () => {
  loading.value = true
  try {
    const data = await fetchBedPage({
      page: pageNo.value,
      size: pageSize.value,
      roomId: roomId.value || undefined,
      status: status.value || undefined
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

const openCreate = () => {
  dialogMode.value = 'create'
  form.id = null
  form.roomId = roomId.value || (roomOptions.value[0] ? roomOptions.value[0].id : null)
  form.bedNo = ''
  form.status = 'EMPTY'
  dialogVisible.value = true
}

const openEdit = (row) => {
  dialogMode.value = 'edit'
  form.id = row.id
  form.roomId = row.roomId
  form.bedNo = row.bedNo
  form.status = row.status
  dialogVisible.value = true
}

const onSave = async () => {
  if (!form.roomId || !form.bedNo || !form.status) {
    ElMessage.error('请填写完整信息')
    return
  }
  saving.value = true
  try {
    const payload = {
      roomId: form.roomId,
      bedNo: form.bedNo,
      status: form.status
    }
    if (dialogMode.value === 'create') {
      await createBed(payload)
      ElMessage.success('创建成功')
    } else {
      await updateBed(form.id, payload)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const onDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除床位【${row.bedNo}】？`, '提示', { type: 'warning' })
  await deleteBed(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(async () => {
  await loadRoomOptions()
  await load()
})
</script>
