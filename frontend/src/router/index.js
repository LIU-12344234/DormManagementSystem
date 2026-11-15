import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'

import Login from '../views/Login.vue'
import MainLayout from '../layouts/MainLayout.vue'

import Dashboard from '../views/Dashboard.vue'
import Users from '../views/user/Users.vue'
import Students from '../views/student/Students.vue'
import Buildings from '../views/dorm/Buildings.vue'
import Rooms from '../views/dorm/Rooms.vue'
import Beds from '../views/dorm/Beds.vue'
import Assignments from '../views/dorm/Assignments.vue'
import Repairs from '../views/repair/Repairs.vue'
import Visitors from '../views/visitor/Visitors.vue'
import LateReturns from '../views/late/LateReturns.vue'
import Reports from '../views/report/Reports.vue'
import Notices from '../views/notice/Notices.vue'
import Settings from '../views/setting/Settings.vue'
import MyProfile from '../views/student/MyProfile.vue'
import MyRepairs from '../views/student/MyRepairs.vue'

const routes = [
  { path: '/login', component: Login },
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: Dashboard },
      { path: 'users', component: Users, meta: { roles: ['ADMIN'] } },
      { path: 'students', component: Students, meta: { roles: ['ADMIN', 'DORM_MANAGER'] } },
      { path: 'buildings', component: Buildings, meta: { roles: ['ADMIN', 'DORM_MANAGER'] } },
      { path: 'rooms', component: Rooms, meta: { roles: ['ADMIN', 'DORM_MANAGER'] } },
      { path: 'beds', component: Beds, meta: { roles: ['ADMIN', 'DORM_MANAGER'] } },
      { path: 'assignments', component: Assignments, meta: { roles: ['ADMIN', 'DORM_MANAGER'] } },
      { path: 'repairs', component: Repairs, meta: { roles: ['ADMIN', 'DORM_MANAGER', 'WORKER'] } },
      { path: 'visitors', component: Visitors, meta: { roles: ['ADMIN', 'DORM_MANAGER'] } },
      { path: 'late-returns', component: LateReturns, meta: { roles: ['ADMIN', 'DORM_MANAGER'] } },
      { path: 'reports', component: Reports, meta: { roles: ['ADMIN', 'DORM_MANAGER', 'WORKER'] } },
      { path: 'notices', component: Notices },
      { path: 'settings', component: Settings, meta: { roles: ['ADMIN'] } },
      { path: 'my-profile', component: MyProfile, meta: { roles: ['STUDENT'] } },
      { path: 'my-repairs', component: MyRepairs, meta: { roles: ['STUDENT'] } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!auth.token) auth.load()

  if (to.path !== '/login' && !auth.token) {
    return '/login'
  }

  if (to.path === '/login' && auth.token) {
    return '/'
  }

  const needRoles = to.meta?.roles
  if (needRoles && Array.isArray(needRoles) && needRoles.length > 0) {
    const ok = auth.roles.some((r) => needRoles.includes(r))
    if (!ok) return '/dashboard'
  }

  return true
})

export default router
