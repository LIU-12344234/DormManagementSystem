import { request } from './request'

export function fetchRoomPage(params) {
  return request({
    url: '/api/rooms',
    method: 'GET',
    params
  })
}

export function fetchRoomOptions(params) {
  return request({
    url: '/api/rooms/options',
    method: 'GET',
    params
  })
}

export function createRoom(data) {
  return request({
    url: '/api/rooms',
    method: 'POST',
    data
  })
}

export function updateRoom(id, data) {
  return request({
    url: `/api/rooms/${id}`,
    method: 'PUT',
    data
  })
}

export function deleteRoom(id) {
  return request({
    url: `/api/rooms/${id}`,
    method: 'DELETE'
  })
}
