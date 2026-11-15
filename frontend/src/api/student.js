import { request } from './request'

export function fetchStudentOptions(params) {
  return request({
    url: '/api/students/options',
    method: 'GET',
    params
  })
}

export function fetchMyStudentProfile() {
  return request({
    url: '/api/students/me',
    method: 'GET'
  })
}
