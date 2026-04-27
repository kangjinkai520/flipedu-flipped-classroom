<template>
  <view class="fc-page page">
    <view class="header-section">
      <view>
        <text class="course-name">{{ courseName }}</text>
        <text class="page-title">签到管理</text>
      </view>
      <view class="header-actions">
        <button class="secondary-btn" @click="loadTasks">刷新</button>
        <button class="primary-btn" @click="toggleEditor">{{ showEditor ? '收起' : '新建签到' }}</button>
      </view>
    </view>

    <view v-if="showEditor" class="editor-card">
      <text class="editor-title">创建签到任务</text>
      <view class="form-body">
        <view class="input-group">
          <text class="label">签到标题</text>
          <input v-model="form.title" class="input" placeholder="例如：第 5 周课堂签到" />
        </view>

        <view class="form-row">
          <view class="input-group flex-1">
            <text class="label">签到码</text>
            <input v-model="form.signCode" class="input" maxlength="6" placeholder="4-6 位数字" />
          </view>
          <view class="input-group flex-1">
            <text class="label">签到类型</text>
            <view class="type-selector" @click="form.locationEnabled = !form.locationEnabled">
              <text :class="['type-tag', !form.locationEnabled ? 'active' : '']">普通</text>
              <text :class="['type-tag', form.locationEnabled ? 'active' : '']">定位</text>
            </view>
          </view>
        </view>

        <view v-if="form.locationEnabled" class="location-config-box">
          <view class="input-group">
            <text class="label">签到地点名称</text>
            <input v-model="form.locationName" class="input" placeholder="例如：教学楼 302" />
          </view>
          <view class="form-row">
            <view class="input-group flex-1">
              <text class="label">允许范围（米）</text>
              <input v-model="form.allowedRadiusMeters" class="input" type="number" placeholder="100" />
            </view>
            <view class="input-group flex-1">
              <text class="label">定位坐标</text>
              <button class="location-btn" @click="fetchCurrentLocation">
                {{ form.targetLatitude ? '已获取定位' : '获取当前位置' }}
              </button>
            </view>
          </view>
          <view class="form-row">
            <view class="input-group flex-1">
              <text class="label">纬度</text>
              <input v-model="form.targetLatitude" class="input" type="digit" placeholder="例如 30.5728" />
            </view>
            <view class="input-group flex-1">
              <text class="label">经度</text>
              <input v-model="form.targetLongitude" class="input" type="digit" placeholder="例如 104.0668" />
            </view>
          </view>
          <text v-if="form.targetLatitude" class="coord-text">
            {{ form.targetLatitude }}, {{ form.targetLongitude }}
          </text>
        </view>

        <view class="editor-footer">
          <button class="primary-btn submit-btn" @click="submitTask">发布签到</button>
          <button class="secondary-btn cancel-btn" @click="resetForm">取消</button>
        </view>
      </view>
    </view>

    <view class="list-section">
      <text class="section-label">签到任务</text>
      <view v-if="tasks.length" class="task-stack">
        <view v-for="item in tasks" :key="item.id" :class="['task-item', item.status === 'FINISHED' ? 'task-done' : '']">
          <view class="task-main" @click="loadRecords(item.id)">
            <view class="task-info">
              <view class="task-top">
                <text class="task-title">{{ item.title }}</text>
                <text :class="['status-pill', item.status === 'ONGOING' ? 'pill-green' : 'pill-gray']">
                  {{ item.status === 'ONGOING' ? '进行中' : '已结束' }}
                </text>
              </view>
              <view class="task-meta-row">
                <text class="meta-text">签到码 {{ item.signCode }}</text>
                <text class="meta-text">{{ item.locationEnabled ? '定位签到' : '普通签到' }}</text>
                <text v-if="item.locationName" class="meta-text">{{ item.locationName }}</text>
              </view>
            </view>
            <text class="arrow-icon">›</text>
          </view>

          <view class="task-actions">
            <button class="task-btn" @click="toggleStatus(item)">
              {{ item.status === 'ONGOING' ? '结束签到' : '重新开启' }}
            </button>
            <button class="task-btn btn-primary" @click="loadRecords(item.id)">查看记录</button>
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <text class="empty-text">暂无签到任务，点击右上角创建。</text>
      </view>
    </view>

    <view v-if="selectedTaskId" class="records-overlay" @click="selectedTaskId = null">
      <view class="records-drawer" @click.stop>
        <view class="drawer-handle"></view>
        <view class="drawer-header">
          <text class="drawer-title">签到记录</text>
          <text class="drawer-count">{{ records.length }} 条记录</text>
        </view>

        <scroll-view scroll-y class="records-list">
          <view v-for="record in records" :key="record.id" class="record-card">
            <view class="record-user">
              <view class="user-avatar">{{ avatarText(record.studentName) }}</view>
              <view class="user-info">
                <text class="user-name">{{ record.studentName || '学生' }}</text>
                <text class="user-id">{{ record.studentUsername || '-' }}</text>
              </view>
              <text :class="['record-status', getStatusClass(record.status)]">
                {{ formatRecordStatus(record.status) }}
              </text>
            </view>

            <view class="record-detail">
              <text v-if="record.signType" class="detail-line">类型：{{ record.signType === 'EXCEPTION' ? '异常申请' : '正常签到' }}</text>
              <text v-if="record.distanceMeters !== null && record.distanceMeters !== undefined" class="detail-line">距离：{{ formatNumber(record.distanceMeters) }} 米</text>
              <text v-if="record.exceptionReason" class="detail-line warn">异常原因：{{ record.exceptionReason }}</text>
              <text v-if="record.reviewComment" class="detail-line">审核意见：{{ record.reviewComment }}</text>
            </view>

            <view v-if="record.status === 'PENDING_REVIEW'" class="review-area">
              <button class="review-btn approve" @click="reviewRecord(record, 'APPROVED')">通过</button>
              <button class="review-btn reject" @click="reviewRecord(record, 'REJECTED')">驳回</button>
            </view>
          </view>
        </scroll-view>

        <button class="close-drawer-btn" @click="selectedTaskId = null">关闭记录</button>
      </view>
    </view>
  </view>
