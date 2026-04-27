<template>
  <view class="fc-page page">
    <!-- 顶部状态栏 -->
    <view class="header-section">
      <view class="header-content">
        <text class="course-name">{{ courseName }}</text>
        <text class="page-title">课后作业中心</text>
      </view>
      <view class="header-actions">
        <button class="fc-secondary-btn icon-btn" @click="loadAssignments">🔄</button>
        <button class="fc-primary-btn add-btn" @click="toggleEditor">
          {{ showEditor ? '收起' : '+ 发布作业' }}
        </button>
      </view>
    </view>

    <!-- 折叠式编辑器 -->
    <view v-if="showEditor" class="fc-panel editor-card animated-slide-down">
      <view class="editor-header">
        <text class="editor-title">{{ editingId ? '编辑作业内容' : '开启新课后任务' }}</text>
      </view>
      
      <view class="form-body">
        <view class="input-group">
          <text class="label">作业标题</text>
          <input v-model="form.title" class="input" placeholder="例如：第二次 实验报告提交" />
        </view>

        <view class="form-row">
          <view class="input-group flex-1">
            <text class="label">截止日期</text>
            <input v-model="form.deadline" class="input" placeholder="2026-04-20 23:59" />
          </view>
          <view class="input-group flex-1">
            <text class="label">立即发布</text>
            <view class="status-switch" @click="form.published = form.published === 1 ? 0 : 1">
              <view :class="['switch-dot', form.published === 1 ? 'dot-on' : '']"></view>
              <text class="switch-text">{{ form.published === 1 ? '已开启' : '草稿箱' }}</text>
            </view>
          </view>
        </view>

        <view class="input-group">
          <text class="label">作业要求与说明</text>
          <textarea v-model="form.description" class="textarea" placeholder="请详细描述作业内容、附件格式要求等..." />
        </view>

        <view class="editor-footer">
          <button class="fc-primary-btn submit-btn" @click="submitAssignmentForm">{{ editingId ? '保存修改' : '确认发布' }}</button>
          <button class="fc-secondary-btn cancel-btn" @click="resetForm">取消</button>
        </view>
      </view>
    </view>

    <!-- 作业列表 -->
    <view class="list-section">
      <view v-if="assignments.length" class="assignment-stack">
        <view v-for="item in assignments" :key="item.id" class="assignment-item">
          <view class="item-main">
            <view class="item-info">
              <view class="item-top">
                <text class="item-title">{{ item.title }}</text>
                <text :class="['status-badge', item.published === 1 ? 'badge-blue' : 'badge-gray']">
                  {{ item.published === 1 ? '进行中' : '草稿' }}
                </text>
              </view>
              <view class="item-meta">
                <text class="meta-txt">截止: {{ item.deadline || '未设置' }}</text>
              </view>
            </view>
          </view>

          <view class="item-desc line-clamp">{{ item.description || '暂无说明' }}</view>

          <view class="item-actions">
            <view class="action-btns">
              <button class="btn-sm" @click="startEdit(item.id)">编辑</button>
              <button class="btn-sm btn-accent" @click="loadSubmissions(item.id)">去批改</button>
            </view>
            <view class="action-toggle" @click="togglePublished(item)">
              <text :class="['toggle-txt', item.published === 1 ? 'text-red' : 'text-green']">
                {{ item.published === 1 ? '撤回' : '发布' }}
              </text>
            </view>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon">📂</text>
        <text class="empty-text">暂无作业，点击右上角发布</text>
      </view>
    </view>

    <!-- 批改抽屉 -->
    <view v-if="selectedAssignmentId" class="drawer-overlay" @click="selectedAssignmentId = null">
      <view class="drawer-content animated-slide-up" @click.stop>
        <view class="drawer-handle"></view>
        <view class="drawer-header">
          <text class="drawer-title">作业批改与详情</text>
          <text class="drawer-subtitle">已收到 {{ records.length }} 份提交</text>
        </view>

        <scroll-view scroll-y class="drawer-scroll">
          <view v-for="record in records" :key="record.id" class="record-card">
            <view class="record-top">
              <view class="r-user">
                <text class="r-name">{{ record.studentName }}</text>
                <text class="r-id">{{ record.studentUsername }}</text>
              </view>
              <text :class="['r-status', record.score != null ? 'st-done' : 'st-pending']">
                {{ record.score != null ? '已评分' : '待批改' }}
              </text>
            </view>
            
            <view class="r-content">
              <text class="r-txt">{{ record.content || '未填写文字内容' }}</text>
              <view v-if="record.attachmentUrl" class="r-link" @click="copyText(record.attachmentUrl)">
                📎 查看附件链接
              </view>
            </view>

            <view class="r-score-area">
              <view class="score-input-box">
                <input v-model="record.scoreDraft" type="number" class="score-field" placeholder="分值" />
                <text class="score-total">/ 100</text>
              </view>
              <button class="score-submit-btn" @click="scoreRecord(record)">提交分数</button>
            </view>
          </view>
          <view v-if="!records.length" class="empty-mini">暂无学生提交</view>
        </scroll-view>
        
        <button class="drawer-close" @click="selectedAssignmentId = null">返回列表</button>
      </view>
    </view>
  </view>
