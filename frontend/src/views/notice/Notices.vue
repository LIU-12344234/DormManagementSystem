<template>
  <el-card>
    <div style="display: flex; justify-content: space-between; align-items: center; gap: 12px">
      <div style="font-size: 16px; font-weight: 600">通知管理</div>
      <div style="display: flex; gap: 8px; align-items: center">
        <el-input v-model="keyword" placeholder="标题关键字" style="width: 240px" clearable @keyup.enter="load" />
        <el-button v-if="canPublish" type="primary" @click="openPublish">发布通知</el-button>
      </div>
    </div>

    <el-table :data="rows" style="width: 100%; margin-top: 12px" v-loading="loading">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="title" label="标题" min-width="220" />
      <el-table-column prop="publishAt" label="发布时间" width="180" />
      <el-table-column prop="publisherUserId" label="发布人ID" width="110" />
      <el-table-column label="已读" width="90">
        <template #default="scope">
          <el-tag :type="readMap[scope.row.id] ? 'success' : 'info'">{{ readMap[scope.row.id] ? '已读' : '未读' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="openDetail(scope.row)">查看</el-button>
          <el-button size="small" type="primary" :disabled="readMap[scope.row.id]" @click="onRead(scope.row)">标记已读</el-button>
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

  <el-dialog v-model="publishVisible" title="发布通知" width="680px">
    <el-form :model="publishForm" label-width="90px">
      <el-form-item label="标题"><el-input v-model="publishForm.title" /></el-form-item>
      <el-form-item label="内容"><el-input v-model="publishForm.content" type="textarea" :rows="6" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="publishVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onPublish">发布</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="detailVisible" title="通知详情" width="720px">
    <div style="font-size: 16px; font-weight: 700; margin-bottom: 8px">{{ detail.title }}</div>
    <div style="color: #999; margin-bottom: 12px">发布时间：{{ detail.publishAt }} / 发布人：{{ detail.publisherUserId }}</div>
    <div style="white-space: pre-wrap; line-height: 1.7">{{ detail.content }}</div>
    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../store/auth'
import { fetchNoticePage, publishNotice, markNoticeRead, getNoticeReadState } from '../../api/notice'

const auth = useAuthStore()
const canPublish = computed(() => auth.roles.includes('ADMIN') || auth.roles.includes('DORM_MANAGER'))

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')

const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rows = ref([])

const readMap = reactive({})

const loadReadStateBatch = async (list) => {
  for (const n of list) {
    try {
      const data = await getNoticeReadState(n.id)
      readMap[n.id] = !!data.read
    } catch {
      readMap[n.id] = false
    }
  }
}

const load = async () => {
  loading.value = true
  try {
    const data = await fetchNoticePage({ page: pageNo.value, size: pageSize.value, keyword: keyword.value || undefined })
    rows.value = data.records || []
    total.value = data.total || 0
    await loadReadStateBatch(rows.value)
  } finally {
    loading.value = false
  }
}

const publishVisible = ref(false)
const publishForm = reactive({ title: '', content: '' })

const openPublish = () => {
  publishForm.title = ''
  publishForm.content = ''
  publishVisible.value = true
}

const onPublish = async () => {
  if (!publishForm.title || !publishForm.content) {
    ElMessage.error('请填写标题与内容')
    return
  }
  saving.value = true
  try {
    await publishNotice({ title: publishForm.title, content: publishForm.content })
    ElMessage.success('发布成功')
    publishVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const onRead = async (row) => {
  await markNoticeRead(row.id)
  readMap[row.id] = true
  ElMessage.success('已读')
}

const detailVisible = ref(false)
const detail = reactive({ title: '', content: '', publishAt: '', publisherUserId: '' })

const openDetail = (row) => {
  Object.assign(detail, row)
  detailVisible.value = true
}

onMounted(() => load())
</script>
