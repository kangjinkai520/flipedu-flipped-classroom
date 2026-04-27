<template>
  <view class="fc-page assistant-page">
    <view class="hero-card">
      <view class="hero-main">
        <text class="hero-kicker">AI STUDY COMPANION</text>
        <text class="hero-title">{{ courseName }} · AI 助手</text>
        <text class="hero-desc">
          面向当前课程的学习助手，可以帮你解释知识点、总结资料重点、梳理解题思路，也支持图片提问。
        </text>
        <view class="hero-tags">
          <text class="hero-tag">课程答疑</text>
          <text class="hero-tag">资料解读</text>
          <text class="hero-tag">作业辅导</text>
        </view>
      </view>
      <view class="hero-side">
        <view class="hero-side-pill">轻量模式</view>
        <text class="hero-side-note">当前版本不保留历史记录，适合快速提问与即时答疑。</text>
      </view>
    </view>

    <scroll-view class="prompt-scroll" scroll-x show-scrollbar="false">
      <view class="prompt-row">
        <text
          v-for="item in quickPrompts"
          :key="item"
          class="prompt-chip"
          @click="applyPrompt(item)"
        >{{ item }}</text>
      </view>
    </scroll-view>

    <view class="fc-panel capability-panel">
      <view class="mode-grid">
        <view :class="['mode-chip', websearch ? 'mode-chip-active' : '']" @click="websearch = !websearch">
          <text class="mode-chip-title">联网搜索</text>
          <text class="mode-chip-desc">需要扩展信息时可打开</text>
        </view>
        <view :class="['mode-chip', deepthink ? 'mode-chip-active' : '']" @click="deepthink = !deepthink">
          <text class="mode-chip-title">深度思考</text>
          <text class="mode-chip-desc">回答会更完整，但速度更慢</text>
        </view>
      </view>
      <text class="mode-caption">当前默认关闭旧知识库检索，先以通用学习问答为主，后续可接入课程资料知识库。</text>
    </view>

    <scroll-view
      class="chat-panel"
      scroll-y
      scroll-with-animation
      :scroll-into-view="scrollAnchor"
    >
      <view v-if="!messages.length" class="empty-state">
        <view class="empty-orb">AI</view>
        <text class="empty-title">还没有开始提问</text>
        <text class="empty-desc">你可以让它解释本章重点、分析题目思路、总结课件内容，或者直接上传题目截图。</text>
      </view>

      <view
        v-for="(message, index) in messages"
        :id="`msg-${index}`"
        :key="`msg-${index}`"
        :class="['message-row', message.role === 'assistant' ? 'is-assistant' : 'is-user']"
      >
        <view v-if="message.role === 'assistant'" class="message-avatar">
          <text class="message-avatar-text">AI</text>
        </view>

        <view :class="['message-bubble', message.role === 'assistant' ? 'message-bubble-assistant' : 'message-bubble-user']">
          <image
            v-if="message.imageUrl"
            :src="message.imageUrl"
            class="message-image"
            mode="widthFix"
          />
          <text class="message-text">{{ message.content || '正在整理回答…' }}</text>
        </view>
      </view>
    </scroll-view>

    <view v-if="uploadedImage.fullUrl" class="fc-panel preview-panel">
      <view class="preview-head">
        <view>
          <text class="preview-title">已选择图片</text>
          <text class="preview-subtitle">{{ uploadedImage.filename || '题目截图' }}</text>
        </view>
        <text class="preview-clear" @click="clearImage">移除</text>
      </view>
      <image :src="uploadedImage.fullUrl" class="preview-image" mode="aspectFill" />
    </view>

    <view class="composer-shell">
      <view class="composer-card">
        <textarea
          v-model="question"
          class="composer-input"
          auto-height
          maxlength="-1"
          placeholder="请输入课程问题，例如：请帮我解释这一章的重点知识。"
          placeholder-class="composer-placeholder"
        />

        <view class="composer-actions">
          <button class="fc-secondary-btn composer-ghost" @click="chooseImage">图片提问</button>
          <button class="fc-primary-btn composer-send" :disabled="sending" @click="sendQuestion">
            {{ sending ? '回答中…' : '发送给 AI' }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { sendStudyAssistantStream, uploadStudyImage } from '@/api/assistant'
import { requireSession } from '@/common/session'

const DEFAULT_IMAGE_PROMPT = '请结合这张图片内容，帮我分析题目或知识点，并给出清晰的学习建议。'

export default {
  data() {
    return {
      session: null,
      courseId: 0,
      courseName: '当前课程',
      question: '',
      websearch: false,
      deepthink: false,
      sending: false,
      messages: [],
      uploadedImage: {
        relativeUrl: '',
        fullUrl: '',
        filename: ''
      },
      scrollAnchor: '',
      quickPrompts: [
        '帮我解释这一章的重点知识',
        '请用更容易理解的话总结课件内容',
        '这道题应该从哪里入手',
        '帮我生成一个复习提纲'
      ]
    }
  },
  onLoad(query) {
    const session = requireSession()
    if (!session) {
      return
    }
    this.session = session
    this.courseId = Number(query.courseId || 0)
    this.courseName = query.courseName || '当前课程'
    uni.setNavigationBarTitle({
      title: 'AI 学习助手'
    })
  },
  methods: {
    applyPrompt(prompt) {
      this.question = prompt
    },
    async chooseImage() {
      try {
        const chooseResult = await new Promise((resolve, reject) => {
          uni.chooseImage({
            count: 1,
            sizeType: ['compressed'],
            sourceType: ['album', 'camera'],
            success: resolve,
            fail: reject
          })
        })

        uni.showLoading({ title: '上传图片中…' })
        const uploaded = await uploadStudyImage(chooseResult.tempFilePaths[0])
        this.uploadedImage = uploaded
        uni.hideLoading()
        uni.showToast({
          title: '图片已准备好',
          icon: 'success'
        })
      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '图片上传失败',
          icon: 'none'
        })
      }
    },
    clearImage() {
      this.uploadedImage = {
        relativeUrl: '',
        fullUrl: '',
        filename: ''
      }
    },
    async sendQuestion() {
      if (this.sending) {
        return
      }

      const rawQuestion = (this.question || '').trim()
      const finalQuestion = rawQuestion || (this.uploadedImage.relativeUrl ? DEFAULT_IMAGE_PROMPT : '')
      if (!finalQuestion) {
        uni.showToast({
          title: '请先输入问题或上传图片',
          icon: 'none'
        })
        return
      }

      const displayQuestion = rawQuestion || '请结合这张图片帮我分析题目。'
      this.messages.push({
        role: 'user',
        content: displayQuestion,
        imageUrl: this.uploadedImage.fullUrl
      })
      this.messages.push({
        role: 'assistant',
        content: ''
      })

      this.question = ''
      this.sending = true
      this.scrollToBottom()

      try {
        await sendStudyAssistantStream({
          question: finalQuestion,
          userId: this.session.userId,
          imageUrl: this.uploadedImage.relativeUrl,
          websearch: this.websearch,
          deepthink: this.deepthink,
          onChunk: (chunk) => {
            const lastMessage = this.messages[this.messages.length - 1]
            if (!lastMessage || lastMessage.role !== 'assistant') {
              return
            }
            if (chunk.content) {
              lastMessage.content += chunk.content
              this.scrollToBottom()
            }
          },
          onDone: () => {
            this.sending = false
            this.clearImage()
            this.scrollToBottom()
          },
          onError: () => {
            this.sending = false
          }
        })
      } catch (error) {
        const lastMessage = this.messages[this.messages.length - 1]
        if (lastMessage && lastMessage.role === 'assistant') {
          lastMessage.content = '当前无法连接 AI 服务，请确认学习助手服务已经启动，并检查 8000 端口是否可用。'
        }
        this.sending = false
      }
    },
    scrollToBottom() {
      this.$nextTick(() => {
        this.scrollAnchor = `msg-${Math.max(this.messages.length - 1, 0)}`
      })
    }
  }
}
</script>

