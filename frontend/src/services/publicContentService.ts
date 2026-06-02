import api from './api'
import type {
  ApiResponse,
  ContentCategory,
  CountyCode,
  PublicContentDetailData,
  PublicContentListData,
  PublicScope,
} from '../types/api'

export interface PublicContentListParams {
  category: ContentCategory
  scope: PublicScope
  county_code?: CountyCode
  page: number
  page_size: number
}

export async function listPublicContents(params: PublicContentListParams) {
  const response = await api.get<ApiResponse<PublicContentListData>>(
    '/public/contents',
    { params },
  )

  return response.data.data
}

export async function getPublicContentDetail(id: number) {
  const response = await api.get<ApiResponse<PublicContentDetailData>>(
    `/public/contents/${id}`,
  )

  return response.data.data
}
