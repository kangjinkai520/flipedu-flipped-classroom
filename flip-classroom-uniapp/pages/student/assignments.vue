<template>
  <view class="fc-page page">
    <!-- 顶部状态栏 -->
    <view class="header-section">
      <view class="header-content">
        <text class="course-name">{{ courseName }}</text>
        <text class="page-title">课后作业 📚</text>
      </view>
      <view class="header-actions">
        <button class="icon-btn" @click="loadAssignments">🔄</button>
      </view>
    </view>

    <loading-state v-if="loading" text="正在获取作业列表..." />

    <!-- 作业列表模式 -->
    <view v-else-if="!currentAssignment" class="content-body">
      <view v-if="assignments.length" class="assignment-stack">
        <view 
          v-for="(item, index) in assignments" 
          :key="item.id" 
          :class="['assignment-card', `card-style-${index % 4}`]"
          @click="openAssignment(item)"
        >
          <view class="card-icon-box">
            <text class="card-emoji">🧩</text>
          </view>
          <view class="card-main">
            <text class="assignment-title">{{ item.title }}</text>
            <view class="assignment-info">
              <text class="deadline-tag">截止：{{ formatDate(item.deadline) }}</text>
              <text class="info-desc">{{ item.description || '点击进入提交作业' }}</text>
            </view>
          </view>
          <view class="card-arrow">→</view>
        </view>
      </view>
      <empty-state 
        v-else 
        icon="✨" 
        title="暂时没有作业" 
        desc="当前课程还没有发布课后作业，尽情享受你的自由时间吧。"
      />
    </view>

    <!-- 作业提交与详情模式 -->
    <view v-else class="assignment-active-area animated-fade-in">
      <view class="active-header">
        <view class="back-btn" @click="currentAssignment = null">← 列表</view>
        <text class="active-title">作业详情</text>
      </view>

      <!-- 作业要求卡片 -->
      <view class="detail-card">
        <text class="detail-title">{{ currentAssignment.title }}</text>
        <view class="detail-meta">
          <text class="meta-item">📅 截止时间：{{ formatDate(currentAssignment.deadline) }}</text>
        </view>
        <view class="detail-body">
          <text class="body-label">作业要求：</text>
          <text class="body-content">{{ currentAssignment.description || '教师未填写具体要求' }}</text>
        </view>
      </view>

      <!-- 提交记录 (如果有) -->
      <view v-if="mySubmission" class="submission-view">
        <view class="section-divider">
          <text class="divider-text">提交状态</text>
        </view>
        <view class="status-card">
          <view class="status-header">
            <text :class="['status-pill', mySubmission.score != null ? 'pill-scored' : 'pill-pending']">
              {{ mySubmission.score != null ? '已评分' : '待评分' }}
            </text>
            <text class="submit-time">提交于：{{ formatFullDate(mySubmission.submittedAt) }}</text>
          </view>
          
          <view v-if="mySubmission.score != null" class="score-display">
            <text class="score-num">{{ mySubmission.score }}</text>
            <text class="score-unit">分</text>
          </view>

          <view v-if="mySubmission.feedback" class="feedback-box">
            <text class="feedback-label">教师评语：</text>
            <text class="feedback-text">{{ mySubmission.feedback }}</text>
          </view>
        </view>
      </view>

      <!-- 提交表单区 -->
      <view class="form-section">
        <view class="section-divider">
          <text class="divider-text">{{ mySubmission ? '修改提交内容' : '开始提交' }}</text>
        </view>
        
        <view class="form-card">
          <view class="input-group">
            <text class="group-label">回答内容</text>
            <textarea 
              v-model="submissionForm.content" 
              class="form-textarea" 
              placeholder="在这里输入你的作业答案..." 
              maxlength="-1"
            />
          </view>

          <view class="input-group">
            <text class="group-label">附件链接 (可选)</text>
            <input 
              v-model="submissionForm.attachmentUrl" 
              class="form-input" 
              placeholder="https://..." 
            />
          </view>
        </view>

        <view class="form-footer">
          <button class="submit-btn" :disabled="submitting" @click="submitCurrentAssignment">
            {{ submitting ? '正在提交...' : (mySubmission ? '更新我的提交' : '确认提交作业') }}
          </button>
        </view>
      </view>
    </view>

    <!-- 全局 AI 助手 -->
    <view class="ai-fab" @click="showAi = true">
      <text class="ai-icon">🤖</text>
      <text class="ai-label">问 AI</text>
    </view>
    <ai-assistant-drawer 
      :visible="showAi" 
      :course-id="courseId" 
      :course-name="courseName"
      @close="showAi = false"
    />
  </view>