<style scoped>
.assistant-page {
  min-height: 100vh;
  padding: 24rpx 24rpx 248rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  background:
    radial-gradient(circle at top right, rgba(255, 194, 131, 0.18), transparent 25%),
    radial-gradient(circle at 10% 18%, rgba(255, 234, 204, 0.26), transparent 18%),
    linear-gradient(180deg, #fffdf9 0%, #fff8f1 100%);
}

.hero-card {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) 200rpx;
  gap: 18rpx;
  padding: 28rpx;
  border-radius: 34rpx;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.98), rgba(255, 246, 234, 0.96));
  border: 2rpx solid rgba(242, 140, 40, 0.08);
  box-shadow: 0 20rpx 48rpx rgba(189, 132, 65, 0.1);
}

.hero-kicker {
  display: inline-flex;
  align-items: center;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(255, 145, 61, 0.12);
  color: #d87912;
  font-size: 20rpx;
  font-weight: 700;
  letter-spacing: 2rpx;
}

.hero-title {
  display: block;
  margin-top: 18rpx;
  color: #20253a;
  font-size: 42rpx;
  line-height: 1.18;
  font-weight: 800;
}

.hero-desc {
  display: block;
  margin-top: 14rpx;
  color: #7e7063;
  font-size: 24rpx;
  line-height: 1.7;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 18rpx;
}

.hero-tag {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.88);
  color: #4a4350;
  font-size: 22rpx;
  border: 2rpx solid rgba(202, 166, 121, 0.14);
}

.hero-side {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 18rpx;
}

.hero-side-pill {
  align-self: flex-start;
  min-height: 60rpx;
  padding: 0 18rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #313b59, #242d47);
  color: #fff;
  font-size: 22rpx;
  font-weight: 700;
}

.hero-side-note {
  color: #998b7e;
  font-size: 22rpx;
  line-height: 1.7;
}

.prompt-scroll {
  white-space: nowrap;
}

.prompt-row {
  display: inline-flex;
  gap: 14rpx;
  padding-bottom: 4rpx;
}

