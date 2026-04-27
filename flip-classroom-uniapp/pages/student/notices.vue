<template>
  <view class="fc-page page">
    <!-- 顶部状态栏 -->
    <view class="header-section">
      <view class="header-content">
        <text class="course-name">{{ courseName }}</text>
        <text class="page-title">课程通知 🔔</text>
      </view>
      <view class="header-actions">
        <button class="icon-btn" @click="loadNotices">🔄</button>
      </view>
    </view>

    <!-- 装饰性的统计卡片 -->
    <view class="stats-banner">
      <view class="stat-item">
        <text class="stat-num">{{ notices.length }}</text>
        <text class="stat-label">全部通知</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-num">{{ recentNoticeCount }}</text>
        <text class="stat-label">最近更新</text>
      </view>
    </view>

    <loading-state v-if="loading" text="正在获取最新公告..." />

    <view v-else-if="notices.length" class="notice-stack">
      <view 
        v-for="(item, index) in notices" 
        :key="item.id" 
        :class="['notice-card', `card-style-${index % 4}`]"
      >
        <view class="card-side-icon">
          <text class="side-emoji">{{ getNoticeEmoji(index) }}</text>
        </view>
        <view class="card-main">
          <view class="card-top">
            <text class="notice-title">{{ item.title }}</text>
            <text class="notice-time">{{ formatDate(item.publishTime) }}</text>
          </view>
          <view class="notice-body">
            <text class="notice-content">{{ item.content || '暂无详细内容' }}</text>
          </view>
          <view class="card-footer">
            <view class="tag-pill">课程公告</view>
            <view class="read-btn">
              <text class="read-text">已阅读</text>
              <text class="check-icon">✓</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <empty-state 
      v-else 
      icon="📭" 
      title="暂时没有通知" 
      desc="老师还没发布任何通知，可以先去看看资料或测验哦。"
    />

    <!-- 全局 AI 助手悬浮按钮 -->
    <view class="ai-fab" @click="showAi = true">
      <text class="ai-icon">🤖</text>
      <text class="ai-label">AI 助手</text>
    </view>

    <!-- AI 侧边栏插件 -->
    <ai-assistant-drawer 
      :visible="showAi" 
      :course-id="courseId" 
      :course-name="courseName"
      @close="showAi = false"
    />
  </view>
</template>

<script>
import { listNotices } from '@/api/notice'
import { requireSession } from '@/common/session'
import LoadingState from '@/components/LoadingState.vue'
import EmptyState from '@/components/EmptyState.vue'
import AiAssistantDrawer from '@/components/AiAssistantDrawer.vue'

export default {
  components: {
    LoadingState,
    EmptyState,
    AiAssistantDrawer
  },
  data() {
    return {
      courseId: null,
      courseName: '',
      notices: [],
      loading: false,
      showAi: false
    }
  },
  computed: {
    recentNoticeCount() {
      // 简单逻辑：假设最后两条为最近更新
      return Math.min(this.notices.length, 2)
    }
  },
  onLoad(query) {
    const session = requireSession()
    if (!session) return
    this.courseId = Number(query.courseId)
    this.courseName = query.courseName || '课程'
    this.loadNotices()
  },
  methods: {
    async loadNotices() {
      this.loading = true
      try {
        const list = await listNotices(this.courseId)
        this.notices = list.filter((item) => item.status === 1)
      } catch (e) {
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return '刚刚'
      return dateStr.split(' ')[0]
    },
    getNoticeEmoji(index) {
      const emojis = ['📢', '💡', '🗓️', '📌']
      return emojis[index % emojis.length]
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

/* 统计条 */
.stats-banner {
  background: #ffffff;
  padding: 30rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin-bottom: 40rpx;
  box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.02);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-num {
  font-size: 40rpx;
  font-weight: 800;
  color: #7c3aed;
}

.stat-label {
  font-size: 22rpx;
  color: #a0aec0;
  font-weight: 600;
  margin-top: 4rpx;
}

.stat-divider {
  width: 2rpx;
  height: 40rpx;
  background: #edf2f7;
}

/* 通知卡片 */
.notice-stack {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.notice-card {
  background: #ffffff;
  border-radius: 50rpx;
  display: flex;
  padding: 30rpx;
  gap: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.02);
  border: 1px solid rgba(0,0,0,0.01);
}

.card-side-icon {
  width: 90rpx;
  height: 90rpx;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.side-emoji { font-size: 44rpx; }

.card-main { flex: 1; }

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12rpx;
}

.notice-title {
  font-size: 32rpx;
  font-weight: 800;
  color: #2d3748;
  flex: 1;
  padding-right: 20rpx;
}

.notice-time {
  font-size: 20rpx;
  color: #cbd5e0;
  font-weight: 600;
}

.notice-body {
  margin-bottom: 24rpx;
}

.notice-content {
  font-size: 26rpx;
  color: #718096;
  line-height: 1.6;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tag-pill {
  font-size: 20rpx;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background: #f7fafc;
  color: #a0aec0;
  font-weight: 700;
}

.read-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.read-text { font-size: 22rpx; color: #48bb78; font-weight: 700; }
.check-icon { font-size: 22rpx; color: #48bb78; }

/* 不同卡片风格 */
.card-style-0 .card-side-icon { background: #fff1f1; }
.card-style-1 .card-side-icon { background: #eff6ff; }
.card-style-2 .card-style-0 .card-side-icon { background: #fffaf0; }
.card-style-3 .card-side-icon { background: #f5f3ff; }

/* 悬浮按钮 */
.ai-fab {
  position: fixed;
  right: 40rpx;
  bottom: 60rpx;
  background: linear-gradient(135deg, #7c3aed, #a855f7);
  padding: 20rpx 40rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
  box-shadow: 0 20rpx 40rpx rgba(124, 58, 237, 0.3);
  z-index: 50;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10rpx); }
}

.ai-icon { font-size: 40rpx; }
.ai-label { color: #ffffff; font-size: 24rpx; font-weight: 800; }
</style>
