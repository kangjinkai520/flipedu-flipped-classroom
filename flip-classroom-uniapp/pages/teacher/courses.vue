<template>
  <view class="fc-page osmium-backend">
    <!-- 侧边栏：奶油色 Saas 风格重塑 -->
    <view class="sidebar">
      <view class="sidebar-top-section">
        <view class="sidebar-brand">
          <text class="brand-logo">🎓</text>
          <text class="brand-text">FlipEdu</text>
        </view>

        <view class="sidebar-search">
          <view class="search-inner">
            <text class="search-icon">🔍</text>
            <input class="search-input" placeholder="全局搜索 (Ctrl+F)" />
          </view>
        </view>

        <view class="sidebar-nav">
          <view v-for="group in navGroups" :key="group.title" class="nav-group">
            <text class="group-title">{{ group.title }}</text>
            <view
              v-for="item in group.items"
              :key="item.text"
              :class="['nav-item', item.active ? 'active' : 'hover-effect', item.disabled ? 'is-disabled' : '']"
              @click="handleNavClick(item)"
            >
              <text class="nav-icon">{{ item.icon }}</text>
              <text class="nav-text">{{ item.text }}</text>
              <text v-if="item.disabled" class="nav-badge">即将开放</text>
            </view>
          </view>
        </view>
      </view>

      <view class="sidebar-user-card" @click="handleLogout">
        <view class="user-avatar">👤</view>
        <view class="user-info">
          <text class="user-name">{{ session.realName || '张老师' }}</text>
          <text class="user-role">系统管理员</text>
        </view>
        <view class="logout-btn">
          <text class="logout-icon">🚪</text>
        </view>
      </view>
    </view>

    <!-- 内容区：奶油色背景 -->
    <view class="content-area">
      <view class="page-header">
        <view class="header-left">
          <text class="page-title">Dashboard</text>
          <text class="page-subtitle">欢迎回来，今天有 {{ courses.length }} 门课需要关注。</text>
        </view>
        <view class="header-btns">
          <view class="btn-secondary" @click="loadCourses">🔄 刷新数据</view>
          <view class="btn-primary" @click="toggleEditor">＋ 新建课程项目</view>
        </view>
      </view>

      <!-- KPI 指标：更加紧凑和精致 -->
      <view class="kpi-grid">
        <view class="kpi-card">
          <view class="kpi-icon bg-cream-blue">📈</view>
          <view class="kpi-data">
            <text class="kpi-label">活跃课程项目</text>
            <view class="kpi-row">
              <text class="kpi-value">{{ activeCourseCount }}</text>
              <text class="kpi-badge badge-blue">+2.5%</text>
            </view>
          </view>
        </view>
        <view class="kpi-card">
          <view class="kpi-icon bg-cream-green">👥</view>
          <view class="kpi-data">
            <text class="kpi-label">累计注册学生</text>
            <view class="kpi-row">
              <text class="kpi-value">{{ totalStudentCount }}</text>
              <text class="kpi-badge badge-green">+5.8%</text>
            </view>
          </view>
        </view>
        <view class="kpi-card">
          <view class="kpi-icon bg-cream-orange">⚡</view>
          <view class="kpi-data">
            <text class="kpi-label">互动活跃度</text>
            <view class="kpi-row">
              <text class="kpi-value">High</text>
              <text class="kpi-badge badge-orange">稳定</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 表单编辑器 -->
      <view v-if="showEditor" class="web-panel editor-box animated-slide-down">
        <view class="panel-header">
          <text class="panel-title">{{ editingId ? '修改课程核心信息' : '创建新教学课程' }}</text>
          <text class="panel-close" @click="resetForm">×</text>
        </view>
        <view :key="formRenderKey" class="panel-body">
          <view class="input-grid">
            <view class="input-group">
              <text class="input-label">课程全称</text>
              <input :value="form.courseName" class="os-input" placeholder="例如：Java程序设计高级开发" @input="updateField('courseName', $event)" />
            </view>
            <view class="input-group">
              <text class="input-label">唯一编码</text>
              <input :value="form.courseCode" class="os-input" placeholder="例如：JAVA2026" @input="updateField('courseCode', $event)" />
            </view>
            <view class="input-group">
              <text class="input-label">开课学期</text>
              <input :value="form.term" class="os-input" placeholder="例如：2025-2026-2" @input="updateField('term', $event)" />
            </view>
          </view>
          <view class="form-actions">
            <view class="btn-cancel" @click="resetForm">取消操作</view>
            <view class="btn-submit" @click="submitCourse">保存并提交审核</view>
          </view>
        </view>
      </view>

      <!-- 主视图：两栏布局 -->
      <view class="main-grid">
        <!-- 课程大表 -->
        <view class="web-panel table-widget">
          <view class="panel-header">
            <text class="panel-title">我的教学课程明细</text>
            <text class="panel-tag">共有 {{ courses.length }} 条记录</text>
          </view>
          
          <view class="os-table">
            <view class="os-thead">
              <text class="th col-id">ID</text>
              <text class="th col-name">课程信息</text>
              <text class="th col-term">学期</text>
              <text class="th col-status">状态</text>
              <text class="th col-action">管理操作</text>
            </view>
            
            <LoadingState v-if="loading" />
            <ErrorState v-else-if="error" @retry="loadCourses" />
            
            <view v-else class="os-tbody">
              <view v-for="course in courses" :key="course.id" class="os-tr">
                <text class="td col-id">#{{ course.id }}</text>
                <view class="td col-name info-stack">
                  <text class="info-main">{{ course.courseName }}</text>
                  <text class="info-sub">{{ course.courseCode }}</text>
                  <text v-if="course.status === -1 && rejectedReasons[course.id]" class="reject-reason">
                    驳回理由：{{ rejectedReasons[course.id] }}
                  </text>
                </view>
                <text class="td col-term">{{ course.term }}</text>
                <view class="td col-status">
                  <text :class="['pill-status', statusClass(course.status)]">
                    {{ statusLabel(course.status) }}
                  </text>
                </view>
                <view class="td col-action">
                  <view class="action-link blue" @click="startEdit(course.id)">配置</view>
                  <view class="action-link purple" @click="openCourseActions(course)">进入后台</view>
                </view>
              </view>
              <view v-if="!courses.length" class="empty-hint">暂无课程，点击上方按钮创建新课程</view>
            </view>
          </view>
        </view>

        <!-- 近期活动 -->
        <view class="web-panel side-widget">
          <view class="panel-header">
            <text class="panel-title">近期教学动态</text>
          </view>
          <view class="timeline">
            <view class="timeline-item">
              <view class="t-dot dot-blue"></view>
              <view class="t-content">
                <text class="t-title">Java程序设计</text>
                <text class="t-desc">发布了新教学视频 · 10:30 AM</text>
              </view>
            </view>
            <view class="timeline-item">
              <view class="t-dot dot-green"></view>
              <view class="t-content">
                <text class="t-title">Python基础</text>
                <text class="t-desc">12 名学生完成了随堂测验 · 09:15 AM</text>
              </view>
            </view>
            <view class="timeline-item">
              <view class="t-dot dot-orange"></view>
              <view class="t-content">
                <text class="t-title">系统提醒</text>
                <text class="t-desc">本周考勤报表已自动生成 · 昨日 18:00</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 工作台右侧抽屉：修复点击逻辑 -->
    <view v-if="showWorkbench" class="drawer-mask" @click="closeWorkbench">
      <view class="drawer-content" @click.stop>
        <view class="drawer-head">
          <text class="d-title">{{ currentCourse.courseName }}</text>
          <text class="d-subtitle">教学子模块管理后台</text>
        </view>
        
        <view class="d-grid">
          <view v-for="action in workbenchActions" :key="action.text" class="d-item" @click="openModule(action.path, currentCourse)">
            <view :class="['d-icon-box', action.colorClass]">
              <text class="d-icon">{{ action.icon }}</text>
            </view>
            <text class="d-label">{{ action.text }}</text>
          </view>
        </view>
        
        <view class="d-footer">
          <view class="d-close-btn" @click="closeWorkbench">返回主面板</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { createCourse, getCourse, listCourses, updateCourse } from '@/api/course'
