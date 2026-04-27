<template>
  <view class="fc-page page">
    <!-- 顶部状态栏 -->
    <view class="header-section">
      <view class="header-content">
        <text class="course-name">{{ courseName }}</text>
        <text class="page-title">随堂测验 ✍️</text>
      </view>
      <view class="header-actions">
        <button class="icon-btn" @click="loadQuizzes">🔄</button>
      </view>
    </view>

    <loading-state v-if="loading" text="正在准备测验题目..." />

    <!-- 测验列表模式 -->
    <view v-else-if="!currentQuiz" class="content-body">
      <view v-if="quizzes.length" class="quiz-stack">
        <view 
          v-for="(item, index) in quizzes" 
          :key="item.id" 
          :class="['quiz-card', `card-style-${index % 4}`]"
          @click="openQuiz(item)"
        >
          <view class="card-icon-box">
            <text class="card-emoji">📝</text>
          </view>
          <view class="card-main">
            <text class="quiz-title">{{ item.title }}</text>
            <view class="quiz-info">
              <text class="info-tag">总分 {{ item.totalScore }}</text>
              <text class="info-desc">{{ item.description || '点击开始作答' }}</text>
            </view>
          </view>
          <view class="card-arrow">→</view>
        </view>
      </view>
      <empty-state 
        v-else 
        icon="🧘" 
        title="目前没有测验" 
        desc="老师还没有发布随堂测验，放松一下吧。"
      />
    </view>

    <!-- 答题模式 -->
    <view v-else class="quiz-active-area animated-fade-in">
      <view class="active-header">
        <view class="back-btn" @click="currentQuiz = null">← 退出</view>
        <text class="active-title">{{ currentQuiz.title }}</text>
        <view class="score-pill">满分 {{ currentQuiz.totalScore }}</view>
      </view>

      <view class="question-list">
        <view v-for="question in questions" :key="question.id" class="question-card">
          <view class="q-header">
            <text class="q-num">第 {{ question.sortOrder }} 题</text>
            <text class="q-score">{{ question.score }} 分</text>
          </view>
          <text class="q-title">{{ question.title }}</text>
          
          <view class="options-group">
            <view 
              v-for="opt in getOptions(question)" 
              :key="opt.value"
              :class="optionClass(question, opt.value)"
              @click="setAnswer(question.id, opt.value)"
            >
              <text class="opt-label">{{ opt.value }}</text>
              <text class="opt-text">{{ opt.text }}</text>
              <view v-if="answers[question.id] === opt.value && !reviewMode" class="opt-check">✓</view>
            </view>
          </view>
          <view v-if="reviewMode" class="answer-review">
            <text class="review-line">我的答案：{{ answers[question.id] || '未作答' }}</text>
            <text class="review-line">正确答案：{{ question.correctAnswer }}</text>
            <text :class="['review-result', isQuestionCorrect(question) ? 'right' : 'wrong']">
              {{ isQuestionCorrect(question) ? '回答正确' : '回答错误' }}
            </text>
          </view>
        </view>
      </view>

      <view class="submit-footer">
        <view v-if="reviewMode && reviewRecord" class="review-summary">
          <text class="summary-score">最终得分：{{ reviewRecord.score }} / {{ currentQuiz.totalScore }}</text>
          <text class="summary-time">提交时间：{{ reviewRecord.submittedAt }}</text>
        </view>
        <button v-else class="submit-btn" :disabled="submitting" @click="submitCurrentQuiz">
          {{ submitting ? '提交中...' : '确认提交测验' }}
        </button>
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
import { listQuizQuestions, listQuizRecords, listQuizzes, submitQuiz } from '@/api/quiz'
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
      initialQuizId: 0,
      initialReview: false,
      quizzes: [],
      currentQuiz: null,
      questions: [],
      answers: {},
      reviewMode: false,
      reviewRecord: null,
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
    this.initialQuizId = Number(query.quizId || 0)
    this.initialReview = String(query.review || '') === '1'
    this.loadQuizzes()
  },
  methods: {
    async loadQuizzes() {
      this.loading = true
      try {
        const list = await listQuizzes(this.courseId)
        this.quizzes = list.filter(item => item.status === 1)
        if (this.initialQuizId) {
          const target = this.quizzes.find(item => Number(item.id) === this.initialQuizId)
          if (target) {
            this.initialQuizId = 0
            await this.openQuiz(target)
          }
        }
      } finally {
        this.loading = false
      }
    },
    async openQuiz(item) {
      uni.showLoading({ title: '加载题目...' })
      try {
        this.questions = await listQuizQuestions(item.id)
        this.currentQuiz = item
        this.answers = {}
        this.reviewMode = false
        this.reviewRecord = null
        if (this.initialReview) {
          await this.loadReviewRecord(item.id)
          this.initialReview = false
        }
        uni.pageScrollTo({ scrollTop: 0, duration: 300 })
      } finally {
        uni.hideLoading()
      }
    },
    async loadReviewRecord(quizId) {
      const records = await listQuizRecords(quizId)
      const mine = (records || []).find(record => Number(record.studentId) === Number(this.session.userId))
      if (!mine) {
        uni.showToast({ title: '暂未找到测验记录', icon: 'none' })
        return
      }
      this.reviewRecord = mine
      this.reviewMode = true
      try {
        this.answers = JSON.parse(mine.answersJson || '{}')
      } catch (error) {
        this.answers = {}
      }
    },
    getOptions(q) {
      return [
        { value: 'A', text: q.optionA },
        { value: 'B', text: q.optionB },
        { value: 'C', text: q.optionC },
        { value: 'D', text: q.optionD }
      ]
    },
    setAnswer(qId, val) {
      if (this.reviewMode) return
      this.$set(this.answers, qId, val)
    },
    optionClass(question, value) {
      const classes = ['option-item']
      if (!this.reviewMode && this.answers[question.id] === value) {
        classes.push('option-active')
      }
      if (this.reviewMode && question.correctAnswer === value) {
        classes.push('option-correct')
      }
      if (this.reviewMode && this.answers[question.id] === value && question.correctAnswer !== value) {
        classes.push('option-wrong')
      }
      return classes
    },
    isQuestionCorrect(question) {
      return String(this.answers[question.id] || '').toUpperCase() === String(question.correctAnswer || '').toUpperCase()
    },
    async submitCurrentQuiz() {
      if (Object.keys(this.answers).length < this.questions.length) {
        const ok = await new Promise(resolve => {
          uni.showModal({
            title: '提示',
            content: '还有题目没做完，确定要提交吗？',
            success: (res) => resolve(res.confirm)
          })
        })
        if (!ok) return
      }

      this.submitting = true
      try {
        const result = await submitQuiz(this.currentQuiz.id, {
          studentId: this.session.userId,
          answers: this.answers
        })
        uni.showModal({
          title: '测验已完成 ✨',
          content: `得分：${result.score}\n提交时间：${result.submittedAt}`,
          showCancel: false,
          success: () => {
            this.currentQuiz = null
            this.loadQuizzes()
          }
        })
      } finally {
        this.submitting = false
      }
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

/* 测验列表卡片 */
.quiz-stack {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.quiz-card {
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

.quiz-title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #2d3748;
  margin-bottom: 12rpx;
}

.quiz-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.info-tag {
  font-size: 22rpx;
  font-weight: 800;
  color: #7c3aed;
  background: #f5f3ff;
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

/* 激活答题区 */
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
  flex: 1;
  font-size: 32rpx;
  font-weight: 800;
  color: #2d3748;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.score-pill {
  font-size: 22rpx;
  background: #7c3aed;
  color: #ffffff;
  padding: 6rpx 20rpx;
  border-radius: 999rpx;
  font-weight: 700;
}

.question-card {
  background: #ffffff;
  border-radius: 50rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.02);
}

.q-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.q-num { font-size: 24rpx; color: #a0aec0; font-weight: 800; }
.q-score { font-size: 24rpx; color: #7c3aed; font-weight: 800; }

.q-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #2d3748;
  line-height: 1.6;
  margin-bottom: 40rpx;
}

.options-group {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.option-item {
  background: #f8faff;
  padding: 30rpx;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  position: relative;
  transition: all 0.2s;
  border: 4rpx solid transparent;
}

.opt-label {
  width: 60rpx;
  height: 60rpx;
  background: #ffffff;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  color: #7c3aed;
  box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.05);
}

.opt-text {
  font-size: 28rpx;
  color: #4a5568;
  flex: 1;
}

.option-active {
  background: #f5f3ff;
  border-color: #7c3aed;
}

.option-active .opt-label {
  background: #7c3aed;
  color: #ffffff;
}

.option-correct {
  background: #ecfdf5;
  border-color: #10b981;
}

.option-correct .opt-label {
  background: #10b981;
  color: #ffffff;
}

.option-wrong {
  background: #fef2f2;
  border-color: #ef4444;
}

.option-wrong .opt-label {
  background: #ef4444;
  color: #ffffff;
}

.opt-check {
  font-size: 32rpx;
  color: #7c3aed;
  font-weight: 800;
}

.answer-review {
  margin-top: 24rpx;
  padding: 24rpx;
  background: #ffffff;
  border-radius: 24rpx;
  border: 2rpx solid #eef2ff;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.review-line {
  font-size: 24rpx;
  color: #475569;
  font-weight: 700;
}

.review-result {
  font-size: 26rpx;
  font-weight: 900;
}

.review-result.right { color: #10b981; }
.review-result.wrong { color: #ef4444; }

.submit-footer {
  margin-top: 60rpx;
  padding-bottom: 40rpx;
}

.review-summary {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 30rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.03);
}

.summary-score {
  font-size: 34rpx;
  color: #7c3aed;
  font-weight: 900;
}

.summary-time {
  font-size: 24rpx;
  color: #64748b;
  font-weight: 700;
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
