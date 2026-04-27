<template>
  <view class="admin-page">
    <view class="side-nav">
      <view class="brand">FlipEdu</view>
      <view class="nav-item" @click="go('/pages/admin/dashboard')">总览</view>
      <view class="nav-item active">课程管理</view>
      <view class="nav-item" @click="go('/pages/admin/users')">用户管理</view>
      <view class="nav-item" @click="go('/pages/admin/reviews')">审核中心</view>
      <view class="nav-spacer"></view>
      <view class="user-line">{{ session.realName || '系统管理员' }}</view>
      <view class="logout" @click="logout">退出登录</view>
    </view>

    <view class="main">
      <view class="page-head">
        <view>
          <text class="page-title">课程管理</text>
          <text class="page-desc">创建课程、调整授课教师，并进入成员管理。</text>
        </view>
        <view class="head-actions">
          <button class="secondary-btn" @click="loadData">刷新</button>
          <button class="primary-btn" @click="resetForm">新建课程</button>
        </view>
      </view>

      <view class="layout-grid">
        <view class="form-card">
          <text class="card-title">{{ editingId ? '编辑课程' : '新建课程' }}</text>
          <view class="field">
            <text class="label">课程名称</text>
            <input v-model="form.courseName" class="input" placeholder="例如：Java程序设计" />
          </view>
          <view class="field">
            <text class="label">课程代码</text>
            <input v-model="form.courseCode" class="input" placeholder="例如：JAVA-2026" />
          </view>
          <view class="field">
            <text class="label">学期</text>
            <input v-model="form.term" class="input" placeholder="例如：2025-2026 第二学期" />
          </view>
          <view class="field">
            <text class="label">授课教师</text>
            <picker :range="teacherOptions" range-key="label" @change="selectTeacher">
              <view class="picker-box">{{ selectedTeacherLabel }}</view>
            </picker>
          </view>
          <view class="field">
            <text class="label">课程状态</text>
            <view class="segmented">
              <text :class="['seg-item', form.status === 1 ? 'active' : '']" @click="form.status = 1">启用</text>
              <text :class="['seg-item', form.status === 0 ? 'active' : '']" @click="form.status = 0">停用</text>
            </view>
          </view>
          <view class="field">
            <text class="label">课程简介</text>
            <textarea v-model="form.introduction" class="textarea" placeholder="填写课程说明，便于管理员快速识别。" />
          </view>
          <view class="form-actions">
            <button class="secondary-btn wide" @click="resetForm">清空</button>
            <button class="primary-btn wide" @click="submitCourse">{{ editingId ? '保存修改' : '创建课程' }}</button>
          </view>
        </view>

        <view class="table-card">
          <view class="table-headline">
            <text class="card-title">课程列表</text>
            <text class="muted">{{ courses.length }} 门课程</text>
          </view>
          <scroll-view scroll-y class="table-body">
            <view v-for="course in courses" :key="course.id" class="course-row">
              <view class="course-main">
                <view class="title-line">
                  <text class="course-name">{{ course.courseName }}</text>
                  <text :class="['status-tag', course.status === 1 ? 'on' : 'off']">{{ course.status === 1 ? '启用' : '停用' }}</text>
                </view>
                <text class="course-sub">编号 {{ course.courseCode || '-' }} · {{ course.term || '未设置学期' }}</text>
                <text class="course-sub">教师：{{ course.teacherName || '未分配' }}</text>
                <text class="course-desc">{{ course.introduction || '暂无课程简介' }}</text>
              </view>
              <view class="ops">
                <button class="link-btn" @click="startEdit(course)">编辑</button>
                <button class="link-btn" @click="viewMembers(course)">成员</button>
              </view>
            </view>
            <view v-if="!courses.length" class="empty">暂无课程</view>
          </scroll-view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { listUsers } from '@/api/admin'
import { createCourse, listCourses, updateCourse } from '@/api/course'
import { clearSession, requireSession } from '@/common/session'

function emptyForm() {
  return {
    courseName: '',
    courseCode: '',
    term: '',
    introduction: '',
    teacherId: null,
    status: 1
  }
}

