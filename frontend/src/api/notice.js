import { request } from './request'

export function fetchNoticePage(params) {
  return request({ url: '/api/notices', method: 'GET', params })
}

export function publishNotice(data) {
  return request({ url: '/api/notices', method: 'POST', data })
}

export function markNoticeRead(id) {
  return request({ url: `/api/notices/${id}/read`, method: 'POST' })
}

export function getNoticeReadState(id) {
  return request({ url: `/api/notices/${id}/read`, method: 'GET' })
}
