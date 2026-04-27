import { request } from '@/common/request'

export function listNotices(courseId) {
  return request({
    url: '/courses/' + courseId + '/notices'
  })
}

export function getNotice(id) {
  return request({
    url: '/notices/' + id
  })
}

export function createNotice(courseId, data) {
  return request({
    url: '/courses/' + courseId + '/notices',
    method: 'POST',
    data
  })
}

export function updateNotice(id, data) {
  return request({
    url: '/notices/' + id,
    method: 'PUT',
    data
  })
}

export function updateNoticeStatus(id, status) {
  return request({
    url: '/notices/' + id + '/status',
    method: 'PATCH',
    data: { status }
  })
}