</template>

<script>
import { createSignTask, listSignRecords, listSignTasks, reviewSignRecord, updateSignTaskStatus } from '@/api/sign'
import { requireSession } from '@/common/session'

function createForm() {
  return {
    title: '',
    signCode: '',
    startsAt: '',
    endsAt: '',
    status: 'ONGOING',
    locationEnabled: false,
    locationName: '',
    targetLatitude: '',
    targetLongitude: '',
    allowedRadiusMeters: 100
  }
}

export default {
  data() {
    return {
      courseId: null,
      courseName: '',
      tasks: [],
      selectedTaskId: null,
      records: [],
      showEditor: false,
      form: createForm()
    }
  },
  onLoad(query) {
    const session = requireSession()
    if (!session) return
    this.courseId = Number(query.courseId)
    this.courseName = this.decodeCourseName(query.courseName) || '课程'
    this.loadTasks()
  },
  methods: {
    decodeCourseName(value) {
      if (!value) return ''
      try { return decodeURIComponent(value) } catch (e) { return value }
    },
    toggleEditor() {
      this.showEditor = !this.showEditor
      if (!this.showEditor) this.resetForm()
    },
    resetForm() {
      this.form = createForm()
      this.showEditor = false
    },
    async loadTasks() {
      this.tasks = await listSignTasks(this.courseId)
    },
    async fetchCurrentLocation() {
      uni.getLocation({
        type: 'gcj02',
        success: (res) => {
          this.form.targetLatitude = res.latitude
          this.form.targetLongitude = res.longitude
          uni.showToast({ title: '定位已获取', icon: 'success' })
        },
        fail: () => uni.showToast({ title: '获取定位失败，请检查浏览器/设备权限', icon: 'none' })
      })
    },
    validateForm() {
      if (!this.form.title || !this.form.signCode) {
        uni.showToast({ title: '请填写标题和签到码', icon: 'none' })
        return false
      }
      if (this.form.locationEnabled && (!this.form.locationName || !this.form.targetLatitude || !this.form.targetLongitude)) {
        uni.showToast({ title: '定位签到需要地点和坐标', icon: 'none' })
        return false
      }
      return true
    },
    async submitTask() {
      if (!this.validateForm()) return
      const payload = {
        ...this.form,
        targetLatitude: this.form.locationEnabled ? Number(this.form.targetLatitude) : null,
        targetLongitude: this.form.locationEnabled ? Number(this.form.targetLongitude) : null,
        allowedRadiusMeters: Number(this.form.allowedRadiusMeters || 100)
      }
      await createSignTask(this.courseId, payload)
      this.resetForm()
      this.loadTasks()
      uni.showToast({ title: '签到已发布', icon: 'success' })
    },
    async toggleStatus(item) {
      const nextStatus = item.status === 'ONGOING' ? 'FINISHED' : 'ONGOING'
      await updateSignTaskStatus(item.id, nextStatus)
      this.loadTasks()
    },
    async loadRecords(id) {
      this.selectedTaskId = id
      this.records = await listSignRecords(id)
    },
    async reviewRecord(record, status) {
      await reviewSignRecord(record.id, {
        status,
        reviewComment: status === 'APPROVED' ? '教师审核通过' : '教师审核驳回'
      })
      this.loadRecords(this.selectedTaskId)
    },
    avatarText(name) {
      return (name || '学').charAt(0)
    },
    formatRecordStatus(status) {
      const map = { SIGNED: '已签到', PENDING_REVIEW: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }
      return map[status] || status
    },
    getStatusClass(status) {
      const map = { SIGNED: 'st-green', PENDING_REVIEW: 'st-orange', APPROVED: 'st-green', REJECTED: 'st-red' }
      return map[status] || ''
    },
    formatNumber(val) {
      return val !== null && val !== undefined ? Number(val).toFixed(0) : '0'
    }
  }
}
</script>

