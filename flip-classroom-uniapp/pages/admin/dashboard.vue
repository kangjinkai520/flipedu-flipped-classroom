<template>
  <view class="admin-page">
    <view class="side-nav">
      <view class="brand">FlipEdu</view>
      <view class="nav-item active">总览</view>
      <view class="nav-item" @click="go('/pages/admin/courses')">课程管理</view>
      <view class="nav-item" @click="go('/pages/admin/users')">用户管理</view>
      <view class="nav-item" @click="go('/pages/admin/reviews')">审核中心</view>
      <view class="nav-spacer"></view>
      <view class="user-line">{{ session.realName || '系统管理员' }}</view>
      <view class="logout" @click="logout">退出登录</view>
    </view>

    <view class="main">
      <view class="page-head">
        <view>
          <text class="page-title">管理员总览</text>
          <text class="page-desc">查看系统用户、课程和成员规模。</text>
        </view>
        <view class="head-actions">
          <button class="refresh-btn" @click="loadData">刷新</button>
          <button class="logout-btn" @click="logout">退出登录</button>
        </view>
      </view>

      <view class="stats-grid">
        <view class="stat-card">
          <text class="stat-label">用户总数</text>
          <text class="stat-value">{{ users.length }}</text>
        </view>
        <view class="stat-card">
          <text class="stat-label">教师</text>
          <text class="stat-value">{{ roleCount.teacher }}</text>
        </view>
        <view class="stat-card">
          <text class="stat-label">学生</text>
          <text class="stat-value">{{ roleCount.student }}</text>
        </view>
        <view class="stat-card">
          <text class="stat-label">课程</text>
          <text class="stat-value">{{ courses.length }}</text>
        </view>
      </view>

      <view class="content-grid">
        <view class="panel">
          <view class="panel-head">
            <text class="panel-title">最近课程</text>
            <text class="panel-link" @click="go('/pages/admin/courses')">查看全部</text>
          </view>
          <view v-for="course in recentCourses" :key="course.id" class="list-row">
            <view>
              <text class="row-title">{{ course.courseName }}</text>
              <text class="row-sub">教师：{{ course.teacherName || '未分配' }} · {{ course.term || '未设置学期' }}</text>
            </view>
            <text :class="['row-tag', course.status === 1 ? 'on' : 'off']">{{ course.status === 1 ? '启用' : '停用' }}</text>
          </view>
          <view v-if="!recentCourses.length" class="empty">暂无课程</view>
        </view>

        <view class="panel">
          <view class="panel-head">
            <text class="panel-title">用户构成</text>
          </view>
          <view class="role-row">
            <text>管理员</text>
            <text>{{ roleCount.admin }}</text>
          </view>
          <view class="role-row">
            <text>教师</text>
            <text>{{ roleCount.teacher }}</text>
          </view>
          <view class="role-row">
            <text>学生</text>
            <text>{{ roleCount.student }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { listUsers } from '@/api/admin'
import { listCourses } from '@/api/course'
import { clearSession, requireSession } from '@/common/session'

export default {
  data() {
    return {
      session: {},
      users: [],
      courses: []
    }
  },
  computed: {
    roleCount() {
      return this.users.reduce((acc, item) => {
        const role = (item.role || '').toLowerCase()
        acc[role] = (acc[role] || 0) + 1
        return acc
      }, { admin: 0, teacher: 0, student: 0 })
    },
    recentCourses() {
      return this.courses.slice(0, 5)
    }
  },
  onLoad() {
    const session = requireSession()
    if (!session || session.role !== 'admin') return
    this.session = session
  },
  onShow() {
    if (!this.session || !this.session.userId) {
      const session = requireSession()
      if (!session || session.role !== 'admin') return
      this.session = session
    }
    this.loadData()
  },
  methods: {
    async loadData() {
      const [users, courses] = await Promise.all([listUsers(''), listCourses()])
      this.users = users || []
      this.courses = courses || []
    },
    go(url) {
      uni.redirectTo({ url })
    },
    logout() {
      clearSession()
      uni.reLaunch({ url: '/pages/login/index' })
    }
  }
}
</script>

<style scoped>
.admin-page { display: flex; min-height: 100vh; background: #f6f7fb; color: #172033; }
.side-nav { width: 360rpx; background: #172033; color: #fff; padding: 48rpx 30rpx; display: flex; flex-direction: column; box-sizing: border-box; }
.brand { font-size: 34rpx; font-weight: 900; margin-bottom: 54rpx; }
.nav-item { padding: 24rpx 26rpx; border-radius: 16rpx; margin-bottom: 12rpx; color: #cbd5e1; font-size: 26rpx; font-weight: 800; }
.nav-item.active { background: #fff; color: #172033; }
.nav-spacer { flex: 1; }
.user-line { font-size: 24rpx; color: #e2e8f0; margin-bottom: 14rpx; }
.logout { font-size: 24rpx; color: #fca5a5; font-weight: 800; }
.main { flex: 1; padding: 54rpx; box-sizing: border-box; }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 36rpx; gap: 24rpx; }
.page-title { display: block; font-size: 46rpx; font-weight: 900; }
.page-desc { display: block; margin-top: 10rpx; font-size: 25rpx; color: #64748b; }
.head-actions { display: flex; gap: 14rpx; align-items: center; }
.refresh-btn, .logout-btn { margin: 0; height: 76rpx; line-height: 76rpx; padding: 0 30rpx; border-radius: 14rpx; border: none; font-weight: 800; }
.refresh-btn { background: #fff; border: 1rpx solid #e2e8f0; color: #334155; }
.logout-btn { background: #fee2e2; color: #dc2626; }
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24rpx; margin-bottom: 30rpx; }
.stat-card, .panel { background: #fff; border: 1rpx solid #e8ecf3; border-radius: 18rpx; padding: 30rpx; box-sizing: border-box; }
.stat-label { display: block; color: #64748b; font-size: 24rpx; font-weight: 800; }
.stat-value { display: block; margin-top: 18rpx; font-size: 50rpx; font-weight: 900; color: #172033; }
.content-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 24rpx; }
.panel-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.panel-title { font-size: 30rpx; font-weight: 900; }
.panel-link { color: #4f46e5; font-size: 24rpx; font-weight: 800; }
.list-row, .role-row { display: flex; justify-content: space-between; align-items: center; gap: 20rpx; padding: 22rpx 0; border-bottom: 1rpx solid #eef2f7; }
.row-title { display: block; font-size: 27rpx; font-weight: 900; }
.row-sub { display: block; margin-top: 6rpx; color: #64748b; font-size: 22rpx; }
.row-tag { border-radius: 999rpx; padding: 8rpx 18rpx; font-size: 22rpx; font-weight: 900; }
.row-tag.on { background: #dcfce7; color: #15803d; }
.row-tag.off { background: #fee2e2; color: #dc2626; }
.role-row text { font-size: 27rpx; font-weight: 800; }
.empty { padding: 50rpx 0; color: #94a3b8; text-align: center; }
</style>
