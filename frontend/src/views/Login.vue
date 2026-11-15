<template>
  <div style="height: 100vh; display: flex; justify-content: center; align-items: center; background: #f6f7fb">
    <el-card style="width: 380px">
      <div style="font-size: 18px; font-weight: 600; margin-bottom: 16px">登录</div>
      <el-form :model="form" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="onLogin">登录</el-button>
        </el-form-item>
      </el-form>
      <el-alert
        title="默认账号：admin / admin123"
        type="info"
        :closable="false"
        show-icon
      />
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const auth = useAuthStore()

const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: 'admin123'
})

const onLogin = async () => {
  loading.value = true
  try {
    const resp = await http.post('/api/auth/login', form)
    const body = resp.data
    if (body?.code !== 0) {
      ElMessage.error(body?.message || '登录失败')
      return
    }
    auth.setAuth(body.data)
    router.push('/')
  } catch (e) {
    ElMessage.error('登录失败')
  } finally {
    loading.value = false
  }
}
</script>