</template>

<script>
import {
  createAssignment,
  getAssignment,
  listAssignments,
  listAssignmentSubmissions,
  scoreAssignmentSubmission,
  updateAssignment,
  updateAssignmentPublished
} from '@/api/assignment'
import { createReview } from '@/api/review'
import { requireSession } from '@/common/session'

function createForm() {
  return { title: '', description: '', deadline: '', published: 1 }
}

export default {
  data() {
    return {
      courseId: null,
      courseName: '',
      assignments: [],
      records: [],
      editingId: null,
      selectedAssignmentId: null,
      showEditor: false,
      form: createForm(),
      session: null
    }
  },
  onLoad(query) {
    const session = requireSession()
    if (!session) return
    this.session = session
    this.courseId = Number(query.courseId)
    this.courseName = query.courseName || '课程'
    this.loadAssignments()
  },
  methods: {
    toggleEditor() {
      this.showEditor = !this.showEditor
      if (!this.showEditor) this.resetForm()
    },
    async loadAssignments() {
      this.assignments = await listAssignments(this.courseId)
    },
    resetForm() {
      this.editingId = null
      this.form = createForm()
      this.showEditor = false
    },
    async startEdit(id) {
      const detail = await getAssignment(id)
      this.editingId = id
      this.form = { ...detail }
      this.showEditor = true
      uni.pageScrollTo({ scrollTop: 0, duration: 300 })
    },
    async submitAssignmentForm() {
      if (!this.form.title) return uni.showToast({ title: '标题不能为空', icon: 'none' })
      const payload = { ...this.form, published: 0 }
      let targetId = this.editingId
      if (this.editingId) {
        await updateAssignment(this.editingId, payload)
      } else {
        const created = await createAssignment(this.courseId, payload)
        targetId = created.id
      }
      await createReview({
        targetType: 'ASSIGNMENT',
        targetId,
        actionType: this.editingId ? 'UPDATE' : 'PUBLISH',
        requesterId: this.session.userId,
        title: this.form.title,
        summary: '教师提交课后作业发布审核'
      })
      this.resetForm()
      this.loadAssignments()
      uni.showToast({ title: '已提交审核', icon: 'none' })
    },
    async togglePublished(item) {
      if (item.published !== 1) {
        await createReview({
          targetType: 'ASSIGNMENT',
          targetId: item.id,
          actionType: 'PUBLISH',
          requesterId: this.session.userId,
          title: item.title,
          summary: '教师申请发布课后作业'
        })
        uni.showToast({ title: '已提交审核', icon: 'none' })
        return
      }
      await updateAssignmentPublished(item.id, 0)
      this.loadAssignments()
    },
    async loadSubmissions(id) {
      this.selectedAssignmentId = id
      const list = await listAssignmentSubmissions(id)
      this.records = list.map(r => ({
        ...r,
        scoreDraft: r.score == null ? '' : String(r.score)
      }))
    },
    async scoreRecord(record) {
      if (!record.scoreDraft) return uni.showToast({ title: '请输入分数', icon: 'none' })
      await scoreAssignmentSubmission(record.id, {
        score: Number(record.scoreDraft),
        feedback: '批改完成'
      })
      this.loadSubmissions(this.selectedAssignmentId)
      uni.showToast({ title: '评分成功', icon: 'none' })
    },
    copyText(text) {
      uni.setClipboardData({ data: text, success: () => uni.showToast({ title: '链接已复制', icon: 'none' }) })
    }
  }
}
</script>

<style scoped>
.page { padding: 32rpx; background: var(--fc-bg); min-height: 100vh; }

/* 顶部 */
.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 40rpx; }
.course-name { font-size: 24rpx; color: var(--fc-text-soft); font-weight: 600; }
.page-title { font-size: 40rpx; font-weight: 800; color: var(--fc-text); }
.header-actions { display: flex; gap: 16rpx; }
.icon-btn { width: 80rpx; height: 80rpx; border-radius: 20rpx; display: flex; align-items: center; justify-content: center; }
.add-btn { height: 80rpx; padding: 0 32rpx; border-radius: 20rpx; font-size: 26rpx; font-weight: 700; }

