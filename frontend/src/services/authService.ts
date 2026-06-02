import api from './api'
import type { ApiResponse, LoginRequest, LoginResponseData } from '../types/api'

export async function loginAdmin(payload: LoginRequest) {
  const response = await api.post<ApiResponse<LoginResponseData>>(
    '/auth/login',
    payload,
  )

  if (!response.data.data) {
    throw new Error(response.data.message)
  }

  return response.data.data
}
