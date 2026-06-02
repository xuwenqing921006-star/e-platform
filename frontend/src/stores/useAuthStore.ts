import { defineStore } from 'pinia'

import api from '../services/api'
import type { ApiResponse, AuthUser, LoginResponseData } from '../types/api'
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
      const response = await api.post<ApiResponse<LoginResponseData>>(
        '/auth/login',
        {
          username: payload.username,
          password: payload.password,
        },
      )
      const result = response.data.data
      if (!result) {
        throw new Error(response.data.message)
      }

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
