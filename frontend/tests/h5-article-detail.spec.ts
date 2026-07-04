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
  window.history.replaceState({}, '', path)
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
    expect(listSource).toContain(':to="contentDetailRoute(item.id)"')
    expect(listSource).toContain('buildH5DetailBackQuery')
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
      '/h5/api/public/attachments/9001/download',
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
    const topbarRule = styles.match(/\.article-topbar span\s*\{[^}]*\}/s)?.[0] || ''
    const titleRule = styles.match(/\.article-detail-card > h1\s*\{[^}]*\}/s)?.[0] || ''
    const richTextRule = styles.match(/\.article-rich-text\s*\{[^}]*\}/s)?.[0] || ''
    const richTextHeadingRule =
      styles.match(/\.article-rich-text h2\s*\{[^}]*\}/s)?.[0] || ''
    const attachmentHeadingRule =
      styles.match(/\.article-attachments h2\s*\{[^}]*\}/s)?.[0] || ''

    expect(topbarRule).toContain('font-size: 17px;')
    expect(titleRule).toContain('font-size: 19px;')
    expect(richTextRule).toContain('font-size: 15px;')
    expect(richTextHeadingRule).toContain('font-size: 16px;')
    expect(attachmentHeadingRule).toContain('font-size: 16px;')
    expect(styles).not.toContain('font-size: 28px;\n  line-height: 1.28;')
    expect(styles).not.toContain('font-size: 20px;\n  line-height: 1.58;')
  })

  it('uses a compact mobile list style for article attachments', () => {
    const styles = source('src/styles/global.css')
    const attachmentLinkRule =
      styles.match(/\.article-attachment-link\s*\{[^}]*\}/s)?.[0] || ''
    const attachmentNameRule =
      styles.match(/\.article-attachment-link span\s*\{[^}]*\}/s)?.[0] || ''
    const attachmentTypeRule =
      styles.match(/\.article-attachment-link small\s*\{[^}]*\}/s)?.[0] || ''
    const attachmentIconRule =
      styles.match(/\.article-attachment-link svg\s*\{[^}]*\}/s)?.[0] || ''

    expect(attachmentLinkRule).toContain('min-height: 48px;')
    expect(attachmentLinkRule).toContain('grid-template-columns: 18px minmax(0, 1fr) auto;')
    expect(attachmentLinkRule).toContain('padding: 11px 12px;')
    expect(attachmentLinkRule).toContain('font-size: 14px;')
    expect(attachmentLinkRule).toContain('font-weight: 500;')
    expect(attachmentNameRule).toContain('line-height: 1.42;')
    expect(attachmentTypeRule).toContain('border-radius: 999px;')
    expect(attachmentTypeRule).toContain('font-size: 11px;')
    expect(attachmentIconRule).toContain('width: 18px;')
    expect(attachmentIconRule).toContain('height: 18px;')
  })

  it('does not shrink long rich-text images into a fixed-height preview', () => {
    const styles = source('src/styles/global.css')
    const richTextImageRule = styles.match(/\.article-rich-text img\s*\{[^}]*\}/s)?.[0] || ''
    const viewerImageRule = styles.match(/\.article-image-viewer img\s*\{[^}]*\}/s)?.[0] || ''

    expect(richTextImageRule).toContain('width: 100%;')
    expect(richTextImageRule).toContain('height: auto;')
    expect(richTextImageRule).not.toContain('max-height:')
    expect(richTextImageRule).not.toContain('object-fit:')
    expect(styles).toMatch(/\.article-image-viewer\s*\{[^}]*overflow:\s*auto;/s)
    expect(viewerImageRule).toContain('height: auto;')
    expect(viewerImageRule).not.toContain('max-height:')
  })

  it('keeps the original-image close button visible on light images', () => {
    const styles = source('src/styles/global.css')
    const closeButtonRule =
      styles.match(/\.article-image-viewer-close\s*\{[^}]*\}/s)?.[0] || ''
    const closeIconRule =
      styles.match(/\.article-image-viewer-close svg\s*\{[^}]*\}/s)?.[0] || ''

    expect(closeButtonRule).toContain('color: white;')
    expect(closeButtonRule).toContain('background: rgb(9 19 38 / 88%)')
    expect(closeButtonRule).toContain('border: 1px solid rgb(255 255 255 / 72%) !important;')
    expect(closeButtonRule).toContain('z-index: 2;')
    expect(closeButtonRule).toContain('box-shadow:')
    expect(closeIconRule).toContain('width: 24px;')
    expect(closeIconRule).toContain('height: 24px;')
  })

  it('keeps pinch zoom scoped to the original-image viewer instead of the page', () => {
    const detailSource = source('src/pages/h5/H5ArticleDetailPage.vue')
    const styles = source('src/styles/global.css')
    const viewerRule = styles.match(/\.article-image-viewer\s*\{[^}]*\}/s)?.[0] || ''
    const viewerImageRule = styles.match(/\.article-image-viewer img\s*\{[^}]*\}/s)?.[0] || ''

    expect(detailSource).toContain('imageViewerStyle')
    expect(detailSource).toContain('@touchstart="startImageGesture"')
    expect(detailSource).toContain('@touchmove="moveImageGesture"')
    expect(detailSource).toContain('@touchend="endImageGesture"')
    expect(detailSource).toContain('clampImageScale')
    expect(viewerRule).toContain('touch-action: none;')
    expect(viewerImageRule).toContain('transform:')
    expect(viewerImageRule).toContain('transform-origin: center center;')
    expect(viewerImageRule).toContain('user-select: none;')
  })
})
