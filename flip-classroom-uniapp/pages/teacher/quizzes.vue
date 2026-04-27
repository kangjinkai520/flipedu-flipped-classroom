<template>
  <view class="fc-page page">
    <!-- 顶部状态栏 -->
    <view class="header-section">
      <view class="header-content">
        <text class="course-name">{{ courseName }}</text>
        <text class="page-title">随堂测验库</text>
      </view>
      <view class="header-actions">
        <button class="fc-secondary-btn icon-btn" @click="loadQuizzes">🔄</button>
        <button class="fc-primary-btn add-btn" @click="toggleEditor">
          {{ showEditor ? '收起' : '+ 创建测验' }}
        </button>
      </view>
    </view>

    <!-- 折叠式向导编辑器 -->
    <view v-if="showEditor" class="fc-panel editor-card animated-slide-down">
      <view class="editor-steps">
        <text :class="['step-item', activeStep === 0 ? 'step-active' : '']" @click="activeStep = 0">1. 基本信息</text>
        <text :class="['step-item', activeStep === 1 ? 'step-active' : '']" @click="activeStep = 1">2. 编写题目 ({{ form.questions.length }})</text>
      </view>
      
      <view v-if="activeStep === 0" class="form-body animated-fade-in">
        <view class="input-group">
          <text class="label">测验名称</text>
          <input v-model="form.title" class="input" placeholder="例如：Java 基础语法测验" />
        </view>
        <view class="input-group">
          <text class="label">测验说明</text>
          <textarea v-model="form.description" class="textarea" placeholder="填写测验要求、时间限制等..." />
        </view>
        <view class="form-row">
          <view class="input-group flex-1">
            <text class="label">总分</text>
            <input v-model="form.totalScore" class="input" type="number" />
          </view>
          <view class="input-group flex-1">
            <text class="label">状态</text>
            <view class="status-switch" @click="form.status = form.status === 1 ? 0 : 1">
              <view :class="['switch-dot', form.status === 1 ? 'dot-on' : '']"></view>
              <text class="switch-text">{{ form.status === 1 ? '立即发布' : '存为草稿' }}</text>
            </view>
          </view>
        </view>
        <button class="fc-primary-btn next-btn" @click="activeStep = 1">下一步：编写题目</button>
      </view>

      <view v-if="activeStep === 1" class="form-body animated-fade-in">
        <view class="questions-editor">
          <view v-for="(q, idx) in form.questions" :key="idx" class="q-edit-box">
            <view class="q-edit-head">
              <text class="q-idx">第 {{ idx + 1 }} 题</text>
              <text class="q-del" @click="removeQuestion(idx)">删除</text>
            </view>
            <input v-model="q.title" class="q-input-title" placeholder="输入题目内容..." />
            <view class="options-grid">
              <input v-model="q.optionA" class="opt-input" placeholder="A. 选项" />
              <input v-model="q.optionB" class="opt-input" placeholder="B. 选项" />
              <input v-model="q.optionC" class="opt-input" placeholder="C. 选项" />
              <input v-model="q.optionD" class="opt-input" placeholder="D. 选项" />
            </view>
            <view class="q-footer">
              <view class="ans-box">
                <text class="ans-label">正确答案:</text>
                <picker :range="['A','B','C','D']" :value="['A','B','C','D'].indexOf(q.correctAnswer)" @change="e => q.correctAnswer = ['A','B','C','D'][e.detail.value]">
                  <view class="ans-val">{{ q.correctAnswer }}</view>
                </picker>
              </view>
              <view class="score-box">
                <text class="ans-label">分值:</text>
                <input v-model="q.score" type="number" class="score-input" />
              </view>
            </view>
          </view>
          <button class="add-q-btn" @click="addQuestion">+ 继续添加题目</button>
        </view>
        <view class="editor-footer">
          <button class="fc-primary-btn submit-btn" @click="submitQuiz">确认发布测验</button>
          <button class="fc-secondary-btn cancel-btn" @click="activeStep = 0">返回修改信息</button>
        </view>
      </view>
    </view>

    <!-- 测验列表 -->
    <view class="list-section">
      <view v-if="quizzes.length" class="quiz-stack">
        <view v-for="item in quizzes" :key="item.id" class="quiz-item">
          <view class="quiz-main">
            <view class="quiz-icon-box">📝</view>
            <view class="quiz-info">
              <view class="quiz-top">
                <text class="quiz-title">{{ item.title }}</text>
                <text :class="['status-pill', item.status === 1 ? 'pill-blue' : 'pill-gray']">
                  {{ item.status === 1 ? '开放中' : '已关闭' }}
                </text>
              </view>
              <view class="quiz-meta-row">
                <text class="meta-tag">💯 总分 {{ item.totalScore }}</text>
                <text class="meta-tag">⏰ {{ item.publishTime || '刚才' }}</text>
              </view>
            </view>
          </view>
          
          <view class="quiz-actions">
            <button class="q-action-btn" @click="loadQuestions(item.id)">题目预览</button>
            <button class="q-action-btn" @click="loadRecords(item.id)">成绩统计</button>
            <button class="q-action-btn btn-danger" @click="toggleStatus(item)">{{ item.status === 1 ? '停用' : '开启' }}</button>
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <text class="empty-icon">🍵</text>
        <text class="empty-text">这里空空如也，快去出一套题吧</text>
      </view>
    </view>

    <!-- 成绩/题目详情抽屉 -->
    <view v-if="selectedQuizId" class="drawer-overlay" @click="selectedQuizId = null">
      <view class="drawer-content animated-slide-up" @click.stop>
        <view class="drawer-handle"></view>
        <view class="tabs-header">
          <text :class="['tab-item', activeDetailTab === 'q' ? 'tab-active' : '']" @click="activeDetailTab = 'q'">题目列表</text>
          <text :class="['tab-item', activeDetailTab === 'r' ? 'tab-active' : '']" @click="activeDetailTab = 'r'">学生成绩</text>
        </view>

        <scroll-view scroll-y class="drawer-scroll">
          <!-- 题目视图 -->
          <view v-if="activeDetailTab === 'q'" class="questions-list">
            <view v-for="(q, i) in questions" :key="q.id" class="q-detail-item">
              <text class="q-detail-title">{{ i + 1 }}. {{ q.title }} ({{ q.score }}分)</text>
              <view class="q-detail-opts">
                <text :class="['opt-text', q.correctAnswer === 'A' ? 'opt-correct' : '']">A. {{ q.optionA }}</text>
                <text :class="['opt-text', q.correctAnswer === 'B' ? 'opt-correct' : '']">B. {{ q.optionB }}</text>
                <text :class="['opt-text', q.correctAnswer === 'C' ? 'opt-correct' : '']">C. {{ q.optionC }}</text>
                <text :class="['opt-text', q.correctAnswer === 'D' ? 'opt-correct' : '']">D. {{ q.optionD }}</text>
              </view>
            </view>
          </view>
          <!-- 成绩视图 -->
          <view v-if="activeDetailTab === 'r'" class="records-list">
            <view v-for="r in records" :key="r.id" class="record-row">
              <view class="r-user-info">
                <text class="r-name">{{ r.studentName }}</text>
                <text class="r-id">{{ r.studentUsername }}</text>
              </view>
              <text :class="['r-score', r.score >= 60 ? 'text-green' : 'text-red']">{{ r.score }}分</text>
            </view>
            <view v-if="!records.length" class="empty-mini">暂无提交记录</view>
          </view>
        </scroll-view>
        <button class="close-btn" @click="selectedQuizId = null">返回</button>
      </view>
    </view>
  </view>
