import request from '@/utils/request'

export function listAuditLog(query) {
  return request({
    url: '/api/admin/audit-logs',
    method: 'get',
    params: query
  })
}
