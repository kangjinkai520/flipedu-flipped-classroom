import { request } from '@/common/request'

export function listAssignments(courseId) {
  return request({
    url: '/courses/' + courseId + '/assignments'
  })
}

export function getAssignment(id) {
  return request({
    url: '/assignments/' + id
  })
}

export function createAssignment(courseId, data) {
  return request({
    url: '/courses/' + courseId + '/assignments',
    method: 'POST',
    data
  })
}

export function updateAssignment(id, data) {
  return request({
    url: '/assignments/' + id,
    method: 'PUT',
    data
  })
}

export function updateAssignmentPublished(id, published) {
  return request({
    url: '/assignments/' + id + '/published',
    method: 'PATCH',
    data: { published }
  })
}

export function listAssignmentSubmissions(id) {
  return request({
    url: '/assignments/' + id + '/submissions'
  })
}

export function submitAssignment(id, data) {
  return request({
    url: '/assignments/' + id + '/submit',
    method: 'POST',
    data
  })
}

export function scoreAssignmentSubmission(id, data) {
  return request({
    url: '/assignment-submissions/' + id + '/score',
    method: 'PATCH',
    data
  })
}
