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

describe('T-003 public article detail mock contract', () => {
  it('maps article detail entities to the GET /api/public/contents/{id} DTO subset', () => {
    const response = dispatchMockRequest({
      method: 'GET',
      url: '/public/contents/201',
    })
    const data = response.data.data as Record<string, unknown> | null

    expect(response.status).toBe(200)
    expect(data).toEqual({
      id: 201,
      title: '[Mock] 征信-大庆市县域征信服务指引',
      category: 'SERVICE_GUIDE',
      office_name: '征信管理科',
      published_at: '2026-05-28T09:30:00+08:00',
      rich_text_html: expect.stringContaining('办理说明'),
      attachments: [
        {
          id: 9001,
          file_name: '[Mock] 县域征信服务网点信息表.xlsx',
          file_type: 'EXCEL',
          file_size: 20480,
          download_url: '/api/public/attachments/9001/download',
        },
        {
          id: 9002,
          file_name: '[Mock] 个人信用报告查询指引.pdf',
          file_type: 'PDF',
          file_size: 51200,
          download_url: '/api/public/attachments/9002/download',
        },
        {
          id: 9003,
          file_name: '[Mock] 征信业务申请材料清单.docx',
          file_type: 'WORD',
          file_size: 36864,
          download_url: '/api/public/attachments/9003/download',
        },
      ],
    })
    expect(Object.keys(data || {})).toEqual([
      'id',
      'title',
      'category',
      'office_name',
      'published_at',
      'rich_text_html',
      'attachments',
    ])
    expect((data?.attachments as unknown[]) || []).toHaveLength(3)
  })

  it('returns the contract 404 response for unknown public content details', () => {
    expect(
      dispatchMockRequest({
        method: 'GET',
        url: '/public/contents/999999',
      }),
    ).toEqual({
      status: 404,
      data: {
        code: 404,
        message: '内容不存在',
        data: null,
      },
    })
  })
})

describe('T-003 H5 article detail wiring', () => {
  it('registers the detail route and loads detail data through publicContentService', () => {
    const routerSource = source('src/router/index.ts')
    const listSource = source('src/pages/h5/H5LandingPage.vue')
    const detailSource = source('src/pages/h5/H5ArticleDetailPage.vue')
    const serviceSource = source('src/services/publicContentService.ts')

    expect(routerSource).toContain("path: '/h5/contents/:id'")
    expect(listSource).toContain(':to="`/h5/contents/${item.id}`"')
    expect(detailSource).toContain("from '../../services/publicContentService'")
    expect(detailSource).not.toContain("from '../../mocks'")
    expect(serviceSource).toContain('getPublicContentDetail')
    expect(serviceSource).toContain(
      "api.get<ApiResponse<PublicContentDetailData>>",
    )
    expect(serviceSource).toContain("`/public/contents/${id}`")
  })

  it('renders title, publish metadata, rich text and at most three download links', async () => {
    const detailPath = resolve(root, 'src/pages/h5/H5ArticleDetailPage.vue')

    expect(existsSync(detailPath)).toBe(true)
    if (!existsSync(detailPath)) return

    await import(/* @vite-ignore */ pathToFileURL(detailPath).href)
    const { host, unmount } = await mountAppAt('/h5/contents/201')

    expect(host.textContent).toContain('服务指引')
    expect(host.textContent).toContain('[Mock] 征信-大庆市县域征信服务指引')
    expect(host.textContent).toContain('征信管理科')
    expect(host.textContent).toContain('2026-05-28')
    expect(host.textContent).toContain('办理说明')
    expect(host.querySelectorAll('.article-attachment-link')).toHaveLength(3)
    expect(host.querySelectorAll('.article-attachment-link')[0]?.getAttribute('href')).toBe(
      '/api/public/attachments/9001/download',
    )
    expect(host.textContent).not.toContain('H5 内预览')
    expect(host.querySelector('.safe-entry')).toBeNull()

    unmount()
  })

  it('opens and closes an original-image viewer from rich text images', async () => {
    const detailPath = resolve(root, 'src/pages/h5/H5ArticleDetailPage.vue')

    expect(existsSync(detailPath)).toBe(true)
    if (!existsSync(detailPath)) return

    const { host, unmount } = await mountAppAt('/h5/contents/201')
    const image = host.querySelector<HTMLImageElement>('.article-rich-text img')

    expect(image?.src).toContain('/article-credit-service-long.svg')
    image?.click()
    await nextTick()

    expect(host.querySelector('.article-image-viewer')).not.toBeNull()
    expect(
      host.querySelector<HTMLImageElement>('.article-image-viewer img')?.src,
    ).toContain('/article-credit-service-long.svg')

    host.querySelector<HTMLButtonElement>('.article-image-viewer-close')?.click()
    await nextTick()

    expect(host.querySelector('.article-image-viewer')).toBeNull()

    unmount()
  })

  it('keeps the mobile article detail typography compact', () => {
    const styles = source('src/styles/global.css')

    expect(styles).toContain('.article-detail-card > h1')
    expect(styles).toContain('font-size: 21px;')
    expect(styles).toContain('.article-rich-text')
    expect(styles).toContain('font-size: 16px;')
    expect(styles).toContain('.article-rich-text h2')
    expect(styles).toContain('font-size: 18px;')
    expect(styles).not.toContain('font-size: 28px;\n  line-height: 1.28;')
    expect(styles).not.toContain('font-size: 20px;\n  line-height: 1.58;')
  })
})
