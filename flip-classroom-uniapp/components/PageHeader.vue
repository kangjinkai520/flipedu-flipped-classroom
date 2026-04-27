<template>
  <view class="page-header">
    <view class="header-copy">
      <text v-if="label" class="header-label">{{ label }}</text>
      <text class="header-title">{{ title }}</text>
      <text v-if="subtitle" class="header-subtitle">{{ subtitle }}</text>
    </view>
    <slot name="action"></slot>
  </view>
</template>

<script>
export default {
  props: {
    label: {
      type: String,
      default: ''
    },
    title: {
      type: String,
      required: true
    },
    subtitle: {
      type: String,
      default: ''
    }
  }
}
</script>

<style scoped>
.page-header {
  padding: 40rpx 32rpx;
  background: var(--fc-surface);
  border-bottom: 1px solid var(--fc-border);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-copy {
  flex: 1;
}

.header-label {
  display: block;
  font-size: 24rpx;
  color: var(--fc-text-soft);
  font-weight: 500;
  margin-bottom: 8rpx;
}

.header-title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: var(--fc-text);
  letter-spacing: -0.5rpx;
  font-family: var(--fc-font-display);
}

.header-subtitle {
  display: block;
  margin-top: 12rpx;
  color: var(--fc-text-soft);
  line-height: 1.6;
  font-size: 26rpx;
}
</style>
