import request from '@/utils/request'

export function getDashboardSummary() {
  return request({
    url: '/api/admin/dashboard/summary',
    method: 'get'
  })
}
