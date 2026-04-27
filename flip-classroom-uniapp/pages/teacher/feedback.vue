<template>
  <view class="fc-page page">
    <view class="header-section">
      <view class="header-content">
        <text class="course-name">{{ courseName }}</text>
        <text class="page-title">课后反馈分析</text>
        <text class="page-subtitle">查看学生对课堂内容的掌握情况和改进建议。</text>
      </view>
      <button class="refresh-btn" :disabled="loading" @click="loadFeedback">
        {{ loading ? '...' : '刷新' }}
      </button>
    </view>

    <view class="summary-grid">
      <view class="summary-card">
        <text class="summary-label">反馈数量</text>
        <text class="summary-value">{{ summary.totalCount || 0 }}</text>
      </view>
      <view class="summary-card accent-card">
        <text class="summary-label">平均评分</text>
        <text class="summary-value">{{ summary.averageRating || '0.0' }}</text>
      </view>
    </view>

    <view class="mastery-panel">
      <view class="panel-head">
        <text class="panel-title">掌握情况</text>
        <text class="panel-subtitle">根据学生自评统计</text>
      </view>
      <view class="mastery-stats">
        <view class="mastery-item">
          <text class="mastery-dot dot-green"></text>
          <text class="mastery-label">已掌握</text>
          <text class="mastery-value">{{ summary.masteredCount || 0 }}</text>
        </view>
        <view class="mastery-item">
          <text class="mastery-dot dot-blue"></text>
          <text class="mastery-label">基本掌握</text>
          <text class="mastery-value">{{ summary.basicCount || 0 }}</text>
        </view>
        <view class="mastery-item">
          <text class="mastery-dot dot-orange"></text>
          <text class="mastery-label">还不太懂</text>
          <text class="mastery-value">{{ summary.weakCount || 0 }}</text>
        </view>
      </view>
    </view>

    <view class="list-section">
      <view class="section-head">
        <text class="section-title">反馈明细</text>
        <text class="section-count">{{ feedbackList.length }} 条</text>
      </view>

      <view v-if="feedbackList.length" class="feedback-list">
        <view v-for="item in feedbackList" :key="item.id" class="feedback-card">
          <view class="feedback-top">
            <view>
              <text class="lesson-title">{{ item.lessonTitle }}</text>
              <text class="student-line">{{ item.studentName || '学生' }} · {{ item.studentUsername || 'unknown' }}</text>
            </view>
            <view class="rating-box">
              <text class="rating-value">{{ item.rating }}</text>
              <text class="rating-unit">星</text>
            </view>
          </view>

          <view class="tag-row">
            <text :class="['mastery-tag', masteryClass(item.masteryLevel)]">{{ masteryLabel(item.masteryLevel) }}</text>
            <text class="time-tag">{{ item.createdAt || '刚刚' }}</text>
          </view>

          <text v-if="item.content" class="feedback-text">{{ item.content }}</text>
          <view v-if="item.suggestion" class="suggestion-box">
            <text class="suggestion-title">学生希望下节课重点讲</text>
            <text class="suggestion-text">{{ item.suggestion }}</text>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon">💬</text>
        <text class="empty-title">还没有学生反馈</text>
        <text class="empty-desc">学生提交课后反馈后，会在这里形成列表和统计。</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getCourseFeedbackSummary, listCourseFeedback } from '@/api/feedback'
import { requireSession } from '@/common/session'

