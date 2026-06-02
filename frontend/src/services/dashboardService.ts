import api from './api'
import type { ApiResponse, DashboardSummaryData } from '../types/api'

export async function getDashboardSummary() {
  const response = await api.get<ApiResponse<DashboardSummaryData>>(
    '/admin/dashboard/summary',
  )

  if (!response.data.data) {
    throw new Error(response.data.message)
  }

  return response.data.data
}
