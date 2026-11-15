import { request } from './request'

export function fetchSettingPage(params) {
  return request({ url: '/api/settings', method: 'GET', params })
}

export function createSetting(data) {
  return request({ url: '/api/settings', method: 'POST', data })
}

export function updateSetting(id, data) {
  return request({ url: `/api/settings/${id}`, method: 'PUT', data })
}

export function deleteSetting(id) {
  return request({ url: `/api/settings/${id}`, method: 'DELETE' })
}
