<template>
  <el-card>
    <div style="font-size: 16px; font-weight: 600; margin-bottom: 8px">我的信息</div>
    <el-descriptions v-if="profile" :column="2" border>
      <el-descriptions-item label="学号">{{ profile.studentNo }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ profile.name }}</el-descriptions-item>
      <el-descriptions-item label="性别">{{ profile.gender }}</el-descriptions-item>
      <el-descriptions-item label="班级">{{ profile.className }}</el-descriptions-item>
      <el-descriptions-item label="学院">{{ profile.college }}</el-descriptions-item>
      <el-descriptions-item label="专业">{{ profile.major }}</el-descriptions-item>
      <el-descriptions-item label="电话">{{ profile.phone }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ profile.status }}</el-descriptions-item>
    </el-descriptions>
    <el-empty v-else description="暂无学生信息" />
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchMyStudentProfile } from '../../api/student'

const profile = ref(null)

const load = async () => {
  profile.value = await fetchMyStudentProfile()
}

onMounted(() => {
  load()
})
</script>
