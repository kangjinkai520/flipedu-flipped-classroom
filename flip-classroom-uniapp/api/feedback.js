import { request } from '@/common/request'

export function createCourseFeedback(courseId, data) {
  return request({
    url: '/courses/' + courseId + '/feedback',
    method: 'POST',
    data
  })
}

export function listCourseFeedback(courseId) {
  return request({
    url: '/courses/' + courseId + '/feedback'
  })
}

export function listMyCourseFeedback(courseId, studentId) {
  return request({
    url: '/courses/' + courseId + '/feedback/my',
    data: { studentId }
  })
}

export function getCourseFeedbackSummary(courseId) {
  return request({
    url: '/courses/' + courseId + '/feedback/summary'
  })
}
