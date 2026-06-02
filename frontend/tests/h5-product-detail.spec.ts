import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { pathToFileURL } from 'node:url'
import { createApp, nextTick } from 'vue'
import { createMemoryHistory } from 'vue-router'
import { afterEach, describe, expect, it } from 'vitest'

import App from '../src/App.vue'
import { dispatchMockRequest } from '../src/mocks'
import { createAppRouter } from '../src/router'

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

async function mountAppAt(path: string) {
  const host = document.createElement('div')
  document.body.append(host)
  const router = createAppRouter(createMemoryHistory())
  const app = createApp(App)
  app.use(router)
  router.push(path)
  await router.isReady()
  app.mount(host)
  await flushUi()

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
})

describe('T-004 public product detail mock contract', () => {
  it('maps product detail entities to the GET /api/public/products/{id} DTO subset', () => {
    const response = dispatchMockRequest({
      method: 'GET',
      url: '/public/products/2001',
    })
    const data = response.data.data as Record<string, unknown> | null

    expect(response.status).toBe(200)
    expect(data).toEqual({
      id: 2001,
      bank_name: '[Mock] 中国农业银行',
      product_name: '[Mock] 惠农e贷',
      product_type: 'AGRICULTURAL',
      admission_conditions: '[Mock] 面向符合条件的涉农经营主体。',
      product_intro: '[Mock] 用于满足农业生产经营流动资金需求。',
      business_manager: '[Mock] 张经理',
      contact_info: '[Mock] 0459-0002001',
    })
    expect(Object.keys(data || {})).toEqual([
      'id',
      'bank_name',
      'product_name',
      'product_type',
      'admission_conditions',
      'product_intro',
      'business_manager',
      'contact_info',
    ])
    expect(Object.keys(data || {})).not.toContain('reference_rate')
    expect(Object.keys(data || {})).not.toContain('loan_amount')
    expect(Object.keys(data || {})).not.toContain('loan_term')
  })

  it('returns the contract 404 response for unknown public product details', () => {
    expect(
      dispatchMockRequest({
        method: 'GET',
        url: '/public/products/999999',
      }),
    ).toEqual({
      status: 404,
      data: {
        code: 404,
        message: '产品不存在',
        data: null,
      },
    })
  })
})

describe('T-004 H5 product detail wiring', () => {
  it('registers the detail route and loads detail data through publicProductService', () => {
    const routerSource = source('src/router/index.ts')
    const listSource = source('src/pages/h5/H5LandingPage.vue')
    const detailSource = source('src/pages/h5/H5ProductDetailPage.vue')
    const serviceSource = source('src/services/publicProductService.ts')

    expect(routerSource).toContain("path: '/h5/products/:id'")
    expect(listSource).toContain(':to="`/h5/products/${item.id}`"')
    expect(detailSource).toContain("from '../../services/publicProductService'")
    expect(detailSource).not.toContain("from '../../mocks'")
    expect(serviceSource).toContain('getPublicProductDetail')
    expect(serviceSource).toContain(
      'api.get<ApiResponse<PublicProductDetailData>>',
    )
    expect(serviceSource).toContain('`/public/products/${id}`')
  })

  it('renders the identity card and only the allowed business detail fields', async () => {
    const detailPath = resolve(root, 'src/pages/h5/H5ProductDetailPage.vue')

    expect(existsSync(detailPath)).toBe(true)

    await import(/* @vite-ignore */ pathToFileURL(detailPath).href)
    const { host, unmount } = await mountAppAt('/h5/products/2001')

    expect(host.textContent).toContain('助企通道')
    expect(host.textContent).toContain('[Mock] 惠农e贷')
    expect(host.textContent).toContain('[Mock] 中国农业银行')
    expect(host.textContent).toContain('涉农信贷')
    expect(host.textContent).toContain('准入条件')
    expect(host.textContent).toContain('产品介绍')
    expect(host.textContent).toContain('业务经办人')
    expect(host.textContent).toContain('联系方式')
    expect(host.textContent).toContain('[Mock] 0459-0002001')
    expect(host.querySelector('.product-identity-card')).not.toBeNull()
    expect(host.querySelector('.product-detail-card')).not.toBeNull()
    expect(host.querySelectorAll('.product-detail-field')).toHaveLength(4)
    expect(host.textContent).not.toContain('参考利率')
    expect(host.textContent).not.toContain('贷款额度')
    expect(host.textContent).not.toContain('期限')
    expect(host.querySelector('.safe-entry')).toBeNull()

    unmount()
  })
})
