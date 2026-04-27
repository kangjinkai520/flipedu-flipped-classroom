<template>
  <view class="fc-page page">
    <!-- 顶部状态栏 -->
    <view class="header-section">
      <view class="header-content">
        <text class="course-name">{{ courseName }}</text>
        <text class="page-title">成绩反馈 🏆</text>
      </view>
      <view class="header-actions">
        <button class="icon-btn" @click="loadScores">🔄</button>
      </view>
    </view>

    <loading-state v-if="loading" text="正在生成你的成绩报表..." />

    <view v-else class="content-body">
      <!-- 成绩总览大卡片 -->
      <view class="score-hero-card">
        <view class="hero-left">
          <text class="hero-label">综合平均分</text>
          <view class="hero-score-row">
            <text class="hero-score">{{ normalizedAverageScore }}</text>
            <text class="hero-unit">分</text>
          </view>
          <view class="hero-rank">
            <text class="rank-tag">{{ getRankLabel }}</text>
          </view>
        </view>
        <view class="hero-right">
          <text class="medal-emoji">{{ getMedalEmoji }}</text>
        </view>
        <!-- 装饰背景 -->
        <view class="hero-circle-1"></view>
        <view class="hero-circle-2"></view>
      </view>

      <!-- 快捷统计区 -->
      <view class="stat-grid">
        <view class="stat-mini-card bg-orange">
          <text class="mini-val">{{ scores.length }}</text>
          <text class="mini-label">已录入记录</text>
        </view>
        <view class="stat-mini-card bg-pink">
          <text class="mini-val">{{ passCount }}</text>
          <text class="mini-label">及格项目</text>
        </view>
      </view>

      <!-- 成绩明细列表 -->
      <view class="detail-section">
        <text class="section-title">成绩明细</text>
        <view v-if="scores.length" class="score-stack">
          <view 
            v-for="(item, index) in scores" 
            :key="item.id" 
            :class="['score-item-card', `card-style-${index % 4}`]"
            @click="openScoreItem(item)"
          >
            <view class="item-left">
              <text class="item-type-badge">{{ item.itemType === 'quiz' ? '测验' : '作业' }}</text>
              <text class="item-name">{{ item.itemName }}</text>
              <text class="item-date">原始分: {{ item.score }} / {{ itemFullScore(item) }}</text>
            </view>
            <view class="item-right">
              <view :class="['score-circle', getScoreColorClass(itemNormalizedScore(item))]">
                <text class="val">{{ Math.round(itemNormalizedScore(item)) }}</text>
              </view>
            </view>
            <!-- 底部百分制进度条 -->
            <view class="item-progress-bar">
              <view 
                class="progress-fill" 
                :style="{ width: itemNormalizedScore(item) + '%', background: getScoreHex(itemNormalizedScore(item)) }"
              ></view>
            </view>
          </view>
        </view>
        <empty-state 
          v-else 
          icon="📊" 
          title="暂时没有成绩" 
          desc="完成测验或作业并由教师评分后，这里会自动同步。"
        />
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
import { listUserScores } from '@/api/score'
import { listQuizzes } from '@/api/quiz'
import { listAssignments } from '@/api/assignment'
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
      scores: [],
      itemMetaMap: {},
      loading: false,
      showAi: false
    }
  },
  computed: {
    normalizedAverageScore() {
      if (!this.scores.length) return '0.0'
      const total = this.scores.reduce((sum, item) => sum + Number(this.itemNormalizedScore(item)), 0)
      return (total / this.scores.length).toFixed(1)
    },
    passCount() {
      return this.scores.filter(item => this.itemNormalizedScore(item) >= 60).length
    },
    getRankLabel() {
      const avg = parseFloat(this.normalizedAverageScore)
      if (avg >= 90) return '学术大牛'
      if (avg >= 80) return '表现出色'
      if (avg >= 60) return '继续努力'
      return '待提升'
    },
    getMedalEmoji() {
      const avg = parseFloat(this.normalizedAverageScore)
      if (avg >= 90) return '🥇'
      if (avg >= 80) return '🥈'
      if (avg >= 60) return '🥉'
      return '🎗️'
    }
  },
  onLoad(query) {
    const session = requireSession()
    if (!session) return
    this.session = session
    this.courseId = Number(query.courseId)
    this.courseName = query.courseName || '课程'
    this.loadScores()
  },
  methods: {
    async loadScores() {
      this.loading = true
      try {
        const [scores, quizzes, assignments] = await Promise.all([
          listUserScores(this.session.userId, this.courseId),
          listQuizzes(this.courseId),
          listAssignments(this.courseId)
        ])

        const metaMap = {}
        quizzes.forEach(item => {
          metaMap[`quiz::${item.title}`] = { id: item.id, fullScore: Number(item.totalScore || 100) }
        })
        assignments.forEach(item => {
          metaMap[`assignment::${item.title}`] = { id: item.id, fullScore: 100 }
        })

        this.itemMetaMap = metaMap
        this.scores = scores
      } finally {
        this.loading = false
      }
    },
    itemFullScore(item) {
      const meta = this.itemMetaMap[`${item.itemType}::${item.itemName}`]
      return meta ? meta.fullScore : 100
    },
    itemNormalizedScore(item) {
      const full = this.itemFullScore(item)
      return (((item.score || 0) / full) * 100).toFixed(1)
    },
    getScoreColorClass(score) {
      const s = parseFloat(score)
      if (s >= 90) return 'circle-excellent'
      if (s >= 60) return 'circle-pass'
      return 'circle-fail'
    },
    getScoreHex(score) {
      const s = parseFloat(score)
      if (s >= 90) return '#10b981'
      if (s >= 60) return '#7c3aed'
      return '#ef4444'
    },
    openScoreItem(item) {
      const meta = this.itemMetaMap[`${item.itemType}::${item.itemName}`]
      if (!meta || !meta.id) {
        uni.showToast({ title: '暂未找到对应详情', icon: 'none' })
        return
      }

      const courseName = encodeURIComponent(this.courseName || '')
      if (item.itemType === 'quiz') {
        uni.navigateTo({
          url: `/pages/student/quizzes?courseId=${this.courseId}&courseName=${courseName}&quizId=${meta.id}&review=1`
        })
        return
      }

      uni.navigateTo({
        url: `/pages/student/assignments?courseId=${this.courseId}&courseName=${courseName}&assignmentId=${meta.id}`
      })
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

/* 成绩大卡片 */
.score-hero-card {
  background: linear-gradient(135deg, #06b6d4, #3b82f6);
  border-radius: 60rpx;
  padding: 50rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba(6, 182, 212, 0.2);
  margin-bottom: 40rpx;
}

.hero-left { position: relative; z-index: 2; }
.hero-label { font-size: 24rpx; color: rgba(255,255,255,0.8); font-weight: 700; }
.hero-score-row { display: flex; align-items: baseline; margin: 10rpx 0; }
.hero-score { font-size: 80rpx; font-weight: 900; color: #ffffff; }
.hero-unit { font-size: 32rpx; color: #ffffff; margin-left: 10rpx; font-weight: 700; }
.hero-rank { display: inline-block; background: rgba(255,255,255,0.2); padding: 6rpx 24rpx; border-radius: 999rpx; }
.rank-tag { color: #ffffff; font-size: 22rpx; font-weight: 800; }

.medal-emoji { font-size: 120rpx; position: relative; z-index: 2; }

.hero-circle-1 { position: absolute; top: -40rpx; right: -40rpx; width: 240rpx; height: 240rpx; background: rgba(255,255,255,0.1); border-radius: 50%; }
.hero-circle-2 { position: absolute; bottom: -80rpx; left: 100rpx; width: 180rpx; height: 180rpx; background: rgba(255,255,255,0.05); border-radius: 50%; }

/* 迷你卡片网格 */
.stat-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 30rpx; margin-bottom: 50rpx; }
.stat-mini-card { padding: 30rpx; border-radius: 40rpx; display: flex; flex-direction: column; gap: 8rpx; }
.mini-val { font-size: 40rpx; font-weight: 900; color: #2d3748; }
.mini-label { font-size: 22rpx; color: #a0aec0; font-weight: 700; }

.bg-orange { background: #fff7ed; }
.bg-pink { background: #fdf2f8; }

/* 列表部分 */
.section-title { display: block; margin-bottom: 30rpx; font-size: 32rpx; font-weight: 800; color: #2d3748; }
.score-stack { display: flex; flex-direction: column; gap: 30rpx; }

.score-item-card {
  background: #ffffff;
  border-radius: 50rpx;
  padding: 40rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.02);
}

.item-left { flex: 1; display: flex; flex-direction: column; gap: 8rpx; }
.item-type-badge { font-size: 18rpx; background: #f1f5f9; color: #64748b; padding: 4rpx 16rpx; border-radius: 999rpx; align-self: flex-start; font-weight: 800; }
.item-name { font-size: 30rpx; font-weight: 800; color: #2d3748; margin-top: 4rpx; }
.item-date { font-size: 22rpx; color: #cbd5e0; font-weight: 600; }

.score-circle {
  width: 90rpx;
  height: 90rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 6rpx solid #f1f5f9;
}

.score-circle .val { font-size: 32rpx; font-weight: 900; }

.circle-excellent { border-color: #d1fae5; color: #10b981; }
.circle-pass { border-color: #f5f3ff; color: #7c3aed; }
.circle-fail { border-color: #fee2e2; color: #ef4444; }

.item-progress-bar { position: absolute; bottom: 0; left: 0; right: 0; height: 8rpx; background: #f1f5f9; }
.progress-fill { height: 100%; transition: width 0.6s ease; }

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
</style>
