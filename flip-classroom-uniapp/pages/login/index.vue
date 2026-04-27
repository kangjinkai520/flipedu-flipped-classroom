<template>
  <view class="dispatching-screen">
    <!-- 智能分发器：不显示界面，只处理跳转 -->
  </view>
</template>

<script>
export default {
  onLoad() {
    this.dispatchLogin()
  },
  methods: {
    dispatchLogin() {
      // #ifdef MP-WEIXIN
      // 微信小程序 -> 学生登录页
      uni.redirectTo({
        url: '/pages/login/student'
      })
      // #endif

      // #ifndef MP-WEIXIN
      // H5/Web/其他 -> 管理端/教师 Web 登录页
      uni.redirectTo({
        url: '/pages/login/web'
      })
      // #endif
    }
  }
}
</script>

<style scoped>
.dispatching-screen {
  min-height: 100vh;
  background: #ffffff;
}
</style>
