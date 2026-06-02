export const SAFE_PORTAL_URL = 'http://zwfw.safe.gov.cn/asone/'
export const SAFE_LOGO_PATH = '/safe-logo.png'

export function shouldShowSafeEntry(path: string) {
  const isDetailPath = /^\/h5\/(?:contents|products)\//.test(path)

  return (
    !isDetailPath &&
    (path === '/h5/' ||
      path.startsWith('/h5/rural/') ||
      path.startsWith('/h5/financial/'))
  )
}
