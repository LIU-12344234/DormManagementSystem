<template>
  <el-card>
    <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px">
      <div style="font-size: 16px; font-weight: 600">学生信息管理</div>
      <div style="display: flex; gap: 8px; align-items: center">
        <el-input v-model="keyword" placeholder="学号/姓名/班级" style="width: 240px" clearable @keyup.enter="load" />
        <el-button v-if="isAdmin" type="primary" @click="openCreate">新增</el-button>
      </div>
    </div>

    <el-table :data="rows" style="width: 100%; margin-top: 12px" v-loading="loading">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="studentNo" label="学号" width="140" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="gender" label="性别" width="90" />
      <el-table-column prop="className" label="班级" width="140" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="status" label="状态" width="110" />
      <el-table-column label="操作" width="200" fixed="right" v-if="isAdmin">
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

  <el-dialog v-model="editVisible" :title="editTitle" width="620px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="学号"><el-input v-model="form.studentNo" /></el-form-item>
      <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="性别">
        <el-select v-model="form.gender" style="width: 100%">
          <el-option label="男" value="M" />
          <el-option label="女" value="F" />
        </el-select>
      </el-form-item>
      <el-form-item label="学院"><el-input v-model="form.college" /></el-form-item>
      <el-form-item label="专业"><el-input v-model="form.major" /></el-form-item>
      <el-form-item label="班级"><el-input v-model="form.className" /></el-form-item>
      <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status" style="width: 100%">
          <el-option label="在校" value="ACTIVE" />
          <el-option label="毕业/离校" value="INACTIVE" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../../store/auth'
import { request } from '../../api/request'

const auth = useAuthStore()
const isAdmin = computed(() => auth.roles.includes('ADMIN'))

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
    const data = await request({ url: '/api/students', method: 'GET', params: { page: pageNo.value, size: pageSize.value, keyword: keyword.value || undefined } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const editVisible = ref(false)
const mode = ref('create')
const editTitle = computed(() => (mode.value === 'create' ? '新增学生' : '编辑学生'))

const form = reactive({ id: null, studentNo: '', name: '', gender: 'M', college: '', major: '', className: '', phone: '', status: 'ACTIVE' })

const openCreate = () => {
  mode.value = 'create'
  form.id = null
  form.studentNo = ''
  form.name = ''
  form.gender = 'M'
  form.college = ''
  form.major = ''
  form.className = ''
  form.phone = ''
  form.status = 'ACTIVE'
  editVisible.value = true
}

const openEdit = (row) => {
  mode.value = 'edit'
  Object.assign(form, row)
  editVisible.value = true
}

const onSave = async () => {
  if (!form.studentNo || !form.name || !form.gender || !form.status) {
    ElMessage.error('请填写完整信息')
    return
  }
  saving.value = true
  try {
    const payload = {
      studentNo: form.studentNo,
      name: form.name,
      gender: form.gender,
      college: form.college || undefined,
      major: form.major || undefined,
      className: form.className || undefined,
      phone: form.phone || undefined,
      status: form.status
    }
    if (mode.value === 'create') {
      await request({ url: '/api/students', method: 'POST', data: payload })
      ElMessage.success('创建成功')
    } else {
      await request({ url: `/api/students/${form.id}`, method: 'PUT', data: payload })
      ElMessage.success('更新成功')
    }
    editVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const onDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除学生【${row.name}】？`, '提示', { type: 'warning' })
  await request({ url: `/api/students/${row.id}`, method: 'DELETE' })
  ElMessage.success('删除成功')
  await load()
}

onMounted(() => load())
</script>
