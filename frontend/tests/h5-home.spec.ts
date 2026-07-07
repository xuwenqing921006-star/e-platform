import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { pathToFileURL } from 'node:url'
import { createApp, nextTick, type Component } from 'vue'
import { afterEach, describe, expect, it, vi } from 'vitest'

import H5SafeEntry from '../src/components/h5/H5SafeEntry.vue'
import { dispatchMockRequest } from '../src/mocks'
import H5LandingPage from '../src/pages/h5/H5LandingPage.vue'
import type {
  PublicContentListData,
  PublicProductListData,
} from '../src/types/api'

const root = resolve(__dirname, '..')

function source(relativePath: string) {
  const absolutePath = resolve(root, relativePath)
  return existsSync(absolutePath) ? readFileSync(absolutePath, 'utf8') : ''
}

async function flushUi() {
  await Promise.resolve()
  await Promise.resolve()
  await new Promise((resolve) => setTimeout(resolve, 0))
  await nextTick()
}

function mount(component: Component) {
  const host = document.createElement('div')
  document.body.append(host)
  const app = createApp(component)
  app.mount(host)

  return {
    host,
    unmount() {
      app.unmount()
      host.remove()
    },
  }
}

afterEach(() => {
  document.body.innerHTML = ''
  vi.restoreAllMocks()
})

describe('T-002 H5 home mock endpoints', () => {
  it('maps financial content list entities to the contract DTO subset', () => {
    const response = dispatchMockRequest({
      method: 'GET',
      url: '/public/contents',
      params: {
        category: 'SERVICE_GUIDE',
        page: 1,
        page_size: 10,
        scope: 'FINANCIAL',
      },
    })
    const data = response.data.data as PublicContentListData | null

    expect(response.status).toBe(200)
    expect(data?.items[0]).toEqual({
      id: 201,
      title: '[Mock] 征信-大庆市征信代理查询网点地址及电话',
      category: 'SERVICE_GUIDE',
      office_name: '征信管理科',
      published_at: '2026-05-28T09:30:00+08:00',
    })
  })

  it('slices content list pages for load-more requests', () => {
    const response = dispatchMockRequest({
      method: 'GET',
      url: '/public/contents',
      params: {
        category: 'SERVICE_GUIDE',
        county_code: 'ZHAOZHOU',
        page: 2,
        page_size: 2,
        scope: 'RURAL',
      },
    })
    const data = response.data.data as PublicContentListData | null

    expect(response.status).toBe(200)
    expect(data).toMatchObject({
      page: 2,
      page_size: 2,
      total: 3,
    })
    expect(data?.items).toHaveLength(1)
    expect(data?.items[0]?.id).toBe(103)
  })

  it('returns an explicit error when rural content requests omit county code', () => {
    const response = dispatchMockRequest({
      method: 'GET',
      url: '/public/contents',
      params: {
        category: 'SERVICE_GUIDE',
        page: 1,
        page_size: 10,
        scope: 'RURAL',
      },
    })

    expect(response).toEqual({
      status: 400,
      data: {
        code: 400,
        message: '乡村振兴服务指引必须指定县域',
        data: null,
      },
    })
  })

  it('maps product list entities to the public list DTO subset', () => {
    const response = dispatchMockRequest({
      method: 'GET',
      url: '/public/products',
      params: {
        page: 1,
        page_size: 4,
      },
    })
    const data = response.data.data as PublicProductListData | null

    expect(response.status).toBe(200)
    expect(data).toMatchObject({
      page: 1,
      page_size: 4,
      total: 112,
    })
    expect(data?.items[0]).toEqual({
      id: 2001,
      bank_name: '[Mock] 中国农业银行',
      product_name: '[Mock] 惠农e贷',
      product_type: 'AGRICULTURAL',
    })
    expect(Object.keys(data?.items[0] || {})).toEqual([
      'id',
      'bank_name',
      'product_name',
      'product_type',
    ])
  })
})

