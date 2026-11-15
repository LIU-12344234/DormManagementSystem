<template>
  <el-card>
    <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px">
      <div style="font-size: 16px; font-weight: 600">系统设置</div>
      <div style="display: flex; gap: 8px; align-items: center">
        <el-input v-model="keyword" placeholder="Key/描述" style="width: 240px" clearable @keyup.enter="load" />
        <el-button type="primary" @click="openCreate">新增</el-button>
      </div>
    </div>

    <el-table :data="rows" style="width: 100%; margin-top: 12px" v-loading="loading">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="settingKey" label="Key" width="220" />
      <el-table-column prop="settingValue" label="Value" min-width="220" />
      <el-table-column prop="description" label="描述" min-width="200" />
      <el-table-column prop="updatedBy" label="更新人ID" width="110" />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
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

  <el-dialog v-model="editVisible" :title="editTitle" width="720px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="Key"><el-input v-model="form.settingKey" /></el-form-item>
      <el-form-item label="Value"><el-input v-model="form.settingValue" /></el-form-item>
      <el-form-item label="描述"><el-input v-model="form.description" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchSettingPage, createSetting, updateSetting, deleteSetting } from '../../api/setting'

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')

const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rows = ref([])

const load = async () => {
  loading.value = true
  try {
    const data = await fetchSettingPage({ page: pageNo.value, size: pageSize.value, keyword: keyword.value || undefined })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const editVisible = ref(false)
const mode = ref('create')
const editTitle = computed(() => (mode.value === 'create' ? '新增配置' : '编辑配置'))
const form = reactive({ id: null, settingKey: '', settingValue: '', description: '' })

const openCreate = () => {
  mode.value = 'create'
  form.id = null
  form.settingKey = ''
  form.settingValue = ''
  form.description = ''
  editVisible.value = true
}

const openEdit = (row) => {
  mode.value = 'edit'
  Object.assign(form, row)
  editVisible.value = true
}

const onSave = async () => {
  if (!form.settingKey) {
    ElMessage.error('Key不能为空')
    return
  }
  saving.value = true
  try {
    const payload = { settingKey: form.settingKey, settingValue: form.settingValue || undefined, description: form.description || undefined }
    if (mode.value === 'create') {
      await createSetting(payload)
      ElMessage.success('创建成功')
    } else {
      await updateSetting(form.id, payload)
      ElMessage.success('更新成功')
    }
    editVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const onDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除配置【${row.settingKey}】？`, '提示', { type: 'warning' })
  await deleteSetting(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(() => load())
</script>