</template>

<script>
import { createQuiz, listQuizQuestions, listQuizRecords, listQuizzes, updateQuizStatus } from '@/api/quiz'
import { createReview } from '@/api/review'
import { requireSession } from '@/common/session'

function createQuestion(index) {
  return { type: 'single', title: '', optionA: '', optionB: '', optionC: '', optionD: '', correctAnswer: 'A', score: 10, sortOrder: index + 1 }
}
function createForm() {
  return { title: '', description: '', totalScore: 100, status: 1, publishTime: '', questions: [createQuestion(0)] }
}

export default {
  data() {
    return {
      courseId: null,
      courseName: '',
      quizzes: [],
      questions: [],
      records: [],
      selectedQuizId: null,
      showEditor: false,
      activeStep: 0,
      activeDetailTab: 'q',
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
    this.loadQuizzes()
  },
  methods: {
    toggleEditor() {
      this.showEditor = !this.showEditor
      if (!this.showEditor) this.resetForm()
    },
    resetForm() {
      this.form = createForm()
      this.activeStep = 0
      this.showEditor = false
    },
    async loadQuizzes() {
      this.quizzes = await listQuizzes(this.courseId)
    },
    addQuestion() { this.form.questions.push(createQuestion(this.form.questions.length)) },
    removeQuestion(idx) { this.form.questions.splice(idx, 1) },
    async submitQuiz() {
      if (!this.form.title) return uni.showToast({ title: '标题不能为空', icon: 'none' })
      const payload = { ...this.form, status: 0 }
      const created = await createQuiz(this.courseId, payload)
      await createReview({
        targetType: 'QUIZ',
        targetId: created.id,
        actionType: 'PUBLISH',
        requesterId: this.session.userId,
        title: this.form.title,
        summary: '教师提交课堂测验发布审核'
      })
      this.resetForm()
      this.loadQuizzes()
      uni.showToast({ title: '已提交审核', icon: 'none' })
    },
    async toggleStatus(item) {
      if (item.status !== 1) {
        await createReview({
          targetType: 'QUIZ',
          targetId: item.id,
          actionType: 'PUBLISH',
          requesterId: this.session.userId,
          title: item.title,
          summary: '教师申请发布课堂测验'
        })
        uni.showToast({ title: '已提交审核', icon: 'none' })
        return
      }
      await updateQuizStatus(item.id, 0)
      this.loadQuizzes()
    },
    async loadQuestions(id) {
      this.selectedQuizId = id
      this.activeDetailTab = 'q'
      this.questions = await listQuizQuestions(id)
    },
    async loadRecords(id) {
      this.selectedQuizId = id
      this.activeDetailTab = 'r'
      this.records = await listQuizRecords(id)
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

/* 编辑器向导 */
.editor-card { background: #fff; border-radius: 32rpx; padding: 32rpx; margin-bottom: 40rpx; border: 1px solid var(--fc-border-light); }
.editor-steps { display: flex; gap: 24rpx; margin-bottom: 40rpx; border-bottom: 1px solid #f1f5f9; padding-bottom: 20rpx; }
.step-item { font-size: 24rpx; font-weight: 700; color: var(--fc-text-soft); }
.step-active { color: var(--fc-accent); border-bottom: 4rpx solid var(--fc-accent); padding-bottom: 16rpx; }

.label { font-size: 22rpx; font-weight: 700; color: var(--fc-text-soft); margin-bottom: 12rpx; display: block; }
.input, .textarea { background: var(--fc-bg); border-radius: 16rpx; padding: 24rpx; font-size: 28rpx; box-sizing: border-box; width: 100%; }
.textarea { height: 160rpx; }
.form-row { display: flex; gap: 24rpx; margin-top: 24rpx; }
.flex-1 { flex: 1; }
.status-switch { display: flex; align-items: center; gap: 16rpx; height: 88rpx; background: var(--fc-bg); border-radius: 16rpx; padding: 0 24rpx; }
.switch-dot { width: 14rpx; height: 14rpx; border-radius: 50%; background: #ccc; }
.dot-on { background: #27ae60; }
.next-btn { margin-top: 40rpx; height: 90rpx; }

/* 题目编辑器 */
.questions-editor { max-height: 60vh; overflow-y: scroll; padding-right: 10rpx; }
.q-edit-box { background: var(--fc-bg); padding: 24rpx; border-radius: 24rpx; margin-bottom: 24rpx; }
.q-edit-head { display: flex; justify-content: space-between; margin-bottom: 16rpx; }
.q-idx { font-size: 24rpx; font-weight: 800; color: var(--fc-text); }
.q-del { font-size: 22rpx; color: #ef4444; font-weight: 700; }
.q-input-title { font-size: 28rpx; font-weight: 700; margin-bottom: 20rpx; border-bottom: 1px solid #ddd; padding: 10rpx 0; }
.options-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16rpx; }
.opt-input { background: #fff; padding: 16rpx; border-radius: 12rpx; font-size: 24rpx; }
.q-footer { display: flex; gap: 24rpx; margin-top: 20rpx; align-items: center; }
.ans-box, .score-box { display: flex; align-items: center; gap: 8rpx; }
.ans-label { font-size: 22rpx; color: #666; font-weight: 600; }
.ans-val { background: #fff; padding: 8rpx 24rpx; border-radius: 8rpx; font-weight: 800; color: var(--fc-accent); border: 1px solid #eee; }
.score-input { width: 80rpx; background: #fff; padding: 8rpx; border-radius: 8rpx; font-size: 24rpx; text-align: center; }
.add-q-btn { background: #fff; border: 1px dashed var(--fc-accent); color: var(--fc-accent); font-size: 24rpx; font-weight: 700; margin-top: 20rpx; }
.editor-footer { display: flex; gap: 16rpx; margin-top: 32rpx; }

/* 测验列表 */
.quiz-stack { display: flex; flex-direction: column; gap: 24rpx; }
.quiz-item { background: #fff; border-radius: 36rpx; padding: 32rpx; border: 1px solid var(--fc-border-light); }
.quiz-main { display: flex; gap: 24rpx; align-items: center; }
.quiz-icon-box { width: 90rpx; height: 90rpx; border-radius: 24rpx; background: #fff7ed; display: flex; align-items: center; justify-content: center; font-size: 40rpx; }
.quiz-info { flex: 1; }
.quiz-top { display: flex; justify-content: space-between; align-items: center; }
.quiz-title { font-size: 32rpx; font-weight: 800; color: var(--fc-text); }
.status-pill { font-size: 20rpx; font-weight: 800; padding: 4rpx 16rpx; border-radius: 8rpx; }
.pill-blue { background: #eef2ff; color: #4f46e5; }
.pill-gray { background: #f3f4f6; color: #6b7280; }
.quiz-meta-row { display: flex; gap: 20rpx; margin-top: 12rpx; }
.meta-tag { font-size: 22rpx; color: var(--fc-text-soft); font-weight: 600; }
.quiz-actions { display: flex; gap: 16rpx; margin-top: 32rpx; padding-top: 24rpx; border-top: 1px solid var(--fc-border-light); }
.q-action-btn { flex: 1; height: 72rpx; line-height: 72rpx; font-size: 24rpx; font-weight: 700; border-radius: 16rpx; background: var(--fc-bg); color: var(--fc-text); border: none; }
.btn-danger { color: #ef4444; background: #fef2f2; }

/* 抽屉详情 */
.drawer-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.4); backdrop-filter: blur(4px); z-index: 1000; }
.drawer-content { position: absolute; left: 0; right: 0; bottom: 0; background: #fff; border-radius: 48rpx 48rpx 0 0; padding: 32rpx; box-shadow: 0 -10rpx 40rpx rgba(0,0,0,0.1); }
.drawer-handle { width: 80rpx; height: 8rpx; background: #eee; border-radius: 999rpx; margin: 0 auto 32rpx; }
.tabs-header { display: flex; border-bottom: 1px solid #f1f5f9; margin-bottom: 32rpx; }
.tab-item { flex: 1; text-align: center; padding-bottom: 24rpx; font-size: 28rpx; font-weight: 700; color: var(--fc-text-soft); }
.tab-active { color: var(--fc-accent); border-bottom: 4rpx solid var(--fc-accent); }
.drawer-scroll { max-height: 60vh; }
.q-detail-item { padding: 24rpx; border-bottom: 1px solid #f8fafc; }
.q-detail-title { font-size: 28rpx; font-weight: 700; display: block; margin-bottom: 16rpx; }
.q-detail-opts { display: flex; flex-direction: column; gap: 8rpx; }
.opt-text { font-size: 24rpx; color: #666; }
.opt-correct { color: #27ae60; font-weight: 700; }
.record-row { display: flex; justify-content: space-between; align-items: center; padding: 24rpx; border-bottom: 1px solid #f8fafc; }
.r-name { font-size: 28rpx; font-weight: 700; display: block; }
.r-id { font-size: 22rpx; color: #999; }
.r-score { font-size: 32rpx; font-weight: 800; }
.text-green { color: #27ae60; }
.text-red { color: #ef4444; }
.close-btn { height: 100rpx; line-height: 100rpx; background: var(--fc-bg); border-radius: 24rpx; font-size: 30rpx; font-weight: 700; color: var(--fc-text-soft); border: none; margin-top: 32rpx; }

.empty-state { padding: 120rpx 0; text-align: center; }
.empty-icon { font-size: 100rpx; display: block; margin-bottom: 24rpx; }
.empty-text { color: var(--fc-text-soft); font-size: 28rpx; font-weight: 600; }

.animated-slide-down { animation: slideDown 0.3s ease-out; }
.animated-slide-up { animation: slideUp 0.3s ease-out; }
.animated-fade-in { animation: fadeIn 0.4s ease-out; }

@keyframes slideDown { from { opacity: 0; transform: translateY(-20rpx); } to { opacity: 1; transform: translateY(0); } }
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
</style>
