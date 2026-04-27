<template>
  <view class="empty-state">
    <text class="empty-icon">{{ icon || '📭' }}</text>
    <text class="empty-title">{{ title || '暂无数据' }}</text>
    <text class="empty-desc">{{ desc || '这里还没有内容' }}</text>
    <slot></slot>
  </view>
</template>

<script>
export default {
  props: {
    icon: {
      type: String,
      default: '📭'
    },
    title: {
      type: String,
      default: '暂无数据'
    },
    desc: {
      type: String,
      default: '这里还没有内容'
    }
  }
}
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
  text-align: center;
}

.empty-icon {
  font-size: 120rpx;
  line-height: 1;
  opacity: 0.5;
}

.empty-title {
  margin-top: 32rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: var(--fc-text);
}

.empty-desc {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: var(--fc-text-soft);
  line-height: 1.6;
  max-width: 500rpx;
}
</style>
