<template>
  <el-card>
    <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px">
      <div style="font-size: 16px; font-weight: 600">用户管理</div>
      <div style="display: flex; gap: 8px; align-items: center">
        <el-input v-model="keyword" placeholder="用户名/姓名/电话" style="width: 240px" clearable @keyup.enter="load" />
        <el-button type="primary" @click="openCreate">新增用户</el-button>
      </div>
    </div>

    <el-table :data="rows" style="width: 100%; margin-top: 12px" v-loading="loading">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="displayName" label="姓名" width="140" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="enabled" label="启用" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.enabled === 1 ? 'success' : 'info'">{{ scope.row.enabled === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="roles" label="角色" min-width="200">
        <template #default="scope">
          <el-tag v-for="r in scope.row.roles" :key="r" style="margin-right: 6px">{{ r }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-button size="small" @click="openRoles(scope.row)">角色</el-button>
          <el-button size="small" type="warning" @click="openReset(scope.row)">重置密码</el-button>
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

  <el-dialog v-model="editVisible" :title="editTitle" width="560px">
    <el-form :model="editForm" label-width="90px">
      <el-form-item label="用户名" v-if="mode === 'create'">
        <el-input v-model="editForm.username" />
      </el-form-item>
      <el-form-item label="密码" v-if="mode === 'create'">
        <el-input v-model="editForm.password" type="password" show-password />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="editForm.displayName" />
      </el-form-item>
      <el-form-item label="电话">
        <el-input v-model="editForm.phone" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="editForm.email" />
      </el-form-item>
      <el-form-item label="启用">
        <el-switch v-model="enabledBool" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="rolesVisible" title="分配角色" width="520px">
    <el-form :model="rolesForm" label-width="90px">
      <el-form-item label="角色">
        <el-select v-model="rolesForm.roleIds" multiple style="width: 100%">
          <el-option v-for="r in roleOptions" :key="r.id" :label="`${r.roleKey}-${r.roleName}`" :value="r.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="rolesVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSaveRoles">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="resetVisible" title="重置密码" width="420px">
    <el-form :model="resetForm" label-width="90px">
      <el-form-item label="新密码">
        <el-input v-model="resetForm.newPassword" type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="resetVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onReset">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchUserPage, createUser, updateUser, resetUserPassword, setUserRoles } from '../../api/user'
import { fetchRoles } from '../../api/role'

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rows = ref([])

const roleOptions = ref([])

const loadRoles = async () => {
  roleOptions.value = await fetchRoles()
}

const load = async () => {
  loading.value = true
  try {
    const data = await fetchUserPage({ page: pageNo.value, size: pageSize.value, keyword: keyword.value || undefined })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const editVisible = ref(false)
const mode = ref('create')
const editTitle = computed(() => (mode.value === 'create' ? '新增用户' : '编辑用户'))
const editForm = reactive({ id: null, username: '', password: '', displayName: '', phone: '', email: '', enabled: 1 })

const enabledBool = computed({
  get: () => editForm.enabled === 1,
  set: (v) => (editForm.enabled = v ? 1 : 0)
})

const openCreate = async () => {
  await loadRoles()
  mode.value = 'create'
  editForm.id = null
  editForm.username = ''
  editForm.password = ''
  editForm.displayName = ''
  editForm.phone = ''
  editForm.email = ''
  editForm.enabled = 1
  editVisible.value = true
}

const openEdit = (row) => {
  mode.value = 'edit'
  editForm.id = row.id
  editForm.displayName = row.displayName
  editForm.phone = row.phone
  editForm.email = row.email
  editForm.enabled = row.enabled
  editVisible.value = true
}

const onSave = async () => {
  if (mode.value === 'create') {
    if (!editForm.username || !editForm.password || !editForm.displayName) {
      ElMessage.error('请填写用户名、密码、姓名')
      return
    }
  } else {
    if (!editForm.displayName) {
      ElMessage.error('请填写姓名')
      return
    }
  }
  saving.value = true
  try {
    if (mode.value === 'create') {
      await createUser({
        username: editForm.username,
        password: editForm.password,
        displayName: editForm.displayName,
        phone: editForm.phone || undefined,
        email: editForm.email || undefined,
        enabled: editForm.enabled,
        roleIds: []
      })
      ElMessage.success('创建成功')
    } else {
      await updateUser(editForm.id, {
        displayName: editForm.displayName,
        phone: editForm.phone || undefined,
        email: editForm.email || undefined,
        enabled: editForm.enabled
      })
      ElMessage.success('更新成功')
    }
    editVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const rolesVisible = ref(false)
const rolesForm = reactive({ id: null, roleIds: [] })

const openRoles = async (row) => {
  await loadRoles()
  rolesForm.id = row.id
  // row.roles are roleKey; map to ids
  const keys = row.roles || []
  rolesForm.roleIds = roleOptions.value.filter((r) => keys.includes(r.roleKey)).map((r) => r.id)
  rolesVisible.value = true
}

const onSaveRoles = async () => {
  saving.value = true
  try {
    await setUserRoles(rolesForm.id, { roleIds: rolesForm.roleIds })
    ElMessage.success('保存成功')
    rolesVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const resetVisible = ref(false)
const resetForm = reactive({ id: null, newPassword: '' })

const openReset = (row) => {
  resetForm.id = row.id
  resetForm.newPassword = ''
  resetVisible.value = true
}

const onReset = async () => {
  if (!resetForm.newPassword) {
    ElMessage.error('请输入新密码')
    return
  }
  saving.value = true
  try {
    await resetUserPassword(resetForm.id, { newPassword: resetForm.newPassword })
    ElMessage.success('重置成功')
    resetVisible.value = false
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  load()
})
</script>
