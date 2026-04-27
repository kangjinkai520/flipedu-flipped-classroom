import { request } from '@/common/request'

export function listUsers(role) {
  const suffix = role ? ('?role=' + encodeURIComponent(role)) : ''
  return request({
    url: '/admin/users' + suffix
  })
}

export function getUser(id) {
  return request({
    url: '/admin/users/' + id
  })
}

export function createUser(data) {
  return request({
    url: '/admin/users',
    method: 'POST',
    data
  })
}

export function updateUser(id, data) {
  return request({
    url: '/admin/users/' + id,
    method: 'PUT',
    data
  })
}

export function updateUserStatus(id, status) {
  return request({
    url: '/admin/users/' + id + '/status',
    method: 'PATCH',
    data: { status }
  })
}
