<template>
  <view class="fc-page page">
    <!-- 统一页面头部 -->
    <page-header
      :label="courseName"
      title="成绩汇总中心"
      subtitle="按测验或作业条目进行数据透视，实时监控班级平均分与参与度。"
    >
      <template #action>
        <button class="fc-secondary-btn refresh-btn" @click="loadScores" :disabled="loading">
          {{ loading ? '...' : '刷新' }}
        </button>
      </template>
    </page-header>

    <view class="content-body">
      <!-- 汇总概览卡片 -->
      <view class="summary-overview" v-if="!loading && groupedScores.length">
        <view class="stat-card">
          <text class="stat-label">参与项目</text>
          <text class="stat-value">{{ groupedScores.length }}</text>
        </view>
        <view class="stat-card">
          <text class="stat-label">覆盖学生</text>
          <text class="stat-value">{{ studentCount }}</text>
        </view>
      </view>

      <!-- 分类筛选器 -->
      <view class="filter-tabs">
        <view 
          v-for="tab in tabs" 
          :key="tab.value" 
          :class="['tab-item', filterType === tab.value ? 'tab-active' : '']"
          @click="filterType = tab.value"
        >
          <text class="tab-text">{{ tab.label }}</text>
          <view class="tab-line" v-if="filterType === tab.value"></view>
        </view>
      </view>

      <loading-state v-if="loading" text="正在生成成绩报表..." />

      <block v-else-if="filteredScores.length">
        <view class="score-list">
          <view
            v-for="(item, index) in filteredScores"
            :key="item.groupKey"
            class="score-item-card"
          >
            <view class="item-header">
              <view class="title-area">
                <text :class="['type-badge', item.itemType === 'quiz' ? 'badge-quiz' : 'badge-assignment']">
                  {{ item.itemType === 'quiz' ? '测验' : '作业' }}
                </text>
                <text class="item-name">{{ item.itemName }}</text>
              </view>
              <view class="item-count">
                <text class="count-num">{{ item.recordCount }}</text>
                <text class="count-unit">份记录</text>
              </view>
            </view>

            <view class="metrics-grid">
              <view class="metric-box">
                <text class="metric-label">平均得分</text>
                <text class="metric-val primary-val">{{ item.averageScore }}</text>
              </view>
              <view class="metric-box">
                <text class="metric-label">最高分</text>
                <text class="metric-val">{{ item.maxScore }}</text>
              </view>
              <view class="metric-box">
                <text class="metric-label">最低分</text>
                <text class="metric-val">{{ item.minScore }}</text>
              </view>
              <view class="metric-box">
                <text class="metric-label">参与人数</text>
                <text class="metric-val">{{ item.studentCount }}</text>
              </view>
            </view>

            <view class="item-footer">
              <text class="remark-text">数据来源：{{ item.itemType === 'quiz' ? '学生自助提交' : '教师手动批阅' }}</text>
              <button class="detail-link" @click="viewDetails(item)">
                查看详情 <text class="arrow">→</text>
              </button>
            </view>
          </view>
        </view>
      </block>

      <empty-state 
        v-else 
        icon="📊" 
        title="暂无汇总数据" 
        desc="学生完成任务并产生评分后，此处将自动生成多维度的成绩分析报表。"
      />
    </view>
  </view>
</template>

<script>
import PageHeader from '@/components/PageHeader.vue'
import LoadingState from '@/components/LoadingState.vue'
import EmptyState from '@/components/EmptyState.vue'
import { listCourseScores } from '@/api/score'
import { requireSession } from '@/common/session'

