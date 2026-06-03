import request from '@/utils/request'

export function listProduct(query) {
  return request({
    url: '/api/admin/products',
    method: 'get',
    params: query
  })
}

export function getProduct(id) {
  return request({
    url: '/api/admin/products/' + id,
    method: 'get'
  })
}

export function addProduct(data) {
  return request({
    url: '/api/admin/products',
    method: 'post',
    data: data
  })
}

export function updateProduct(id, data) {
  return request({
    url: '/api/admin/products/' + id,
    method: 'put',
    data: data
  })
}

export function delProduct(id) {
  return request({
    url: '/api/admin/products/' + id,
    method: 'delete'
  })
}

export function listOptions() {
  return request({
    url: '/api/admin/options',
    method: 'get'
  })
}

export function downloadProductTemplate() {
  return request({
    url: '/api/admin/products/import-template/download',
    method: 'get',
    responseType: 'blob'
  })
}

export function validateProductImport(data) {
  return request({
    url: '/api/admin/products/import/validate',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function commitProductImport(data) {
  return request({
    url: '/api/admin/products/import/commit',
    method: 'post',
    data: data
  })
}
