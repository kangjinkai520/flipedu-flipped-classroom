import { request } from '@/common/request'

export function listMaterials(courseId) {
  return request({
    url: '/courses/' + courseId + '/materials'
  })
}

export function getMaterial(id) {
  return request({
    url: '/materials/' + id
  })
}

export function createMaterial(courseId, data) {
  return request({
    url: '/courses/' + courseId + '/materials',
    method: 'POST',
    data
  })
}

export function updateMaterial(id, data) {
  return request({
    url: '/materials/' + id,
    method: 'PUT',
    data
  })
}

export function updateMaterialStatus(id, status) {
  return request({
    url: '/materials/' + id + '/status',
    method: 'PATCH',
    data: { status }
  })
}
