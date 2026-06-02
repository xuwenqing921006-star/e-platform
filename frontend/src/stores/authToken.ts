export const ADMIN_TOKEN_STORAGE_KEY = 'central_bank_admin_access_token'

export function getAdminToken() {
  return (
    localStorage.getItem(ADMIN_TOKEN_STORAGE_KEY) ||
    sessionStorage.getItem(ADMIN_TOKEN_STORAGE_KEY)
  )
}

export function saveAdminToken(token: string, remember: boolean) {
  clearAdminToken()
  const storage = remember ? localStorage : sessionStorage
  storage.setItem(ADMIN_TOKEN_STORAGE_KEY, token)
}

export function clearAdminToken() {
  localStorage.removeItem(ADMIN_TOKEN_STORAGE_KEY)
  sessionStorage.removeItem(ADMIN_TOKEN_STORAGE_KEY)
}
