<template>
  <view class="admin-page">
    <view class="side-nav">
      <view class="brand">FlipEdu</view>
      <view class="nav-item" @click="go('/pages/admin/dashboard')">总览</view>
      <view class="nav-item" @click="go('/pages/admin/courses')">课程管理</view>
      <view class="nav-item active">用户管理</view>
      <view class="nav-item" @click="go('/pages/admin/reviews')">审核中心</view>
      <view class="nav-spacer"></view>
      <view class="user-line">{{ session.realName || '系统管理员' }}</view>
      <view class="logout" @click="handleLogout">退出登录</view>
    </view>

    <view class="main">
      <view class="page-head">
        <view>
          <text class="page-title">用户管理</text>
          <text class="page-desc">创建账号、编辑角色信息，并控制账号启用状态。</text>
        </view>
        <button class="secondary-btn" @click="loadUsers">刷新</button>
      </view>

      <view class="layout-grid">
        <view class="form-card">
          <text class="card-title">{{ editingId ? '编辑用户' : '新建用户' }}</text>
          <view class="field">
            <text class="label">用户名</text>
            <input v-model="form.username" class="input" placeholder="请输入登录用户名" />
          </view>
          <view v-if="!editingId" class="field">
            <text class="label">初始密码</text>
            <input v-model="form.password" class="input" password placeholder="建议使用 123456" />
          </view>
          <view class="field">
            <text class="label">姓名</text>
            <input v-model="form.realName" class="input" placeholder="请输入真实姓名" />
          </view>
          <view class="field">
            <text class="label">手机号</text>
            <input v-model="form.phone" class="input" placeholder="可选" />
          </view>
          <view class="field">
            <text class="label">角色</text>
            <view class="role-pills">
              <text
                v-for="item in roleOnlyOptions"
                :key="item.value"
                :class="['role-pill', form.role === item.value ? 'active' : '']"
                @click="form.role = item.value"
              >{{ item.label }}</text>
            </view>
          </view>
          <view v-if="form.role === 'student'" class="field">
            <text class="label">学号</text>
            <input v-model="form.studentNo" class="input" placeholder="学生账号可填写学号" />
          </view>
          <view v-if="form.role === 'teacher'" class="field">
            <text class="label">工号</text>
            <input v-model="form.teacherNo" class="input" placeholder="教师账号可填写工号" />
          </view>
          <view class="form-actions">
            <button class="secondary-btn wide" @click="resetForm">清空</button>
            <button class="primary-btn wide" @click="submitForm">{{ editingId ? '保存修改' : '创建用户' }}</button>
          </view>
        </view>

        <view class="table-card">
          <view class="table-headline">
            <text class="card-title">用户列表</text>
            <view class="filters">
              <text
                v-for="item in roleOptions"
                :key="item.value"
                :class="['filter', roleFilter === item.value ? 'active' : '']"
                @click="changeRole(item.value)"
              >{{ item.label }}</text>
            </view>
          </view>

          <scroll-view scroll-y class="table-body">
            <view v-for="item in users" :key="item.id" class="user-row">
              <view class="avatar">{{ avatar(item.realName || item.username) }}</view>
              <view class="user-main">
                <text class="user-name">{{ item.realName || item.username }}</text>
                <text class="user-sub">@{{ item.username }} · {{ roleLabel(item.role) }}</text>
              </view>
              <text :class="['status', item.status === 1 ? 'on' : 'off']">
                {{ item.status === 1 ? '启用' : '停用' }}
              </text>
              <view class="ops">
                <button class="mini-btn" @click="startEdit(item)">编辑</button>
                <button
                  v-if="item.role !== 'admin'"
                  :class="['mini-btn', item.status === 1 ? 'danger' : 'success']"
                  @click="toggleStatus(item)"
                >
                  {{ item.status === 1 ? '停用' : '启用' }}
                </button>
                <button v-else class="mini-btn locked">保护</button>
              </view>
            </view>
            <view v-if="!users.length" class="empty">暂无用户</view>
          </scroll-view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { createUser, getUser, listUsers, updateUser, updateUserStatus } from '@/api/admin'
