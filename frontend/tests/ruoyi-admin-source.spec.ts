import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const root = resolve(__dirname, '..', '..')

function source(relativePath: string) {
  const absolutePath = resolve(root, relativePath)
  return existsSync(absolutePath) ? readFileSync(absolutePath, 'utf8') : ''
}

describe('ruoyi admin deployment source contracts', () => {
  it('returns to the deployed admin login path after logout or session expiry', () => {
    const navbarSource = source('ruoyi-ui/src/layout/components/Navbar.vue')
    const requestSource = source('ruoyi-ui/src/utils/request.js')

    expect(navbarSource).toContain("location.href = '/admin/login'")
    expect(requestSource).toContain("location.href = '/admin/login'")
    expect(navbarSource).not.toContain("location.href = '/index'")
    expect(requestSource).not.toContain("location.href = '/index'")
  })

  it('downloads content attachments through the admin API base path', () => {
    const contentSource = source('ruoyi-ui/src/views/centralbank/content/index.vue')

    expect(contentSource).toContain('resolveAttachmentDownloadUrl')
    expect(contentSource).toContain('process.env.VUE_APP_BASE_API')
    expect(contentSource).toContain(':href="resolveAttachmentDownloadUrl(file.download_url)"')
  })

  it('keeps account form limits aligned with sys_user column lengths', () => {
    const formSource = source('ruoyi-ui/src/views/centralbank/account/form.vue')

    expect(formSource).toContain('maxlength="30" placeholder="请输入登录账号"')
    expect(formSource).toContain('maxlength="30" placeholder="请输入姓名"')
    expect(formSource).not.toContain('maxlength="40" placeholder="请输入登录账号"')
    expect(formSource).not.toContain('maxlength="40" placeholder="请输入姓名"')
  })
})
