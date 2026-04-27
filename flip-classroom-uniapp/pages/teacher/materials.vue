<template>
  <view class="fc-page page">
    <!-- 情况 A：尚未选择课程 -->
    <view v-if="!courseId" class="selection-screen animated-fade-in">
      <view class="page-intro">
        <text class="intro-eyebrow">COURSE MATERIALS</text>
        <text class="intro-title">教学资源库</text>
        <text class="intro-subtitle">请选择一门课程来查看或上传教学课件与资料</text>
      </view>

      <view class="course-grid">
        <view 
          v-for="course in teacherCourses" 
          :key="course.id" 
          class="course-select-card"
          @click="selectCourse(course)"
        >
          <view class="card-icon">📂</view>
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

      <!-- KPI/Header -->
      <view class="page-header-row">
        <view class="header-left">
          <text class="p-title">资料库 ({{ materials.length }})</text>
          <text class="p-subtitle">支持 PDF, PPT, 视频等多种格式</text>
        </view>
        <view class="header-right">
          <view class="action-round" @click="loadMaterials">🔄</view>
          <view class="action-add-new" @click="toggleEditor">
             {{ showEditor ? '收起' : '＋ 新增' }}
          </view>
        </view>
      </view>

      <!-- 快速编辑器 -->
      <view v-if="showEditor" class="editor-panel animated-slide-down">
        <view class="ep-inner">
          <view class="ep-field">
            <text class="ep-label">资料标题 / TITLE</text>
            <input v-model="form.title" class="ep-input" placeholder="请输入资料名称" />
          </view>
          
          <view class="ep-row">
            <view class="ep-field flex-1">
              <text class="ep-label">类型 / TYPE</text>
              <picker :range="typeOptions" :value="typeIndex" @change="handleTypeChange">
                <view class="ep-picker-val">{{ typeOptions[typeIndex] }}</view>
              </picker>
            </view>
            <view class="ep-field flex-1">
              <text class="ep-label">状态 / STATUS</text>
              <view class="status-toggle" @click="form.status = form.status === 1 ? 0 : 1">
                <text :class="['st-dot', form.status === 1 ? 'st-on' : '']"></text>
                <text class="st-text">{{ form.status === 1 ? '公开' : '私密' }}</text>
              </view>
            </view>
          </view>

          <view class="ep-field">
            <text class="ep-label">访问链接 / URL</text>
            <input v-model="form.materialUrl" class="ep-input" placeholder="https://" />
          </view>

          <button class="ep-submit" @click="submitMaterial">{{ editingId ? '更新资料信息' : '发布教学资料' }}</button>
        </view>
      </view>

      <!-- 列表 -->
      <view class="material-list">
        <view v-if="materials.length" class="card-stack">
          <view v-for="item in materials" :key="item.id" class="mat-card">
            <view class="mat-top">
              <view :class="['mat-icon-box', getFileColor(item.materialType)]">
                {{ getFileEmoji(item.materialType) }}
              </view>
              <view class="mat-main">
                <view class="mat-title-row">
                  <text class="mat-title">{{ item.title }}</text>
                  <text :class="['mat-tag', item.status === 1 ? 't-green' : 't-gray']">
                    {{ item.status === 1 ? '启用' : '隐藏' }}
                  </text>
                </view>
                <text class="mat-info">{{ item.materialType.toUpperCase() }} · {{ item.publishTime || '刚才' }}</text>
              </view>
            </view>
            
            <view class="mat-footer">
              <view class="mat-link" @click="copyText(item.materialUrl)">
                <text class="ml-icon">🔗</text>
                <text class="ml-text">复制资料链接</text>
              </view>
              <view class="mat-btns">
                <view class="m-btn btn-edit" @click="startEdit(item.id)">编辑</view>
                <view :class="['m-btn', item.status === 1 ? 'btn-hide' : 'btn-show']" @click="toggleStatus(item)">
                  {{ item.status === 1 ? '撤回' : '展示' }}
                </view>
              </view>
            </view>
          </view>
        </view>
        <view v-else class="empty-holder">
          <text class="eh-icon">🌵</text>
          <text class="eh-text">暂无资料上传</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { createMaterial, getMaterial, listMaterials, updateMaterial, updateMaterialStatus } from '@/api/material'
