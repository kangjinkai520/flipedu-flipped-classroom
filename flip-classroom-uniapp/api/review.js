import { request } from '@/common/request'

export function createReview(data) {
  return request({
    url: '/reviews',
    method: 'POST',
    data
  })
}

export function listReviews(status = '') {
  const suffix = status ? ('?status=' + encodeURIComponent(status)) : ''
  return request({
    url: '/admin/reviews' + suffix
  })
}

export function reviewRequest(id, data) {
  return request({
    url: '/admin/reviews/' + id,
    method: 'PATCH',
    data
  })
}
