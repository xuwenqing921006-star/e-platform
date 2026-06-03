import request from '@/utils/request'

export function listContent(query) {
  return request({
    url: '/api/admin/contents',
    method: 'get',
    params: query
  })
}

export function getContent(id) {
  return request({
    url: '/api/admin/contents/' + id,
    method: 'get'
  })
}

export function addContent(data) {
  return request({
    url: '/api/admin/contents',
    method: 'post',
    data: data
  })
}

export function updateContent(id, data) {
  return request({
    url: '/api/admin/contents/' + id,
    method: 'put',
    data: data
  })
}

export function delContent(id) {
  return request({
    url: '/api/admin/contents/' + id,
    method: 'delete'
  })
}

export function listOptions() {
  return request({
    url: '/api/admin/options',
    method: 'get'
  })
}

export function deleteAttachment(id) {
  return request({
    url: '/api/admin/attachments/' + id,
    method: 'delete'
  })
}
