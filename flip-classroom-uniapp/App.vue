<script>
export default {
  onLaunch() {
    console.log('flip-classroom-uniapp launched')
  }
}
</script>

<style>
:root {
  /* iOS 风格基准色 */
  --fc-bg: #F2F2F7; /* iOS System Gray 6 */
  --fc-bg-deep: #1C1C1E; /* 深色背景 */
  --fc-surface: #ffffff;
  --fc-surface-soft: #F9FAFB; /* 柔和表面 */
  --fc-text: #000000;
  --fc-text-soft: #8E8E93; /* iOS System Gray */
  --fc-accent: #007AFF; /* iOS Blue */
  --fc-accent-soft: #E5F1FF; /* 浅蓝背景 */
  --fc-primary: #007AFF; /* 主色 */
  --fc-border: #D1D1D6; /* iOS System Gray 4 */
  --fc-border-light: #E5E5EA; /* iOS System Gray 5 */
  --fc-success: #34C759; /* iOS Green */
  --fc-warning: #FF9500; /* iOS Orange */
  --fc-danger: #FF3B30; /* iOS Red */

  --fc-radius-xl: 60rpx;
  --fc-radius-lg: 40rpx;
  --fc-radius-md: 24rpx;
  --fc-radius-sm: 16rpx;

  --fc-shadow-sm: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  --fc-shadow-md: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  --fc-shadow-lg: 0 16rpx 48rpx rgba(0, 0, 0, 0.12);

  --fc-font-body: "SF Pro Rounded", "SF Pro Text", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei UI", "Microsoft YaHei", -apple-system, BlinkMacSystemFont, "Helvetica Neue", sans-serif;
  --fc-font-display: "SF Pro Rounded", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei UI", "Microsoft YaHei", -apple-system, BlinkMacSystemFont, "Helvetica Neue", sans-serif;
}

page {
  background-color: var(--fc-bg);
  color: var(--fc-text);
  font-size: 28rpx;
  font-family: var(--fc-font-body);
  -webkit-font-smoothing: antialiased;
}

view,
text,
button,
input,
textarea,
picker {
  box-sizing: border-box;
}

button::after {
  border: none;
}

/* iOS 风格主按钮 */
.fc-primary-btn {
  background: var(--fc-accent);
  color: #ffffff;
  font-weight: 600;
  border-radius: var(--fc-radius-md);
  transition: opacity 0.2s;
}

.fc-primary-btn:active {
  opacity: 0.7;
}

/* 玻璃感面板 */
.fc-panel {
  background: var(--fc-surface);
  border-radius: var(--fc-radius-lg);
  border: 1px solid var(--fc-border-light);
}

.fc-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8rpx 20rpx;
  border-radius: 12rpx;
  background: var(--fc-border-light);
  color: var(--fc-text-soft);
  font-size: 22rpx;
  font-weight: 500;
}

/* 次要按钮 */
.fc-secondary-btn {
  background: var(--fc-surface);
  color: var(--fc-text);
  font-weight: 600;
  border: 1px solid var(--fc-border);
  border-radius: var(--fc-radius-md);
  transition: all 0.2s;
}

.fc-secondary-btn:active {
  background: var(--fc-bg);
}

/* 强调按钮 */
.fc-accent-btn {
  background: var(--fc-accent-soft);
  color: var(--fc-accent);
  font-weight: 700;
  border: none;
  border-radius: var(--fc-radius-md);
  transition: all 0.2s;
}

.fc-accent-btn:active {
  opacity: 0.8;
}

/* 标题样式 */
.fc-section-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: var(--fc-text);
  letter-spacing: -0.5rpx;
}

.fc-caption {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: var(--fc-text-soft);
  line-height: 1.6;
}

/* 页面容器 */
.fc-page {
  min-height: 100vh;
  background: var(--fc-bg);
}
</style>
