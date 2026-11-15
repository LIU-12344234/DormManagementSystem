export const ROLE = {
  ADMIN: 'ADMIN',
  STUDENT: 'STUDENT',
  WORKER: 'WORKER',
  DORM_MANAGER: 'DORM_MANAGER'
}

export function getMenusByRoles(roles) {
  const has = (r) => roles.includes(r)

  const adminMenus = [
    { path: '/dashboard', title: '仪表盘' },
    { path: '/users', title: '用户管理' },
    { path: '/students', title: '学生信息管理' },
    { path: '/buildings', title: '宿舍楼管理' },
    { path: '/rooms', title: '宿舍信息管理' },
    { path: '/beds', title: '床位管理' },
    { path: '/assignments', title: '床位分配' },
    { path: '/repairs', title: '报修管理' },
    { path: '/visitors', title: '访客管理' },
    { path: '/late-returns', title: '晚归登记' },
    { path: '/reports', title: '统计报表' },
    { path: '/notices', title: '通知管理' },
    { path: '/settings', title: '系统设置' }
  ]

  const dormManagerMenus = [
    { path: '/dashboard', title: '仪表盘' },
    { path: '/students', title: '学生信息管理' },
    { path: '/buildings', title: '宿舍楼管理' },
    { path: '/rooms', title: '宿舍信息管理' },
    { path: '/beds', title: '床位管理' },
    { path: '/assignments', title: '床位分配' },
    { path: '/repairs', title: '报修管理' },
    { path: '/visitors', title: '访客管理' },
    { path: '/late-returns', title: '晚归登记' },
    { path: '/reports', title: '统计报表' },
    { path: '/notices', title: '通知管理' }
  ]

  const workerMenus = [
    { path: '/dashboard', title: '仪表盘' },
    { path: '/repairs', title: '报修工单' },
    { path: '/reports', title: '统计报表' },
    { path: '/notices', title: '通知' }
  ]

  const studentMenus = [
    { path: '/dashboard', title: '个人中心' },
    { path: '/my-profile', title: '我的信息' },
    { path: '/my-repairs', title: '我的报修' },
    { path: '/notices', title: '通知' }
  ]

  if (has(ROLE.ADMIN)) return adminMenus
  if (has(ROLE.DORM_MANAGER)) return dormManagerMenus
  if (has(ROLE.WORKER)) return workerMenus
  return studentMenus
}
