<template>
  <view class="fc-page page">
    <!-- 情况 A：尚未选择课程 -->
    <view v-if="!courseId" class="selection-screen animated-fade-in">
      <view class="page-intro">
        <text class="intro-eyebrow">COMMUNICATION CENTER</text>
        <text class="intro-title">消息通知中心</text>
        <text class="intro-subtitle">请选择一个教学班级来发布或管理课程公告</text>
      </view>

      <view class="course-grid">
        <view 
          v-for="course in teacherCourses" 
          :key="course.id" 
          class="course-select-card"
          @click="selectCourse(course)"
        >
          <view class="card-icon">🔔</view>
          <view class="card-info">
            <text class="card-name">{{ course.courseName }}</text>
            <text class="card-code">{{ course.courseCode }}</text>
          </view>
          <view class="card-arrow">➜</view>
        </view>
      </view>
    </view>

    <!-- 情况 B：已选择课程 -->
    <view v-else class="content-screen animated-fade-in">
      <!-- 顶部切换条 -->
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
      </view>

      <!-- Header -->
      <view class="page-header-row">
        <view class="header-left">
          <text class="p-title">课程公告 ({{ notices.length }})</text>
          <text class="p-subtitle">及时同步教学动态与重要通知</text>
        </view>
        <view class="header-right">
          <view class="action-round" @click="loadNotices">🔄</view>
          <view class="action-add-new" @click="toggleEditor">
             {{ showEditor ? '收起' : '＋ 发布' }}
          </view>
        </view>
      </view>

      <!-- 快速编辑器：深色风格 -->
      <view v-if="showEditor" class="editor-dark animated-slide-down">
        <view class="ed-header">
          <text class="ed-title">{{ editingId ? '编辑公告内容' : '编写新教学公告' }}</text>
        </view>
        <view class="ed-body">
          <view class="ed-field">
            <text class="ed-label">通知标题 / SUBJECT</text>
            <input v-model="form.title" class="ed-input" placeholder="简要概括通知内容" />
          </view>
          <view class="ed-field">
            <text class="ed-label">详细正文 / CONTENT</text>
            <textarea v-model="form.content" class="ed-textarea" placeholder="在此输入需要传达给学生的详细信息..." />
          </view>
          <view class="ed-footer">
            <view class="ed-status" @click="form.status = form.status === 1 ? 0 : 1">
              <text :class="['ed-dot', form.status === 1 ? 'ed-on' : '']"></text>
              <text class="ed-st-text">{{ form.status === 1 ? '立即公示' : '暂存草稿' }}</text>
            </view>
            <button class="ed-submit" @click="submitNotice">{{ editingId ? '保存修改' : '确认发布' }}</button>
          </view>
        </view>
      </view>

      <!-- 列表：时间轴卡片流 -->
      <view class="notice-list">
        <view v-if="notices.length" class="notice-stack">
          <view v-for="item in notices" :key="item.id" :class="['nt-card', item.status === 0 ? 'nt-dim' : '']">
            <view class="nt-top">
              <view class="nt-icon">📢</view>
              <view class="nt-main">
                <view class="nt-title-row">
                  <text class="nt-title">{{ item.title }}</text>
                  <text :class="['nt-badge', item.status === 1 ? 'b-blue' : 'b-gray']">
                    {{ item.status === 1 ? '已发布' : '待激活' }}
                  </text>
                </view>
                <text class="nt-date">{{ item.publishTime || '刚才' }}</text>
              </view>
            </view>
            
            <view class="nt-content">
              <text>{{ item.content || '（此通知暂无详细说明文字）' }}</text>
            </view>

            <view class="nt-footer">
              <view class="nt-btn nt-btn-edit" @click="startEdit(item.id)">✍️ 编辑</view>
              <view :class="['nt-btn', item.status === 1 ? 'nt-btn-warn' : 'nt-btn-ok']" @click="toggleStatus(item)">
                {{ item.status === 1 ? '🚫 撤回' : '✅ 发布' }}
              </view>
            </view>
          </view>
        </view>
        <view v-else class="empty-holder">
          <text class="eh-icon">🎐</text>
          <text class="eh-text">还没有发布过任何公告</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { createNotice, getNotice, listNotices, updateNotice, updateNoticeStatus } from '@/api/notice'
import { listCourses } from '@/api/course'
import { createReview } from '@/api/review'
import { requireSession } from '@/common/session'

function createForm() {
  return { title: '', content: '', publishTime: '', status: 1 }
}

