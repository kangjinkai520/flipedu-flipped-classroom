import { request } from '@/common/request'

export function listQuizzes(courseId) {
  return request({
    url: '/courses/' + courseId + '/quizzes'
  })
}

export function createQuiz(courseId, data) {
  return request({
    url: '/courses/' + courseId + '/quizzes',
    method: 'POST',
    data
  })
}

export function getQuiz(id) {
  return request({
    url: '/quizzes/' + id
  })
}

export function updateQuizStatus(id, status) {
  return request({
    url: '/quizzes/' + id + '/status',
    method: 'PATCH',
    data: { status }
  })
}

export function listQuizQuestions(id) {
  return request({
    url: '/quizzes/' + id + '/questions'
  })
}

export function listQuizRecords(id) {
  return request({
    url: '/quizzes/' + id + '/records'
  })
}

export function submitQuiz(id, data) {
  return request({
    url: '/quizzes/' + id + '/submit',
    method: 'POST',
    data
  })
}