import { clearSession, requireSession } from '@/common/session'

function createEmptyForm() {
  return {
    username: '',
    password: '',
    realName: '',
    role: 'student',
    studentNo: '',
    teacherNo: '',
    phone: ''
  }
}

export default {
  data() {
    return {
      session: {},
      users: [],
      roleFilter: '',
      editingId: null,
      form: createEmptyForm(),
      roleOptions: [
        { label: '全部', value: '' },
        { label: '学生', value: 'student' },
        { label: '教师', value: 'teacher' },
        { label: '管理员', value: 'admin' }
      ],
      roleOnlyOptions: [
        { label: '学生', value: 'student' },
        { label: '教师', value: 'teacher' },
        { label: '管理员', value: 'admin' }
      ]
    }
  },
  onLoad() {
    const session = requireSession()
    if (!session || session.role !== 'admin') return
    this.session = session
  },
  onShow() {
    if (!this.session || !this.session.userId) {
      const session = requireSession()
      if (!session || session.role !== 'admin') return
      this.session = session
    }
    this.loadUsers()
  },
  methods: {
    avatar(name) {
      return (name || '用').charAt(0)
    },
    roleLabel(role) {
      const map = { admin: '管理员', teacher: '教师', student: '学生' }
      return map[role] || role
    },
    async loadUsers() {
      this.users = await listUsers(this.roleFilter)
    },
    changeRole(role) {
      this.roleFilter = role
      this.loadUsers()
    },
    async startEdit(user) {
      const detail = await getUser(user.id)
      this.editingId = detail.id
      this.form = {
        username: detail.username || '',
        password: '',
        realName: detail.realName || '',
        role: detail.role || 'student',
        studentNo: detail.studentNo || '',
        teacherNo: detail.teacherNo || '',
        phone: detail.phone || ''
      }
      uni.pageScrollTo({ scrollTop: 0, duration: 250 })
    },
    resetForm() {
      this.editingId = null
      this.form = createEmptyForm()
    },
    async submitForm() {
      if (!this.form.username || !this.form.realName || !this.form.role) {
        uni.showToast({ title: '请填写用户名、姓名和角色', icon: 'none' })
        return
      }
      if (!this.editingId && !this.form.password) {
        uni.showToast({ title: '新建用户需要初始密码', icon: 'none' })
        return
      }
      if (this.editingId) {
        await updateUser(this.editingId, this.form)
      } else {
        await createUser(this.form)
      }
      uni.showToast({ title: '保存成功', icon: 'success' })
      this.resetForm()
      this.loadUsers()
    },
    async toggleStatus(user) {
      if (user.role === 'admin') {
        uni.showToast({ title: '管理员账号不能被停用', icon: 'none' })
        return
      }
      await updateUserStatus(user.id, user.status === 1 ? 0 : 1)
      this.loadUsers()
    },
    go(url) {
      uni.redirectTo({ url })
    },
    handleLogout() {
      clearSession()
      uni.reLaunch({ url: '/pages/login/index' })
    }
  }
}
</script>