</template>

<script>
import { getAssignment, listAssignments, listAssignmentSubmissions, submitAssignment } from '@/api/assignment'
import { requireSession } from '@/common/session'
import LoadingState from '@/components/LoadingState.vue'
import EmptyState from '@/components/EmptyState.vue'
import AiAssistantDrawer from '@/components/AiAssistantDrawer.vue'

export default {
  components: { LoadingState, EmptyState, AiAssistantDrawer },
  data() {
    return {
      session: null,
      courseId: null,
      courseName: '',
      initialAssignmentId: 0,
      assignments: [],
      currentAssignment: null,
      mySubmission: null,
      submissionForm: {
        content: '',
        attachmentUrl: ''
      },
      loading: false,
      submitting: false,
      showAi: false
    }
  },
  onLoad(query) {
    const session = requireSession()
    if (!session) return
    this.session = session
    this.courseId = Number(query.courseId)
    this.courseName = query.courseName || '课程'
    this.initialAssignmentId = Number(query.assignmentId || 0)
    this.loadAssignments()
  },
  methods: {
    async loadAssignments() {
      this.loading = true
      try {
        const list = await listAssignments(this.courseId)
        this.assignments = list.filter(item => item.published === 1)
        if (this.initialAssignmentId) {
          const target = this.assignments.find(item => Number(item.id) === this.initialAssignmentId)
          if (target) {
            this.initialAssignmentId = 0
            await this.openAssignment(target)
          }
        }
      } finally {
        this.loading = false
      }
    },
    async openAssignment(item) {
      uni.showLoading({ title: '加载中...' })
      try {
        this.currentAssignment = await getAssignment(item.id)
        await this.loadMySubmission(item.id)
        uni.pageScrollTo({ scrollTop: 0, duration: 300 })
      } finally {
        uni.hideLoading()
      }
    },
    async loadMySubmission(assignmentId) {
      const list = await listAssignmentSubmissions(assignmentId)
      const mine = list.find(s => Number(s.studentId) === Number(this.session.userId)) || null
      this.mySubmission = mine
      this.submissionForm = {
        content: mine ? (mine.content || '') : '',
        attachmentUrl: mine ? (mine.attachmentUrl || '') : ''
      }
    },
    async submitCurrentAssignment() {
      if (!this.submissionForm.content.trim()) {
        uni.showToast({ title: '请输入内容', icon: 'none' })
        return
      }
      this.submitting = true
      try {
        await submitAssignment(this.currentAssignment.id, {
          studentId: this.session.userId,
          content: this.submissionForm.content,
          attachmentUrl: this.submissionForm.attachmentUrl
        })
        await this.loadMySubmission(this.currentAssignment.id)
        uni.showToast({ title: '提交成功 ✨', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: '提交失败', icon: 'none' })
      } finally {
        this.submitting = false
      }
    },
    formatDate(d) {
      return d ? d.split(' ')[0] : '未设置'
    },
    formatFullDate(d) {
      return d || '刚刚'
    }
  }
}
</script>

<style scoped>
.page {
  padding: 100rpx 40rpx 60rpx;
  background: #f8faff;
  min-height: 100vh;
}

/* 顶部样式 */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
}

.course-name {
  display: block;
  font-size: 24rpx;
  color: #a0aec0;
  font-weight: 600;
}

.page-title {
  display: block;
  font-size: 48rpx;
  font-weight: 800;
  color: #2d3748;
}

.icon-btn {
  width: 80rpx;
  height: 80rpx;
  background: #ffffff;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 20rpx rgba(0,0,0,0.03);
  font-size: 32rpx;
  border: none;
}

/* 作业列表 */
.assignment-stack {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.assignment-card {
  background: #ffffff;
  border-radius: 50rpx;
  display: flex;
  padding: 40rpx;
  gap: 30rpx;
  align-items: center;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.02);
}

.card-icon-box {
  width: 110rpx;
  height: 110rpx;
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-emoji { font-size: 56rpx; }
.card-main { flex: 1; }

.assignment-title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #2d3748;
  margin-bottom: 12rpx;
}

