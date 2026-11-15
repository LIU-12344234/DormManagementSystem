import { request } from './request'

export function fetchVisitorPage(params) {
  return request({ url: '/api/visitors', method: 'GET', params })
}

export function createVisitor(data) {
  return request({ url: '/api/visitors', method: 'POST', data })
}

export function checkoutVisitor(id) {
  return request({ url: `/api/visitors/${id}/checkout`, method: 'POST' })
}

export function deleteVisitor(id) {
  return request({ url: `/api/visitors/${id}`, method: 'DELETE' })
}
