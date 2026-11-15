<template>
  <el-card>
    <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px">
      <div style="font-size: 16px; font-weight: 600">宿舍信息管理</div>
      <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap; justify-content: flex-end">
        <el-select v-model="buildingId" placeholder="全部楼栋" clearable style="width: 180px" @change="onFilterChange">
          <el-option v-for="b in buildingOptions" :key="b.id" :label="`${b.buildingCode}-${b.buildingName}`" :value="b.id" />
        </el-select>
        <el-input v-model="keyword" placeholder="房间号" style="width: 180px" clearable @keyup.enter="load" />
        <el-button type="primary" @click="openCreate">新增</el-button>
      </div>
    </div>

    <el-table :data="rows" style="width: 100%; margin-top: 12px" v-loading="loading">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="buildingId" label="楼栋ID" width="100" />
      <el-table-column prop="floorNo" label="楼层" width="90" />
      <el-table-column prop="roomNo" label="房间号" width="120" />
      <el-table-column prop="roomType" label="类型" width="120" />
      <el-table-column prop="capacity" label="容量" width="90" />
      <el-table-column prop="genderLimit" label="性别限制" width="120" />
      <el-table-column prop="enabled" label="启用" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.enabled === 1 ? 'success' : 'info'">
            {{ scope.row.enabled === 1 ? '启用' : '停用' }}
          </el-tag>
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

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="楼栋">
        <el-select v-model="form.buildingId" placeholder="请选择" style="width: 100%">
          <el-option v-for="b in buildingOptions" :key="b.id" :label="`${b.buildingCode}-${b.buildingName}`" :value="b.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="楼层">
        <el-input-number v-model="form.floorNo" :min="1" />
      </el-form-item>
      <el-form-item label="房间号">
        <el-input v-model="form.roomNo" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="form.roomType" style="width: 100%">
          <el-option label="普通" value="NORMAL" />
        </el-select>
      </el-form-item>
      <el-form-item label="容量">
        <el-input-number v-model="form.capacity" :min="1" :max="12" />
      </el-form-item>
      <el-form-item label="性别限制">
        <el-select v-model="form.genderLimit" style="width: 100%">
          <el-option label="男" value="M" />
          <el-option label="女" value="F" />
          <el-option label="混合" value="MIX" />
        </el-select>
      </el-form-item>
      <el-form-item label="启用">
        <el-switch v-model="enabledBool" />
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
import { fetchBuildingOptions } from '../../api/building'
import { fetchRoomPage, createRoom, updateRoom, deleteRoom } from '../../api/room'

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const buildingId = ref(null)

const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rows = ref([])

const buildingOptions = ref([])

const dialogVisible = ref(false)
const dialogMode = ref('create')
const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增房间' : '编辑房间'))

const form = reactive({
  id: null,
  buildingId: null,
  floorNo: 1,
  roomNo: '',
  roomType: 'NORMAL',
  capacity: 4,
  genderLimit: 'MIX',
  enabled: 1
})

const enabledBool = computed({
  get: () => form.enabled === 1,
  set: (v) => (form.enabled = v ? 1 : 0)
})

const loadBuildingOptions = async () => {
  buildingOptions.value = await fetchBuildingOptions()
}

const load = async () => {
  loading.value = true
  try {
    const data = await fetchRoomPage({
      page: pageNo.value,
      size: pageSize.value,
      buildingId: buildingId.value || undefined,
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

const openCreate = () => {
  dialogMode.value = 'create'
  form.id = null
  form.buildingId = buildingId.value || (buildingOptions.value[0] ? buildingOptions.value[0].id : null)
  form.floorNo = 1
  form.roomNo = ''
  form.roomType = 'NORMAL'
  form.capacity = 4
  form.genderLimit = 'MIX'
  form.enabled = 1
  dialogVisible.value = true
}

const openEdit = (row) => {
  dialogMode.value = 'edit'
  form.id = row.id
  form.buildingId = row.buildingId
  form.floorNo = row.floorNo
  form.roomNo = row.roomNo
  form.roomType = row.roomType
  form.capacity = row.capacity
  form.genderLimit = row.genderLimit
  form.enabled = row.enabled
  dialogVisible.value = true
}

const onSave = async () => {
  if (!form.buildingId || !form.roomNo || !form.roomType || !form.genderLimit) {
    ElMessage.error('请填写完整信息')
    return
  }

  saving.value = true
  try {
    const payload = {
      buildingId: form.buildingId,
      floorNo: form.floorNo,
      roomNo: form.roomNo,
      roomType: form.roomType,
      capacity: form.capacity,
      genderLimit: form.genderLimit,
      enabled: form.enabled
    }
    if (dialogMode.value === 'create') {
      await createRoom(payload)
      ElMessage.success('创建成功')
    } else {
      await updateRoom(form.id, payload)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const onDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除房间【${row.roomNo}】？`, '提示', { type: 'warning' })
  await deleteRoom(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(async () => {
  await loadBuildingOptions()
  await load()
})
</script>
