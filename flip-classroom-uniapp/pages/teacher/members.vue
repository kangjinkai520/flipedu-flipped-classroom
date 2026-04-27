<template>
  <view class="fc-page page">
    <!-- 情况 A：尚未选择课程 - 展示精美的课程网格 -->
    <view v-if="!courseId" class="selection-screen animated-fade-in">
      <view class="page-intro">
        <text class="intro-eyebrow">COURSE MANAGEMENT</text>
        <text class="intro-title">请选择管理目标</text>
        <text class="intro-subtitle">您需要指定一门课程来查看其成员组成</text>
      </view>

      <view class="course-grid">
        <view 
          v-for="course in teacherCourses" 
          :key="course.id" 
          class="course-select-card"
          @click="selectCourse(course)"
        >
          <view class="card-icon">📚</view>
          <view class="card-info">
            <text class="card-name">{{ course.courseName }}</text>
            <text class="card-code">{{ course.courseCode }}</text>
          </view>
          <view class="card-arrow">➜</view>
        </view>
      </view>

      <view v-if="!teacherCourses.length" class="empty-loader">
        <text class="loader-text">正在检索您的课程目录...</text>
      </view>
    </view>

    <!-- 情况 B：已选择课程 - 展示成员列表 -->
    <view v-else class="content-screen animated-fade-in">
      <!-- 顶部紧凑切换条 -->
      <view class="top-nav">
        <scroll-view class="nav-scroll" scroll-x enable-flex>
          <view 
            v-for="course in teacherCourses" 
            :key="course.id" 
            :class="['nav-pill', course.id === courseId ? 'nav-pill-active' : '']"
            @click="selectCourse(course)"
          >
            {{ course.courseName }}
          </view>
        </scroll-view>
        <view class="nav-shadow"></view>
      </view>

      <!-- KPI 概览 -->
      <view class="kpi-group">
        <view class="kpi-box">
          <text class="kpi-val">{{ members.length }}</text>
          <text class="kpi-lab">总人数</text>
        </view>
        <view class="kpi-box">
          <text class="kpi-val blue">{{ studentCount }}</text>
          <text class="kpi-lab">学生</text>
        </view>
        <view class="kpi-box">
          <text class="kpi-val purple">{{ teacherCount }}</text>
          <text class="kpi-lab">教师组</text>
        </view>
        <view class="action-trigger" @click="toggleEditor">
          <text class="trigger-icon">{{ showEditor ? '×' : '＋' }}</text>
        </view>
      </view>

      <!-- 快速编辑器 -->
      <view v-if="showEditor" class="floating-editor animated-slide-down">
        <view class="editor-inner">
          <text class="editor-label">邀请新成员 (USER ID)</text>
          <view class="input-row">
            <input
              :value="form.userId"
              type="text"
              inputmode="numeric"
              class="saas-input"
              placeholder="输入学生 ID"
              @input="updateMemberField"
            />
            <view class="role-toggle" @click="form.role = form.role === 'student' ? 'teacher' : 'student'">
              {{ form.role === 'student' ? '学生' : '教师' }}
            </view>
            <view class="confirm-btn" @click="submitMember">发送</view>
          </view>
        </view>
      </view>

      <!-- 搜索栏 -->
      <view class="search-section">
        <view class="glass-search">
          <text class="si">🔍</text>
          <input v-model="searchKey" class="si-input" placeholder="搜索姓名或学号" />
        </view>
      </view>

      <!-- 成员列表 -->
      <view class="member-stack">
        <view v-if="filteredMembers.length" class="stack-inner">
          <view v-for="item in filteredMembers" :key="item.id" class="user-row">
            <view :class="['u-avatar', getAvatarColor(item.realName)]">
              {{ item.realName ? item.realName.charAt(0) : '?' }}
            </view>
            <view class="u-info">
              <text class="u-name">{{ item.realName }}</text>
              <text class="u-id">@{{ item.username }}</text>
            </view>
            <view :class="['u-tag', item.role === 'teacher' ? 't-purple' : 't-blue']">
              {{ roleLabel(item.role || item.userRole) }}
            </view>
          </view>
        </view>
        <view v-else class="empty-placeholder">
          <text class="ep-icon">🍃</text>
          <text class="ep-text">暂无数据记录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { addCourseMember, listCourseMembers, listCourses } from '@/api/course'
import { requireSession } from '@/common/session'

