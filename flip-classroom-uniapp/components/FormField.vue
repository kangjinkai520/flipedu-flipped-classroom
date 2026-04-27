<template>
  <view class="form-field">
    <text class="field-label">{{ label }}<text v-if="required" class="required-mark">*</text></text>
    <slot></slot>
  </view>
</template>

<script>
export default {
  props: {
    label: {
      type: String,
      required: true
    },
    required: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style scoped>
.form-field {
  margin-bottom: 32rpx;
}

.field-label {
  display: block;
  font-size: 26rpx;
  font-weight: 600;
  color: var(--fc-text);
  margin-bottom: 16rpx;
  margin-left: 8rpx;
}

.required-mark {
  color: var(--fc-danger);
  margin-left: 4rpx;
}
</style>
