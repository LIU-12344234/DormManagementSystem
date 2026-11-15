import { defineStore } from 'pinia'

const KEY = 'dorm_auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    username: '',
    displayName: '',
    roles: []
  }),
  actions: {
    load() {
      const raw = localStorage.getItem(KEY)
      if (!raw) return
      try {
        const obj = JSON.parse(raw)
        this.token = obj.token || ''
        this.username = obj.username || ''
        this.displayName = obj.displayName || ''
        this.roles = Array.isArray(obj.roles) ? obj.roles : []
      } catch {
        localStorage.removeItem(KEY)
      }
    },
    save() {
      localStorage.setItem(
        KEY,
        JSON.stringify({
          token: this.token,
          username: this.username,
          displayName: this.displayName,
          roles: this.roles
        })
      )
    },
    setAuth(payload) {
      this.token = payload.token
      this.username = payload.username
      this.displayName = payload.displayName
      this.roles = payload.roles || []
      this.save()
    },
    logout() {
      this.token = ''
      this.username = ''
      this.displayName = ''
      this.roles = []
      localStorage.removeItem(KEY)
    }
  }
})
