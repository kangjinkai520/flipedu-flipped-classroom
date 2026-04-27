<template>
  <view class="fc-page page">
    <view class="header-section">
      <view>
        <text class="course-name">{{ courseName }}</text>
        <text class="page-title">课程签到</text>
      </view>
      <button class="refresh-btn" @click="loadTasks">刷新</button>
    </view>

    <loading-state v-if="loading" text="正在加载签到任务..." />

    <view v-else-if="tasks.length" class="content-body">
      <view class="sign-hero-card" v-for="item in [tasks[0]]" :key="item.id">
        <view class="task-info-bar">
          <text class="task-badge">{{ item.locationEnabled ? '定位签到' : '普通签到' }}</text>
          <text class="task-title-main">{{ item.title }}</text>
          <text class="task-time">截止 {{ formatTime(item.endsAt) }}</text>
        </view>

        <view class="pulse-container">
          <view :class="['pulse-circle', isSigned(item.id) ? 'circle-success' : 'circle-active']" @click="handleSignClick(item)">
            <view class="inner-circle">
              <text class="circle-text">{{ getRecordText(item.id) }}</text>
              <text class="circle-sub">{{ isSigned(item.id) ? '查看下方记录' : '点击提交签到' }}</text>
            </view>
            <view v-if="!isSigned(item.id)" class="pulse-ring ring-1"></view>
            <view v-if="!isSigned(item.id)" class="pulse-ring ring-2"></view>
          </view>
        </view>

        <view v-if="!isSigned(item.id)" class="input-section">
          <view class="code-input-box">
            <text class="input-label">签到码</text>
            <input
              v-model="signCodes[item.id]"
              class="real-input"
              placeholder="请输入签到码"
              maxlength="6"
              type="number"
            />
          </view>
        </view>

        <view v-if="item.locationEnabled && !isSigned(item.id)" class="exception-area">
          <view class="exception-toggle" @click="showException = !showException">
            <text>无法到达定位范围？</text>
            <text class="toggle-link">{{ showException ? '收起' : '申请异常签到' }}</text>
          </view>
          <view v-if="showException" class="exception-form">
            <textarea
              v-model="exceptionReasons[item.id]"
              class="exception-input"
              placeholder="说明原因，例如设备定位失败、外出审批、网络异常等"
            />
            <button class="exception-btn" @click="submitException(item)">提交异常申请</button>
          </view>
        </view>
      </view>
    </view>

    <empty-state
      v-else
      icon="签到"
      title="暂无进行中的签到"
      desc="老师发布签到后会显示在这里"
    />

    <view class="history-section" v-if="tasks.length">
      <text class="section-title">签到记录</text>
      <view class="history-card" v-for="item in tasks" :key="'info-'+item.id">
        <view class="history-main">
          <text class="history-title">{{ item.title }}</text>
          <text class="history-time">截止 {{ formatTime(item.endsAt) }}</text>
          <text v-if="item.locationName" class="history-loc">{{ item.locationName }}</text>
        </view>
        <view class="history-side">
          <text :class="['status-tag', getRecordClass(item.id)]">{{ getRecordText(item.id) }}</text>
          <text v-if="recordMap[item.id] && recordMap[item.id].distanceMeters !== null" class="distance-text">
            {{ formatDistance(recordMap[item.id].distanceMeters) }}
          </text>
        </view>
      </view>
    </view>

    <view class="ai-fab" @click="showAi = true">
      <text class="ai-label">AI 助手</text>
    </view>
    <ai-assistant-drawer
      :visible="showAi"
      :course-id="courseId"
      :course-name="courseName"
      @close="showAi = false"
    />
  </view>
</template>

<script>
import { listSignRecords, listSignTasks, submitSign } from '@/api/sign'
import { requireSession } from '@/common/session'
import LoadingState from '@/components/LoadingState.vue'
import EmptyState from '@/components/EmptyState.vue'
import AiAssistantDrawer from '@/components/AiAssistantDrawer.vue'

function getLocationPromise() {
  return new Promise((resolve, reject) => {
    uni.getLocation({ type: 'gcj02', success: resolve, fail: reject })
  })
}

