<template>
  <view class="fc-page page">
    <!-- 沉浸式顶部栏 -->
    <view class="top-nav">
      <view class="user-intro" @click="handleBack">
        <view class="avatar-box">
          <text class="avatar-emoji">👋</text>
          <view class="logout-indicator">
            <text class="logout-icon">↩</text>
          </view>
        </view>
        <view class="intro-text">
          <text class="greeting">你好, {{ studentName }} (点击返回)</text>
          <text class="course-name">{{ courseName }}</text>
        </view>
      </view>
      <view class="nav-actions">
        <view class="action-icon-btn" @click="navigateTo('/pages/student/notices')">
          <text class="icon">🔔</text>
          <view v-if="noticeCount > 0" class="dot"></view>
        </view>
      </view>
    </view>

    <!-- 任务 Hero Card (KidZo 风格) -->
    <view class="hero-section">
      <view class="hero-card" @click="navigateTo('/pages/student/assignments')">
        <view class="hero-content">
          <view class="hero-badge">今日挑战</view>
          <text class="hero-title">课后作业</text>
          <text class="hero-desc">完成本次作业可获得 {{ assignmentCount }} 份记录更新</text>
          <button class="hero-btn">立即前往</button>
        </view>
        <view class="hero-img">
          <text class="emoji-3d">🧩</text>
        </view>
      </view>
    </view>

    <!-- 功能网格 (Categories) -->
    <view class="ai-entry-card" @click="showAi = true">
      <view>
        <text class="ai-entry-title">AI 学习助手</text>
        <text class="ai-entry-desc">总结重点、解释题目、整理复习提纲</text>
      </view>
      <text class="ai-entry-action">打开</text>
    </view>

    <view class="category-section">
      <text class="section-title">学习模块</text>
      <view class="category-grid">
        <view class="cat-card bg-orange" @click="navigateTo('/pages/student/materials')">
          <view class="cat-icon-box">
            <text class="cat-icon">📚</text>
          </view>
          <text class="cat-name">教学资料</text>
        </view>

        <view class="cat-card bg-blue" @click="navigateTo('/pages/student/signs')">
          <view class="cat-icon-box">
            <text class="cat-icon">📍</text>
          </view>
          <text class="cat-name">课堂签到</text>
        </view>

        <view class="cat-card bg-pink" @click="navigateTo('/pages/student/quizzes')">
          <view class="cat-icon-box">
            <text class="cat-icon">✍️</text>
          </view>
          <text class="cat-name">随堂测验</text>
        </view>

        <view class="cat-card bg-mint" @click="navigateTo('/pages/student/feedback')">
          <view class="cat-icon-box">
            <text class="cat-icon">💬</text>
          </view>
          <text class="cat-name">课后反馈</text>
        </view>

        <view class="cat-card bg-purple" @click="showAi = true">
          <view class="cat-icon-box">
            <text class="cat-icon">🤖</text>
          </view>
          <text class="cat-name">AI 助手</text>
        </view>
      </view>
    </view>

    <!-- 学习进度 (Learning Progress) -->
    <view class="progress-section">
      <text class="section-title">学习任务进度 ✨</text>
      <view class="progress-list">
        <view class="progress-item" @click="navigateTo('/pages/student/assignments')">
          <view class="progress-info">
            <view class="info-left">
              <text class="info-icon">📝</text>
              <text class="info-label">作业提交完成度</text>
            </view>
            <text class="info-percent">{{ assignmentProgress }}%</text>
          </view>
          <view class="progress-bar-bg">
            <view class="progress-bar-fill bg-gradient-cyan" :style="{ width: assignmentProgress + '%' }"></view>
          </view>
        </view>

        <view class="progress-item" @click="navigateTo('/pages/student/scores')">
          <view class="progress-info">
            <view class="info-left">
              <text class="info-icon">📊</text>
              <text class="info-label">成绩反馈查阅</text>
            </view>
            <text class="info-percent">已更新</text>
          </view>
          <view class="progress-bar-bg">
            <view class="progress-bar-fill bg-gradient-purple" style="width: 100%"></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 全局 AI 助手 -->
    <ai-assistant-drawer 
      :visible="showAi" 
      :course-id="courseId" 
      :course-name="courseName"
      @close="showAi = false"
    />
  </view>
</template>

