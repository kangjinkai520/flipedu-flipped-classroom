<template>
  <view class="fc-page page">
    <!-- 顶部状态栏 -->
    <view class="header-section">
      <view class="header-content">
        <text class="course-name">{{ courseName }}</text>
        <text class="page-title">教学资料库</text>
      </view>
      <view class="header-actions">
        <button class="icon-btn" @click="loadMaterials">🔄</button>
      </view>
    </view>

    <!-- 搜索与筛选 -->
    <view class="search-section">
      <view class="search-bar">
        <text class="search-icon">🔍</text>
        <input v-model="keyword" class="search-input" placeholder="搜索课件、视频或文档..." />
      </view>
    </view>

    <loading-state v-if="loading" text="正在整理学习资料..." />

    <view v-else-if="filteredMaterials.length" class="material-grid">
      <view 
        v-for="item in filteredMaterials" 
        :key="item.id" 
        class="material-card"
        @click="openMaterial(item)"
      >
        <view :class="['file-icon-box', getFileColor(item.materialType)]">
          <text class="file-emoji">{{ getFileEmoji(item.materialType) }}</text>
        </view>
        <view class="card-info">
          <text class="file-title">{{ item.title }}</text>
          <view class="file-meta">
            <text class="type-tag">{{ item.materialType.toUpperCase() }}</text>
            <text class="dot">·</text>
            <text class="date-text">{{ formatDate(item.publishTime) }}</text>
          </view>
        </view>
        <view class="arrow-box">
          <text class="arrow">→</text>
        </view>
      </view>
    </view>

    <empty-state 
      v-else 
      icon="📂" 
      title="这里空空如也" 
      desc="老师还没上传任何资料，先去看看别的模块吧。"
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
import { listMaterials } from '@/api/material'
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
      materials: [],
      loading: false,
      keyword: '',
      showAi: false
    }
  },
  computed: {
    filteredMaterials() {
      const k = this.keyword.toLowerCase().trim()
      if (!k) return this.materials
      return this.materials.filter(m => m.title.toLowerCase().includes(k))
    }
  },
  onLoad(query) {
    const session = requireSession()
    if (!session) return
    this.courseId = Number(query.courseId)
    this.courseName = query.courseName || '课程'
    this.loadMaterials()
  },
  methods: {
    async loadMaterials() {
      this.loading = true
      try {
        const list = await listMaterials(this.courseId)
        this.materials = (list || []).filter(item => item.status === 1)
      } catch (e) {
        uni.showToast({ title: '获取资料失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    openMaterial(item) {
      if (!item.materialUrl) {
        uni.showToast({ title: '该资料暂无有效地址', icon: 'none' })
        return
      }
      uni.setClipboardData({
        data: item.materialUrl,
        success: () => {
          uni.showModal({
            title: '资料链接已复制',
            content: '由于移动端限制，请在浏览器中打开此链接查看或下载资料。',
            showCancel: false
          })
        }
      })
    },
    getFileEmoji(type) {
      const map = { pdf: '📄', video: '🎬', ppt: '📊', link: '🔗', doc: '📝' }
      return map[type.toLowerCase()] || '📁'
    },
    getFileColor(type) {
      const map = { pdf: 'red', video: 'blue', ppt: 'orange', link: 'green', doc: 'indigo' }
      return `bg-${map[type.toLowerCase()] || 'gray'}`
    },
    formatDate(dateStr) {
      if (!dateStr) return '未知时间'
      return dateStr.split(' ')[0]
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

/* 搜索栏 */
.search-section {
  margin-bottom: 40rpx;
}

.search-bar {
  background: #ffffff;
  height: 100rpx;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  padding: 0 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.02);
  border: 4rpx solid #f1f5f9;
}

.search-icon { font-size: 32rpx; margin-right: 20rpx; }
.search-input { flex: 1; font-size: 28rpx; color: #2d3748; }

/* 资料列表 */
.material-grid {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.material-card {
  background: #ffffff;
  padding: 30rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.02);
  transition: transform 0.2s;
}

.material-card:active { transform: scale(0.98); }

.file-icon-box {
  width: 100rpx;
  height: 100rpx;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-emoji { font-size: 48rpx; }

.card-info { flex: 1; }

.file-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 8rpx;
}

.file-meta {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.type-tag {
  font-size: 20rpx;
  font-weight: 800;
  color: #a0aec0;
}

.dot { color: #cbd5e0; }

.date-text {
  font-size: 22rpx;
  color: #cbd5e0;
}

.arrow-box {
  width: 60rpx;
  height: 60rpx;
  background: #f8fafc;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrow { font-size: 24rpx; color: #a0aec0; font-weight: 800; }

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

/* 颜色类 */
.bg-red { background: #fff1f1; }
.bg-blue { background: #f0f7ff; }
.bg-orange { background: #fffaf0; }
.bg-green { background: #f0fff4; }
.bg-indigo { background: #f5f3ff; }
.bg-gray { background: #f8fafc; }
</style>
