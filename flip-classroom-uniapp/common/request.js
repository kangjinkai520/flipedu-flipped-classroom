import config from './config'
import { getSession } from './session'

function normalizeErrorMessage(message) {
  const map = {
    'Username and password cannot be empty': '请输入用户名和密码',
    'Username or password is incorrect': '用户名或密码错误',
    'Account has been disabled': '账号已被停用，请联系管理员',
    'StudentId and signCode are required': '请填写签到码',
    'Sign task is not available': '签到任务不可用',
    'Sign code is incorrect': '签到码不正确',
    'Student has already signed in': '你已经完成签到',
    'Current location is outside the allowed range, please submit an exception request': '当前位置不在允许范围内，请提交异常签到申请',
    'Location is required, please submit an exception request if location is unavailable': '需要定位信息，如果无法定位请提交异常签到申请',
    'Course code already exists': '课程代码已存在',
    'TeacherId, courseName, courseCode and term are required': '请填写课程名称、课程代码、学期并选择教师',
    'TeacherId must point to a teacher user': '请选择教师账号',
    'User is already a course member': '该用户已经是课程成员',
    'Teacher is already bound to the course': '该教师已经绑定为课程授课教师',
    'User role does not match member role': '用户角色与成员角色不匹配',
    'Username already exists': '用户名已存在',
    '管理员账号不能被停用': '管理员账号不能被停用',
    'Review targetType, targetId, actionType and title are required': '审核申请信息不完整',
    'Review request has already been processed': '该审核申请已经处理过',
    'Review request does not exist': '审核申请不存在'
  }
  return map[message] || message
}

function showError(message) {
  uni.showToast({
    title: normalizeErrorMessage(message) || '请求失败',
    icon: 'none',
    duration: 2200
  })
}

export function request(options) {
  const session = getSession()
  const header = {
    'Content-Type': 'application/json'
  }

  if (session && session.token) {
    header.Authorization = 'Bearer ' + session.token
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: config.baseURL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success: (response) => {
        const body = response.data || {}
        if (body.code === 200) {
          resolve(body.data)
          return
        }
        const message = normalizeErrorMessage(body.message) || '请求失败'
        showError(message)
        reject(new Error(message))
      },
      fail: (error) => {
        showError('网络连接失败，请确认后端服务已启动')
        reject(error)
      }
    })
  })
}
