import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { createPinia, setActivePinia } from 'pinia'
import { afterEach, describe, expect, it } from 'vitest'

import { dispatchMockRequest } from '../src/mocks'
import { ADMIN_TOKEN_STORAGE_KEY } from '../src/stores/authToken'
import { useAuthStore } from '../src/stores/useAuthStore'

const root = resolve(__dirname, '..')

function source(relativePath: string) {
  const absolutePath = resolve(root, relativePath)
  return existsSync(absolutePath) ? readFileSync(absolutePath, 'utf8') : ''
}

afterEach(() => {
  localStorage.clear()
  sessionStorage.clear()
})

describe('admin login and dashboard mock', () => {
  it('returns the contract-aligned login DTO for the documented mock account', () => {
    const response = dispatchMockRequest({
      method: 'POST',
      url: '/auth/login',
      data: {
        password: 'Admin123!',
        username: 'admin',
      },
    })

    expect(response).toEqual({
      status: 200,
      data: {
        code: 200,
        message: 'success',
        data: {
          access_token: 'mock-admin-token',
          token_type: 'Bearer',
          expires_in: 7200,
          user: {
            id: 1,
            username: 'admin',
            display_name: '[Mock] 系统管理员',
            role: 'ADMIN',
            office_code: null,
            office_name: null,
            permissions: [
              'CONTENT_MANAGE',
              'PRODUCT_MANAGE',
              'ACCOUNT_MANAGE',
              'AUDIT_LOG_VIEW',
            ],
          },
        },
      },
    })
  })

  it('returns the documented 401 response for invalid credentials', () => {
    const response = dispatchMockRequest({
      method: 'POST',
      url: '/auth/login',
      data: {
        password: 'wrong-password',
        username: 'admin',
      },
    })

    expect(response).toEqual({
      status: 401,
      data: {
        code: 401,
        message: '用户名或密码错误',
        data: null,
      },
    })
  })

  it('stores the mock token in localStorage when remember login is selected', async () => {
    setActivePinia(createPinia())
    const authStore = useAuthStore()

    await authStore.login({
      password: 'Admin123!',
      remember: true,
      username: 'admin',
    })

    expect(localStorage.getItem(ADMIN_TOKEN_STORAGE_KEY)).toBe('mock-admin-token')
    expect(sessionStorage.getItem(ADMIN_TOKEN_STORAGE_KEY)).toBeNull()
  })

  it('returns an explicit dashboard DTO with the prototype recent contents', () => {
    const response = dispatchMockRequest({
      method: 'GET',
      url: '/admin/dashboard/summary',
    })

    expect(response.status).toBe(200)
    expect(response.data.data).toEqual({
      published_content_count: 48,
      product_count: 112,
      account_count: 9,
      today_operation_count: 16,
      recent_contents: [
        {
          id: 301,
          title: '[Mock] 中国人民银行公告〔2025〕第12号',
          category: 'POLICY_PROMOTION',
          published_at: '2026-05-30T16:42:00+08:00',
        },
        {
          id: 101,
          title: '[Mock] 肇州县金融服务便民联系指南',
          category: 'SERVICE_GUIDE',
          published_at: '2026-05-29T09:30:00+08:00',
        },
        {
          id: 201,
          title: '[Mock] 征信代理查询网点地址及电话',
          category: 'SERVICE_GUIDE',
          published_at: '2026-05-28T09:30:00+08:00',
        },
      ],
    })
  })

  it('uses an auth service and exposes routed dashboard quick entries', () => {
    const authServiceSource = source('src/services/authService.ts')
    const authStoreSource = source('src/stores/useAuthStore.ts')
    const dashboardSource = source('src/pages/admin/AdminDashboardPage.vue')

    expect(authServiceSource).toContain("api.post<ApiResponse<LoginResponseData>>")
    expect(authServiceSource).toContain("'/auth/login'")
    expect(authStoreSource).toContain("from '../services/authService'")
    expect(dashboardSource).toContain("to: '/admin/contents/new'")
    expect(dashboardSource).toContain("to: '/admin/products/new'")
    expect(dashboardSource).toContain("to: '/admin/products/import'")
    expect(dashboardSource).toContain('<RouterLink')
  })

  it('wires the login page success redirect and visible error message', () => {
    const loginSource = source('src/pages/admin/AdminLoginPage.vue')

    expect(loginSource).toContain("await router.push('/admin/dashboard')")
    expect(loginSource).toContain('v-if="errorMessage"')
    expect(loginSource).toContain('{{ errorMessage }}')
  })
})