export default {
  data() {
    return {
      session: null,
      courseId: null,
      teacherCourses: [],
      notices: [],
      editingId: null,
      showEditor: false,
      form: createForm()
    }
  },
  async onLoad(query) {
    this.session = requireSession()
    if (!this.session) return
    await this.loadTeacherCourses()
    if (query.courseId) {
      this.courseId = Number(query.courseId)
      this.loadNotices()
    }
  },
  methods: {
    async loadTeacherCourses() {
      this.teacherCourses = await listCourses({ teacherId: this.session.userId })
    },
    selectCourse(course) {
      this.courseId = course.id
      this.loadNotices()
      this.showEditor = false
    },
    toggleEditor() {
      this.showEditor = !this.showEditor
      if (!this.showEditor) this.resetForm()
    },
    async loadNotices() {
      if (!this.courseId) return
      this.notices = await listNotices(this.courseId)
    },
    resetForm() {
      this.editingId = null; this.form = createForm(); this.showEditor = false
    },
    async startEdit(id) {
      const detail = await getNotice(id)
      this.editingId = id; this.form = { ...detail }
      this.showEditor = true
      uni.pageScrollTo({ scrollTop: 0, duration: 300 })
    },
    async submitNotice() {
      if (!this.form.title || !this.form.content) return
      try {
        const payload = { ...this.form, status: 0 }
        let targetId = this.editingId
        if (this.editingId) await updateNotice(this.editingId, payload)
        else {
          const created = await createNotice(this.courseId, payload)
          targetId = created.id
        }
        await createReview({
          targetType: 'NOTICE',
          targetId,
          actionType: this.editingId ? 'UPDATE' : 'PUBLISH',
          requesterId: this.session.userId,
          title: this.form.title,
          summary: '教师提交课程公告发布审核'
        })
        uni.showToast({ title: '已提交审核', icon: 'none' })
        this.resetForm(); this.loadNotices()
      } catch (e) { uni.showToast({ title: '操作失败' }) }
    },
    async toggleStatus(item) {
      if (item.status !== 1) {
        await createReview({
          targetType: 'NOTICE',
          targetId: item.id,
          actionType: 'PUBLISH',
          requesterId: this.session.userId,
          title: item.title,
          summary: '教师申请发布课程公告'
        })
        uni.showToast({ title: '已提交审核', icon: 'none' })
        return
      }
      await updateNoticeStatus(item.id, 0)
      this.loadNotices()
    }
  }
}
</script>