import { listCourses } from '@/api/course'
import { createReview } from '@/api/review'
import { requireSession } from '@/common/session'

function createForm() {
  return { title: '', materialType: 'pdf', description: '', materialUrl: '', publishTime: '', status: 1 }
}

export default {
  data() {
    return {
      session: null,
      courseId: null,
      teacherCourses: [],
      materials: [],
      editingId: null,
      showEditor: false,
      form: createForm(),
      typeOptions: ['pdf', 'video', 'ppt', 'link', 'doc'],
      typeIndex: 0
    }
  },
  async onLoad(query) {
    this.session = requireSession()
    if (!this.session) return
    await this.loadTeacherCourses()
    if (query.courseId) {
      this.courseId = Number(query.courseId)
      this.loadMaterials()
    }
  },
  methods: {
    async loadTeacherCourses() {
      this.teacherCourses = await listCourses({ teacherId: this.session.userId })
    },
    selectCourse(course) {
      this.courseId = course.id
      this.loadMaterials()
      this.showEditor = false
    },
    handleTypeChange(e) {
      this.typeIndex = e.detail.value
      this.form.materialType = this.typeOptions[this.typeIndex]
    },
    toggleEditor() {
      this.showEditor = !this.showEditor
      if (!this.showEditor) this.resetForm()
    },
    async loadMaterials() {
      if (!this.courseId) return
      this.materials = await listMaterials(this.courseId)
    },
    resetForm() {
      this.editingId = null; this.form = createForm(); this.typeIndex = 0; this.showEditor = false
    },
    async startEdit(id) {
      const detail = await getMaterial(id)
      this.editingId = id; this.form = { ...detail }
      this.typeIndex = this.typeOptions.indexOf(detail.materialType.toLowerCase())
      if (this.typeIndex === -1) this.typeIndex = 0
      this.showEditor = true
      uni.pageScrollTo({ scrollTop: 0, duration: 300 })
    },
    async submitMaterial() {
      if (!this.form.title || !this.form.materialUrl) return
      try {
        const payload = { ...this.form, status: 0 }
        let targetId = this.editingId
        if (this.editingId) await updateMaterial(this.editingId, payload)
        else {
          const created = await createMaterial(this.courseId, payload)
          targetId = created.id
        }
        await createReview({
          targetType: 'MATERIAL',
          targetId,
          actionType: this.editingId ? 'UPDATE' : 'PUBLISH',
          requesterId: this.session.userId,
          title: this.form.title,
          summary: '教师提交教学资料发布审核'
        })
        uni.showToast({ title: '已提交审核', icon: 'none' })
        this.resetForm(); this.loadMaterials()
      } catch (e) { uni.showToast({ title: '保存失败' }) }
    },
    async toggleStatus(item) {
      if (item.status !== 1) {
        await createReview({
          targetType: 'MATERIAL',
          targetId: item.id,
          actionType: 'PUBLISH',
          requesterId: this.session.userId,
          title: item.title,
          summary: '教师申请发布教学资料'
        })
        uni.showToast({ title: '已提交审核', icon: 'none' })
        return
      }
      await updateMaterialStatus(item.id, 0)
      this.loadMaterials()
    },
    copyText(text) {
      uni.setClipboardData({ data: text, success: () => uni.showToast({ title: '已复制链接', icon: 'none' }) })
    },
    getFileEmoji(type) {
      const map = { pdf: '📄', video: '🎬', ppt: '📊', link: '🔗', doc: '📝' }
      return map[type.toLowerCase()] || '📁'
    },
    getFileColor(type) {
      const map = { pdf: 'c-red', video: 'c-blue', ppt: 'c-orange', link: 'c-green', doc: 'c-indigo' }
      return map[type.toLowerCase()] || 'c-gray'
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

/* Editor */
.editor-panel { background: #fff; border-radius: 40rpx; padding: 40rpx; margin-bottom: 50rpx; border: 2rpx solid #F0EAE2; box-shadow: 0 10rpx 40rpx rgba(0,0,0,0.03); }
.ep-field { margin-bottom: 30rpx; }
.ep-label { font-size: 20rpx; font-weight: 800; color: #C5BDB0; text-transform: uppercase; margin-bottom: 16rpx; display: block; }
.ep-input, .ep-picker-val { background: #F9F6F2; height: 100rpx; border-radius: 24rpx; padding: 0 30rpx; font-size: 28rpx; font-weight: 700; line-height: 100rpx; }
.ep-row { display: flex; gap: 30rpx; }
.flex-1 { flex: 1; }

.status-toggle { height: 100rpx; background: #F9F6F2; border-radius: 24rpx; display: flex; align-items: center; padding: 0 30rpx; gap: 16rpx; }
.st-dot { width: 14rpx; height: 14rpx; border-radius: 50%; background: #cbd5e0; }
.st-on { background: #10B981; box-shadow: 0 0 12rpx rgba(16,185,129,0.3); }
.st-text { font-size: 26rpx; font-weight: 800; color: #1A1A1A; }

.ep-submit { margin-top: 20rpx; height: 110rpx; background: #1A1A1A; color: #fff; border-radius: 30rpx; font-size: 30rpx; font-weight: 900; }

/* List */
.card-stack { display: flex; flex-direction: column; gap: 30rpx; }
.mat-card { background: #fff; border-radius: 40rpx; padding: 40rpx; border: 2rpx solid #F0EAE2; transition: all 0.3s; }
.mat-card:hover { transform: translateY(-10rpx); box-shadow: 0 20rpx 40rpx rgba(0,0,0,0.03); }

.mat-top { display: flex; gap: 30rpx; align-items: center; }
.mat-icon-box { width: 110rpx; height: 110rpx; border-radius: 32rpx; display: flex; align-items: center; justify-content: center; font-size: 52rpx; }
.mat-main { flex: 1; }
.mat-title-row { display: flex; justify-content: space-between; align-items: flex-start; }
.mat-title { font-size: 32rpx; font-weight: 900; color: #1A1A1A; }
.mat-tag { font-size: 18rpx; font-weight: 800; padding: 4rpx 16rpx; border-radius: 10rpx; }
.t-green { background: #EBF9F1; color: #10B981; }
.t-gray { background: #F5F5F5; color: #8E8E93; }
.mat-info { font-size: 22rpx; color: #B5A99A; font-weight: 700; margin-top: 8rpx; display: block; }

.mat-footer { margin-top: 32rpx; padding-top: 32rpx; border-top: 2rpx dashed #FAF7F2; display: flex; justify-content: space-between; align-items: center; }
.ml-icon { font-size: 28rpx; margin-right: 8rpx; }
.ml-text { font-size: 24rpx; font-weight: 800; color: #3B82F6; }

.mat-btns { display: flex; gap: 16rpx; }
.m-btn { font-size: 22rpx; font-weight: 800; padding: 12rpx 30rpx; border-radius: 12rpx; }
.btn-edit { background: #F9F6F2; color: #1A1A1A; }
.btn-hide { background: #FFF1F1; color: #EF4444; }
.btn-show { background: #EBF9F1; color: #10B981; }

/* Colors */
.c-red { background: #FFF1F1; }
.c-blue { background: #EBF2FF; }
.c-orange { background: #FFF4EB; }
.c-green { background: #EBF9F1; }
.c-indigo { background: #F2EEF9; }
.c-gray { background: #F5F5F5; }

.empty-holder { padding: 120rpx 0; text-align: center; }
.eh-icon { font-size: 100rpx; display: block; margin-bottom: 30rpx; }
.eh-text { font-size: 28rpx; color: #C5BDB0; font-weight: 700; }

.animated-fade-in { animation: fadeIn 0.6s ease-out; }
.animated-slide-down { animation: slideDown 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideDown { from { opacity: 0; transform: translateY(-20rpx); } to { opacity: 1; transform: translateY(0); } }
</style>
