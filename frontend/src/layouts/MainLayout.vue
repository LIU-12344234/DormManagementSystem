<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="border-right: 1px solid var(--el-border-color)">
      <div style="height: 56px; display: flex; align-items: center; padding: 0 16px; font-weight: 600;">
        宿舍管理系统
      </div>
      <el-menu :default-active="active" router>
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          {{ m.title }}
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header style="display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--el-border-color)">
        <div></div>
        <div style="display: flex; align-items: center; gap: 12px">
          <span>{{ auth.displayName || auth.username }}</span>
          <el-button size="small" @click="onLogout">退出</el-button>
        </div>
      </el-header>
      <el-main style="background: #f6f7fb">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { getMenusByRoles } from '../menus'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const menus = computed(() => getMenusByRoles(auth.roles || []))
const active = computed(() => route.path)

const onLogout = () => {
  auth.logout()
  router.push('/login')
}
</script>
