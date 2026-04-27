<template>
  <view class="fc-page page">
    <view class="header">
      <view class="header-left">
        <text class="header-label">学习空间</text>
        <text class="header-title">我的课程</text>
      </view>
      <view class="header-actions">
        <view class="action-item" @click="loadCourses">
          <text class="action-icon">🔄</text>
        </view>
        <view class="action-item danger" @click="handleLogout">
          <text class="action-icon">🚪</text>
        </view>
      </view>
    </view>

    <view class="search-section">
      <view class="search-bar">
        <text class="search-icon">🔍</text>
        <input
          v-model="keyword"
          class="search-input"
          placeholder="搜索课程名称或教师"
          placeholder-class="search-placeholder"
        />
      </view>
    </view>

    <view class="hero-banner">
      <view class="banner-content">
        <text class="banner-title">课程主线已接通</text>
        <text class="banner-desc">进入课程后，可继续查看资料、通知、签到、测验、作业和成绩。</text>
      </view>
      <text class="banner-emoji">🚀</text>
    </view>

    <LoadingState v-if="loading" text="正在同步课程列表..." />

    <ErrorState v-else-if="error" @retry="loadCourses" />

    <view v-else-if="filteredCourses.length" class="course-list">
      <view
        v-for="(course, index) in filteredCourses"
        :key="course.id"
        :class="['course-card', `card-style-${index % 4}`]"
        @click="openCourse(course)"
      >
        <view class="card-main">
          <view class="course-info">
            <text class="course-name">{{ course.courseName }}</text>
            <view class="teacher-info">
              <text class="teacher-icon">👤</text>
              <text class="teacher-name">{{ course.teacherName || '任课教师' }}</text>
            </view>
          </view>
          <view class="course-tag">{{ course.term || '本学期' }}</view>
        </view>
        
        <view class="card-footer">
          <view class="module-pills">
            <text class="module-pill">课前</text>
            <text class="module-pill">课中</text>
            <text class="module-pill">课后</text>
          </view>
          <view v-if="isJoined(course)" class="enter-btn">
            <text class="enter-text">进入学习</text>
            <text class="enter-icon">➜</text>
          </view>
          <view v-else class="enter-btn join-btn" @click.stop="requestJoin(course)">
            <text class="enter-text">申请加入</text>
          </view>
        </view>
      </view>
    </view>

    <EmptyState
      v-else
      icon="📚"
      title="暂无加入的课程"
      desc="请等待教师将您加入课程，或尝试更换搜索词。"
    />
  </view>
</template>

<script>
import { listCourses } from '@/api/course'
import { createReview } from '@/api/review'
import { clearSession, requireSession } from '@/common/session'
import LoadingState from '@/components/LoadingState.vue'
import EmptyState from '@/components/EmptyState.vue'
import ErrorState from '@/components/ErrorState.vue'