import { createReview, listReviews } from '@/api/review'
import { clearSession, requireSession } from '@/common/session'
import LoadingState from '@/components/LoadingState.vue'
import ErrorState from '@/components/ErrorState.vue'

function createForm(userId) {
  return {
    teacherId: userId, courseName: '', courseCode: '', term: '', introduction: '', status: 0
  }
}

export default {
  components: { LoadingState, ErrorState },
  data() {
    return {
      session: {},
      courses: [],
      editingId: null,
      showEditor: false,
      showWorkbench: false,
      currentCourse: null,
      formRenderKey: 0,
      form: createForm(null),
      rejectedReasons: {},
      loading: false,
      error: false,
      navGroups: [
        {
          title: 'GENERAL',
          items: [
            { icon: '📊', text: '控制面板', path: '/pages/teacher/courses', active: true },
            { icon: '👥', text: '课程成员', path: '/pages/teacher/members' },
            { icon: '📂', text: '教学资源库', path: '/pages/teacher/materials' },
            { icon: '📈', text: '数据分析', disabled: true }
          ]
        },
        {
          title: 'SUPPORT',
          items: [
            { icon: '🔔', text: '消息通知中心', path: '/pages/teacher/notices' },
            { icon: '❓', text: '帮助与技术支持', disabled: true }
          ]
        }
      ],
      workbenchActions: [
        { icon: '📁', text: '资料管理', path: '/pages/teacher/materials', colorClass: 'bg-blue' },
        { icon: '📢', text: '课程通知', path: '/pages/teacher/notices', colorClass: 'bg-orange' },
        { icon: '👥', text: '成员管理', path: '/pages/teacher/members', colorClass: 'bg-indigo' },
        { icon: '📍', text: '课堂签到', path: '/pages/teacher/signs', colorClass: 'bg-green' },
        { icon: '📝', text: '随堂测验', path: '/pages/teacher/quizzes', colorClass: 'bg-cyan' },
        { icon: '📅', text: '作业管理', path: '/pages/teacher/assignments', colorClass: 'bg-purple' },
        { icon: '💬', text: '课后反馈', path: '/pages/teacher/feedback', colorClass: 'bg-orange' },
        { icon: '📊', text: '成绩汇总', path: '/pages/teacher/scores', colorClass: 'bg-red' }
      ]
    }
  },
  computed: {
    activeCourseCount() { return this.courses.filter(c => c.status === 1).length },
    totalStudentCount() { return this.courses.reduce((sum, c) => sum + (Number(c.studentCount) || 0), 0) }
  },
  onLoad() {
    const session = requireSession()
    if (!session || session.role !== 'teacher') return
    this.session = session
    this.form = createForm(session.userId)
    this.loadCourses()
  },
  methods: {
    toggleEditor() { this.showEditor = !this.showEditor },
    updateField(field, event) { this.form[field] = event.detail.value },
    statusLabel(status) {
      if (status === 1) return '进行中'
      if (status === -1) return '已驳回'
      return '待审核'
    },
    statusClass(status) {
      if (status === 1) return 's-active'
      if (status === -1) return 's-rejected'
      return 's-idle'
    },
    async loadCourses() {
      this.loading = true; this.error = false
      try {
        const [list, rejectedReviews] = await Promise.all([
          listCourses({ teacherId: this.session.userId }),
          listReviews('REJECTED').catch(() => [])
        ])
        this.rejectedReasons = this.buildRejectedReasons(rejectedReviews || [])
        this.courses = list || []
      } catch (e) { this.error = true }
      finally { this.loading = false }
    },
    buildRejectedReasons(reviews) {
      const map = {}
      reviews
        .filter(item => item.targetType === 'COURSE')
        .sort((a, b) => Number(b.id) - Number(a.id))
        .forEach(item => {
          if (!map[item.targetId]) {
            map[item.targetId] = item.reviewComment || '管理员未填写驳回理由'
          }
        })
      return map
    },
    resetForm() {
      this.editingId = null; this.showEditor = false
      this.form = createForm(this.session.userId); this.formRenderKey += 1
    },
    async startEdit(id) {
      try {
        const detail = await getCourse(id)
        this.editingId = id; this.form = { ...detail }
        this.showEditor = true
        uni.pageScrollTo({ scrollTop: 0, duration: 300 })
      } catch (e) { uni.showToast({ title: '加载失败', icon: 'none' }) }
    },
    async submitCourse() {
      if (!this.form.courseName || !this.form.courseCode || !this.form.term) {
        uni.showToast({ title: '信息不全', icon: 'none' }); return
      }
      try {
        const payload = { ...this.form, status: 0 }
        let targetId = this.editingId
        if (this.editingId) await updateCourse(this.editingId, payload)
        else {
          const created = await createCourse(payload)
          targetId = created.id
        }
        await createReview({
          targetType: 'COURSE',
          targetId,
          actionType: this.editingId ? 'UPDATE' : 'PUBLISH',
          requesterId: this.session.userId,
          title: this.form.courseName,
          summary: this.editingId ? '教师提交课程信息修改审核' : '教师提交新课程发布审核'
        })
        uni.showToast({ title: '已提交审核', icon: 'none' })
        this.resetForm(); this.loadCourses()
      } catch (e) { uni.showToast({ title: '保存异常', icon: 'none' }) }
    },
    openCourseActions(course) {
      this.currentCourse = course; this.showWorkbench = true
    },
    closeWorkbench() { this.showWorkbench = false },
    handleNavClick(item) {
      if (item.disabled) {
        uni.showToast({
          title: '该模块将在后续版本开放',
          icon: 'none'
        })
        return
      }
      if (item.active) return

      uni.navigateTo({
        url: item.path
      })
    },
    openModule(path, course) {
      this.showWorkbench = false
      uni.navigateTo({
        url: `${path}?courseId=${course.id}&courseName=${encodeURIComponent(course.courseName)}`
      })
    },
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出教师管理系统吗？',
        success: (res) => {
          if (res.confirm) {
            clearSession(); uni.reLaunch({ url: '/pages/login/index' })
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.osmium-backend {
  display: flex;
  min-height: 100vh;
  background: #FAF7F2; /* 奶油色背景 */
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

/* 侧边栏：极致拓宽与优化 */
.sidebar {
  width: 520rpx; /* 大幅拓宽 */
  background: #ffffff;
  border-right: 1rpx solid #EAE2D9;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  flex-shrink: 0;
  box-shadow: 10rpx 0 40rpx rgba(0,0,0,0.02);
}

.sidebar-brand {
  height: 180rpx;
  display: flex;
  align-items: center;
  padding: 0 60rpx;
  gap: 24rpx;
}
.brand-logo { font-size: 64rpx; }
.brand-text { font-size: 42rpx; font-weight: 900; color: #3D3D3D; letter-spacing: -1rpx; }

.sidebar-search { padding: 0 50rpx 60rpx; }
.search-inner {
  background: #F5F1EB;
  padding: 24rpx 32rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.search-icon { font-size: 32rpx; color: #B5A99A; }
.search-input { font-size: 26rpx; color: #3D3D3D; flex: 1; }

.sidebar-nav { flex: 1; padding: 0 40rpx; }
.nav-group { margin-bottom: 70rpx; }
.group-title { font-size: 22rpx; font-weight: 800; color: #C5BDB0; text-transform: uppercase; letter-spacing: 4rpx; padding-left: 24rpx; margin-bottom: 30rpx; display: block; }

.nav-item {
  display: flex;
  align-items: center;
  padding: 32rpx 36rpx;
  gap: 32rpx;
  border-radius: 24rpx;
  transition: all 0.3s;
  cursor: pointer;
  margin-bottom: 12rpx;
}
.nav-item.active { background: #F2EEF9; color: #8B5CF6; }
.nav-item.active .nav-icon { color: #8B5CF6; }
.nav-icon { font-size: 40rpx; color: #8E8E93; }
.nav-text { font-size: 30rpx; font-weight: 700; color: #5D5D5D; }
.nav-item.active .nav-text { color: #8B5CF6; }

.nav-item.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.nav-badge {
  font-size: 18rpx;
  background: #EEE;
  color: #999;
  padding: 4rpx 10rpx;
  border-radius: 8rpx;
  margin-left: auto;
  font-weight: 800;
}

.hover-effect:hover {
  background: #F9F6F2;
  transform: translateX(10rpx);
}

.sidebar-user-card {
  margin: 40rpx;
  padding: 32rpx;
  background: #FDFBF7;
  border: 2rpx solid #F0EAE2;
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  cursor: pointer;
  transition: all 0.2s;
}
.sidebar-user-card:hover { border-color: #D1C4BC; background: #ffffff; }
.user-avatar { width: 80rpx; height: 80rpx; background: #EEE; border-radius: 20rpx; display: flex; align-items: center; justify-content: center; font-size: 40rpx; }
.user-info { flex: 1; display: flex; flex-direction: column; }
.user-name { font-size: 28rpx; font-weight: 800; color: #3D3D3D; }
.user-role { font-size: 22rpx; color: #A09688; margin-top: 4rpx; }
.logout-icon { font-size: 32rpx; color: #C5BDB0; }

/* 内容区域 */
.content-area { flex: 1; padding: 80rpx; overflow-y: auto; }

.page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 60rpx; }
.page-title { font-size: 64rpx; font-weight: 900; color: #1A1A1A; display: block; }
.page-subtitle { font-size: 28rpx; color: #8E8E93; margin-top: 16rpx; display: block; }

.header-btns { display: flex; gap: 30rpx; }
.btn-secondary { background: #ffffff; border: 2rpx solid #EAE2D9; padding: 24rpx 40rpx; border-radius: 20rpx; font-size: 26rpx; font-weight: 700; color: #5D5D5D; cursor: pointer; }
.btn-primary { background: #1A1A1A; color: #ffffff; padding: 24rpx 48rpx; border-radius: 20rpx; font-size: 26rpx; font-weight: 700; cursor: pointer; }

/* KPI 卡片 */
.kpi-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 40rpx; margin-bottom: 80rpx; }
.kpi-card { background: #ffffff; padding: 48rpx; border-radius: 40rpx; display: flex; align-items: center; gap: 40rpx; box-shadow: 0 10rpx 40rpx rgba(0,0,0,0.02); }
.kpi-icon { width: 110rpx; height: 110rpx; border-radius: 30rpx; display: flex; align-items: center; justify-content: center; font-size: 48rpx; }
.bg-cream-blue { background: #EBF2FF; }
.bg-cream-green { background: #EBF9F1; }
.bg-cream-orange { background: #FFF4EB; }

.kpi-label { font-size: 26rpx; font-weight: 700; color: #A09688; margin-bottom: 12rpx; display: block; }
.kpi-row { display: flex; align-items: baseline; gap: 20rpx; }
.kpi-value { font-size: 52rpx; font-weight: 900; color: #1A1A1A; }
.kpi-badge { font-size: 22rpx; font-weight: 800; padding: 4rpx 16rpx; border-radius: 10rpx; }
.badge-blue { background: #D1E3FF; color: #3B82F6; }
.badge-green { background: #D1F2E1; color: #10B981; }
.badge-orange { background: #FFEBD1; color: #F59E0B; }

/* 布局挂件 */
.main-grid { display: flex; gap: 40rpx; align-items: flex-start; }
.table-widget { flex: 2.5; }
.side-widget { flex: 1; }

.web-panel { background: #ffffff; border-radius: 40rpx; padding: 50rpx; box-shadow: 0 10rpx 40rpx rgba(0,0,0,0.02); border: 1rpx solid #F0EAE2; }
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 50rpx; }
.panel-title { font-size: 36rpx; font-weight: 900; color: #1A1A1A; }
.panel-tag { font-size: 24rpx; color: #B5A99A; font-weight: 700; }

/* 表格：极致清晰编排 */
.os-table { width: 100%; }
.os-thead { display: flex; padding-bottom: 24rpx; border-bottom: 2rpx solid #FAF7F2; }
.th { font-size: 22rpx; font-weight: 800; color: #C5BDB0; text-transform: uppercase; letter-spacing: 2rpx; }

.os-tbody { padding-top: 10rpx; }
.os-tr { display: flex; align-items: center; padding: 44rpx 0; border-bottom: 1rpx solid #F9F6F2; transition: all 0.2s; }
.os-tr:hover { background: #FDFBF7; }

.td { font-size: 28rpx; color: #3D3D3D; font-weight: 600; }
.col-id { width: 100rpx; color: #D1C4BC; font-size: 24rpx; }
.col-name { width: 450rpx; }
.col-term { width: 200rpx; }
.col-status { width: 180rpx; }
.col-action { flex: 1; display: flex; justify-content: flex-end; gap: 32rpx; }

.info-stack { display: flex; flex-direction: column; gap: 8rpx; }
.info-main { font-size: 32rpx; font-weight: 800; color: #1A1A1A; }
.info-sub { font-size: 22rpx; color: #B5A99A; font-weight: 700; }
.reject-reason { font-size: 20rpx; color: #DC2626; font-weight: 700; line-height: 1.4; }

.pill-status { font-size: 22rpx; font-weight: 800; padding: 8rpx 20rpx; border-radius: 12rpx; }
.s-active { background: #EBF9F1; color: #10B981; }
.s-idle { background: #F5F5F5; color: #8E8E93; }
.s-rejected { background: #FEE2E2; color: #DC2626; }

.action-link { font-size: 26rpx; font-weight: 800; cursor: pointer; padding: 10rpx 0; }
.action-link.blue { color: #3B82F6; }
.action-link.purple { color: #8B5CF6; }
.action-link:hover { text-decoration: underline; opacity: 0.8; }

/* 侧边挂件：动态时间轴 */
.timeline { display: flex; flex-direction: column; gap: 50rpx; }
.timeline-item { display: flex; gap: 30rpx; }
.t-dot { width: 16rpx; height: 16rpx; border-radius: 50%; margin-top: 12rpx; flex-shrink: 0; }
.dot-blue { background: #3B82F6; box-shadow: 0 0 15rpx rgba(59,130,246,0.3); }
.dot-green { background: #10B981; box-shadow: 0 0 15rpx rgba(16,185,129,0.3); }
.dot-orange { background: #F59E0B; box-shadow: 0 0 15rpx rgba(245,158,11,0.3); }
.t-content { display: flex; flex-direction: column; gap: 8rpx; }
.t-title { font-size: 28rpx; font-weight: 800; color: #3D3D3D; }
.t-desc { font-size: 24rpx; color: #A09688; font-weight: 600; }

/* 工作台抽屉 */
.drawer-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(61,61,61,0.15); backdrop-filter: blur(10px); z-index: 3000; display: flex; justify-content: flex-end; }
.drawer-content { width: 720rpx; background: #ffffff; height: 100%; padding: 80rpx; box-shadow: -30rpx 0 80rpx rgba(0,0,0,0.05); display: flex; flex-direction: column; }
.drawer-head { margin-bottom: 80rpx; }
.d-title { font-size: 52rpx; font-weight: 900; color: #1A1A1A; display: block; }
.d-subtitle { font-size: 28rpx; color: #A09688; margin-top: 20rpx; display: block; font-weight: 700; }

.d-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 40rpx; flex: 1; }
.d-item { 
  display: flex; align-items: center; padding: 48rpx; border: 3rpx solid #F9F6F2; border-radius: 40rpx; gap: 32rpx; 
  transition: all 0.3s; cursor: pointer; 
}
.d-item:hover { background: #FDFBF7; border-color: #EAE2D9; transform: translateY(-8rpx); box-shadow: 0 20rpx 40rpx rgba(0,0,0,0.03); }
.d-icon-box { width: 120rpx; height: 120rpx; border-radius: 32rpx; display: flex; align-items: center; justify-content: center; font-size: 56rpx; }
.d-label { font-size: 32rpx; font-weight: 800; color: #3D3D3D; }

.d-footer { margin-top: 60rpx; padding-top: 40rpx; border-top: 2rpx solid #F9F6F2; }
.d-close-btn { 
  width: 100%; height: 110rpx; line-height: 110rpx; text-align: center; background: #1A1A1A; 
  color: #ffffff; border-radius: 24rpx; font-size: 30rpx; font-weight: 800; cursor: pointer; 
}

/* 颜色类 */
.bg-blue { background: #EBF2FF; color: #3B82F6; }
.bg-orange { background: #FFF4EB; color: #F59E0B; }
.bg-indigo { background: #F2EEF9; color: #8B5CF6; }
.bg-green { background: #EBF9F1; color: #10B981; }
.bg-cyan { background: #E6F9FB; color: #06B6D4; }
.bg-purple { background: #FAF5FF; color: #A855F7; }
.bg-red { background: #FFF1F1; color: #EF4444; }

.animated-slide-down { animation: slideDown 0.4s cubic-bezier(0.4, 0, 0.2, 1); }
@keyframes slideDown { from { opacity: 0; transform: translateY(-30rpx); } to { opacity: 1; transform: translateY(0); } }
</style>
