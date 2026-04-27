<template>
  <view class="fc-page page">
    <view class="header-section">
      <view class="header-content">
        <text class="course-name">{{ courseName }}</text>
        <text class="page-title">课后反馈</text>
        <text class="page-subtitle">把这一节课的感受告诉老师，帮助下一节课讲得更准。</text>
      </view>
      <button class="refresh-btn" @click="loadMyFeedback">刷新</button>
    </view>

    <view class="feedback-card">
      <view class="input-group">
        <text class="label">本节课标题</text>
        <input v-model="form.lessonTitle" class="input" placeholder="例如：第一章 面向对象基础" />
      </view>

      <view class="input-group">
        <text class="label">课堂评分</text>
        <view class="star-row">
          <text
            v-for="star in 5"
            :key="star"
            :class="['star', form.rating >= star ? 'star-active' : '']"
            @click="form.rating = star"
          >★</text>
        </view>
      </view>

      <view class="input-group">
        <text class="label">掌握程度</text>
        <view class="mastery-row">
          <view
            v-for="item in masteryOptions"
            :key="item.value"
            :class="['mastery-chip', form.masteryLevel === item.value ? 'chip-active' : '']"
            @click="form.masteryLevel = item.value"
          >
            <text>{{ item.label }}</text>
          </view>
        </view>
      </view>

      <view class="input-group">
        <text class="label">反馈内容</text>
        <textarea
          v-model="form.content"
          class="textarea"
          placeholder="这节课哪里听懂了，哪里还不太清楚？"
          maxlength="-1"
        />
      </view>

      <view class="input-group">
        <text class="label">希望下节课重点讲</text>
        <textarea
          v-model="form.suggestion"
          class="textarea small-textarea"
          placeholder="例如：希望老师再讲接口和抽象类的区别"
          maxlength="-1"
        />
      </view>

      <button class="submit-btn" :disabled="submitting" @click="submitFeedback">
        {{ submitting ? '正在提交...' : '提交课后反馈' }}
      </button>
    </view>

    <view class="history-section">
      <view class="section-head">
        <text class="section-title">我的反馈记录</text>
        <text class="section-count">{{ feedbackList.length }} 条</text>
      </view>

      <view v-if="feedbackList.length" class="feedback-list">
        <view v-for="item in feedbackList" :key="item.id" class="history-card">
          <view class="history-top">
            <text class="history-title">{{ item.lessonTitle }}</text>
            <text class="rating-pill">{{ item.rating }} 星</text>
          </view>
          <text class="history-meta">{{ masteryLabel(item.masteryLevel) }} · {{ item.createdAt || '刚刚' }}</text>
          <text v-if="item.content" class="history-text">{{ item.content }}</text>
          <view v-if="item.suggestion" class="suggestion-box">
            <text class="suggestion-label">下节课建议</text>
            <text class="suggestion-text">{{ item.suggestion }}</text>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon">✨</text>
        <text class="empty-title">还没有提交反馈</text>
        <text class="empty-desc">完成本节课后，写一条反馈给老师吧。</text>
      </view>
    </view>
  </view>
</template>

<script>
import { createCourseFeedback, listMyCourseFeedback } from '@/api/feedback'
import { requireSession } from '@/common/session'

