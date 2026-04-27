<template>
  <view class="admin-page">
    <view class="side-nav">
      <view class="brand">FlipEdu</view>
      <view class="nav-item" @click="go('/pages/admin/dashboard')">总览</view>
      <view class="nav-item" @click="go('/pages/admin/courses')">课程管理</view>
      <view class="nav-item" @click="go('/pages/admin/users')">用户管理</view>
      <view class="nav-item active">审核中心</view>
      <view class="nav-spacer"></view>
      <view class="user-line">{{ session.realName || '系统管理员' }}</view>
      <view class="logout" @click="logout">退出登录</view>
    </view>

    <view class="main">
      <view class="page-head">
        <view>
          <text class="page-title">审核中心</text>
          <text class="page-desc">统一处理课程、资料、公告、测验、作业和加入课程申请。</text>
        </view>
        <button class="secondary-btn" @click="loadReviews">刷新</button>
      </view>

      <view class="filters">
        <text
          v-for="item in statusOptions"
          :key="item.value"
          :class="['filter', statusFilter === item.value ? 'active' : '']"
          @click="changeStatus(item.value)"
        >{{ item.label }}</text>
      </view>

      <view class="review-list">
        <view v-for="item in reviews" :key="item.id" class="review-card">
          <view class="review-top">
            <view>
              <text class="review-title">{{ item.title }}</text>
              <text class="review-sub">{{ typeLabel(item.targetType) }} · {{ actionLabel(item.actionType) }} · 申请人：{{ item.requesterName || '-' }}</text>
            </view>
            <text :class="['status-tag', statusClass(item.status)]">{{ statusLabel(item.status) }}</text>
          </view>

          <text class="summary">{{ item.summary || '暂无申请说明' }}</text>
          <view v-if="item.payload" class="payload">{{ item.payload }}</view>
          <text class="time">提交时间：{{ item.createdAt || '-' }}</text>

          <view v-if="item.status === 'PENDING'" class="actions">
            <input v-model="item.commentDraft" class="comment-input" placeholder="审核意见，可选" />
            <button class="reject-btn" @click="submitDecision(item, 'REJECTED')">驳回</button>
            <button class="approve-btn" @click="submitDecision(item, 'APPROVED')">通过</button>
          </view>
          <view v-else class="reviewed">
            <text>审核意见：{{ item.reviewComment || '无' }}</text>
            <text>审核时间：{{ item.reviewedAt || '-' }}</text>
          </view>
        </view>
        <view v-if="!reviews.length" class="empty">暂无审核申请</view>
      </view>
    </view>
  </view>
</template>

<script>
import { listReviews, reviewRequest } from '@/api/review'
import { clearSession, requireSession } from '@/common/session'

export default {
  data() {
    return {
      session: {},
      reviews: [],
      statusFilter: 'PENDING',
      statusOptions: [
        { label: '待审核', value: 'PENDING' },
        { label: '已通过', value: 'APPROVED' },
        { label: '已驳回', value: 'REJECTED' },
        { label: '全部', value: '' }
      ]
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
    this.loadReviews()
  },
  methods: {
    async loadReviews() {
      const list = await listReviews(this.statusFilter)
      this.reviews = (list || []).map(item => ({ ...item, commentDraft: item.reviewComment || '' }))
    },
    changeStatus(status) {
      this.statusFilter = status
      this.loadReviews()
    },
    async submitDecision(item, status) {
      await reviewRequest(item.id, {
        status,
        reviewComment: item.commentDraft || ''
      })
      uni.showToast({ title: status === 'APPROVED' ? '已通过' : '已驳回', icon: 'success' })
      this.loadReviews()
    },
    typeLabel(type) {
      const map = {
        COURSE: '课程',
        MATERIAL: '教学资料',
        NOTICE: '课程公告',
        QUIZ: '课堂测验',
        ASSIGNMENT: '课后作业',
        COURSE_MEMBER: '课程成员'
      }
      return map[type] || type
    },
    actionLabel(action) {
      const map = {
        CREATE: '新建',
        UPDATE: '修改',
        PUBLISH: '发布',
        JOIN: '加入课程'
      }
      return map[action] || action
    },
    statusLabel(status) {
      const map = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }
      return map[status] || status
    },
    statusClass(status) {
      if (status === 'APPROVED') return 'approved'
      if (status === 'REJECTED') return 'rejected'
      return 'pending'
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
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 28rpx; gap: 24rpx; }
.page-title { display: block; font-size: 44rpx; font-weight: 900; }
.page-desc { display: block; margin-top: 8rpx; color: #64748b; font-size: 24rpx; }
.secondary-btn { margin: 0; height: 72rpx; line-height: 72rpx; padding: 0 24rpx; border-radius: 14rpx; background: #fff; color: #334155; border: 1rpx solid #e2e8f0; font-size: 24rpx; font-weight: 800; }
.filters { display: flex; gap: 14rpx; margin-bottom: 24rpx; }
.filter { padding: 14rpx 24rpx; border-radius: 999rpx; background: #fff; border: 1rpx solid #e2e8f0; color: #475569; font-size: 24rpx; font-weight: 800; }
.filter.active { background: #4f46e5; color: #fff; border-color: #4f46e5; }
.review-list { display: flex; flex-direction: column; gap: 20rpx; }
.review-card { background: #fff; border: 1rpx solid #e8ecf3; border-radius: 18rpx; padding: 30rpx; }
.review-top { display: flex; justify-content: space-between; gap: 20rpx; align-items: flex-start; }
.review-title { display: block; font-size: 31rpx; font-weight: 900; }
.review-sub { display: block; margin-top: 8rpx; color: #64748b; font-size: 23rpx; }
.status-tag { padding: 8rpx 18rpx; border-radius: 999rpx; font-size: 22rpx; font-weight: 900; white-space: nowrap; }
.status-tag.pending { background: #fef3c7; color: #b45309; }
.status-tag.approved { background: #dcfce7; color: #15803d; }
.status-tag.rejected { background: #fee2e2; color: #dc2626; }
.summary { display: block; margin-top: 22rpx; color: #334155; font-size: 25rpx; line-height: 1.6; }
.payload { margin-top: 16rpx; padding: 18rpx; border-radius: 14rpx; background: #f8fafc; color: #64748b; font-size: 22rpx; word-break: break-all; }
.time { display: block; margin-top: 16rpx; color: #94a3b8; font-size: 22rpx; }
.actions { display: grid; grid-template-columns: 1fr 150rpx 150rpx; gap: 14rpx; margin-top: 24rpx; align-items: center; }
.comment-input { height: 70rpx; background: #f8fafc; border: 1rpx solid #e2e8f0; border-radius: 14rpx; padding: 0 20rpx; font-size: 24rpx; box-sizing: border-box; }
.approve-btn, .reject-btn { margin: 0; height: 70rpx; line-height: 70rpx; border: none; border-radius: 14rpx; color: #fff; font-size: 24rpx; font-weight: 900; }
.approve-btn { background: #16a34a; }
.reject-btn { background: #dc2626; }
.reviewed { display: flex; flex-direction: column; gap: 8rpx; margin-top: 20rpx; color: #64748b; font-size: 23rpx; }
.empty { padding: 80rpx; text-align: center; color: #94a3b8; background: #fff; border-radius: 18rpx; }
</style>