describe('T-002 H5 home composition', () => {
  it('keeps fixed tabs and all four county service teams in centralized config', () => {
    const configSource = source('src/config/h5Home.ts')

    expect(configSource).toContain('RURAL_TABS')
    expect(configSource).toContain('FINANCIAL_TABS')
    expect(configSource).toContain('COUNTY_SERVICE_TEAMS')
    expect(configSource).toContain('肇州县金融业务服务队')
    expect(configSource).toContain('肇源县金融业务服务队')
    expect(configSource).toContain('林甸县金融业务服务队')
    expect(configSource).toContain('杜蒙县金融业务服务队')
    expect(configSource).toContain('members: []')
    expect(configSource).not.toContain('[Mock]')
  })

  it('loads list data through dedicated services and exposes loading states', () => {
    const pageSource = source('src/pages/h5/H5LandingPage.vue')
    const contentServiceSource = source('src/services/publicContentService.ts')
    const productServiceSource = source('src/services/publicProductService.ts')

    expect(pageSource).toContain("from '../../services/publicContentService'")
    expect(pageSource).toContain("from '../../services/publicProductService'")
    expect(pageSource).not.toContain("from '../../services/api'")
    expect(pageSource).not.toContain('加载更多')
    expect(pageSource).not.toContain('h5-load-more')
    expect(source('.env')).toContain('VITE_USE_MOCK=false')
    expect(source('.env.test')).toContain('VITE_USE_MOCK=true')
    expect(source('src/services/api.ts')).toContain(
      "import.meta.env.VITE_USE_MOCK === 'true'",
    )
    expect(pageSource).not.toContain('showMockNotice')
    expect(pageSource).not.toContain('h5-mock-notice')
    expect(pageSource).toContain('暂无相关内容')
    expect(pageSource).toContain('重新加载')
    expect(contentServiceSource).toContain("api.get<ApiResponse<PublicContentListData>>")
    expect(contentServiceSource).toContain("'/public/contents'")
    expect(productServiceSource).toContain("api.get<ApiResponse<PublicProductListData>>")
    expect(productServiceSource).toContain("'/public/products'")
  })

  it('switches home columns, auto-loads on scroll and keeps load-more controls hidden', async () => {
    const { host, unmount } = mount(H5LandingPage)
    await flushUi()

    expect(host.querySelector('.h5-primary-tabs .active')?.textContent).toContain(
      '乡村振兴',
    )

    const primaryTabs = host.querySelectorAll<HTMLButtonElement>(
      '.h5-primary-tabs button',
    )
    primaryTabs[1]?.click()
    await flushUi()

    const financialTabs = host.querySelectorAll<HTMLButtonElement>(
      '.h5-financial-tabs button',
    )
    financialTabs[2]?.click()
    await flushUi()

    expect(host.querySelectorAll('.h5-product-card')).toHaveLength(4)
    expect(host.textContent).toContain('共 112 项助企金融产品')
    expect(host.querySelector('.h5-load-more')).toBeNull()
    expect(host.textContent).not.toContain('加载更多')

    window.dispatchEvent(new Event('scroll'))
    await flushUi()

    expect(host.querySelectorAll('.h5-product-card')).toHaveLength(8)
    expect(host.querySelector('.h5-load-more')).toBeNull()

    primaryTabs[0]?.click()
    await flushUi()
    const countyTabs = host.querySelectorAll<HTMLButtonElement>(
      '.h5-county-tabs button',
    )
    countyTabs[3]?.click()
    await flushUi()

    expect(host.textContent).toContain('暂无相关内容')

    const ruralSections = host.querySelectorAll<HTMLButtonElement>(
      '.h5-section-tabs button',
    )
    ruralSections[1]?.click()
    await flushUi()

    expect(host.textContent).toContain('杜蒙县金融业务服务队')
    expect(host.textContent).toContain('暂无资料')
    expect(host.textContent).not.toContain('0459-0004001')

    unmount()
  })
})

describe('T-002 SAFE floating entry', () => {
  it('uses the fixed SAFE URL and hides the entry on detail paths', async () => {
    const configPath = resolve(root, 'src/config/safePortal.ts')

    expect(existsSync(configPath)).toBe(true)
    if (!existsSync(configPath)) return

    const { SAFE_LOGO_PATH, SAFE_PORTAL_URL, shouldShowSafeEntry } = await import(
      /* @vite-ignore */ pathToFileURL(configPath).href
    )

    expect(SAFE_PORTAL_URL).toBe('http://zwfw.safe.gov.cn/asone/')
    expect(SAFE_LOGO_PATH).toBe('/h5/safe-logo.png')
    expect(shouldShowSafeEntry('/h5/')).toBe(true)
    expect(shouldShowSafeEntry('/h5/rural/zhaozhou')).toBe(true)
    expect(shouldShowSafeEntry('/h5/financial/service-guide')).toBe(true)
    expect(shouldShowSafeEntry('/h5/contents/101')).toBe(false)
    expect(shouldShowSafeEntry('/h5/products/2001')).toBe(false)
    expect(shouldShowSafeEntry('/admin')).toBe(false)
  })

  it('provides expand, close and a native current-page link in a reusable component', () => {
    const appSource = source('src/App.vue')
    const safeSource = source('src/components/h5/H5SafeEntry.vue')
    const styles = source('src/styles/global.css')
    const safeLogoRule = styles.match(/\.safe-entry img\s*\{[^}]*\}/s)?.[0] || ''

    expect(appSource).toContain('<H5SafeEntry')
    expect(appSource).toContain('shouldShowSafeEntry(route.path)')
    expect(safeSource).toContain('safe-entry-expanded')
    expect(safeSource).toContain('safe-entry-close')
    expect(safeSource).toContain(':href="SAFE_PORTAL_URL"')
    expect(safeSource).not.toContain('window.open')
    expect(safeSource).not.toContain('@click="openPortal"')
    expect(safeSource).not.toContain('数字外管平台打开失败，请稍后重试。')
    expect(safeLogoRule).toContain('height: 33px;')
  })

  it('expands left, exposes the SAFE URL and collapses from the close action', async () => {
    const openWindow = vi.spyOn(window, 'open').mockReturnValue(null)
    const { host, unmount } = mount(H5SafeEntry)

    host.querySelector<HTMLButtonElement>('.safe-entry-logo')?.click()
    await nextTick()

    expect(host.querySelector('.safe-entry-expanded')).not.toBeNull()
    const safeLink = host.querySelector<HTMLAnchorElement>('.safe-entry-link')
    expect(safeLink?.href).toBe('http://zwfw.safe.gov.cn/asone/')
    expect(safeLink?.getAttribute('target')).toBeNull()
    expect(openWindow).not.toHaveBeenCalled()

    host.querySelector<HTMLButtonElement>('.safe-entry-close')?.click()
    await nextTick()

    expect(host.querySelector('.safe-entry-expanded')).toBeNull()

    unmount()
  })
})
