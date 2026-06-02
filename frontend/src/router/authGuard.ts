import type { RouteLocationNormalized } from 'vue-router'

import { getAdminToken } from '../stores/authToken'

export const ADMIN_LOGIN_PATH = '/admin/login'

export function createAdminAuthGuard(readToken = getAdminToken) {
  return (to: Pick<RouteLocationNormalized, 'meta'>) => {
    if (to.meta.requiresAuth && !readToken()) {
      return {
        path: ADMIN_LOGIN_PATH,
      }
    }
    return true
  }
}