<script>
import { getSession, requireSession, clearSession } from '@/common/session'
import { listMaterials } from '@/api/material'
import { listNotices } from '@/api/notice'
import { listAssignments, listAssignmentSubmissions } from '@/api/assignment'
import AiAssistantDrawer from '@/components/AiAssistantDrawer.vue'

export default {
  components: {
    AiAssistantDrawer
  },
  data() {
    return {
      courseId: null,
      courseName: '加载中...',
      studentName: '',
      assignmentCount: 0,
      assignmentProgress: 0,
      noticeCount: 0,
      materialCount: 0,
      showAi: false
    }
  },
  onLoad(query) {
    const session = requireSession()
    if (!session) return
    this.studentName = session.realName || session.username
    this.courseId = Number(query.courseId)
    this.courseName = this.decodeCourseName(query.courseName) || '课程详情'
    this.loadStats()
  },
  onShow() {
    if (this.courseId) {
      this.loadStats()
    }
  },
  methods: {
    decodeCourseName(value) {
      if (!value) return ''
      try {
        return decodeURIComponent(value)
      } catch (e) {
        return value
      }
    },
    async loadStats() {
      const session = getSession()
      const settle = promise => promise
        .then(value => ({ status: 'fulfilled', value }))
        .catch(error => ({ status: 'rejected', error }))
      const results = await Promise.all([
        listMaterials(this.courseId),
        listNotices(this.courseId),
        this.loadAssignmentProgress(session.userId)
      ].map(settle))
      const [materialsResult, noticesResult, assignmentsResult] = results

      if (materialsResult.status === 'fulfilled') {
        this.materialCount = (materialsResult.value || []).length
      }
      if (noticesResult.status === 'fulfilled') {
        this.noticeCount = (noticesResult.value || []).length
      }
      if (assignmentsResult.status === 'fulfilled') {
        const assignments = assignmentsResult.value || []
        this.assignmentCount = assignments.length
        const completed = assignments.filter(a => a.submitted).length
        this.assignmentProgress = assignments.length > 0 
          ? Math.round((completed / assignments.length) * 100) 
          : 0
      }

      if (results.some(item => item.status === 'rejected')) {
        uni.showToast({ title: '部分统计加载失败', icon: 'none' })
      }
    },
    async loadAssignmentProgress(userId) {
      const assignments = await listAssignments(this.courseId)
      const publishedAssignments = (assignments || []).filter(item => item.published === 1)
      this.assignmentCount = publishedAssignments.length

      if (!publishedAssignments.length) {
        this.assignmentProgress = 0
        return []
      }

      const submissionResults = await Promise.all(
        publishedAssignments.map(item => listAssignmentSubmissions(item.id).catch(() => []))
      )
      const completedCount = submissionResults.filter(records => {
        return (records || []).some(record => Number(record.studentId) === Number(userId))
      }).length

      this.assignmentProgress = Math.round((completedCount / publishedAssignments.length) * 100)
      return publishedAssignments.map((assignment, index) => ({
        ...assignment,
        submitted: (submissionResults[index] || []).some(record => Number(record.studentId) === Number(userId))
      }))
    },
    navigateTo(path) {
      const courseName = encodeURIComponent(this.courseName || '')
      uni.navigateTo({
        url: `${path}?courseId=${this.courseId}&courseName=${courseName}`
      })
    },
    handleBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
.page {
  background: #f8faff;
  min-height: 100vh;
  padding-bottom: 60rpx;
}

/* 顶部栏 */
.top-nav {
  padding: 100rpx 40rpx 40rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  border-radius: 0 0 60rpx 60rpx;
  box-shadow: 0 10rpx 40rpx rgba(0,0,0,0.03);
}

.user-intro {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.avatar-box {
  width: 90rpx;
  height: 90rpx;
  background: #f0f4ff;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.logout-indicator {
  position: absolute;
  bottom: -4rpx;
  right: -4rpx;
  width: 32rpx;
  height: 32rpx;
  background: #ef4444;
  border: 4rpx solid #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logout-icon { color: #ffffff; font-size: 18rpx; font-weight: 900; }

.avatar-emoji { font-size: 48rpx; }

.greeting {
  display: block;
  font-size: 24rpx;
  color: #a0aec0;
  font-weight: 600;
}

.course-name {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  color: #2d3748;
  margin-top: 4rpx;
}

.nav-actions {
  display: flex;
  gap: 20rpx;
}

.action-icon-btn {
  width: 84rpx;
  height: 84rpx;
  background: #ffffff;
  border: 4rpx solid #f7fafc;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.icon { font-size: 36rpx; }

.dot {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  width: 16rpx;
  height: 16rpx;
  background: #f56565;
  border: 4rpx solid #ffffff;
  border-radius: 50%;
}

/* Hero Section */
.hero-section {
  padding: 40rpx;
}

.hero-card {
  background: linear-gradient(135deg, #7c3aed, #a855f7);
  border-radius: 50rpx;
  padding: 40rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 20rpx 60rpx rgba(124, 58, 237, 0.25);
  position: relative;
  overflow: hidden;
}

.hero-card::after {
  content: "";
  position: absolute;
  right: -20rpx;
  top: -20rpx;
  width: 200rpx;
  height: 200rpx;
  background: rgba(255,255,255,0.1);
  border-radius: 50%;
}

.hero-content {
  position: relative;
  z-index: 2;
  flex: 1;
}

.hero-badge {
  display: inline-block;
  background: rgba(255,255,255,0.2);
  padding: 8rpx 24rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  color: #ffffff;
  font-weight: 700;
  margin-bottom: 20rpx;
}

.hero-title {
  display: block;
  font-size: 48rpx;
  font-weight: 800;
  color: #ffffff;
}

.hero-desc {
  display: block;
  font-size: 24rpx;
  color: rgba(255,255,255,0.8);
  margin-top: 12rpx;
  margin-bottom: 30rpx;
}

.hero-btn {
  background: #ffffff;
  color: #7c3aed;
  font-size: 24rpx;
  font-weight: 800;
  padding: 16rpx 40rpx;
  border-radius: 20rpx;
  border: none;
  display: inline-block;
  margin: 0;
}

.hero-img {
  width: 160rpx;
  height: 160rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.emoji-3d { font-size: 100rpx; }

.ai-entry-card {
  margin: 0 40rpx 40rpx;
  background: #111827;
  color: #ffffff;
  border-radius: 28rpx;
  padding: 28rpx 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 16rpx 34rpx rgba(15, 23, 42, 0.16);
}

.ai-entry-title {
  display: block;
  font-size: 30rpx;
  font-weight: 900;
}

.ai-entry-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 23rpx;
  color: rgba(255, 255, 255, 0.72);
}

.ai-entry-action {
  font-size: 24rpx;
  font-weight: 900;
  background: #ffffff;
  color: #111827;
  padding: 12rpx 22rpx;
  border-radius: 999rpx;
}

/* Grid Section */
.section-title {
  display: block;
  padding: 0 40rpx;
  font-size: 32rpx;
  font-weight: 800;
  color: #2d3748;
  margin-bottom: 30rpx;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30rpx;
  padding: 0 40rpx;
}

.cat-card {
  padding: 30rpx;
  border-radius: 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
}

.cat-icon-box {
  width: 100rpx;
  height: 100rpx;
  background: #ffffff;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 20rpx rgba(0,0,0,0.05);
}

.cat-icon { font-size: 48rpx; }

.cat-name {
  font-size: 28rpx;
  font-weight: 700;
  color: #2d3748;
}

.bg-orange { background: #fff7ed; }
.bg-blue { background: #eff6ff; }
.bg-pink { background: #fdf2f8; }
.bg-mint { background: #ecfdf5; }
.bg-purple { background: #f5f3ff; }

/* Progress Section */
.progress-section {
  margin-top: 50rpx;
}

.progress-list {
  padding: 0 40rpx;
}

.progress-item {
  background: #ffffff;
  padding: 30rpx;
  border-radius: 40rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.02);
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.info-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.info-icon { font-size: 32rpx; }

.info-label {
  font-size: 26rpx;
  font-weight: 700;
  color: #4a5568;
}

.info-percent {
  font-size: 26rpx;
  font-weight: 800;
  color: #7c3aed;
}

.progress-bar-bg {
  height: 20rpx;
  background: #f1f5f9;
  border-radius: 999rpx;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  border-radius: 999rpx;
  transition: width 0.6s ease;
}

.bg-gradient-cyan { background: linear-gradient(90deg, #22d3ee, #06b6d4); }
.bg-gradient-purple { background: linear-gradient(90deg, #a855f7, #7c3aed); }
</style>