<style scoped>
.page { padding: 34rpx; background: #f7f8fb; min-height: 100vh; }
.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 34rpx; }
.course-name { font-size: 24rpx; color: #64748b; font-weight: 700; }
.page-title { display: block; margin-top: 6rpx; font-size: 42rpx; font-weight: 900; color: #1f2937; }
.header-actions { display: flex; gap: 14rpx; }
.primary-btn, .secondary-btn { margin: 0; height: 76rpx; line-height: 76rpx; padding: 0 28rpx; border-radius: 18rpx; border: none; font-size: 25rpx; font-weight: 800; }
.primary-btn { background: #4f46e5; color: #ffffff; }
.secondary-btn { background: #ffffff; color: #334155; border: 1rpx solid #e2e8f0; }
.editor-card, .task-item { background: #ffffff; border-radius: 24rpx; padding: 30rpx; border: 1rpx solid #e8ecf3; margin-bottom: 28rpx; }
.editor-title { display: block; margin-bottom: 24rpx; font-size: 30rpx; font-weight: 900; color: #1f2937; }
.input-group { margin-bottom: 22rpx; }
.label { display: block; margin-bottom: 10rpx; font-size: 22rpx; font-weight: 800; color: #64748b; }
.input { height: 82rpx; background: #f8fafc; border-radius: 16rpx; padding: 0 22rpx; font-size: 27rpx; }
.form-row { display: flex; gap: 20rpx; }
.flex-1 { flex: 1; }
.type-selector { display: flex; height: 82rpx; background: #f8fafc; padding: 6rpx; border-radius: 16rpx; box-sizing: border-box; }
.type-tag { flex: 1; display: flex; align-items: center; justify-content: center; font-size: 24rpx; font-weight: 800; color: #64748b; border-radius: 12rpx; }
.type-tag.active { background: #ffffff; color: #4f46e5; box-shadow: 0 4rpx 12rpx rgba(15, 23, 42, 0.06); }
.location-config-box { background: #f8fafc; border-radius: 20rpx; padding: 22rpx; margin-bottom: 22rpx; border: 1rpx dashed #cbd5e1; }
.location-btn { height: 82rpx; line-height: 82rpx; border-radius: 16rpx; background: #ffffff; border: 1rpx solid #e2e8f0; font-size: 24rpx; color: #334155; }
.coord-text { display: block; color: #64748b; font-size: 22rpx; }
.editor-footer, .task-actions { display: flex; gap: 16rpx; }
.submit-btn { flex: 2; }
.cancel-btn { flex: 1; }
.section-label { display: block; margin: 18rpx 0 22rpx; font-size: 26rpx; font-weight: 900; color: #334155; }
.task-stack { display: flex; flex-direction: column; gap: 20rpx; }
.task-done { opacity: 0.72; }
.task-main { display: flex; align-items: center; gap: 22rpx; }
.task-info { flex: 1; }
.task-top { display: flex; justify-content: space-between; align-items: center; gap: 18rpx; margin-bottom: 12rpx; }
.task-title { font-size: 30rpx; font-weight: 900; color: #1f2937; }
.status-pill { font-size: 21rpx; font-weight: 800; padding: 6rpx 16rpx; border-radius: 999rpx; }
.pill-green { background: #dcfce7; color: #15803d; }
.pill-gray { background: #f1f5f9; color: #64748b; }
.task-meta-row { display: flex; flex-wrap: wrap; gap: 12rpx; }
.meta-text { font-size: 22rpx; color: #64748b; font-weight: 700; }
.arrow-icon { font-size: 42rpx; color: #94a3b8; }
.task-actions { margin-top: 24rpx; padding-top: 20rpx; border-top: 1rpx solid #eef2f7; }
.task-btn { flex: 1; height: 68rpx; line-height: 68rpx; border-radius: 14rpx; border: none; font-size: 24rpx; font-weight: 800; background: #f1f5f9; color: #334155; }
.btn-primary { background: #eef2ff; color: #4f46e5; }
.empty-state { padding: 80rpx 0; text-align: center; }
.empty-text { color: #64748b; font-size: 26rpx; }
.records-overlay { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.42); z-index: 1000; }
.records-drawer { position: absolute; left: 0; right: 0; bottom: 0; background: #ffffff; border-radius: 34rpx 34rpx 0 0; padding: 30rpx; }
.drawer-handle { width: 72rpx; height: 8rpx; background: #e2e8f0; border-radius: 999rpx; margin: 0 auto 28rpx; }
.drawer-header { text-align: center; margin-bottom: 26rpx; }
.drawer-title { display: block; font-size: 34rpx; font-weight: 900; color: #1f2937; }
.drawer-count { display: block; margin-top: 6rpx; font-size: 23rpx; color: #64748b; }
.records-list { max-height: 60vh; margin-bottom: 22rpx; }
.record-card { padding: 24rpx 0; border-bottom: 1rpx solid #eef2f7; }
.record-user { display: flex; align-items: center; gap: 18rpx; }
.user-avatar { width: 72rpx; height: 72rpx; border-radius: 20rpx; background: #eef2ff; color: #4f46e5; display: flex; align-items: center; justify-content: center; font-weight: 900; }
.user-info { flex: 1; }
.user-name { display: block; font-size: 27rpx; font-weight: 900; color: #1f2937; }
.user-id { display: block; margin-top: 4rpx; color: #64748b; font-size: 22rpx; }
.record-status { font-size: 22rpx; font-weight: 800; padding: 7rpx 16rpx; border-radius: 999rpx; }
.st-green { background: #dcfce7; color: #15803d; }
.st-orange { background: #fef3c7; color: #b45309; }
.st-red { background: #fee2e2; color: #dc2626; }
.record-detail { margin-top: 16rpx; background: #f8fafc; padding: 16rpx; border-radius: 14rpx; }
.detail-line { display: block; font-size: 22rpx; color: #475569; line-height: 1.55; }
.detail-line.warn { color: #dc2626; font-weight: 800; }
.review-area { margin-top: 18rpx; display: flex; gap: 14rpx; }
.review-btn { flex: 1; height: 62rpx; line-height: 62rpx; border-radius: 14rpx; border: none; color: #ffffff; font-size: 23rpx; font-weight: 900; }
.approve { background: #16a34a; }
.reject { background: #dc2626; }
.close-drawer-btn { height: 86rpx; line-height: 86rpx; border-radius: 20rpx; border: none; background: #f1f5f9; color: #334155; font-size: 27rpx; font-weight: 900; }
</style>
