import { request } from './request'

export function fetchLateReturnPage(params) {
  return request({ url: '/api/late-returns', method: 'GET', params })
}

export function createLateReturn(data) {
  return request({ url: '/api/late-returns', method: 'POST', data })
}

export function deleteLateReturn(id) {
  return request({ url: `/api/late-returns/${id}`, method: 'DELETE' })
}