<style scoped>
.page { padding: 40rpx; background: #FAF7F2; min-height: 100vh; }

/* 1. Selection */
.page-intro { padding: 40rpx 0 60rpx; }
.intro-eyebrow { font-size: 20rpx; font-weight: 800; color: #C5BDB0; letter-spacing: 4rpx; display: block; }
.intro-title { font-size: 60rpx; font-weight: 900; color: #1A1A1A; display: block; margin-top: 10rpx; }
.intro-subtitle { font-size: 26rpx; color: #A09688; margin-top: 20rpx; display: block; font-weight: 600; }

.course-grid { display: flex; flex-direction: column; gap: 30rpx; }
.course-select-card { background: #fff; padding: 50rpx; border-radius: 40rpx; display: flex; align-items: center; border: 2rpx solid #F0EAE2; }
.course-select-card:active { transform: scale(0.97); background: #FDFBF7; }
.card-icon { font-size: 60rpx; margin-right: 40rpx; }
.card-info { flex: 1; }
.card-name { font-size: 36rpx; font-weight: 900; color: #1A1A1A; display: block; }
.card-code { font-size: 24rpx; color: #B5A99A; font-weight: 700; margin-top: 6rpx; display: block; }
.card-arrow { font-size: 32rpx; color: #EAE2D9; }

/* 2. Content */
.top-nav { position: sticky; top: 0; z-index: 100; margin: -40rpx -40rpx 40rpx; background: #FAF7F2; padding: 40rpx 40rpx 20rpx; }
.nav-scroll { display: flex; white-space: nowrap; gap: 20rpx; height: 90rpx; }
.nav-pill { display: inline-block; padding: 0 40rpx; height: 80rpx; line-height: 80rpx; background: #fff; border-radius: 40rpx; font-size: 26rpx; font-weight: 800; color: #8E8E93; border: 2rpx solid #F0EAE2; margin-right: 20rpx; }
.nav-pill-active { background: #1A1A1A; color: #fff; border-color: #1A1A1A; }

.page-header-row { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 40rpx; }
.p-title { font-size: 44rpx; font-weight: 900; color: #1A1A1A; display: block; }
.p-subtitle { font-size: 24rpx; color: #A09688; margin-top: 8rpx; font-weight: 600; display: block; }

.header-right { display: flex; gap: 20rpx; }
.action-round { width: 90rpx; height: 90rpx; background: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 36rpx; border: 2rpx solid #F0EAE2; }
.action-add-new { height: 90rpx; padding: 0 40rpx; background: #1A1A1A; color: #fff; border-radius: 30rpx; display: flex; align-items: center; font-size: 28rpx; font-weight: 800; }

/* Dark Editor */
.editor-dark { background: #1A1A1A; border-radius: 45rpx; padding: 50rpx; margin-bottom: 50rpx; box-shadow: 0 20rpx 60rpx rgba(0,0,0,0.1); }
.ed-title { font-size: 32rpx; font-weight: 900; color: #fff; display: block; margin-bottom: 40rpx; }
.ed-label { font-size: 20rpx; font-weight: 800; color: #8E8E93; letter-spacing: 2rpx; margin-bottom: 16rpx; display: block; }
.ed-input { background: #2D2D2D; height: 100rpx; border-radius: 24rpx; padding: 0 30rpx; color: #fff; font-size: 28rpx; margin-bottom: 30rpx; }
.ed-textarea { background: #2D2D2D; width: 100%; height: 240rpx; border-radius: 24rpx; padding: 30rpx; color: #fff; font-size: 28rpx; box-sizing: border-box; }

.ed-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 40rpx; }
.ed-status { display: flex; align-items: center; gap: 16rpx; background: #333; padding: 16rpx 30rpx; border-radius: 20rpx; }
.ed-dot { width: 12rpx; height: 12rpx; border-radius: 50%; background: #555; }
.ed-on { background: #10B981; box-shadow: 0 0 12rpx #10B981; }
.ed-st-text { font-size: 24rpx; font-weight: 800; color: #eee; }
.ed-submit { height: 90rpx; padding: 0 60rpx; background: #fff; color: #1A1A1A; border-radius: 24rpx; font-size: 28rpx; font-weight: 900; }

/* Notice List */
.nt-card { background: #fff; border-radius: 45rpx; padding: 45rpx; border: 2rpx solid #F0EAE2; margin-bottom: 32rpx; position: relative; }
.nt-dim { opacity: 0.7; }
.nt-top { display: flex; gap: 30rpx; align-items: flex-start; margin-bottom: 30rpx; }
.nt-icon { font-size: 40rpx; padding-top: 4rpx; }
.nt-main { flex: 1; }
.nt-title-row { display: flex; justify-content: space-between; align-items: flex-start; }
.nt-title { font-size: 36rpx; font-weight: 900; color: #1A1A1A; flex: 1; padding-right: 20rpx; }
.nt-badge { font-size: 18rpx; font-weight: 800; padding: 6rpx 16rpx; border-radius: 10rpx; }
.b-blue { background: #EBF2FF; color: #3B82F6; }
.b-gray { background: #F5F5F5; color: #8E8E93; }
.nt-date { font-size: 22rpx; color: #B5A99A; font-weight: 700; margin-top: 10rpx; display: block; }

.nt-content { background: #F9F6F2; padding: 30rpx; border-radius: 30rpx; font-size: 28rpx; line-height: 1.7; color: #4A4A4A; margin-bottom: 32rpx; }

.nt-footer { display: flex; gap: 24rpx; justify-content: flex-end; }
.nt-btn { font-size: 24rpx; font-weight: 800; padding: 16rpx 32rpx; border-radius: 16rpx; }
.nt-btn-edit { background: #fff; border: 2rpx solid #F0EAE2; color: #1A1A1A; }
.nt-btn-warn { background: #FFF1F1; color: #EF4444; }
.nt-btn-ok { background: #EBF9F1; color: #10B981; }

.empty-holder { padding: 120rpx 0; text-align: center; }
.eh-icon { font-size: 100rpx; display: block; margin-bottom: 30rpx; }
.eh-text { font-size: 28rpx; color: #C5BDB0; font-weight: 700; }

.animated-fade-in { animation: fadeIn 0.6s ease-out; }
.animated-slide-down { animation: slideDown 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10rpx); } to { opacity: 1; transform: translateY(0); } }
@keyframes slideDown { from { opacity: 0; transform: translateY(-20rpx); } to { opacity: 1; transform: translateY(0); } }
</style>