export default {
  data() {
    return {
      session: {},
      courses: [],
      teachers: [],
      editingId: null,
      form: emptyForm()
    }
  },
  computed: {
    teacherOptions() {
      return this.teachers.map(item => ({
        label: `${item.realName || item.username}（${item.username}）`,
        value: item.id
      }))
    },
    selectedTeacherLabel() {
      const teacher = this.teacherOptions.find(item => Number(item.value) === Number(this.form.teacherId))
      return teacher ? teacher.label : '请选择授课教师'
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
      const [courses, teachers] = await Promise.all([
        listCourses(),
        listUsers('teacher')
      ])
      this.courses = courses || []
      this.teachers = teachers || []
    },
    selectTeacher(event) {
      const option = this.teacherOptions[Number(event.detail.value)]
      this.form.teacherId = option ? option.value : null
    },
    startEdit(course) {
      this.editingId = course.id
      this.form = {
        courseName: course.courseName || '',
        courseCode: course.courseCode || '',
        term: course.term || '',
        introduction: course.introduction || '',
        teacherId: course.teacherId || null,
        status: course.status === 0 ? 0 : 1
      }
      uni.pageScrollTo({ scrollTop: 0, duration: 240 })
    },
    resetForm() {
      this.editingId = null
      this.form = emptyForm()
    },
    async submitCourse() {
      if (!this.form.courseName || !this.form.courseCode || !this.form.term || !this.form.teacherId) {
        uni.showToast({ title: '请填写课程名称、代码、学期并选择教师', icon: 'none' })
        return
      }
      if (this.editingId) {
        await updateCourse(this.editingId, this.form)
      } else {
        await createCourse(this.form)
      }
      uni.showToast({ title: '保存成功', icon: 'success' })
      this.resetForm()
      this.loadData()
    },
    viewMembers(course) {
      uni.navigateTo({
        url: `/pages/admin/course_members?courseId=${course.id}&courseName=${encodeURIComponent(course.courseName || '')}`
      })
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
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 32rpx; gap: 24rpx; }
.page-title { display: block; font-size: 44rpx; font-weight: 900; }
.page-desc { display: block; margin-top: 8rpx; color: #64748b; font-size: 24rpx; }
.head-actions, .form-actions { display: flex; gap: 14rpx; }
.layout-grid { display: grid; grid-template-columns: 460rpx 1fr; gap: 24rpx; align-items: flex-start; }
.form-card, .table-card { background: #fff; border: 1rpx solid #e8ecf3; border-radius: 18rpx; padding: 30rpx; box-sizing: border-box; }
.card-title { display: block; font-size: 30rpx; font-weight: 900; margin-bottom: 22rpx; }
.field { margin-bottom: 22rpx; }
.label { display: block; margin-bottom: 10rpx; color: #64748b; font-size: 23rpx; font-weight: 800; }
.input, .picker-box { height: 78rpx; line-height: 78rpx; background: #f8fafc; border: 1rpx solid #e2e8f0; border-radius: 14rpx; padding: 0 20rpx; font-size: 26rpx; box-sizing: border-box; color: #172033; }
.textarea { width: 100%; height: 150rpx; background: #f8fafc; border: 1rpx solid #e2e8f0; border-radius: 14rpx; padding: 18rpx 20rpx; font-size: 26rpx; box-sizing: border-box; }
.segmented { display: flex; background: #f1f5f9; border-radius: 14rpx; padding: 6rpx; }
.seg-item { flex: 1; text-align: center; height: 60rpx; line-height: 60rpx; border-radius: 12rpx; color: #475569; font-size: 24rpx; font-weight: 800; }
.seg-item.active { background: #fff; color: #4f46e5; box-shadow: 0 6rpx 18rpx rgba(15, 23, 42, 0.08); }
.primary-btn, .secondary-btn, .link-btn { margin: 0; height: 72rpx; line-height: 72rpx; padding: 0 24rpx; border-radius: 14rpx; border: none; font-size: 24rpx; font-weight: 800; }
.primary-btn { background: #4f46e5; color: #fff; }
.secondary-btn { background: #fff; color: #334155; border: 1rpx solid #e2e8f0; }
.wide { flex: 1; }
.table-headline { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.muted { color: #64748b; font-size: 24rpx; font-weight: 700; }
.table-body { height: 980rpx; }
.course-row { display: flex; gap: 20rpx; padding: 26rpx 0; border-bottom: 1rpx solid #eef2f7; }
.course-main { flex: 1; min-width: 0; }
.title-line { display: flex; align-items: center; gap: 14rpx; margin-bottom: 8rpx; }
.course-name { font-size: 30rpx; font-weight: 900; color: #172033; }
.status-tag { padding: 6rpx 16rpx; border-radius: 999rpx; font-size: 21rpx; font-weight: 900; }
.status-tag.on { background: #dcfce7; color: #15803d; }
.status-tag.off { background: #fee2e2; color: #dc2626; }
.course-sub, .course-desc { display: block; margin-top: 6rpx; color: #64748b; font-size: 23rpx; line-height: 1.5; }
.course-desc { color: #334155; }
.ops { width: 170rpx; display: flex; flex-direction: column; gap: 12rpx; }
.link-btn { background: #eef2ff; color: #4f46e5; }
.empty { padding: 70rpx 0; text-align: center; color: #94a3b8; }
</style>