/* 编辑器 */
.editor-card { background: #fff; border-radius: 32rpx; padding: 32rpx; margin-bottom: 40rpx; border: 1px solid var(--fc-border-light); }
.editor-title { font-size: 28rpx; font-weight: 700; color: var(--fc-text); margin-bottom: 32rpx; display: block; }
.input-group { margin-bottom: 24rpx; }
.label { font-size: 22rpx; font-weight: 700; color: var(--fc-text-soft); margin-bottom: 12rpx; display: block; }
.input, .textarea { background: var(--fc-bg); border-radius: 16rpx; padding: 24rpx; font-size: 28rpx; width: 100%; box-sizing: border-box; }
.textarea { height: 180rpx; }

.form-row { display: flex; gap: 24rpx; }
.flex-1 { flex: 1; }
.status-switch { display: flex; align-items: center; gap: 16rpx; height: 88rpx; background: var(--fc-bg); border-radius: 16rpx; padding: 0 24rpx; }
.switch-dot { width: 14rpx; height: 14rpx; border-radius: 50%; background: #ccc; }
.dot-on { background: #27ae60; }
.switch-text { font-size: 24rpx; font-weight: 700; color: var(--fc-text); }

.editor-footer { display: flex; gap: 16rpx; margin-top: 32rpx; }
.submit-btn { flex: 2; height: 90rpx; }
.cancel-btn { flex: 1; height: 90rpx; }

/* 列表 */
.assignment-stack { display: flex; flex-direction: column; gap: 24rpx; }
.assignment-item { background: #fff; border-radius: 36rpx; padding: 32rpx; border: 1px solid var(--fc-border-light); }
.item-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-title { font-size: 32rpx; font-weight: 800; color: var(--fc-text); }
.status-badge { font-size: 18rpx; font-weight: 800; padding: 4rpx 16rpx; border-radius: 8rpx; }
.badge-blue { background: #eef2ff; color: #4f46e5; }
.badge-gray { background: #f3f4f6; color: #6b7280; }
.item-meta { font-size: 22rpx; color: var(--fc-text-soft); font-weight: 600; }
.item-desc { font-size: 26rpx; color: var(--fc-text-soft); margin-top: 20rpx; line-height: 1.6; }
.line-clamp { display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2; overflow: hidden; }

.item-actions { margin-top: 32rpx; padding-top: 24rpx; border-top: 1px dashed #eee; display: flex; justify-content: space-between; align-items: center; }
.action-btns { display: flex; gap: 12rpx; }
.btn-sm { height: 60rpx; line-height: 60rpx; padding: 0 24rpx; border-radius: 12rpx; font-size: 22rpx; font-weight: 700; background: var(--fc-bg); color: var(--fc-text); border: none; }
.btn-accent { background: var(--fc-accent-soft); color: var(--fc-accent); }
.toggle-txt { font-size: 22rpx; font-weight: 800; }
.text-red { color: #ef4444; }
.text-green { color: #27ae60; }

/* 批改抽屉 */
.drawer-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.4); backdrop-filter: blur(4px); z-index: 1000; }
.drawer-content { position: absolute; left: 0; right: 0; bottom: 0; background: #fff; border-radius: 48rpx 48rpx 0 0; padding: 32rpx; box-shadow: 0 -10rpx 40rpx rgba(0,0,0,0.1); }
.drawer-handle { width: 80rpx; height: 8rpx; background: #eee; border-radius: 999rpx; margin: 0 auto 32rpx; }
.drawer-header { margin-bottom: 40rpx; }
.drawer-title { font-size: 36rpx; font-weight: 800; display: block; }
.drawer-subtitle { font-size: 24rpx; color: var(--fc-text-soft); margin-top: 8rpx; display: block; }

.drawer-scroll { max-height: 65vh; }
.record-card { background: var(--fc-bg); padding: 24rpx; border-radius: 24rpx; margin-bottom: 24rpx; }
.record-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.r-name { font-size: 28rpx; font-weight: 800; color: var(--fc-text); }
.r-id { font-size: 22rpx; color: #999; margin-left: 12rpx; }
.r-status { font-size: 20rpx; font-weight: 800; padding: 4rpx 12rpx; border-radius: 6rpx; }
.st-done { background: #e6f7ed; color: #27ae60; }
.st-pending { background: #fff7ed; color: #f59e0b; }

.r-content { margin-bottom: 24rpx; }
.r-txt { font-size: 26rpx; color: var(--fc-text); line-height: 1.6; }
.r-link { margin-top: 16rpx; font-size: 22rpx; color: var(--fc-accent); font-weight: 700; }

.r-score-area { display: flex; gap: 16rpx; align-items: center; border-top: 1px solid #eee; padding-top: 20rpx; }
.score-input-box { flex: 1; height: 72rpx; background: #fff; border-radius: 12rpx; display: flex; align-items: center; padding: 0 16rpx; border: 1px solid #ddd; }
.score-field { flex: 1; font-size: 28rpx; font-weight: 800; }
.score-total { font-size: 22rpx; color: #999; font-weight: 600; }
.score-submit-btn { height: 72rpx; line-height: 72rpx; padding: 0 24rpx; background: var(--fc-accent); color: #fff; font-size: 22rpx; font-weight: 700; border-radius: 12rpx; }

.drawer-close { height: 100rpx; line-height: 100rpx; background: var(--fc-bg); color: var(--fc-text-soft); font-size: 30rpx; font-weight: 700; border-radius: 24rpx; margin-top: 24rpx; }

.empty-state { padding: 120rpx 0; text-align: center; }
.empty-icon { font-size: 80rpx; display: block; margin-bottom: 24rpx; }
.empty-text { color: var(--fc-text-soft); font-size: 26rpx; font-weight: 600; }

.animated-slide-down { animation: slideDown 0.3s ease-out; }
.animated-slide-up { animation: slideUp 0.3s ease-out; }
@keyframes slideDown { from { opacity: 0; transform: translateY(-20rpx); } to { opacity: 1; transform: translateY(0); } }
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
</style>
