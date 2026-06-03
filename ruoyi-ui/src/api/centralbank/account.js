import request from '@/utils/request'

export function listAccount(query) {
  return request({
    url: '/api/admin/accounts',
    method: 'get',
    params: query
  })
}

export function getAccount(id) {
  return request({
    url: '/api/admin/accounts/' + id,
    method: 'get'
  })
}

export function addAccount(data) {
  return request({
    url: '/api/admin/accounts',
    method: 'post',
    data: data
  })
}

export function updateAccount(id, data) {
  return request({
    url: '/api/admin/accounts/' + id,
    method: 'put',
    data: data
  })
}

export function delAccount(id) {
  return request({
    url: '/api/admin/accounts/' + id,
    method: 'delete'
  })
}

export function resetAccountPassword(id, data) {
  return request({
    url: '/api/admin/accounts/' + id + '/reset-password',
    method: 'post',
    data: data
  })
}
