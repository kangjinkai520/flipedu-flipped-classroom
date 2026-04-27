import { request } from '@/common/request'

export function listCourses(params = {}) {
  const query = []
  if (params.teacherId) {
    query.push('teacherId=' + encodeURIComponent(params.teacherId))
  }
  if (params.memberUserId) {
    query.push('memberUserId=' + encodeURIComponent(params.memberUserId))
  }
  const suffix = query.length ? ('?' + query.join('&')) : ''
  return request({
    url: '/courses' + suffix
  })
}

export function getCourse(id) {
  return request({
    url: '/courses/' + id
  })
}

export function createCourse(data) {
  return request({
    url: '/courses',
    method: 'POST',
    data
  })
}

export function updateCourse(id, data) {
  return request({
    url: '/courses/' + id,
    method: 'PUT',
    data
  })
}

export function listCourseMembers(courseId) {
  return request({
    url: '/courses/' + courseId + '/members'
  })
}

export function addCourseMember(courseId, data) {
  return request({
    url: '/courses/' + courseId + '/members',
    method: 'POST',
    data
  })
}
