import api from './api'
import type {
  AdminContentCreateData,
  AdminContentDeleteData,
  AdminContentDetailData,
  AdminContentListData,
  AdminContentListParams,
  AdminContentSaveRequest,
  AdminContentUpdateData,
  ApiResponse,
} from '../types/api'

function unwrap<T>(response: ApiResponse<T>) {
  if (!response.data) {
    throw new Error(response.message)
  }

  return response.data
}

export async function getAdminContents(params: AdminContentListParams) {
  const response = await api.get<ApiResponse<AdminContentListData>>('/admin/contents', { params })
  return unwrap(response.data)
}

export async function createAdminContent(payload: AdminContentSaveRequest) {
  const response = await api.post<ApiResponse<AdminContentCreateData>>(
    '/admin/contents',
    payload,
  )
  return unwrap(response.data)
}

export async function getAdminContentDetail(id: number) {
  const response = await api.get<ApiResponse<AdminContentDetailData>>(
    `/admin/contents/${id}`,
  )
  return unwrap(response.data)
}

export async function updateAdminContent(
  id: number,
  payload: AdminContentSaveRequest,
) {
  const response = await api.put<ApiResponse<AdminContentUpdateData>>(
    `/admin/contents/${id}`,
    payload,
  )
  return unwrap(response.data)
}

export async function deleteAdminContent(id: number) {
  const response = await api.delete<ApiResponse<AdminContentDeleteData>>(
    `/admin/contents/${id}`,
  )
  return unwrap(response.data)
}
