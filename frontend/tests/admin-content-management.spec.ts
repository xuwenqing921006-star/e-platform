import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

import { adminContentOfficeOptions } from '../src/config/adminContent'
import { dispatchMockRequest } from '../src/mocks'
import type {
  AdminAttachmentUploadData,
  AdminContentCreateData,
  AdminContentDetailData,
  AdminContentListData,
} from '../src/types/api'

const root = resolve(__dirname, '..')

function source(relativePath: string) {
  const absolutePath = resolve(root, relativePath)
  return existsSync(absolutePath) ? readFileSync(absolutePath, 'utf8') : ''
}

describe('admin content management mock', () => {
  it('returns a filtered admin list DTO without attachment count fields', () => {
    const response = dispatchMockRequest({
      method: 'GET',
      url: '/admin/contents',
      params: {
        category: 'SERVICE_GUIDE',
        keyword: '征信代理',
        office_code: 'CREDIT_REPORT',
        page: 1,
        page_size: 20,
        published_from: '2026-05-01',
        published_to: '2026-05-31',
      },
    })

    expect(response.status).toBe(200)
    expect(response.data.data).toEqual({
      items: [
        {
          id: 201,
          title: '[Mock] 征信-大庆市征信代理查询网点地址及电话',
          category: 'SERVICE_GUIDE',
          office_code: 'CREDIT_REPORT',
          office_name: '征信管理科',
          published_at: '2026-05-28T09:30:00+08:00',
        },
      ],
      total: 1,
      page: 1,
      page_size: 20,
    })
    const data = response.data.data as AdminContentListData
    expect(data.items[0]).not.toHaveProperty('attachments')
    expect(data.items[0]).not.toHaveProperty('attachment_count')
  })

  it('supports paginated content navigation from the list page', () => {
    const firstPage = dispatchMockRequest({
      method: 'GET',
      url: '/admin/contents',
      params: {
        page: 1,
        page_size: 1,
      },
    })
    const secondPage = dispatchMockRequest({
      method: 'GET',
      url: '/admin/contents',
      params: {
        page: 2,
        page_size: 1,
      },
    })
    const listSource = source('src/pages/admin/AdminContentListPage.vue')
    const firstPageData = firstPage.data.data as AdminContentListData
    const secondPageData = secondPage.data.data as AdminContentListData
    const firstItem = firstPageData.items[0]
    const secondItem = secondPageData.items[0]

    expect(firstPage.status).toBe(200)
    expect(secondPage.status).toBe(200)
    expect(firstPageData.page).toBe(1)
    expect(secondPageData.page).toBe(2)
    expect(firstItem).toBeDefined()
    expect(secondItem).toBeDefined()
    expect(firstItem?.id).not.toBe(secondItem?.id)
    expect(listSource).toContain('totalPages')
    expect(listSource).toContain('goToPage')
    expect(listSource).toContain('上一页')
    expect(listSource).toContain('下一页')
  })

  it('uploads and deletes an attachment with endpoint-specific DTOs', () => {
    const file = new File(['mock attachment'], '[Mock] 政策解读.pdf', {
      type: 'application/pdf',
    })
    const body = new FormData()
    body.append('file', file)

    const uploadResponse = dispatchMockRequest({
      method: 'POST',
      url: '/admin/attachments',
      data: body,
    })

    expect(uploadResponse.status).toBe(200)
    expect(uploadResponse.data.data).toMatchObject({
      file_name: '[Mock] 政策解读.pdf',
      file_type: 'PDF',
      file_size: file.size,
    })
    const attachment = uploadResponse.data.data as AdminAttachmentUploadData
    expect(attachment).toHaveProperty(
      'download_url',
      `/api/public/attachments/${attachment.id}/download`,
    )

    const deleteResponse = dispatchMockRequest({
      method: 'DELETE',
      url: `/admin/attachments/${attachment.id}`,
    })

    expect(deleteResponse).toEqual({
      status: 200,
      data: {
        code: 200,
        message: 'success',
        data: {
          deleted: true,
        },
      },
    })
  })

  it('rejects an attachment larger than 20MB', () => {
    const file = new File(
      [new Uint8Array(20 * 1024 * 1024 + 1)],
      '[Mock] 超限附件.pdf',
      {
        type: 'application/pdf',
      },
    )
    const body = new FormData()
    body.append('file', file)

    expect(
      dispatchMockRequest({
        method: 'POST',
        url: '/admin/attachments',
        data: body,
      }),
    ).toEqual({
      status: 413,
      data: {
        code: 413,
        message: '单个附件不能超过 20MB',
        data: null,
      },
    })
  })

  it('rejects an unsupported attachment file type', () => {
    const body = new FormData()
    body.append('file', new File(['mock'], '[Mock] 不支持的附件.txt'))

    expect(
      dispatchMockRequest({
        method: 'POST',
        url: '/admin/attachments',
        data: body,
      }),
    ).toEqual({
      status: 400,
      data: {
        code: 400,
        message: '仅支持 PDF、Word 或 Excel 文件',
        data: null,
      },
    })
  })

  it('rejects a fourth content attachment before creating content', () => {
    const response = dispatchMockRequest({
      method: 'POST',
      url: '/admin/contents',
      data: {
        attachment_ids: [9001, 9002, 9003, 9004],
        category: 'SERVICE_GUIDE',
        office_code: 'CREDIT_REPORT',
        rich_text_html: '<p>[Mock] 正文</p>',
        title: '[Mock] 超限内容',
      },
    })

    expect(response).toEqual({
      status: 400,
      data: {
        code: 400,
        message: '每篇内容最多上传 3 个附件',
        data: null,
      },
    })
  })

  it('creates, previews, updates and deletes content through explicit DTOs', () => {
    const createResponse = dispatchMockRequest({
      method: 'POST',
      url: '/admin/contents',
      data: {
        attachment_ids: [9001, 9002],
        category: 'SERVICE_GUIDE',
        office_code: 'CREDIT_REPORT',
        rich_text_html: '<h2>[Mock] 办理说明</h2>',
        title: '[Mock] 新增征信服务指引',
      },
    })

    expect(createResponse.status).toBe(200)
    expect(createResponse.data.data).toEqual({
      id: expect.any(Number),
      published_at: expect.any(String),
    })

    const id = (createResponse.data.data as AdminContentCreateData).id
    const detailResponse = dispatchMockRequest({
      method: 'GET',
      url: `/admin/contents/${id}`,
    })

    expect(detailResponse.data.data).toMatchObject({
      id,
      title: '[Mock] 新增征信服务指引',
      category: 'SERVICE_GUIDE',
      office_code: 'CREDIT_REPORT',
      office_name: '征信管理科',
      rich_text_html: '<h2>[Mock] 办理说明</h2>',
      attachments: [
        {
          id: 9001,
        },
        {
          id: 9002,
        },
      ],
    })

    const updateResponse = dispatchMockRequest({
      method: 'PUT',
      url: `/admin/contents/${id}`,
      data: {
        attachment_ids: [9001],
        category: 'POLICY_PROMOTION',
        office_code: 'POLICY_PUBLICITY',
        rich_text_html: '<p>[Mock] 更新后的正文</p>',
        title: '[Mock] 更新后的政策宣传',
      },
    })

    expect(updateResponse).toEqual({
      status: 200,
      data: {
        code: 200,
        message: 'success',
        data: {
          id,
          updated: true,
        },
      },
    })

    const deleteResponse = dispatchMockRequest({
      method: 'DELETE',
      url: `/admin/contents/${id}`,
    })

    expect(deleteResponse).toEqual({
      status: 200,
      data: {
        code: 200,
        message: 'success',
        data: {
          deleted: true,
        },
      },
    })
    expect(
      dispatchMockRequest({
        method: 'GET',
        url: `/admin/contents/${id}`,
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

  it('round-trips every contract office code with a readable detail office name', () => {
    expect(adminContentOfficeOptions.map((option) => option.value)).toContain(
      'CURRENCY_GOLD_SILVER',
    )
    expect(adminContentOfficeOptions.map((option) => option.value)).not.toContain(
      'CURRENCY_GOLD',
    )

    for (const option of adminContentOfficeOptions) {
      const createResponse = dispatchMockRequest({
        method: 'POST',
        url: '/admin/contents',
        data: {
          attachment_ids: [],
          category: 'SERVICE_GUIDE',
          office_code: option.value,
          rich_text_html: `<p>[Mock] ${option.label} 正文</p>`,
          title: `[Mock] ${option.label} 回显测试`,
        },
      })
      const id = (createResponse.data.data as AdminContentCreateData).id
      const detailResponse = dispatchMockRequest({
        method: 'GET',
        url: `/admin/contents/${id}`,
      })
      const detail = detailResponse.data.data as AdminContentDetailData

      expect(detail.office_code).toBe(option.value)
      expect(detail.office_name).toBe(option.label)
    }
  })

  it('exposes observable link image and table rich text toolbar actions', () => {
    const editorSource = source('src/pages/admin/AdminContentEditorPage.vue')

    expect(editorSource).toContain('插入链接')
    expect(editorSource).toContain('插入图片')
    expect(editorSource).toContain('插入表格')
    expect(editorSource).toContain('insertMockLink')
    expect(editorSource).toContain('insertMockImage')
    expect(editorSource).toContain('insertMockTable')
  })

  it('wires routed list, editor and preview pages through centralized services', () => {
    const routerSource = source('src/router/index.ts')
    const listSource = source('src/pages/admin/AdminContentListPage.vue')
    const editorSource = source('src/pages/admin/AdminContentEditorPage.vue')
    const detailSource = source('src/pages/admin/AdminContentDetailPage.vue')
    const contentServiceSource = source('src/services/adminContentService.ts')
    const attachmentServiceSource = source('src/services/attachmentService.ts')

    expect(routerSource).toContain("path: 'contents'")
    expect(routerSource).toContain("path: 'contents/new'")
    expect(routerSource).toContain("path: 'contents/:id/edit'")
    expect(routerSource).toContain("path: 'contents/:id'")
    expect(listSource).toContain('getAdminContents')
    expect(listSource).toContain('deleteAdminContent')
    expect(listSource).not.toContain('附件个数')
    expect(editorSource).toContain('contenteditable="true"')
    expect(editorSource).toContain('uploadAdminAttachment')
    expect(editorSource).toContain('最多 3 个')
    expect(detailSource).toContain('getAdminContentDetail')
    expect(contentServiceSource).toContain(
      "api.get<ApiResponse<AdminContentListData>>('/admin/contents'",
    )
    expect(attachmentServiceSource).toContain(
      "api.post<ApiResponse<AdminAttachmentUploadData>>('/admin/attachments'",
    )
  })
})
