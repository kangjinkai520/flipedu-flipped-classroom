<template>
  <view class="web-login-container">
    <view class="bg-gradient"></view>

    <view class="login-card">
      <view class="card-inner">
        <view class="card-header">
          <view class="brand-box">
            <text class="brand-icon">FC</text>
            <text class="brand-name">FlipEdu</text>
          </view>
          <text class="login-title">管理端登录</text>
          <view class="title-underline"></view>
          <text class="welcome-text">翻转课堂教学系统 · 管理员与教师工作台</text>
        </view>

        <view class="form-body">
          <view class="form-item">
            <text class="form-label">账号 / Account</text>
            <input
              v-model="form.username"
              class="saas-input"
              placeholder="请输入管理员或教师账号"
            />
          </view>

          <view class="form-item">
            <text class="form-label">密码 / Password</text>
            <input
              v-model="form.password"
              class="saas-input"
              password
              placeholder="请输入密码"
            />
          </view>

          <button class="sign-in-btn" :loading="loading" @click="handleLogin">
            {{ loading ? '正在登录...' : '进入管理端' }}
          </button>
        </view>

        <view class="demo-section">
          <view class="demo-left">
            <text class="demo-tip">演示账号</text>
            <view class="pill-row">
              <text class="p-btn" @click="fillAccount('admin')">管理员</text>
              <text class="p-btn" @click="fillAccount('teacher')">教师</text>
            </view>
          </view>

          <view class="student-preview-link" @click="goToStudentLogin">
            <text class="sp-text">学生入口</text>
          </view>
        </view>

        <view class="copyright-info">
          <text>Flip Classroom Teaching System v2.0</text>
          <text>2026 FlipEdu Education Technology</text>
        </view>
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
      form: {
        username: '',
        password: ''
      }
    }
  },
  methods: {
    fillAccount(role) {
      const presets = {
        admin: { username: 'admin01', password: '123456' },
        teacher: { username: 'teacher01', password: '123456' }
      }
      this.form = { ...presets[role] }
      uni.showToast({ title: '已填入演示账号', icon: 'none', duration: 800 })
    },
    goToStudentLogin() {
      uni.navigateTo({ url: '/pages/login/student' })
    },
    async handleLogin() {
      if (!this.form.username || !this.form.password) {
        uni.showToast({ title: '请输入账号和密码', icon: 'none' })
        return
      }

      try {
        this.loading = true
        const data = await login(this.form)
        if (data.role !== 'admin' && data.role !== 'teacher') {
          uni.showToast({ title: '该入口仅支持管理员和教师账号', icon: 'none' })
          return
        }
        setSession(data)
        const url = data.role === 'admin' ? '/pages/admin/dashboard' : '/pages/teacher/courses'
        uni.reLaunch({ url })
      } catch (error) {
        uni.showToast({ title: error.message || '登录失败，请检查后端服务', icon: 'none' })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.web-login-container {
  width: 100vw;
  height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #4f46e5;
  overflow: hidden;
}

.bg-gradient {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 85% 10%, rgba(168, 85, 247, 0.95), transparent 32%),
    radial-gradient(circle at 10% 80%, rgba(59, 130, 246, 0.95), transparent 34%),
    linear-gradient(135deg, #7c3aed 0%, #4f46e5 48%, #2563eb 100%);
}

.login-card {
  position: relative;
  z-index: 10;
  background: #ffffff;
  width: 1180rpx;
  max-width: 92vw;
  min-height: 960rpx;
  padding: 92rpx 110rpx;
  border-radius: 64rpx;
  box-shadow: 0 60rpx 180rpx rgba(15, 23, 42, 0.34);
  box-sizing: border-box;
}

.card-inner {
  min-height: 960rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.brand-box {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 44rpx;
}

.brand-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background: #4f46e5;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 950;
}

.brand-name {
  font-size: 42rpx;
  font-weight: 950;
  color: #4f46e5;
  letter-spacing: 2rpx;
}

.login-title {
  display: block;
  font-size: 76rpx;
  font-weight: 950;
  color: #111827;
}

.title-underline {
  width: 136rpx;
  height: 12rpx;
  background: #4f46e5;
  margin-top: 18rpx;
  border-radius: 999rpx;
}

.welcome-text {
  display: block;
  margin-top: 30rpx;
  font-size: 28rpx;
  color: #64748b;
  font-weight: 700;
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 42rpx;
  margin-top: 62rpx;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.form-label {
  font-size: 25rpx;
  font-weight: 900;
  color: #475569;
  text-transform: uppercase;
}

.saas-input {
  width: 100%;
  height: 112rpx;
  background: #f8fafc;
  border: 2rpx solid #e2e8f0;
  border-radius: 28rpx;
  padding: 0 40rpx;
  font-size: 31rpx;
  color: #111827;
  font-weight: 700;
  box-sizing: border-box;
}

.sign-in-btn {
  width: 100%;
  height: 124rpx;
  line-height: 124rpx;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #ffffff;
  border-radius: 32rpx;
  font-size: 34rpx;
  font-weight: 950;
  border: none;
  box-shadow: 0 16rpx 42rpx rgba(79, 70, 229, 0.36);
  margin-top: 10rpx;
}

.demo-section {
  margin-top: 54rpx;
  padding: 32rpx 40rpx;
  background: #f8fafc;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
}

.demo-left {
  display: flex;
  align-items: center;
  gap: 22rpx;
}

.demo-tip {
  font-size: 22rpx;
  font-weight: 900;
  color: #94a3b8;
}

.pill-row {
  display: flex;
  gap: 16rpx;
}

.p-btn {
  font-size: 22rpx;
  font-weight: 900;
  color: #4f46e5;
  background: #ffffff;
  padding: 12rpx 30rpx;
  border-radius: 14rpx;
  border: 1rpx solid rgba(79, 70, 229, 0.2);
}

.student-preview-link {
  padding: 16rpx 30rpx;
  background: #eef2ff;
  border-radius: 16rpx;
  border: 2rpx dashed #4f46e5;
}

.sp-text {
  font-size: 22rpx;
  font-weight: 900;
  color: #4f46e5;
}

.copyright-info {
  margin-top: 42rpx;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.copyright-info text {
  font-size: 20rpx;
  color: #cbd5e1;
  font-weight: 700;
  text-transform: uppercase;
}
</style>
