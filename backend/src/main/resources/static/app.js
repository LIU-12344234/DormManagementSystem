/* global window, document */

const $ = (id) => document.getElementById(id)

const state = {
  token: localStorage.getItem('token') || '',
  user: null,
  activeTab: 'overview'
}

function setStatusText(text) {
  $('statusText').textContent = text
}

function setLoginMsg(text) {
  $('loginMsg').textContent = text
}

function setLoggedInUI(isLoggedIn) {
  $('loginBtn').style.display = isLoggedIn ? 'none' : 'inline-block'
  $('logoutBtn').style.display = isLoggedIn ? 'inline-block' : 'none'
  $('appCard').style.display = isLoggedIn ? 'block' : 'none'
}

function setActiveTab(tab) {
  state.activeTab = tab
  const tabs = $('tabs').querySelectorAll('.tab')
  tabs.forEach((t) => {
    t.classList.toggle('active', t.dataset.tab === tab)
  })
  render()
}

async function apiFetch(url, options = {}) {
  const headers = Object.assign({ 'Content-Type': 'application/json' }, options.headers || {})
  if (state.token) headers.Authorization = `Bearer ${state.token}`

  const resp = await fetch(url, { ...options, headers })
  const json = await resp.json().catch(() => null)
  if (!json) throw new Error('服务返回非 JSON')
  if (json.code !== 0) throw new Error(json.message || '请求失败')
  return json.data
}

async function login(username, password) {
  const resp = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  })

  const json = await resp.json().catch(() => null)
  if (!json) throw new Error('服务返回非 JSON')
  if (json.code !== 0) throw new Error(json.message || '登录失败')

  const token = json.data?.token || json.token
  if (!token) throw new Error('登录返回中没有 token')

  state.token = token
  localStorage.setItem('token', token)

  state.user = json.data?.user || null
  return token
}

function logout() {
  state.token = ''
  state.user = null
  localStorage.removeItem('token')
  setStatusText('未登录')
  setLoggedInUI(false)
}

function renderTable(rows) {
  if (!rows || rows.length === 0) return '<div class="muted">暂无数据</div>'

  const keys = Object.keys(rows[0])
  const thead = `<tr>${keys.map((k) => `<th>${k}</th>`).join('')}</tr>`
  const tbody = rows
    .map((r) => `<tr>${keys.map((k) => `<td>${escapeHtml(String(r[k] ?? ''))}</td>`).join('')}</tr>`)
    .join('')

  return `<table><thead>${thead}</thead><tbody>${tbody}</tbody></table>`
}

function escapeHtml(s) {
  return s
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;')
}

async function render() {
  const view = $('view')
  view.innerHTML = '<div class="muted">加载中...</div>'

  try {
    if (state.activeTab === 'swagger') {
      view.innerHTML = `<div class="row"><a href="/swagger-ui/index.html" target="_blank">打开 Swagger（新窗口）</a></div>`
      return
    }

    if (state.activeTab === 'overview') {
      const data = await apiFetch('/api/reports/overview', { method: 'GET' })
      view.innerHTML = `<pre>${escapeHtml(JSON.stringify(data, null, 2))}</pre>`
      return
    }

    if (state.activeTab === 'students') {
      const data = await apiFetch('/api/students?page=1&size=20', { method: 'GET' })
      view.innerHTML = renderTable(data.records || [])
      return
    }

    if (state.activeTab === 'repairs') {
      const data = await apiFetch('/api/repairs?page=1&size=20', { method: 'GET' })
      view.innerHTML = renderTable(data.records || [])
      return
    }

    if (state.activeTab === 'rooms') {
      const data = await apiFetch('/api/rooms?page=1&size=20', { method: 'GET' })
      view.innerHTML = renderTable(data.records || [])
      return
    }

    view.innerHTML = '<div class="muted">未实现的页面</div>'
  } catch (e) {
    view.innerHTML = `<pre>${escapeHtml(String(e?.message || e))}</pre>`
  }
}

function initEvents() {
  $('loginBtn').addEventListener('click', async () => {
    setLoginMsg('')
    const username = $('username').value.trim()
    const password = $('password').value
    if (!username || !password) {
      setLoginMsg('请输入用户名和密码')
      return
    }

    try {
      const token = await login(username, password)
      setStatusText(`已登录（token: ${token.slice(0, 18)}...）`)
      setLoggedInUI(true)
      await render()
    } catch (e) {
      setLoginMsg(e?.message || String(e))
    }
  })

  $('logoutBtn').addEventListener('click', () => {
    logout()
  })

  $('tabs').addEventListener('click', (evt) => {
    const el = evt.target
    if (!el || !el.dataset || !el.dataset.tab) return
    setActiveTab(el.dataset.tab)
  })
}

async function bootstrap() {
  initEvents()

  if (state.token) {
    setStatusText(`已登录（token: ${state.token.slice(0, 18)}...）`)
    setLoggedInUI(true)
    await render()
  } else {
    setLoggedInUI(false)
  }
}

bootstrap()
