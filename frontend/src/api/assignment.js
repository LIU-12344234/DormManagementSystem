import { request } from './request'

export function fetchAssignmentPage(params) {
  return request({
    url: '/api/assignments',
    method: 'GET',
    params
  })
}

export function assignBed(data) {
  return request({
    url: '/api/assignments/assign',
    method: 'POST',
    data
  })
}

export function checkoutStudent(data) {
  return request({
    url: '/api/assignments/checkout',
    method: 'POST',
    data
  })
}

export function fetchMyAssignments(params) {
  return request({
    url: '/api/assignments/my',
    method: 'GET',
    params
  })
}