export default {
  data() {
    return {
      session: null,
      courseId: null,
      teacherCourses: [],
      members: [],
      showEditor: false,
      searchKey: '',
      form: { userId: '', role: 'student' }
    }
  },
  computed: {
    studentCount() { return this.members.filter(m => (m.role || m.userRole) === 'student').length },
    teacherCount() { return this.members.filter(m => (m.role || m.userRole) === 'teacher').length },
    filteredMembers() {
      if (!this.searchKey) return this.members
      const key = this.searchKey.toLowerCase()
      return this.members.filter(m => 
        (m.realName && m.realName.toLowerCase().includes(key)) || 
        (m.username && m.username.toLowerCase().includes(key))
      )
    }
  },
  async onLoad(query) {
    this.session = requireSession()
    if (!this.session) return
    await this.loadTeacherCourses()
    if (query.courseId) {
      this.courseId = Number(query.courseId)
      this.loadMembers()
    }
  },
  methods: {
    async loadTeacherCourses() {
      try {
        this.teacherCourses = await listCourses({ teacherId: this.session.userId })
      } catch (e) {
        uni.showToast({ title: '加载失败', icon: 'none' })
      }
    },
    selectCourse(course) {
      this.courseId = course.id
      this.loadMembers()
      this.showEditor = false
      uni.showToast({ title: `已切换至 ${course.courseName}`, icon: 'none' })
    },
    roleLabel(role) {
      const map = { student: '学生', teacher: '教师', admin: '管理员' }
      return map[role] || role
    },
    updateMemberField(event) {
      this.form.userId = event.detail.value
    },
    toggleEditor() { this.showEditor = !this.showEditor },
    async loadMembers() {
      if (!this.courseId) return
      this.members = await listCourseMembers(this.courseId)
    },
    async submitMember() {
      if (!this.form.userId) return
      try {
        await addCourseMember(this.courseId, { userId: Number(this.form.userId), role: this.form.role })
        uni.showToast({ title: '成功', icon: 'success' })
        this.form.userId = ''
        this.showEditor = false
        this.loadMembers()
      } catch (e) {
        uni.showToast({ title: '失败', icon: 'none' })
      }
    },
    getAvatarColor(name) {
      if (!name) return 'bg-gray'
      const colors = ['bg-blue', 'bg-green', 'bg-orange', 'bg-red', 'bg-indigo', 'bg-cyan']
      return colors[name.charCodeAt(0) % colors.length]
    }
  }
}
</script>

