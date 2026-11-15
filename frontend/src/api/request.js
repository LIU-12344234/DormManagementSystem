import { ElMessage } from 'element-plus'
import { http } from './http'

export async function request(config) {
  const resp = await http.request(config)
  const body = resp.data
  if (body?.code !== 0) {
    ElMessage.error(body?.message || '请求失败')
    throw new Error(body?.message || 'Request failed')
  }
  return body.data
}