<style scoped>
.admin-page { display: flex; min-height: 100vh; background: #f6f7fb; color: #172033; }
.side-nav { width: 360rpx; background: #172033; color: #fff; padding: 48rpx 30rpx; display: flex; flex-direction: column; box-sizing: border-box; }
.brand { font-size: 34rpx; font-weight: 900; margin-bottom: 54rpx; }
.nav-item { padding: 24rpx 26rpx; border-radius: 16rpx; margin-bottom: 12rpx; color: #cbd5e1; font-size: 26rpx; font-weight: 800; }
.nav-item.active { background: #fff; color: #172033; }
.nav-spacer { flex: 1; }
.user-line { font-size: 24rpx; color: #e2e8f0; margin-bottom: 14rpx; }
.logout { font-size: 24rpx; color: #fca5a5; font-weight: 800; }
.main { flex: 1; padding: 54rpx; box-sizing: border-box; }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 32rpx; gap: 24rpx; }
.page-title { display: block; font-size: 44rpx; font-weight: 900; }
.page-desc { display: block; margin-top: 8rpx; color: #64748b; font-size: 24rpx; }
.layout-grid { display: grid; grid-template-columns: 440rpx 1fr; gap: 24rpx; align-items: flex-start; }
.form-card, .table-card { background: #fff; border: 1rpx solid #e8ecf3; border-radius: 18rpx; padding: 30rpx; box-sizing: border-box; }
.card-title { display: block; font-size: 30rpx; font-weight: 900; margin-bottom: 22rpx; }
.field { margin-bottom: 22rpx; }
.label { display: block; margin-bottom: 10rpx; color: #64748b; font-size: 23rpx; font-weight: 800; }
.input { height: 78rpx; background: #f8fafc; border: 1rpx solid #e2e8f0; border-radius: 14rpx; padding: 0 20rpx; font-size: 26rpx; box-sizing: border-box; }
.role-pills { display: flex; gap: 10rpx; flex-wrap: wrap; }
.role-pill { padding: 12rpx 18rpx; border-radius: 999rpx; background: #f1f5f9; color: #334155; font-size: 22rpx; font-weight: 800; }
.role-pill.active { background: #4f46e5; color: #fff; }
.form-actions, .filters, .ops { display: flex; gap: 12rpx; }
.primary-btn, .secondary-btn { margin: 0; height: 72rpx; line-height: 72rpx; padding: 0 24rpx; border-radius: 14rpx; border: none; font-size: 24rpx; font-weight: 800; }
.primary-btn { background: #4f46e5; color: #fff; }
.secondary-btn { background: #fff; color: #334155; border: 1rpx solid #e2e8f0; }
.wide { flex: 1; }
.table-headline { display: flex; justify-content: space-between; align-items: flex-start; gap: 20rpx; margin-bottom: 12rpx; }
.filters { flex-wrap: wrap; justify-content: flex-end; }
.filter { padding: 10rpx 16rpx; border-radius: 999rpx; background: #f1f5f9; color: #475569; font-size: 22rpx; font-weight: 800; }
.filter.active { background: #eef2ff; color: #4f46e5; }
.table-body { height: 900rpx; }
.user-row { display: flex; align-items: center; gap: 18rpx; padding: 22rpx 0; border-bottom: 1rpx solid #eef2f7; }
.avatar { width: 68rpx; height: 68rpx; border-radius: 18rpx; background: #eef2ff; color: #4f46e5; display: flex; align-items: center; justify-content: center; font-size: 28rpx; font-weight: 900; }
.user-main { flex: 1; min-width: 0; }
.user-name { display: block; font-size: 27rpx; font-weight: 900; }
.user-sub { display: block; margin-top: 4rpx; color: #64748b; font-size: 22rpx; }
.status { width: 88rpx; text-align: center; padding: 8rpx 0; border-radius: 999rpx; font-size: 22rpx; font-weight: 800; }
.status.on { background: #dcfce7; color: #15803d; }
.status.off { background: #fee2e2; color: #dc2626; }
.ops { width: 210rpx; justify-content: flex-end; }
.mini-btn { margin: 0; height: 60rpx; line-height: 60rpx; padding: 0 18rpx; border-radius: 12rpx; background: #eef2ff; color: #4f46e5; border: none; font-size: 22rpx; font-weight: 900; }
.mini-btn.danger { background: #fee2e2; color: #dc2626; }
.mini-btn.success { background: #dcfce7; color: #15803d; }
.mini-btn.locked { background: #f1f5f9; color: #64748b; }
.empty { padding: 70rpx 0; text-align: center; color: #94a3b8; }
</style>
