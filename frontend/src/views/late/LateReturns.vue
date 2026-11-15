<template>
  <el-card>
    <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px">
      <div style="font-size: 16px; font-weight: 600">晚归登记</div>
      <div style="display: flex; gap: 8px; align-items: center">
        <el-button type="primary" @click="openCreate">新增登记</el-button>
      </div>
    </div>

    <el-table :data="rows" style="width: 100%; margin-top: 12px" v-loading="loading">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="studentId" label="学生ID" width="110" />
      <el-table-column prop="lateTime" label="晚归时间" width="180" />
      <el-table-column prop="reason" label="原因" min-width="200" />
      <el-table-column prop="handledBy" label="处理人ID" width="110" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="scope">
          <el-button v-if="isAdmin" size="small" type="danger" @click="onDelete(scope.row)">删除</el-button>
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

  <el-dialog v-model="createVisible" title="新增晚归登记" width="560px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="学生">
        <el-select v-model="form.studentId" filterable style="width: 100%">
          <el-option v-for="s in studentOptions" :key="s.id" :label="`${s.studentNo}-${s.name}`" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="原因">
        <el-input v-model="form.reason" placeholder="可选" />
      </el-form-item>
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
import { fetchStudentOptions } from '../../api/student'
import { fetchLateReturnPage, createLateReturn, deleteLateReturn } from '../../api/lateReturn'

const auth = useAuthStore()
const isAdmin = computed(() => auth.roles.includes('ADMIN'))

const loading = ref(false)
const saving = ref(false)

const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rows = ref([])

const studentOptions = ref([])

const load = async () => {
  loading.value = true
  try {
    const data = await fetchLateReturnPage({ page: pageNo.value, size: pageSize.value })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const createVisible = ref(false)
const form = reactive({ studentId: null, reason: '' })

const openCreate = async () => {
  studentOptions.value = await fetchStudentOptions({})
  form.studentId = studentOptions.value[0]?.id ?? null
  form.reason = ''
  createVisible.value = true
}

const onCreate = async () => {
  if (!form.studentId) {
    ElMessage.error('请选择学生')
    return
  }
  saving.value = true
  try {
    await createLateReturn({ studentId: form.studentId, reason: form.reason || undefined })
    ElMessage.success('创建成功')
    createVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const onDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该记录？', '提示', { type: 'warning' })
  await deleteLateReturn(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(() => load())
</script>
