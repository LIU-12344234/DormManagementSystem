import { request } from './request'

export function fetchOverview() {
  return request({
    url: '/api/reports/overview',
    method: 'GET'
  })
}
