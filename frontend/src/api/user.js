import { request } from './request'

export function fetchUserPage(params) {
  return request({ url: '/api/users', method: 'GET', params })
}

export function createUser(data) {
  return request({ url: '/api/users', method: 'POST', data })
}

export function updateUser(id, data) {
  return request({ url: `/api/users/${id}`, method: 'PUT', data })
}

export function resetUserPassword(id, data) {
  return request({ url: `/api/users/${id}/reset-password`, method: 'POST', data })
}

export function setUserRoles(id, data) {
  return request({ url: `/api/users/${id}/roles`, method: 'PUT', data })
}

export function fetchUserOptions(params) {
  return request({ url: '/api/users/options', method: 'GET', params })
}