<style scoped>
.page { padding: 40rpx; background: #FAF7F2; min-height: 100vh; }

/* 1. 选择屏幕 */
.page-intro { padding: 40rpx 0 60rpx; }
.intro-eyebrow { font-size: 22rpx; font-weight: 800; color: #C5BDB0; letter-spacing: 4rpx; display: block; }
.intro-title { font-size: 60rpx; font-weight: 900; color: #1A1A1A; display: block; margin-top: 10rpx; }
.intro-subtitle { font-size: 28rpx; color: #A09688; margin-top: 20rpx; display: block; font-weight: 600; }

.course-grid { display: flex; flex-direction: column; gap: 30rpx; }
.course-select-card {
  background: #ffffff;
  padding: 50rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  border: 2rpx solid #F0EAE2;
  transition: all 0.2s;
}
.course-select-card:active { transform: scale(0.97); background: #FDFBF7; }
.card-icon { font-size: 60rpx; margin-right: 40rpx; }
.card-info { flex: 1; }
.card-name { font-size: 36rpx; font-weight: 900; color: #1A1A1A; display: block; }
.card-code { font-size: 24rpx; color: #B5A99A; font-weight: 700; margin-top: 6rpx; display: block; }
.card-arrow { font-size: 32rpx; color: #EAE2D9; }

/* 2. 列表内容屏 */
.top-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  margin: -40rpx -40rpx 40rpx;
  background: #FAF7F2;
  padding: 40rpx 40rpx 20rpx;
}
.nav-scroll { display: flex; white-space: nowrap; gap: 20rpx; height: 90rpx; }
.nav-pill {
  display: inline-block;
  padding: 0 40rpx;
  height: 80rpx;
  line-height: 80rpx;
  background: #ffffff;
  border-radius: 40rpx;
  font-size: 26rpx;
  font-weight: 800;
  color: #8E8E93;
  border: 2rpx solid #F0EAE2;
  margin-right: 20rpx;
  transition: all 0.3s;
}
.nav-pill-active { background: #1A1A1A; color: #ffffff; border-color: #1A1A1A; }

/* KPI Group */
.kpi-group { display: flex; align-items: center; gap: 24rpx; margin-bottom: 40rpx; }
.kpi-box { flex: 1; background: #ffffff; padding: 30rpx; border-radius: 35rpx; border: 2rpx solid #F0EAE2; }
.kpi-val { font-size: 40rpx; font-weight: 900; color: #1A1A1A; display: block; }
.kpi-lab { font-size: 20rpx; color: #A09688; font-weight: 800; margin-top: 4rpx; display: block; }
.blue { color: #3B82F6; }
.purple { color: #8B5CF6; }

.action-trigger {
  width: 110rpx;
  height: 110rpx;
  background: #1A1A1A;
  border-radius: 35rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 50rpx;
}

/* Floating Editor */
.floating-editor { background: #1A1A1A; border-radius: 40rpx; padding: 40rpx; margin-bottom: 40rpx; box-shadow: 0 20rpx 50rpx rgba(0,0,0,0.15); }
.editor-label { color: #8E8E93; font-size: 20rpx; font-weight: 900; letter-spacing: 2rpx; margin-bottom: 20rpx; display: block; }
.input-row { display: flex; gap: 20rpx; }
.saas-input { flex: 1; background: #2D2D2D; height: 90rpx; border-radius: 20rpx; padding: 0 30rpx; color: #ffffff; font-size: 28rpx; font-weight: 700; }
.role-toggle { width: 120rpx; height: 90rpx; line-height: 90rpx; text-align: center; background: #3D3D3D; border-radius: 20rpx; color: #ffffff; font-size: 24rpx; font-weight: 800; }
.confirm-btn { width: 120rpx; height: 90rpx; line-height: 90rpx; text-align: center; background: #8B5CF6; border-radius: 20rpx; color: #ffffff; font-size: 24rpx; font-weight: 800; }

/* Search */
.search-section { margin-bottom: 40rpx; }
.glass-search { background: #ffffff; height: 100rpx; border-radius: 30rpx; display: flex; align-items: center; padding: 0 30rpx; border: 2rpx solid #F0EAE2; }
.si { font-size: 32rpx; margin-right: 20rpx; }
.si-input { flex: 1; font-size: 28rpx; font-weight: 700; color: #1A1A1A; }

/* Member Stack */
.stack-inner { background: #ffffff; border-radius: 45rpx; padding: 20rpx; border: 2rpx solid #F0EAE2; }
.user-row { display: flex; align-items: center; padding: 30rpx; border-bottom: 1rpx solid #FAF7F2; }
.user-row:last-child { border-bottom: none; }
.u-avatar { width: 90rpx; height: 90rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 36rpx; font-weight: 900; color: #ffffff; margin-right: 30rpx; }
.u-info { flex: 1; }
.u-name { font-size: 32rpx; font-weight: 800; color: #1A1A1A; display: block; }
.u-id { font-size: 22rpx; color: #B5A99A; font-weight: 700; margin-top: 4rpx; display: block; }
.u-tag { font-size: 18rpx; font-weight: 800; padding: 8rpx 20rpx; border-radius: 10rpx; }
.t-blue { background: #EBF2FF; color: #3B82F6; }
.t-purple { background: #F2EEF9; color: #8B5CF6; }

/* Avatar Colors */
.bg-blue { background: #3B82F6; }
.bg-green { background: #10B981; }
.bg-orange { background: #F59E0B; }
.bg-red { background: #EF4444; }
.bg-indigo { background: #6366F1; }
.bg-cyan { background: #06B6D4; }
.bg-gray { background: #A09688; }

.empty-placeholder { padding: 100rpx 0; text-align: center; }
.ep-icon { font-size: 80rpx; display: block; margin-bottom: 20rpx; }
.ep-text { font-size: 26rpx; color: #C5BDB0; font-weight: 700; }

.animated-fade-in { animation: fadeIn 0.6s ease-out; }
.animated-slide-down { animation: slideDown 0.4s cubic-bezier(0.4, 0, 0.2, 1); }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10rpx); } to { opacity: 1; transform: translateY(0); } }
@keyframes slideDown { from { opacity: 0; transform: translateY(-20rpx); } to { opacity: 1; transform: translateY(0); } }
</style>
