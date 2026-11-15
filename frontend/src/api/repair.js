import { request } from './request'

export function fetchRepairPage(params) {
  return request({
    url: '/api/repairs',
    method: 'GET',
    params
  })
}

export function fetchMyRepairPage(params) {
  return request({
    url: '/api/repairs/my',
    method: 'GET',
    params
  })
}

export function submitRepair(data) {
  return request({
    url: '/api/repairs',
    method: 'POST',
    data
  })
}

export function assignRepair(id, data) {
  return request({
    url: `/api/repairs/${id}/assign`,
    method: 'POST',
    data
  })
}

export function startRepair(id, data) {
  return request({
    url: `/api/repairs/${id}/start`,
    method: 'POST',
    data
  })
}

export function finishRepair(id, data) {
  return request({
    url: `/api/repairs/${id}/finish`,
    method: 'POST',
    data
  })
}

export function closeRepair(id, data) {
  return request({
    url: `/api/repairs/${id}/close`,
    method: 'POST',
    data
  })
}

export function rateRepair(id, data) {
  return request({
    url: `/api/repairs/${id}/rate`,
    method: 'POST',
    data
  })
}

export function fetchRepairLogs(id) {
  return request({
    url: `/api/repairs/${id}/logs`,
    method: 'GET'
  })
}
