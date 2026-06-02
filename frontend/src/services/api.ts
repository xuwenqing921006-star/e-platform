import axios, { type AxiosAdapter, type AxiosResponse } from 'axios'

import { dispatchMockRequest } from '../mocks'
import { clearAdminToken, getAdminToken } from '../stores/authToken'

const mockAdapter: AxiosAdapter = async (config) => {
  const mockResponse = dispatchMockRequest({
    method: config.method,
    url: config.url,
    params: config.params as Record<string, unknown>,
    data: config.data,
  })

  return {
    config,
    data: mockResponse.data,
    headers: {},
    status: mockResponse.status,
    statusText: String(mockResponse.status),
  } satisfies AxiosResponse
}

const useMock = import.meta.env.VITE_USE_MOCK !== 'false'

const api = axios.create({
  adapter: useMock ? mockAdapter : undefined,
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

api.interceptors.request.use((config) => {
  const token = getAdminToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (requestError: unknown) => {
    if (axios.isAxiosError(requestError) && requestError.response?.status === 401) {
      clearAdminToken()
      window.location.assign('/admin/login')
    }
    return Promise.reject(requestError)
  },
)

export default api