export default {
  components: {
    PageHeader,
    LoadingState,
    EmptyState
  },
  data() {
    return {
      courseId: null,
      courseName: '',
      scores: [],
      loading: false,
      filterType: 'all',
      tabs: [
        { label: '全部项目', value: 'all' },
        { label: '随堂测验', value: 'quiz' },
        { label: '课后作业', value: 'assignment' }
      ]
    }
  },
  computed: {
    groupedScores() {
      const groups = new Map()
      this.scores.forEach((item) => {
        const groupKey = `${item.itemType}::${item.itemName}`
        if (!groups.has(groupKey)) {
          groups.set(groupKey, {
            groupKey,
            itemType: item.itemType,
            itemName: item.itemName,
            totalScore: 0,
            recordCount: 0,
            maxScore: -Infinity,
            minScore: Infinity,
            studentIds: new Set()
          })
        }
        const current = groups.get(groupKey)
        const score = Number(item.score) || 0
        current.totalScore += score
        current.recordCount += 1
        current.maxScore = Math.max(current.maxScore, score)
        current.minScore = Math.min(current.minScore, score)
        current.studentIds.add(item.studentId)
      })
      
      return Array.from(groups.values()).map((item) => ({
        ...item,
        studentCount: item.studentIds.size,
        averageScore: (item.totalScore / item.recordCount).toFixed(1),
        maxScore: item.maxScore === -Infinity ? 0 : item.maxScore,
        minScore: item.minScore === Infinity ? 0 : item.minScore
      }))
    },
    filteredScores() {
      if (this.filterType === 'all') return this.groupedScores
      return this.groupedScores.filter(s => s.itemType === this.filterType)
    },
    studentCount() {
      const set = new Set(this.scores.map((item) => item.studentId))
      return set.size
    }
  },
  onLoad(query) {
    const session = requireSession()
    if (!session) return
    this.courseId = Number(query.courseId)
    this.courseName = query.courseName || '课程'
    this.loadScores()
  },
  methods: {
    async loadScores() {
      this.loading = true
      try {
        this.scores = await listCourseScores(this.courseId)
      } catch (e) {
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    viewDetails(item) {
      uni.showToast({
        title: '详情功能开发中',
        icon: 'none'
      })
    }
  }
}
</script>

<style scoped>
.page {
  background: var(--fc-bg);
  min-height: 100vh;
}

.refresh-btn {
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 24rpx;
  font-size: 24rpx;
  border-radius: 12rpx;
}

.content-body {
  padding: 32rpx;
}

/* 概览卡片 */
.summary-overview {
  display: flex;
  gap: 20rpx;
  margin-bottom: 40rpx;
}

.stat-card {
  flex: 1;
  background: #ffffff;
  padding: 32rpx;
  border-radius: 24rpx;
  border: 1px solid var(--fc-border-light);
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.02);
}

.stat-label {
  display: block;
  font-size: 22rpx;
  color: var(--fc-text-soft);
  margin-bottom: 8rpx;
}

.stat-value {
  font-size: 40rpx;
  font-weight: 800;
  color: var(--fc-accent);
}

/* 筛选器 */
.filter-tabs {
  display: flex;
  background: #ffffff;
  padding: 8rpx;
  border-radius: 16rpx;
  margin-bottom: 32rpx;
  border: 1px solid var(--fc-border-light);
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  position: relative;
  transition: all 0.2s;
}

.tab-text {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--fc-text-soft);
}

.tab-active .tab-text {
  color: var(--fc-accent);
}

.tab-line {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 4rpx;
  background: var(--fc-accent);
  border-radius: 2rpx;
}

/* 列表卡片 */
.score-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.score-item-card {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 32rpx;
  border: 1px solid var(--fc-border-light);
  box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.03);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32rpx;
}

.title-area {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.type-badge {
  align-self: flex-start;
  font-size: 20rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-weight: 700;
}

.badge-quiz { background: #e0f2fe; color: #0284c7; }
.badge-assignment { background: #fef3c7; color: #d97706; }

.item-name {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--fc-text);
}

.item-count {
  text-align: right;
}

.record-badge {
  display: flex;
  align-items: baseline;
  background: var(--fc-bg);
  padding: 8rpx 20rpx;
  border-radius: 100rpx;
  border: 1px solid var(--fc-border-light);
  gap: 4rpx;
}

.record-label {
  font-size: 20rpx;
  color: var(--fc-text-soft);
  font-weight: 600;
}

.record-val {
  font-size: 32rpx;
  font-weight: 800;
  color: var(--fc-text);
  line-height: 1;
}

.record-unit {
  font-size: 20rpx;
  color: var(--fc-text-soft);
}

/* 指标网格 */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
  background: var(--fc-bg);
  padding: 24rpx;
  border-radius: 20rpx;
  margin-bottom: 24rpx;
}

.metric-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.metric-label {
  font-size: 20rpx;
  color: var(--fc-text-soft);
  margin-bottom: 8rpx;
}

.metric-val {
  font-size: 28rpx;
  font-weight: 700;
  color: var(--fc-text);
}

.primary-val {
  color: var(--fc-accent);
  font-size: 32rpx;
}

/* 卡片页脚 */
.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 24rpx;
  border-top: 1px dashed var(--fc-border-light);
}

.remark-text {
  font-size: 22rpx;
  color: var(--fc-text-soft);
}

.detail-link {
  background: none;
  padding: 0;
  margin: 0;
  border: none;
  font-size: 24rpx;
  font-weight: 700;
  color: var(--fc-accent);
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.arrow { font-size: 28rpx; }
</style>
