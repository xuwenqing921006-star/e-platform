import api from './api'
import type {
  AdminAttachmentDeleteData,
  AdminAttachmentUploadData,
  ApiResponse,
} from '../types/api'

function unwrap<T>(response: ApiResponse<T>) {
  if (!response.data) {
    throw new Error(response.message)
  }

  return response.data
}

export async function uploadAdminAttachment(file: File) {
  const body = new FormData()
  body.append('file', file)
  const response = await api.post<ApiResponse<AdminAttachmentUploadData>>('/admin/attachments', body)
  return unwrap(response.data)
}

export async function deleteAdminAttachment(id: number) {
  const response = await api.delete<ApiResponse<AdminAttachmentDeleteData>>(
    `/admin/attachments/${id}`,
  )
  return unwrap(response.data)
}
