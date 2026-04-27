<template>
  <view class="loading-state">
    <view class="loading-spinner"></view>
    <text class="loading-text">{{ text || '加载中...' }}</text>
  </view>
</template>

<script>
export default {
  props: {
    text: {
      type: String,
      default: '加载中...'
    }
  }
}
</script>

<style scoped>
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
}

.loading-spinner {
  width: 80rpx;
  height: 80rpx;
  border: 6rpx solid var(--fc-border-light);
  border-top-color: var(--fc-accent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 32rpx;
  font-size: 26rpx;
  color: var(--fc-text-soft);
}
</style>