export default {
  data() {
    return {
      courseId: null,
      courseName: '',
      loading: false,
      summary: {
        totalCount: 0,
        averageRating: 0,
        masteredCount: 0,
        basicCount: 0,
        weakCount: 0
      },
      feedbackList: []
    }
  },
  onLoad(query) {
    const session = requireSession()
    if (!session) return
    this.courseId = Number(query.courseId)
    this.courseName = this.decodeCourseName(query.courseName) || '课程'
    this.loadFeedback()
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
    async loadFeedback() {
      this.loading = true
      try {
        const [summary, list] = await Promise.all([
          getCourseFeedbackSummary(this.courseId),
          listCourseFeedback(this.courseId)
        ])
        this.summary = summary || {}
        this.feedbackList = list || []
      } finally {
        this.loading = false
      }
    },
    masteryLabel(value) {
      const map = {
        MASTERED: '已掌握',
        BASIC: '基本掌握',
        WEAK: '还不太懂'
      }
      return map[value] || '未填写'
    },
    masteryClass(value) {
      const map = {
        MASTERED: 'tag-green',
        BASIC: 'tag-blue',
        WEAK: 'tag-orange'
      }
      return map[value] || 'tag-gray'
    }
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f8faff;
  padding: 100rpx 40rpx 60rpx;
}

.header-section {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24rpx;
  margin-bottom: 32rpx;
}

.course-name {
  display: block;
  font-size: 24rpx;
  color: #a0aec0;
  font-weight: 700;
}

.page-title {
  display: block;
  margin-top: 6rpx;
  font-size: 46rpx;
  font-weight: 900;
  color: #2d3748;
}

.page-subtitle {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: #718096;
}

.refresh-btn {
  height: 66rpx;
  line-height: 66rpx;
  padding: 0 26rpx;
  border: none;
  border-radius: 999rpx;
  background: #ffffff;
  color: #f97316;
  font-size: 24rpx;
  font-weight: 900;
  box-shadow: 0 10rpx 24rpx rgba(45, 55, 72, 0.06);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
  margin-bottom: 24rpx;
}

.summary-card,
.mastery-panel,
.feedback-card,
.empty-state {
  background: #ffffff;
  border-radius: 36rpx;
  box-shadow: 0 12rpx 40rpx rgba(45, 55, 72, 0.05);
}

.summary-card {
  padding: 32rpx;
}

.accent-card {
  background: linear-gradient(135deg, #fff7ed, #ffffff);
}

.summary-label {
  display: block;
  font-size: 22rpx;
  color: #a0aec0;
  font-weight: 800;
}

.summary-value {
  display: block;
  margin-top: 10rpx;
  font-size: 52rpx;
  color: #2d3748;
  font-weight: 900;
}

.mastery-panel {
  padding: 32rpx;
  margin-bottom: 42rpx;
}

.panel-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.panel-title {
  font-size: 30rpx;
  color: #2d3748;
  font-weight: 900;
}

.panel-subtitle {
  font-size: 22rpx;
  color: #a0aec0;
}

.mastery-stats {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.mastery-item {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.mastery-dot {
  width: 18rpx;
  height: 18rpx;
  border-radius: 50%;
}

.dot-green { background: #10b981; }
.dot-blue { background: #3b82f6; }
.dot-orange { background: #f97316; }

.mastery-label {
  flex: 1;
  font-size: 26rpx;
  color: #4a5568;
  font-weight: 800;
}

.mastery-value {
  font-size: 28rpx;
  color: #2d3748;
  font-weight: 900;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22rpx;
}

.section-title {
  font-size: 32rpx;
  color: #2d3748;
  font-weight: 900;
}

.section-count {
  font-size: 22rpx;
  color: #a0aec0;
  font-weight: 700;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.feedback-card {
  padding: 34rpx;
}

.feedback-top {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.lesson-title {
  display: block;
  font-size: 30rpx;
  color: #2d3748;
  font-weight: 900;
}

.student-line {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #a0aec0;
  font-weight: 700;
}

.rating-box {
  min-width: 88rpx;
  height: 88rpx;
  border-radius: 28rpx;
  background: #fff7ed;
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4rpx;
}

.rating-value {
  font-size: 40rpx;
  color: #f97316;
  font-weight: 900;
}

.rating-unit {
  font-size: 20rpx;
  color: #f97316;
  font-weight: 800;
}

.tag-row {
  display: flex;
  gap: 14rpx;
  margin-top: 22rpx;
  flex-wrap: wrap;
}

.mastery-tag,
.time-tag {
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 900;
}

.tag-green { background: #ecfdf5; color: #10b981; }
.tag-blue { background: #eff6ff; color: #3b82f6; }
.tag-orange { background: #fff7ed; color: #f97316; }
.tag-gray { background: #f8faff; color: #a0aec0; }
.time-tag { background: #f8faff; color: #a0aec0; }

.feedback-text {
  display: block;
  margin-top: 24rpx;
  font-size: 26rpx;
  line-height: 1.7;
  color: #4a5568;
}

.suggestion-box {
  margin-top: 24rpx;
  padding: 24rpx;
  border-radius: 26rpx;
  background: #f8faff;
}

.suggestion-title {
  display: block;
  margin-bottom: 8rpx;
  font-size: 22rpx;
  color: #7c3aed;
  font-weight: 900;
}

.suggestion-text {
  font-size: 25rpx;
  line-height: 1.6;
  color: #4a5568;
}

.empty-state {
  padding: 80rpx 40rpx;
  text-align: center;
}

.empty-icon,
.empty-title,
.empty-desc {
  display: block;
}

.empty-icon {
  font-size: 76rpx;
}

.empty-title {
  margin-top: 18rpx;
  font-size: 30rpx;
  font-weight: 900;
  color: #2d3748;
}

.empty-desc {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #a0aec0;
}

.refresh-btn:active,
.feedback-card:active {
  transform: scale(0.99);
}
</style>
