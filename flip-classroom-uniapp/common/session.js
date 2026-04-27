const SESSION_KEY = 'flip-classroom-session'

export function getSession() {
  return uni.getStorageSync(SESSION_KEY) || null
}

export function setSession(session) {
  uni.setStorageSync(SESSION_KEY, session)
}

export function clearSession() {
  uni.removeStorageSync(SESSION_KEY)
}

export function requireSession() {
  const session = getSession()
  if (!session) {
    uni.reLaunch({
      url: '/pages/login/index'
    })
    return null
  }
  return session
}
