<template>
  <view class="fc-page student-login">
    <view class="login-header">
      <view class="brand-logo">
        <text class="logo-icon">🎓</text>
      </view>
      <view class="title-group">
        <text class="welcome-title">欢迎回来！</text>
        <text class="welcome-subtitle">Welcome back, please login</text>
      </view>
    </view>

    <view class="form-container">
      <view class="input-item">
        <text class="input-label">账号 / Username</text>
        <view class="input-wrapper">
          <text class="field-icon">👤</text>
          <input
            v-model="form.username"
            class="field-input"
            placeholder="请输入您的用户名"
            placeholder-class="placeholder-style"
          />
        </view>
      </view>

      <view class="input-item">
        <text class="input-label">密码 / Password</text>
        <view class="input-wrapper">
          <text class="field-icon">🔒</text>
          <input
            v-model="form.password"
            class="field-input"
            password
            placeholder="请输入您的登录密码"
            placeholder-class="placeholder-style"
          />
        </view>
      </view>

      <view class="form-actions">
        <view class="remember-me">
          <checkbox-group @change="handleRememberChange">
            <label class="checkbox-label">
              <checkbox value="remember" scale="0.6" color="#2563eb" />
              <text class="checkbox-text">记住我</text>
            </label>
          </checkbox-group>
        </view>
        <text class="forgot-link" @click="showComingSoon('找回密码')">忘记密码？</text>
      </view>

      <button
        class="login-btn"
        :loading="loading"
        :disabled="loading"
        @click="handleLogin"
      >
        {{ loading ? '正在登录...' : '立即登录' }}
      </button>

      <view class="signup-footer">
        <text class="footer-text">还没有账号？</text>
        <text class="signup-link" @click="showComingSoon('账号注册')">立即注册</text>
      </view>
    </view>
  </view>
</template>

<script>
import { login } from '@/api/auth'
import { setSession } from '@/common/session'

export default {
  data() {
    return {
      loading: false,
      remember: false,
      form: {
        username: '',
        password: ''
      }
    }
  },
  methods: {
    handleRememberChange(event) {
      this.remember = event.detail.value.includes('remember')
    },
    showComingSoon(name) {
      uni.showToast({ title: `${name}功能暂未开放`, icon: 'none' })
    },
    async handleLogin() {
      if (!this.form.username || !this.form.password) {
        uni.showToast({ title: '请输入账号和密码', icon: 'none' })
        return
      }

      try {
        this.loading = true
        const data = await login(this.form)
        
        // 校验是否为学生角色
        if (data.role !== 'student') {
          uni.showToast({
            title: '权限不足：该页面仅限学生登录',
            icon: 'none'
          })
          return
        }

        setSession(data)
        
        uni.reLaunch({
          url: '/pages/student/courses'
        })
      } catch (error) {
        uni.showToast({
          title: '登录失败，请检查账号密码',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.student-login {
  background: #ffffff;
  min-height: 100vh;
  padding: 120rpx 60rpx 40rpx;
}

/* Header Section */
.login-header {
  margin-bottom: 80rpx;
}

.brand-logo {
  width: 100rpx;
  height: 100rpx;
  background: #f8faff;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 48rpx;
  border: 1px solid #edf2f7;
}

.logo-icon {
  font-size: 50rpx;
}

.title-group {
  display: flex;
  flex-direction: column;
}

.welcome-title {
  font-size: 56rpx;
  font-weight: 800;
  color: #1a202c;
  letter-spacing: -1rpx;
}

.welcome-subtitle {
  font-size: 24rpx;
  color: #a0aec0;
  margin-top: 8rpx;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 2rpx;
}

/* Form Section */
.form-container {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.input-item {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.input-label {
  font-size: 24rpx;
  font-weight: 700;
  color: #718096;
  margin-left: 8rpx;
}

.input-wrapper {
  background: #f8faff;
  height: 110rpx;
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  padding: 0 32rpx;
  border: 1px solid #edf2f7;
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  border-color: #2563eb;
  background: #ffffff;
  box-shadow: 0 0 0 4rpx rgba(37, 99, 235, 0.1);
}

.field-icon {
  font-size: 32rpx;
  margin-right: 20rpx;
}

.field-input {
  flex: 1;
  font-size: 30rpx;
  color: #1a202c;
  font-weight: 600;
}

.placeholder-style {
  color: #cbd5e0;
  font-weight: 400;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: -10rpx;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.checkbox-text {
  font-size: 24rpx;
  color: #718096;
  font-weight: 600;
}

.forgot-link {
  font-size: 24rpx;
  color: #1a202c;
  font-weight: 700;
}

.login-btn {
  margin-top: 40rpx;
  height: 128rpx;
  line-height: 128rpx;
  background: #1a202c;
  color: #ffffff;
  border-radius: 40rpx;
  font-size: 36rpx;
  font-weight: 800;
  border: none;
  box-shadow: 0 20rpx 40rpx rgba(26, 32, 44, 0.25);
  transition: all 0.3s ease;
}

.login-btn:active {
  transform: scale(0.97);
  opacity: 0.9;
  box-shadow: 0 10rpx 20rpx rgba(26, 32, 44, 0.2);
}

.login-btn[disabled] {
  background: #cbd5e0;
}

.signup-footer {
  margin-top: 40rpx;
  text-align: center;
}

.footer-text {
  font-size: 26rpx;
  color: #718096;
}

.signup-link {
  font-size: 26rpx;
  color: #2563eb;
  font-weight: 800;
  margin-left: 12rpx;
}
</style>
