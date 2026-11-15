<template>
  <el-card>
    <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px">
      <div style="font-size: 16px; font-weight: 600">宿舍楼管理</div>
      <div style="display: flex; gap: 8px; align-items: center">
        <el-input v-model="keyword" placeholder="楼栋编号/名称" style="width: 220px" clearable @keyup.enter="load" />
        <el-button type="primary" @click="openCreate">新增</el-button>
      </div>
    </div>

    <el-table :data="rows" style="width: 100%; margin-top: 12px" v-loading="loading">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="buildingCode" label="楼栋编号" width="140" />
      <el-table-column prop="buildingName" label="楼栋名称" min-width="180" />
      <el-table-column prop="address" label="地址" min-width="200" />
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

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="楼栋编号">
        <el-input v-model="form.buildingCode" />
      </el-form-item>
      <el-form-item label="楼栋名称">
        <el-input v-model="form.buildingName" />
      </el-form-item>
      <el-form-item label="地址">
        <el-input v-model="form.address" />
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
import { ElMessageBox, ElMessage } from 'element-plus'
import { fetchBuildingPage, createBuilding, updateBuilding, deleteBuilding } from '../../api/building'

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rows = ref([])

const dialogVisible = ref(false)
const dialogMode = ref('create')
const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增楼栋' : '编辑楼栋'))

const form = reactive({
  id: null,
  buildingCode: '',
  buildingName: '',
  address: '',
  enabled: 1
})

const enabledBool = computed({
  get: () => form.enabled === 1,
  set: (v) => (form.enabled = v ? 1 : 0)
})

const load = async () => {
  loading.value = true
  try {
    const data = await fetchBuildingPage({
      page: pageNo.value,
      size: pageSize.value,
      keyword: keyword.value || undefined
    })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  dialogMode.value = 'create'
  form.id = null
  form.buildingCode = ''
  form.buildingName = ''
  form.address = ''
  form.enabled = 1
  dialogVisible.value = true
}

const openEdit = (row) => {
  dialogMode.value = 'edit'
  form.id = row.id
  form.buildingCode = row.buildingCode
  form.buildingName = row.buildingName
  form.address = row.address
  form.enabled = row.enabled
  dialogVisible.value = true
}

const onSave = async () => {
  if (!form.buildingCode || !form.buildingName) {
    ElMessage.error('请填写楼栋编号与名称')
    return
  }

  saving.value = true
  try {
    const payload = {
      buildingCode: form.buildingCode,
      buildingName: form.buildingName,
      address: form.address,
      enabled: form.enabled
    }
    if (dialogMode.value === 'create') {
      await createBuilding(payload)
      ElMessage.success('创建成功')
    } else {
      await updateBuilding(form.id, payload)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const onDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除楼栋【${row.buildingName}】？`, '提示', {
    type: 'warning'
  })
  await deleteBuilding(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(() => {
  load()
})
</script>
