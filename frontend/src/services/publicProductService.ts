import api from './api'
import type { ApiResponse, PublicProductListData } from '../types/api'

export interface PublicProductListParams {
  page: number
  page_size: number
}

export async function listPublicProducts(params: PublicProductListParams) {
  const response = await api.get<ApiResponse<PublicProductListData>>(
    '/public/products',
    { params },
  )

  return response.data.data
}
