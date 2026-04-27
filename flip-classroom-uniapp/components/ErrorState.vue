<template>
  <view class="error-state">
    <text class="error-icon">⚠️</text>
    <text class="error-title">{{ title || '加载失败' }}</text>
    <text class="error-desc">{{ desc || '请检查网络连接后重试' }}</text>
    <button v-if="showRetry" class="fc-secondary-btn retry-btn" @click="handleRetry">重新加载</button>
  </view>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      default: '加载失败'
    },
    desc: {
      type: String,
      default: '请检查网络连接后重试'
    },
    showRetry: {
      type: Boolean,
      default: true
    }
  },
  methods: {
    handleRetry() {
      this.$emit('retry')
    }
  }
}
</script>

<style scoped>
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
  text-align: center;
}

.error-icon {
  font-size: 120rpx;
  line-height: 1;
  opacity: 0.6;
}

.error-title {
  margin-top: 32rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: var(--fc-text);
}

.error-desc {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: var(--fc-text-soft);
  line-height: 1.6;
  max-width: 500rpx;
}

.retry-btn {
  margin-top: 40rpx;
  padding: 0 48rpx;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 28rpx;
}
</style>
