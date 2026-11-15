import { request } from './request'

export function fetchBedPage(params) {
  return request({
    url: '/api/beds',
    method: 'GET',
    params
  })
}

export function fetchBedOptions(params) {
  return request({
    url: '/api/beds/options',
    method: 'GET',
    params
  })
}

export function createBed(data) {
  return request({
    url: '/api/beds',
    method: 'POST',
    data
  })
}

export function updateBed(id, data) {
  return request({
    url: `/api/beds/${id}`,
    method: 'PUT',
    data
  })
}

export function deleteBed(id) {
  return request({
    url: `/api/beds/${id}`,
    method: 'DELETE'
  })
}
