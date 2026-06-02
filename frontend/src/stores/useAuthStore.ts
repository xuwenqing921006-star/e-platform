import { defineStore } from 'pinia'

import { loginAdmin } from '../services/authService'
import type { AuthUser } from '../types/api'
import { clearAdminToken, getAdminToken, saveAdminToken } from './authToken'

interface LoginPayload {
  username: string
  password: string
  remember: boolean
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getAdminToken(),
    user: null as AuthUser | null,
  }),
  getters: {
    displayName: (state) => state.user?.display_name || '系统管理员',
  },
  actions: {
    async login(payload: LoginPayload) {
      const result = await loginAdmin({
        username: payload.username,
        password: payload.password,
      })

      saveAdminToken(result.access_token, payload.remember)
      this.token = result.access_token
      this.user = result.user
    },
    logout() {
      clearAdminToken()
      this.token = null
      this.user = null
    },
  },
})
