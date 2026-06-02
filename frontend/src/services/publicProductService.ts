import api from './api'
import type {
  ApiResponse,
  PublicProductDetailData,
  PublicProductListData,
} from '../types/api'

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

export async function getPublicProductDetail(id: number) {
  const response = await api.get<ApiResponse<PublicProductDetailData>>(
    `/public/products/${id}`,
  )

  return response.data.data
}