export default {
  components: {
    LoadingState,
    EmptyState,
    ErrorState
  },
  data() {
    return {
      session: null,
      courses: [],
      joinedIds: [],
      keyword: '',
      loading: false,
      error: false
    }
  },
  computed: {
    filteredCourses() {
      const keyword = (this.keyword || '').trim().toLowerCase()
      if (!keyword) {
        return this.courses
      }
      return this.courses.filter((course) => {
        return [course.courseName, course.courseCode, course.teacherName, course.term]
          .filter(Boolean)
          .some((item) => String(item).toLowerCase().includes(keyword))
      })
    }
  },
  onLoad() {
    const session = requireSession()
    if (!session || session.role !== 'student') {
      return
    }
    this.session = session
    this.loadCourses()
  },
  methods: {
    async loadCourses() {
      this.loading = true
      this.error = false
      try {
        const [allCourses, joinedCourses] = await Promise.all([
          listCourses(),
          listCourses({ memberUserId: this.session.userId })
        ])
        this.courses = allCourses || []
        this.joinedIds = (joinedCourses || []).map(item => Number(item.id))
      } catch (error) {
        this.courses = []
        this.error = true
      } finally {
        this.loading = false
      }
    },
    openCourse(course) {
      if (!this.isJoined(course)) {
        this.requestJoin(course)
        return
      }
      const courseName = encodeURIComponent(course.courseName || '')
      uni.navigateTo({
        url: `/pages/student/course_dashboard?courseId=${course.id}&courseName=${courseName}`
      })
    },
    isJoined(course) {
      return this.joinedIds.includes(Number(course.id))
    },
    async requestJoin(course) {
      await createReview({
        targetType: 'COURSE_MEMBER',
        targetId: course.id,
        actionType: 'JOIN',
        requesterId: this.session.userId,
        title: `申请加入课程：${course.courseName}`,
        summary: `学生 ${this.session.realName || this.session.username} 申请加入 ${course.courseName}`,
        payload: `userId=${this.session.userId};role=student`
      })
      uni.showToast({ title: '已提交加入申请', icon: 'none' })
    },
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            clearSession()
            uni.reLaunch({
              url: '/pages/login/index'
            })
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.page {
  background: #f8faff;
  min-height: 100vh;
  padding: 80rpx 40rpx 60rpx;
}

/* Header Styles */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
}

.header-label {
  display: block;
  font-size: 24rpx;
  color: #a0aec0;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 2rpx;
}

.header-title {
  display: block;
  font-size: 56rpx;
  font-weight: 900;
  color: #1a202c;
  margin-top: 8rpx;
}

.header-actions {
  display: flex;
  gap: 20rpx;
}

.action-item {
  width: 90rpx;
  height: 90rpx;
  background: #ffffff;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.05);
}

.action-item.danger {
  background: #fff5f5;
}

.action-icon {
  font-size: 40rpx;
}

/* Search Section */
.search-section {
  margin-bottom: 40rpx;
}

.search-bar {
  background: #ffffff;
  height: 110rpx;
  border-radius: 35rpx;
  display: flex;
  align-items: center;
  padding: 0 36rpx;
  box-shadow: 0 10rpx 40rpx rgba(0,0,0,0.02);
  border: 4rpx solid #ffffff;
}

.search-bar:focus-within {
  border-color: #e2e8f0;
}

.search-icon {
  font-size: 32rpx;
  margin-right: 20rpx;
}

.search-input {
  flex: 1;
  font-size: 30rpx;
  color: #2d3748;
  font-weight: 600;
}

.search-placeholder {
  color: #cbd5e0;
}

/* Hero Banner */
.hero-banner {
  background: #eff6ff;
  border-radius: 40rpx;
  padding: 40rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 50rpx;
  border: 4rpx solid #dbeafe;
}

.banner-content {
  flex: 1;
}

.banner-title {
  display: block;
  font-size: 32rpx;
  font-weight: 800;
  color: #1e40af;
}

.banner-desc {
  display: block;
  font-size: 24rpx;
  color: #3b82f6;
  margin-top: 10rpx;
  line-height: 1.6;
}

.banner-emoji {
  font-size: 70rpx;
}

/* Course List */
.course-list {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.course-card {
  background: #ffffff;
  border-radius: 45rpx;
  padding: 40rpx;
  box-shadow: 0 15rpx 45rpx rgba(0,0,0,0.04);
  display: flex;
  flex-direction: column;
  gap: 30rpx;
  border: 2rpx solid rgba(0,0,0,0.02);
}

.course-card:active {
  transform: scale(0.98);
}

.card-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.course-name {
  display: block;
  font-size: 40rpx;
  font-weight: 800;
  color: #2d3748;
  max-width: 400rpx;
}

.teacher-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 12rpx;
}

.teacher-icon { font-size: 24rpx; }

.teacher-name {
  font-size: 26rpx;
  color: #718096;
  font-weight: 600;
}

.course-tag {
  background: #f7fafc;
  padding: 10rpx 20rpx;
  border-radius: 15rpx;
  font-size: 20rpx;
  color: #718096;
  font-weight: 700;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20rpx;
  border-top: 2rpx dashed #edf2f7;
}

.module-pills {
  display: flex;
  gap: 12rpx;
}

.module-pill {
  font-size: 20rpx;
  font-weight: 800;
  color: #a0aec0;
  background: #f7fafc;
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
}

.enter-btn {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.join-btn {
  background: #eef2ff;
  color: #4f46e5;
}

.enter-text {
  font-size: 24rpx;
  font-weight: 800;
}

.enter-icon {
  font-size: 24rpx;
}

/* Color Variations */
.card-style-0 .enter-text, .card-style-0 .enter-icon { color: #6366f1; }
.card-style-1 .enter-text, .card-style-1 .enter-icon { color: #f59e0b; }
.card-style-2 .enter-text, .card-style-2 .enter-icon { color: #10b981; }
.card-style-3 .enter-text, .card-style-3 .enter-icon { color: #ec4899; }

.card-style-0 { border-left: 12rpx solid #6366f1; }
.card-style-1 { border-left: 12rpx solid #f59e0b; }
.card-style-2 { border-left: 12rpx solid #10b981; }
.card-style-3 { border-left: 12rpx solid #ec4899; }

</style>