.prompt-chip {
  display: inline-flex;
  align-items: center;
  min-height: 64rpx;
  padding: 0 22rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.92);
  border: 2rpx solid rgba(202, 166, 121, 0.14);
  color: #4a4350;
  font-size: 24rpx;
  box-shadow: 0 10rpx 24rpx rgba(189, 132, 65, 0.08);
}

.prompt-chip:active,
.mode-chip:active,
.preview-clear:active,
.composer-send:active,
.composer-ghost:active {
  transform: scale(0.98);
}

.capability-panel {
  padding: 22rpx 24rpx;
}

.mode-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
}

.mode-chip {
  padding: 18rpx;
  border-radius: 24rpx;
  background: rgba(255, 247, 237, 0.92);
  border: 2rpx solid rgba(214, 177, 132, 0.12);
}

.mode-chip-active {
  background: linear-gradient(135deg, #fff0db, #ffd9ae);
  border-color: rgba(242, 140, 40, 0.2);
  box-shadow: 0 12rpx 24rpx rgba(242, 140, 40, 0.12);
}

.mode-chip-title {
  display: block;
  color: #2f3447;
  font-size: 26rpx;
  font-weight: 700;
}

.mode-chip-desc {
  display: block;
  margin-top: 6rpx;
  color: #8e7f73;
  font-size: 22rpx;
  line-height: 1.55;
}

.mode-caption {
  display: block;
  margin-top: 14rpx;
  color: #998b7e;
  font-size: 22rpx;
  line-height: 1.7;
}

.chat-panel {
  flex: 1;
  min-height: 520rpx;
  padding-right: 4rpx;
}

.empty-state {
  margin-top: 40rpx;
  padding: 56rpx 34rpx;
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.84);
  border: 2rpx dashed rgba(214, 177, 132, 0.2);
  text-align: center;
}

.empty-orb {
  width: 110rpx;
  height: 110rpx;
  margin: 0 auto 18rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #313b59, #f28c28);
  color: #fff;
  font-size: 34rpx;
  font-weight: 800;
  box-shadow: 0 18rpx 34rpx rgba(242, 140, 40, 0.18);
}

.empty-title {
  display: block;
  color: #20253a;
  font-size: 32rpx;
  font-weight: 700;
}

.empty-desc {
  display: block;
  margin-top: 12rpx;
  color: #857669;
  font-size: 24rpx;
  line-height: 1.72;
}

.message-row {
  display: flex;
  gap: 14rpx;
  margin-bottom: 18rpx;
}

.is-user {
  justify-content: flex-end;
}

.message-avatar {
  width: 60rpx;
  height: 60rpx;
  flex: 0 0 60rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #313b59, #f28c28);
  color: #fff;
  box-shadow: 0 12rpx 24rpx rgba(36, 45, 71, 0.18);
}

.message-avatar-text {
  font-size: 22rpx;
  font-weight: 800;
}

.message-bubble {
  max-width: 82%;
  padding: 22rpx 24rpx;
  border-radius: 28rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  box-shadow: 0 16rpx 30rpx rgba(189, 132, 65, 0.08);
}

.message-bubble-assistant {
  background: rgba(255, 255, 255, 0.95);
  border-top-left-radius: 14rpx;
}

.message-bubble-user {
  background: linear-gradient(135deg, #313b59, #4a5a89);
  border-top-right-radius: 14rpx;
}

.message-text {
  color: #2f3447;
  font-size: 26rpx;
  line-height: 1.76;
  white-space: pre-wrap;
}

.message-bubble-user .message-text {
  color: #fff;
}

.message-image {
  width: 100%;
  border-radius: 22rpx;
}

.preview-panel {
  padding: 22rpx;
}

.preview-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12rpx;
}

.preview-title {
  display: block;
  color: #20253a;
  font-size: 28rpx;
  font-weight: 700;
}

.preview-subtitle {
  display: block;
  margin-top: 6rpx;
  color: #918275;
  font-size: 22rpx;
}

.preview-clear {
  color: #d87912;
  font-size: 24rpx;
  font-weight: 700;
}

.preview-image {
  width: 100%;
  height: 220rpx;
  margin-top: 18rpx;
  border-radius: 24rpx;
}

.composer-shell {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 18rpx 20rpx 24rpx;
  background: linear-gradient(180deg, rgba(255, 248, 241, 0), rgba(255, 248, 241, 0.94) 26%, rgba(255, 248, 241, 0.98) 100%);
}

.composer-card {
  padding: 18rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.98);
  border: 2rpx solid rgba(214, 177, 132, 0.12);
  box-shadow: 0 20rpx 40rpx rgba(189, 132, 65, 0.1);
}

.composer-input {
  width: 100%;
  min-height: 120rpx;
  max-height: 320rpx;
  font-size: 28rpx;
  line-height: 1.7;
  color: #2f3447;
}

.composer-placeholder {
  color: #b2a699;
}

.composer-actions {
  display: flex;
  justify-content: space-between;
  gap: 14rpx;
  margin-top: 18rpx;
}

.composer-ghost,
.composer-send {
  flex: 1;
  margin: 0;
}

@media screen and (max-width: 720rpx) {
  .hero-card {
    grid-template-columns: 1fr;
  }

  .mode-grid {
    grid-template-columns: 1fr;
  }
}
</style>