.assignment-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.deadline-tag {
  font-size: 22rpx;
  font-weight: 800;
  color: #ef4444;
  background: #fef2f2;
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
  align-self: flex-start;
}

.info-desc {
  font-size: 24rpx;
  color: #a0aec0;
}

.card-arrow {
  font-size: 32rpx;
  color: #cbd5e0;
  font-weight: 800;
}

.card-style-0 .card-icon-box { background: #fff1f1; }
.card-style-1 .card-icon-box { background: #eff6ff; }
.card-style-2 .card-icon-box { background: #fffaf0; }
.card-style-3 .card-icon-box { background: #f0fff4; }

/* 作业详情与表单 */
.active-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 40rpx;
}

.back-btn {
  font-size: 26rpx;
  color: #7c3aed;
  font-weight: 800;
}

.active-title {
  font-size: 32rpx;
  font-weight: 800;
  color: #2d3748;
}

.detail-card, .status-card, .form-card {
  background: #ffffff;
  border-radius: 50rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.02);
}

.detail-title {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  color: #2d3748;
  margin-bottom: 16rpx;
}

.detail-meta { margin-bottom: 30rpx; }
.meta-item { font-size: 24rpx; color: #a0aec0; font-weight: 600; }

.body-label {
  display: block;
  font-size: 24rpx;
  font-weight: 800;
  color: #7c3aed;
  margin-bottom: 12rpx;
}

.body-content {
  font-size: 28rpx;
  color: #4a5568;
  line-height: 1.6;
}

.section-divider {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
  padding: 0 10rpx;
}

.divider-text {
  font-size: 22rpx;
  font-weight: 800;
  color: #cbd5e0;
  text-transform: uppercase;
  letter-spacing: 2rpx;
}

/* 状态显示 */
.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.status-pill {
  font-size: 22rpx;
  padding: 8rpx 24rpx;
  border-radius: 999rpx;
  font-weight: 800;
}

.pill-scored { background: #f0fff4; color: #38a169; }
.pill-pending { background: #fffaf0; color: #d69e2e; }

.submit-time { font-size: 22rpx; color: #cbd5e0; }

.score-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin: 20rpx 0;
}

.score-num { font-size: 80rpx; font-weight: 900; color: #7c3aed; }
.score-unit { font-size: 32rpx; font-weight: 800; color: #7c3aed; margin-left: 8rpx; }

.feedback-box {
  background: #f8fafc;
  padding: 24rpx;
  border-radius: 24rpx;
  margin-top: 30rpx;
}

.feedback-label { font-size: 22rpx; font-weight: 800; color: #4a5568; display: block; margin-bottom: 8rpx; }
.feedback-text { font-size: 26rpx; color: #718096; line-height: 1.6; }

/* 表单控件 */
.input-group { margin-bottom: 30rpx; }
.group-label { display: block; font-size: 24rpx; font-weight: 800; color: #a0aec0; margin-bottom: 16rpx; }

.form-textarea {
  width: 100%;
  height: 240rpx;
  background: #f8faff;
  border-radius: 30rpx;
  padding: 30rpx;
  font-size: 28rpx;
  color: #2d3748;
  box-sizing: border-box;
}

.form-input {
  width: 100%;
  height: 100rpx;
  background: #f8faff;
  border-radius: 30rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
  color: #2d3748;
  box-sizing: border-box;
}

.submit-btn {
  background: linear-gradient(135deg, #7c3aed, #a855f7);
  color: #ffffff;
  height: 110rpx;
  line-height: 110rpx;
  border-radius: 40rpx;
  font-size: 32rpx;
  font-weight: 800;
  border: none;
  box-shadow: 0 20rpx 40rpx rgba(124, 58, 237, 0.2);
}

/* 悬浮按钮 */
.ai-fab {
  position: fixed;
  right: 40rpx;
  bottom: 60rpx;
  background: linear-gradient(135deg, #7c3aed, #a855f7);
  padding: 20rpx 30rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
  box-shadow: 0 20rpx 40rpx rgba(124, 58, 237, 0.3);
  z-index: 50;
}

.ai-icon { font-size: 40rpx; }
.ai-label { color: #ffffff; font-size: 24rpx; font-weight: 800; }

.animated-fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(20rpx); } to { opacity: 1; transform: translateY(0); } }
</style>
