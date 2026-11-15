import { request } from './request'

export function fetchBuildingPage(params) {
  return request({
    url: '/api/buildings',
    method: 'GET',
    params
  })
}

export function fetchBuildingOptions() {
  return request({
    url: '/api/buildings/options',
    method: 'GET'
  })
}

export function createBuilding(data) {
  return request({
    url: '/api/buildings',
    method: 'POST',
    data
  })
}

export function updateBuilding(id, data) {
  return request({
    url: `/api/buildings/${id}`,
    method: 'PUT',
    data
  })
}

export function deleteBuilding(id) {
  return request({
    url: `/api/buildings/${id}`,
    method: 'DELETE'
  })
}