export default {
  data() {
    return {
      session: null,
      courseId: null,
      courseName: '',
      submitting: false,
      feedbackList: [],
      masteryOptions: [
        { label: '已掌握', value: 'MASTERED' },
        { label: '基本掌握', value: 'BASIC' },
        { label: '还不太懂', value: 'WEAK' }
      ],
      form: {
        lessonTitle: '',
        rating: 5,
        masteryLevel: 'BASIC',
        content: '',
        suggestion: ''
      }
    }
  },
  onLoad(query) {
    const session = requireSession()
    if (!session) return
    this.session = session
    this.courseId = Number(query.courseId)
    this.courseName = this.decodeCourseName(query.courseName) || '课程'
    this.loadMyFeedback()
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
    async loadMyFeedback() {
      if (!this.session || !this.courseId) return
      this.feedbackList = await listMyCourseFeedback(this.courseId, this.session.userId)
    },
    async submitFeedback() {
      if (!this.form.lessonTitle.trim()) {
        uni.showToast({ title: '请填写本节课标题', icon: 'none' })
        return
      }
      try {
        this.submitting = true
        await createCourseFeedback(this.courseId, {
          studentId: this.session.userId,
          lessonTitle: this.form.lessonTitle,
          rating: this.form.rating,
          masteryLevel: this.form.masteryLevel,
          content: this.form.content,
          suggestion: this.form.suggestion
        })
        uni.showToast({ title: '反馈已提交', icon: 'success' })
        this.form = {
          lessonTitle: '',
          rating: 5,
          masteryLevel: 'BASIC',
          content: '',
          suggestion: ''
        }
        this.loadMyFeedback()
      } finally {
        this.submitting = false
      }
    },
    masteryLabel(value) {
      const item = this.masteryOptions.find(option => option.value === value)
      return item ? item.label : '未填写'
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
  font-size: 48rpx;
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
  color: #7c3aed;
  font-size: 24rpx;
  font-weight: 800;
  box-shadow: 0 10rpx 24rpx rgba(45, 55, 72, 0.06);
}

.feedback-card,
.history-card {
  background: #ffffff;
  border-radius: 40rpx;
  padding: 36rpx;
  box-shadow: 0 12rpx 40rpx rgba(45, 55, 72, 0.05);
}

.input-group {
  margin-bottom: 30rpx;
}

.label {
  display: block;
  margin-bottom: 14rpx;
  font-size: 24rpx;
  color: #4a5568;
  font-weight: 800;
}

.input,
.textarea {
  width: 100%;
  box-sizing: border-box;
  background: #f8faff;
  border: 2rpx solid #edf2f7;
  border-radius: 24rpx;
  padding: 24rpx;
  font-size: 28rpx;
  color: #2d3748;
}

.input {
  height: 88rpx;
}

.textarea {
  height: 180rpx;
  line-height: 1.6;
}

.small-textarea {
  height: 130rpx;
}

.star-row {
  display: flex;
  gap: 14rpx;
}

.star {
  font-size: 50rpx;
  color: #e2e8f0;
  transition: transform 0.15s ease, color 0.15s ease;
}

.star-active {
  color: #f59e0b;
  transform: scale(1.05);
}

.mastery-row {
  display: flex;
  gap: 16rpx;
  flex-wrap: wrap;
}

.mastery-chip {
  padding: 18rpx 24rpx;
  border-radius: 999rpx;
  background: #f8faff;
  color: #718096;
  font-size: 24rpx;
  font-weight: 800;
  border: 2rpx solid #edf2f7;
}

.chip-active {
  background: #f5f3ff;
  color: #7c3aed;
  border-color: #c4b5fd;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border: none;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #fb923c, #f97316);
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 900;
  box-shadow: 0 16rpx 36rpx rgba(249, 115, 22, 0.22);
}

.submit-btn:active,
.refresh-btn:active,
.mastery-chip:active {
  transform: scale(0.98);
}

.history-section {
  margin-top: 42rpx;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.history-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.history-title {
  font-size: 30rpx;
  color: #2d3748;
  font-weight: 900;
}

.rating-pill {
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: #fff7ed;
  color: #f97316;
  font-size: 22rpx;
  font-weight: 900;
}

.history-meta {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #a0aec0;
}

.history-text {
  display: block;
  margin-top: 22rpx;
  font-size: 26rpx;
  line-height: 1.7;
  color: #4a5568;
}

.suggestion-box {
  margin-top: 22rpx;
  padding: 22rpx;
  border-radius: 24rpx;
  background: #f8faff;
}

.suggestion-label {
  display: block;
  font-size: 22rpx;
  color: #7c3aed;
  font-weight: 900;
  margin-bottom: 8rpx;
}

.suggestion-text {
  font-size: 25rpx;
  line-height: 1.6;
  color: #4a5568;
}

.empty-state {
  padding: 70rpx 40rpx;
  border-radius: 40rpx;
  background: #ffffff;
  text-align: center;
}

.empty-icon,
.empty-title,
.empty-desc {
  display: block;
}

.empty-icon {
  font-size: 70rpx;
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
</style>