export default {
  components: { LoadingState, EmptyState, AiAssistantDrawer },
  data() {
    return {
      session: null,
      courseId: null,
      courseName: '',
      tasks: [],
      recordMap: {},
      signCodes: {},
      exceptionReasons: {},
      loading: false,
      showAi: false,
      showException: false
    }
  },
  onLoad(query) {
    const session = requireSession()
    if (!session) return
    this.session = session
    this.courseId = Number(query.courseId)
    this.courseName = this.decodeCourseName(query.courseName) || '课程'
    this.loadTasks()
  },
  methods: {
    decodeCourseName(value) {
      if (!value) return ''
      try { return decodeURIComponent(value) } catch (e) { return value }
    },
    async loadTasks() {
      this.loading = true
      try {
        const list = await listSignTasks(this.courseId)
        this.tasks = (list || []).filter((item) => item.status === 'ONGOING')
        await this.loadMyRecords()
      } finally {
        this.loading = false
      }
    },
    async loadMyRecords() {
      const map = {}
      await Promise.all(this.tasks.map(async (task) => {
        try {
          const records = await listSignRecords(task.id)
          const mine = (records || []).find((record) => Number(record.studentId) === Number(this.session.userId))
          if (mine) map[task.id] = mine
        } catch (e) {
          // 学生端记录回显失败不阻断签到。
        }
      }))
      this.recordMap = map
    },
    isSigned(taskId) {
      return Boolean(this.recordMap[taskId])
    },
    getRecordText(taskId) {
      const record = this.recordMap[taskId]
      if (!record) return '立即签到'
      const map = {
        SIGNED: '已签到',
        PENDING_REVIEW: '待老师审核',
        APPROVED: '异常已通过',
        REJECTED: '异常未通过'
      }
      return map[record.status] || '已提交'
    },
    getRecordClass(taskId) {
      const record = this.recordMap[taskId]
      if (!record) return 'pending'
      if (record.status === 'REJECTED') return 'danger'
      if (record.status === 'PENDING_REVIEW') return 'warning'
      return 'success'
    },
    formatTime(t) {
      return t ? (t.split(' ')[1] || t) : '未设置'
    },
    formatDistance(value) {
      if (value === undefined || value === null || value === '') return ''
      return `${Number(value).toFixed(0)} 米`
    },
    getErrorMessage(error, fallback) {
      return (error && (error.message || error.errMsg)) || fallback
    },
    async handleSignClick(item) {
      if (this.isSigned(item.id)) return

      const code = this.signCodes[item.id]
      if (!code) {
        uni.showToast({ title: '请输入签到码', icon: 'none' })
        return
      }

      uni.showLoading({ title: item.locationEnabled ? '获取定位中...' : '提交中...' })
      try {
        const payload = { studentId: this.session.userId, signCode: code }
        if (item.locationEnabled) {
          let loc
          try {
            loc = await getLocationPromise()
          } catch (locationError) {
            this.showException = true
            uni.showModal({
              title: '定位获取失败',
              content: '请检查浏览器/系统定位权限。若当前设备无法定位，可以在下方提交异常签到申请。',
              showCancel: false
            })
            return
          }
          payload.latitude = loc.latitude
          payload.longitude = loc.longitude
        }

        const result = await submitSign(item.id, payload)
        this.recordMap = { ...this.recordMap, [item.id]: result.record || result }
        uni.showModal({
          title: '签到结果',
          content: result.message || '签到已提交',
          showCancel: false
        })
        this.loadMyRecords()
      } catch (error) {
        uni.showToast({ title: this.getErrorMessage(error, '签到失败，请重试'), icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
    async submitException(item) {
      const code = this.signCodes[item.id]
      const reason = this.exceptionReasons[item.id]
      if (!code || !reason) {
        uni.showToast({ title: '请填写签到码和异常原因', icon: 'none' })
        return
      }

      uni.showLoading({ title: '提交申请...' })
      try {
        const result = await submitSign(item.id, {
          studentId: this.session.userId,
          signCode: code,
          exceptionRequest: true,
          exceptionReason: reason
        })
        this.recordMap = { ...this.recordMap, [item.id]: result.record || result }
        uni.showModal({
          title: '已提交审核',
          content: '异常签到申请已提交，请等待老师审核。',
          showCancel: false
        })
        this.loadMyRecords()
      } catch (error) {
        uni.showToast({ title: this.getErrorMessage(error, '异常申请提交失败'), icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  }
}
</script>

<style scoped>
.page { padding: 96rpx 36rpx 60rpx; background: #f7f8fb; min-height: 100vh; }
.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 34rpx; }
.course-name { display: block; font-size: 24rpx; color: #64748b; font-weight: 700; }
.page-title { display: block; margin-top: 8rpx; font-size: 44rpx; font-weight: 900; color: #1f2937; }
.refresh-btn { margin: 0; height: 72rpx; line-height: 72rpx; padding: 0 26rpx; border-radius: 18rpx; border: none; background: #ffffff; color: #334155; font-weight: 800; }
.sign-hero-card { background: #ffffff; border-radius: 28rpx; padding: 40rpx 32rpx; border: 1rpx solid #e8ecf3; text-align: center; }
.task-info-bar { display: flex; flex-direction: column; align-items: center; gap: 12rpx; margin-bottom: 44rpx; }
.task-badge { font-size: 22rpx; background: #eef2ff; color: #4f46e5; padding: 8rpx 20rpx; border-radius: 999rpx; font-weight: 800; }
.task-title-main { font-size: 34rpx; font-weight: 900; color: #1f2937; }
.task-time { font-size: 23rpx; color: #94a3b8; }
.pulse-container { display: flex; justify-content: center; margin-bottom: 42rpx; }
.pulse-circle { width: 300rpx; height: 300rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; position: relative; }
.circle-active { background: #4f46e5; box-shadow: 0 18rpx 42rpx rgba(79, 70, 229, 0.28); }
.circle-success { background: #10b981; box-shadow: 0 18rpx 42rpx rgba(16, 185, 129, 0.24); }
.inner-circle { display: flex; flex-direction: column; align-items: center; gap: 8rpx; color: #ffffff; }
.circle-text { font-size: 34rpx; font-weight: 900; }
.circle-sub { font-size: 22rpx; opacity: 0.85; }
.pulse-ring { position: absolute; inset: 0; border-radius: 50%; border: 3rpx solid #818cf8; opacity: 0; z-index: -1; }
.ring-1 { animation: pulseAnim 3s ease-out infinite; }
.ring-2 { animation: pulseAnim 3s ease-out infinite 1.5s; }
@keyframes pulseAnim { 0% { transform: scale(1); opacity: 0.7; } 100% { transform: scale(1.55); opacity: 0; } }
.input-section { margin-bottom: 34rpx; }
.code-input-box { background: #f8fafc; border-radius: 22rpx; padding: 26rpx; }
.input-label { display: block; margin-bottom: 12rpx; font-size: 22rpx; color: #64748b; font-weight: 800; }
.real-input { font-size: 34rpx; font-weight: 900; color: #1f2937; text-align: center; }
.exception-area { border-top: 1rpx dashed #dbe3ef; padding-top: 26rpx; }
.exception-toggle { display: flex; justify-content: space-between; font-size: 24rpx; color: #64748b; font-weight: 700; }
.toggle-link { color: #4f46e5; }
.exception-form { margin-top: 20rpx; }
.exception-input { width: 100%; height: 150rpx; background: #f8fafc; border-radius: 18rpx; padding: 20rpx; font-size: 26rpx; color: #334155; box-sizing: border-box; }
.exception-btn { margin-top: 16rpx; height: 76rpx; line-height: 76rpx; border-radius: 18rpx; border: none; background: #fff1f2; color: #e11d48; font-size: 25rpx; font-weight: 800; }
.section-title { display: block; margin: 36rpx 0 20rpx; font-size: 28rpx; font-weight: 900; color: #334155; }
.history-card { background: #ffffff; border-radius: 22rpx; padding: 26rpx; display: flex; justify-content: space-between; gap: 20rpx; margin-bottom: 18rpx; border: 1rpx solid #e8ecf3; }
.history-main { flex: 1; }
.history-title { display: block; font-size: 28rpx; font-weight: 800; color: #1f2937; }
.history-time, .history-loc, .distance-text { display: block; margin-top: 6rpx; font-size: 22rpx; color: #64748b; }
.history-side { text-align: right; }
.status-tag { display: inline-block; padding: 8rpx 16rpx; border-radius: 999rpx; font-size: 22rpx; font-weight: 800; }
.status-tag.pending { background: #f1f5f9; color: #64748b; }
.status-tag.success { background: #dcfce7; color: #15803d; }
.status-tag.warning { background: #fef3c7; color: #b45309; }
.status-tag.danger { background: #fee2e2; color: #dc2626; }
.ai-fab { position: fixed; right: 34rpx; bottom: 58rpx; background: #111827; padding: 20rpx 28rpx; border-radius: 999rpx; box-shadow: 0 16rpx 34rpx rgba(15, 23, 42, 0.22); z-index: 50; }
.ai-label { color: #ffffff; font-size: 24rpx; font-weight: 900; }
</style>
