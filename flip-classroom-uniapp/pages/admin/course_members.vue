<template>
  <view class="admin-page">
    <view class="side-nav">
      <view class="brand">FlipEdu</view>
      <view class="nav-item" @click="go('/pages/admin/dashboard')">总览</view>
      <view class="nav-item active" @click="go('/pages/admin/courses')">课程管理</view>
      <view class="nav-item" @click="go('/pages/admin/users')">用户管理</view>
      <view class="nav-item" @click="go('/pages/admin/reviews')">审核中心</view>
      <view class="nav-spacer"></view>
      <view class="user-line">{{ session.realName || '系统管理员' }}</view>
      <view class="logout" @click="logout">退出登录</view>
    </view>

    <view class="main">
      <view class="page-head">
        <view>
          <text class="page-title">课程成员</text>
          <text class="page-desc">{{ courseName }}</text>
        </view>
        <view class="head-actions">
          <button class="secondary-btn" @click="go('/pages/admin/courses')">返回课程</button>
          <button class="primary-btn" @click="loadData">刷新</button>
        </view>
      </view>

      <view class="summary-grid">
        <view class="stat-card">
          <text class="stat-label">成员总数</text>
          <text class="stat-value">{{ members.length }}</text>
        </view>
        <view class="stat-card">
          <text class="stat-label">教师</text>
          <text class="stat-value">{{ teacherMembers.length }}</text>
        </view>
        <view class="stat-card">
          <text class="stat-label">学生</text>
          <text class="stat-value">{{ studentMembers.length }}</text>
        </view>
      </view>

      <view class="layout-grid">
        <view class="form-card">
          <text class="card-title">添加课程成员</text>
          <view class="field">
            <text class="label">成员角色</text>
            <view class="segmented">
              <text :class="['seg-item', memberForm.role === 'teacher' ? 'active' : '']" @click="changeAddRole('teacher')">教师</text>
              <text :class="['seg-item', memberForm.role === 'student' ? 'active' : '']" @click="changeAddRole('student')">学生</text>
            </view>
          </view>
          <view class="field">
            <text class="label">选择用户</text>
            <picker :range="candidateOptions" range-key="label" @change="selectCandidate">
              <view class="picker-box">{{ selectedCandidateLabel }}</view>
            </picker>
          </view>
          <button class="primary-btn full" @click="submitMember">添加到课程</button>
          <text class="hint">这里只会显示尚未加入本课程的可用账号。</text>
        </view>

        <view class="member-card">
          <view class="section-head">
            <text class="card-title">教师</text>
            <text class="muted">{{ teacherMembers.length }} 人</text>
          </view>
          <view v-for="member in teacherMembers" :key="member.id || member.userId" class="member-row">
            <view class="avatar">{{ avatar(member.realName || member.username) }}</view>
            <view class="member-main">
              <text class="member-name">{{ member.realName || member.username }}</text>
              <text class="member-sub">@{{ member.username || '-' }}</text>
            </view>
            <text class="role-tag">教师</text>
          </view>
          <view v-if="!teacherMembers.length" class="empty small">暂无教师成员</view>

          <view class="section-head second">
            <text class="card-title">学生</text>
            <text class="muted">{{ studentMembers.length }} 人</text>
          </view>
          <view v-for="member in studentMembers" :key="member.id || member.userId" class="member-row">
            <view class="avatar green">{{ avatar(member.realName || member.username) }}</view>
            <view class="member-main">
              <text class="member-name">{{ member.realName || member.username }}</text>
              <text class="member-sub">@{{ member.username || '-' }}</text>
            </view>
            <text :class="['status-tag', member.status === 1 ? 'on' : 'off']">{{ member.status === 1 ? '启用' : '停用' }}</text>
          </view>
          <view v-if="!studentMembers.length" class="empty small">暂无学生成员</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { listUsers } from '@/api/admin'
import { addCourseMember, listCourseMembers } from '@/api/course'
import { clearSession, requireSession } from '@/common/session'

