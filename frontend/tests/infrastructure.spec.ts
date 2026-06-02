import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs'
import { resolve } from 'node:path'
import { pathToFileURL } from 'node:url'
import { afterEach, describe, expect, it } from 'vitest'

const root = resolve(__dirname, '..')

function source(relativePath: string) {
  const absolutePath = resolve(root, relativePath)
  return existsSync(absolutePath) ? readFileSync(absolutePath, 'utf8') : ''
}

function filesUnder(relativePath: string): string[] {
  const absolutePath = resolve(root, relativePath)

  return readdirSync(absolutePath).flatMap((name) => {
    const childPath = resolve(absolutePath, name)
    const childRelativePath = `${relativePath}/${name}`

    return statSync(childPath).isDirectory()
      ? filesUnder(childRelativePath)
      : [childRelativePath]
  })
}

afterEach(() => {
  localStorage.clear()
})

describe('frontend infrastructure', () => {
  it('declares the H5 and admin entry routes', () => {
    const routerSource = source('src/router/index.ts')

    expect(routerSource).toContain("path: '/h5/'")
    expect(routerSource).toContain("path: '/admin/login'")
    expect(routerSource).toContain("path: '/admin'")
  })

  it('redirects unauthenticated admin navigation to the admin login page', async () => {
    const authGuardPath = resolve(root, 'src/router/authGuard.ts')

    expect(existsSync(authGuardPath)).toBe(true)
    if (!existsSync(authGuardPath)) return

    const { createAdminAuthGuard } = await import(
      /* @vite-ignore */ pathToFileURL(authGuardPath).href
    )
    const guard = createAdminAuthGuard(() => null)

    expect(guard({ meta: { requiresAuth: true } })).toEqual({
      path: '/admin/login',
    })
    expect(guard({ meta: { requiresAuth: false } })).toBe(true)
  })

  it('returns explicit contract-aligned DTOs from the centralized mock layer', async () => {
    const mocksPath = resolve(root, 'src/mocks/index.ts')

    expect(existsSync(mocksPath)).toBe(true)
    if (!existsSync(mocksPath)) return

    const { dispatchMockRequest } = await import(
      /* @vite-ignore */ pathToFileURL(mocksPath).href
    )
    const response = dispatchMockRequest({
      method: 'GET',
      url: '/public/contents',
      params: {
        category: 'SERVICE_GUIDE',
        county_code: 'ZHAOZHOU',
        page: 1,
        page_size: 10,
        scope: 'RURAL',
      },
    })

    expect(response.status).toBe(200)
    expect(response.data).toMatchObject({
      code: 200,
      message: 'success',
      data: {
        page: 1,
        page_size: 10,
      },
    })
    expect(response.data.data?.items[0]).toEqual({
      id: 101,
      title: '[Mock] 肇州县金融服务便民联系指南',
      category: 'SERVICE_GUIDE',
      office_name: '肇州县金融服务队',
      published_at: '2026-05-29T09:30:00+08:00',
    })
  })

  it('uses a mock Axios adapter so the mock phase never contacts a real backend', () => {
    const apiSource = source('src/services/api.ts')

    expect(apiSource).toContain('mockAdapter')
    expect(apiSource).toContain("import.meta.env.VITE_USE_MOCK !== 'false'")
    expect(apiSource).not.toContain("baseURL: 'http")
  })

  it('defines shared responsive styles for mobile and desktop widths', () => {
    const styleSource = source('src/styles/global.css')

    expect(styleSource).toContain('--primary: #1556d1')
    expect(styleSource).toContain('@media (min-width: 1280px)')
    expect(styleSource).toContain('@media (min-width: 1440px)')
    expect(styleSource).toContain('@media (min-width: 1920px)')
  })

  it('keeps the admin shell and dashboard single-column on mobile widths', () => {
    const styleSource = source('src/styles/global.css')

    expect(styleSource).toMatch(
      /\.admin-shell\s*\{[^}]*grid-template-columns:\s*minmax\(0,\s*1fr\)/s,
    )
    expect(styleSource).toMatch(
      /\.dashboard-grid\s*\{[^}]*grid-template-columns:\s*minmax\(0,\s*1fr\)/s,
    )
    expect(styleSource).toMatch(
      /@media \(min-width:\s*768px\)\s*\{[\s\S]*?\.admin-shell\s*\{[^}]*grid-template-columns:\s*240px minmax\(0,\s*1fr\)/,
    )
    expect(styleSource).toMatch(
      /\.admin-sidebar\s*\{[^}]*padding:\s*18px 16px/s,
    )
    expect(styleSource).toMatch(
      /\.admin-header\s*\{[^}]*padding:\s*16px/s,
    )
    expect(styleSource).toMatch(
      /\.dashboard-page\s*\{[^}]*padding:\s*20px 16px/s,
    )
  })

  it('loads dashboard business data through the centralized mock-backed service', () => {
    const dashboardSource = source('src/pages/admin/AdminDashboardPage.vue')
    const serviceSource = source('src/services/dashboardService.ts')

    expect(dashboardSource).toContain("from '../../services/dashboardService'")
    expect(dashboardSource).toContain('getDashboardSummary')
    expect(dashboardSource).not.toContain('const metrics = [')
    expect(dashboardSource).not.toContain('[Mock] 中国人民银行公告')
    expect(serviceSource).toContain("api.get<ApiResponse<DashboardSummaryData>>")
    expect(serviceSource).toContain("'/admin/dashboard/summary'")
  })

  it('uses reusable SVG icons for the prototype-aligned foundation pages', () => {
    const iconSource = source('src/components/common/AppIcon.vue')
    const layoutSource = source('src/components/admin/AdminLayout.vue')
    const dashboardSource = source('src/pages/admin/AdminDashboardPage.vue')
    const loginSource = source('src/pages/admin/AdminLoginPage.vue')
    const h5Source = source('src/pages/h5/H5LandingPage.vue')

    expect(iconSource).toContain('<svg')
    expect(layoutSource).toContain('<AppIcon')
    expect(layoutSource).toContain('admin-user-avatar')
    expect(layoutSource).toContain('admin-user-details')
    expect(dashboardSource).toContain('<AppIcon')
    expect(dashboardSource).toContain('metric-card-content')
    expect(dashboardSource).toContain('recent-content-list')
    expect(dashboardSource).toContain('quick-entry')
    expect(loginSource).toContain('name="landmark"')
    expect(loginSource).not.toContain('<div class="login-icon">央</div>')
    expect(h5Source).toContain('name="building-2"')
    expect(h5Source).toContain('name="clock-3"')
    expect(h5Source).not.toContain('▥')
    expect(h5Source).not.toContain('◷')
  })

  it('keeps centralized Mock markers out of pages and components', () => {
    const visibleSourcePaths = [
      ...filesUnder('src/pages'),
      ...filesUnder('src/components'),
    ]

    expect(
      visibleSourcePaths.filter((relativePath) =>
        source(relativePath).includes('[Mock]'),
      ),
    ).toEqual([])
  })

  it('returns visible Mock markers from centralized fixtures during the Mock phase', async () => {
    const mocksPath = resolve(root, 'src/mocks/index.ts')
    const { dispatchMockRequest } = await import(
      /* @vite-ignore */ pathToFileURL(mocksPath).href
    )
    const contentResponse = dispatchMockRequest({
      method: 'GET',
      url: '/public/contents',
    })
    const loginResponse = dispatchMockRequest({
      method: 'POST',
      url: '/auth/login',
      data: {
        password: 'Admin123!',
        username: 'admin',
      },
    })

    expect(contentResponse.data.data?.items[0].title).toMatch(/^\[Mock\]/)
    expect(loginResponse.data.data?.user.display_name).toMatch(/^\[Mock\]/)
  })

  it('reads the admin header display name from the authentication store', () => {
    const layoutSource = source('src/components/admin/AdminLayout.vue')

    expect(layoutSource).toContain("from '../../stores/useAuthStore'")
    expect(layoutSource).toContain('authStore.displayName')
    expect(layoutSource).not.toContain('[Mock] 系统管理员')
  })
})
