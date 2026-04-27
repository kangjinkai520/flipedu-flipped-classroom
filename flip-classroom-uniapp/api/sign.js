import { request } from '@/common/request'

export function listSignTasks(courseId) {
  return request({
    url: '/courses/' + courseId + '/sign-tasks'
  })
}

export function createSignTask(courseId, data) {
  return request({
    url: '/courses/' + courseId + '/sign-tasks',
    method: 'POST',
    data
  })
}

export function getSignTask(id) {
  return request({
    url: '/sign-tasks/' + id
  })
}

export function updateSignTaskStatus(id, status) {
  return request({
    url: '/sign-tasks/' + id + '/status',
    method: 'PATCH',
    data: { status }
  })
}

export function listSignRecords(id) {
  return request({
    url: '/sign-tasks/' + id + '/records'
  })
}

export function submitSign(id, data) {
  return request({
    url: '/sign-tasks/' + id + '/sign',
    method: 'POST',
    data
  })
}

export function reviewSignRecord(id, data) {
  return request({
    url: '/sign-records/' + id + '/review',
    method: 'PATCH',
    data
  })
}
