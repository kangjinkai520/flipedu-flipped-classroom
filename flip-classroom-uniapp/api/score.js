import { request } from '@/common/request'

export function listCourseScores(courseId) {
  return request({
    url: '/courses/' + courseId + '/scores'
  })
}

export function listUserScores(userId, courseId) {
  const suffix = courseId ? ('?courseId=' + encodeURIComponent(courseId)) : ''
  return request({
    url: '/users/' + userId + '/scores' + suffix
  })
}