export default {
  data() {
    return {
      session: {},
      courseId: null,
      courseName: '',
      members: [],
      candidates: [],
      memberForm: {
        role: 'student',
        userId: null
      }
    }
  },
  computed: {
    teacherMembers() {
      return this.members.filter(item => (item.memberRole || item.userRole || '').toLowerCase() === 'teacher')
    },
    studentMembers() {
      return this.members.filter(item => (item.memberRole || item.userRole || '').toLowerCase() === 'student')
    },
    existingUserIds() {
      return this.members.map(item => Number(item.userId))
    },
    candidateOptions() {
      return this.candidates
        .filter(item => !this.existingUserIds.includes(Number(item.id)))
        .map(item => ({
          label: `${item.realName || item.username}（${item.username}）`,
          value: item.id
        }))
    },
    selectedCandidateLabel() {
      const target = this.candidateOptions.find(item => Number(item.value) === Number(this.memberForm.userId))
      return target ? target.label : '请选择用户'
    }
  },
  onLoad(query) {
    const session = requireSession()
    if (!session || session.role !== 'admin') return
    this.session = session
    this.courseId = Number(query.courseId)
    this.courseName = this.decode(query.courseName) || '课程'
  },
  onShow() {
    if (!this.session || !this.session.userId) {
      const session = requireSession()
      if (!session || session.role !== 'admin') return
      this.session = session
    }
    if (!this.courseId) return
    this.loadData()
  },
  methods: {
    decode(value) {
      if (!value) return ''
      try { return decodeURIComponent(value) } catch (e) { return value }
    },
    async loadData() {
      const [members, candidates] = await Promise.all([
        listCourseMembers(this.courseId),
        listUsers(this.memberForm.role)
      ])
      this.members = members || []
      this.candidates = candidates || []
      if (!this.candidateOptions.find(item => Number(item.value) === Number(this.memberForm.userId))) {
        this.memberForm.userId = null
      }
    },
    async changeAddRole(role) {
      this.memberForm.role = role
      this.memberForm.userId = null
      this.candidates = await listUsers(role)
    },
    selectCandidate(event) {
      const option = this.candidateOptions[Number(event.detail.value)]
      this.memberForm.userId = option ? option.value : null
    },
    async submitMember() {
      if (!this.memberForm.userId) {
        uni.showToast({ title: '请选择要添加的用户', icon: 'none' })
        return
      }
      await addCourseMember(this.courseId, this.memberForm)
      uni.showToast({ title: '添加成功', icon: 'success' })
      this.memberForm.userId = null
      this.loadData()
    },
    avatar(name) {
      return (name || '成').charAt(0)
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
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30rpx; gap: 24rpx; }
.page-title { display: block; font-size: 44rpx; font-weight: 900; }
.page-desc { display: block; margin-top: 8rpx; color: #64748b; font-size: 24rpx; }
.head-actions { display: flex; gap: 14rpx; }
.summary-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20rpx; margin-bottom: 24rpx; }
.stat-card, .form-card, .member-card { background: #fff; border: 1rpx solid #e8ecf3; border-radius: 18rpx; padding: 30rpx; box-sizing: border-box; }
.stat-label { display: block; color: #64748b; font-size: 23rpx; font-weight: 800; }
.stat-value { display: block; margin-top: 14rpx; font-size: 44rpx; font-weight: 900; }
.layout-grid { display: grid; grid-template-columns: 420rpx 1fr; gap: 24rpx; align-items: flex-start; }
.card-title { display: block; font-size: 30rpx; font-weight: 900; margin-bottom: 22rpx; }
.field { margin-bottom: 22rpx; }
.label { display: block; margin-bottom: 10rpx; color: #64748b; font-size: 23rpx; font-weight: 800; }
.picker-box { height: 78rpx; line-height: 78rpx; background: #f8fafc; border: 1rpx solid #e2e8f0; border-radius: 14rpx; padding: 0 20rpx; font-size: 26rpx; box-sizing: border-box; color: #172033; }
.segmented { display: flex; background: #f1f5f9; border-radius: 14rpx; padding: 6rpx; }
.seg-item { flex: 1; text-align: center; height: 60rpx; line-height: 60rpx; border-radius: 12rpx; color: #475569; font-size: 24rpx; font-weight: 800; }
.seg-item.active { background: #fff; color: #4f46e5; box-shadow: 0 6rpx 18rpx rgba(15, 23, 42, 0.08); }
.primary-btn, .secondary-btn { margin: 0; height: 72rpx; line-height: 72rpx; padding: 0 24rpx; border-radius: 14rpx; border: none; font-size: 24rpx; font-weight: 800; }
.primary-btn { background: #4f46e5; color: #fff; }
.secondary-btn { background: #fff; color: #334155; border: 1rpx solid #e2e8f0; }
.full { width: 100%; }
.hint { display: block; margin-top: 18rpx; color: #94a3b8; font-size: 22rpx; line-height: 1.5; }
.section-head { display: flex; justify-content: space-between; align-items: center; }
.section-head.second { margin-top: 36rpx; }
.muted { color: #64748b; font-size: 24rpx; font-weight: 700; }
.member-row { display: flex; align-items: center; gap: 20rpx; padding: 24rpx 0; border-bottom: 1rpx solid #eef2f7; }
.avatar { width: 72rpx; height: 72rpx; border-radius: 18rpx; background: #eef2ff; color: #4f46e5; display: flex; align-items: center; justify-content: center; font-size: 28rpx; font-weight: 900; }
.avatar.green { background: #ecfdf5; color: #059669; }
.member-main { flex: 1; }
.member-name { display: block; font-size: 28rpx; font-weight: 900; }
.member-sub { display: block; margin-top: 5rpx; color: #64748b; font-size: 22rpx; }
.role-tag, .status-tag { padding: 8rpx 18rpx; border-radius: 999rpx; font-size: 22rpx; font-weight: 900; }
.role-tag { background: #eef2ff; color: #4f46e5; }
.status-tag.on { background: #dcfce7; color: #15803d; }
.status-tag.off { background: #fee2e2; color: #dc2626; }
.empty { padding: 70rpx 0; text-align: center; color: #94a3b8; }
.empty.small { padding: 30rpx 0; }
</style>
