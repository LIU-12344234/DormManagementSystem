import { request } from './request'

export function fetchRoles() {
  return request({ url: '/api/roles', method: 'GET' })
}
