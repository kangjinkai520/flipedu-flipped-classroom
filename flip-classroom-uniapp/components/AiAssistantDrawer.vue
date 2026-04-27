<template>
  <view v-if="visible" class="assistant-layer">
    <view class="assistant-mask" @click="$emit('close')"></view>
    <view class="assistant-drawer" @click.stop @wheel.stop @touchmove.stop>
      <view class="assistant-head">
        <view>
          <text class="head-title">AI 学习助手</text>
          <text class="head-subtitle">{{ courseName }} · 支持提问、总结和解题思路</text>
        </view>
        <button class="close-btn" @click="$emit('close')">×</button>
      </view>

      <view class="tools-row">
        <view :class="['mode-btn', websearch ? 'active' : '']" @click="websearch = !websearch">
          <text>联网</text>
        </view>
        <view :class="['mode-btn', deepthink ? 'active' : '']" @click="deepthink = !deepthink">
          <text>深度思考</text>
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

      <scroll-view
        class="chat-scroll"
        scroll-y
        scroll-with-animation
        :scroll-into-view="scrollAnchor"
        @wheel.stop
        @touchmove.stop
      >
        <view v-if="!messages.length" class="welcome-card">
          <text class="welcome-title">可以直接问这门课的问题</text>
          <text class="welcome-desc">8000 端口未启动时这里会给出提示，不会影响课程页面继续使用。</text>
        </view>

        <view
          v-for="(message, index) in messages"
          :id="`drawer-msg-${index}`"
          :key="`drawer-msg-${index}`"
          :class="['message-row', message.role === 'assistant' ? 'assistant' : 'user']"
        >
          <view class="message-bubble">
            <image
              v-if="message.imageUrl"
              :src="message.imageUrl"
              class="message-image"
              mode="widthFix"
            />
            <text class="message-text">{{ message.content || '正在思考...' }}</text>
            <view v-if="message.loading" class="typing-loader">
              <view></view><view></view><view></view>
            </view>
          </view>
        </view>
        <view class="scroll-bottom-pad"></view>
      </scroll-view>

      <view v-if="uploadedImage.fullUrl" class="image-preview">
        <image :src="uploadedImage.fullUrl" class="preview-img" mode="aspectFill" />
        <text class="clear-image" @click="clearImage">移除图片</text>
      </view>

      <view class="composer">
        <textarea
          v-model="question"
          class="composer-input"
          auto-height
          maxlength="-1"
          placeholder="输入问题，或者上传图片后直接发送"
        />
        <view class="composer-actions">
          <button class="tool-btn" @click="chooseImage">图片</button>
          <button class="send-btn" :disabled="sending" @click="sendQuestion">
            {{ sending ? '发送中' : '发送' }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { sendStudyAssistantStream, uploadStudyImage } from '@/api/assistant'
import { requireSession } from '@/common/session'

const DEFAULT_IMAGE_PROMPT = '请根据我上传的图片，帮我提炼重点、解释思路，并指出容易出错的地方。'
const AI_OFFLINE_MESSAGE = 'AI 助手暂时不可用，请确认 Python 后端 kevin 环境已启动，并监听 8000 端口。'

export default {
  props: {
    visible: { type: Boolean, default: false },
    courseId: { type: Number, default: 0 },
    courseName: { type: String, default: '当前课程' }
  },
  data() {
    return {
      session: null,
      question: '',
      websearch: false,
      deepthink: false,
      sending: false,
      messages: [],
      currentTopicId: 0,
      scrollAnchor: '',
      uploadedImage: { relativeUrl: '', fullUrl: '', filename: '' },
      quickPrompts: [
        '总结本节课重点',
        '帮我整理复习提纲',
        '用例子解释这个知识点',
        '生成 5 道自测题',
        '分析我的错题原因'
      ]
    }
  },
  watch: {
    visible(nextValue) {
      if (nextValue && !this.session) {
        this.session = requireSession()
      }
    }
  },
  methods: {
    applyPrompt(prompt) {
      this.question = prompt
    },
    async chooseImage() {
      try {
        const chooseResult = await new Promise((resolve, reject) => {
          uni.chooseImage({ count: 1, success: resolve, fail: reject })
        })
        uni.showLoading({ title: '上传中...' })
        this.uploadedImage = await uploadStudyImage(chooseResult.tempFilePaths[0])
      } catch (error) {
        uni.showToast({ title: error.message || AI_OFFLINE_MESSAGE, icon: 'none', duration: 2600 })
      } finally {
        uni.hideLoading()
      }
    },
    clearImage() {
      this.uploadedImage = { relativeUrl: '', fullUrl: '', filename: '' }
    },
    async sendQuestion() {
      if (this.sending) return
      if (!this.session) this.session = requireSession()
      if (!this.session) return

      const rawQuestion = (this.question || '').trim()
      const finalQuestion = rawQuestion || (this.uploadedImage.relativeUrl ? DEFAULT_IMAGE_PROMPT : '')
      if (!finalQuestion) {
        uni.showToast({ title: '请输入问题', icon: 'none' })
        return
      }

      this.messages.push({
        role: 'user',
        content: rawQuestion || '请分析这张图片',
        imageUrl: this.uploadedImage.fullUrl
      })
      this.messages.push({ role: 'assistant', content: '', loading: true })
      this.question = ''
      this.sending = true
      this.scrollToBottom()

      try {
        await sendStudyAssistantStream({
          question: finalQuestion,
          userId: this.session.userId,
          topicId: this.currentTopicId,
          imageUrl: this.uploadedImage.relativeUrl,
          websearch: this.websearch,
          deepthink: this.deepthink,
          saveHistory: true,
          useRag: false,
          onChunk: (chunk) => {
            if (chunk.topic_id) {
              this.currentTopicId = chunk.topic_id
              return
            }
            const last = this.messages[this.messages.length - 1]
            if (last && chunk.content) {
              last.loading = false
              last.content += chunk.content
              this.scrollToBottom()
            }
          },
          onDone: () => {
            this.finishSending()
            this.clearImage()
          },
          onError: (error) => {
            this.showAiError(error)
          }
        })
      } catch (error) {
        this.showAiError(error)
      } finally {
        this.finishSending()
      }
    },
    showAiError(error) {
      const last = this.messages[this.messages.length - 1]
      if (last && last.role === 'assistant') {
        last.loading = false
        last.content = (error && error.message) || AI_OFFLINE_MESSAGE
      }
      this.scrollToBottom()
    },
    finishSending() {
      const last = this.messages[this.messages.length - 1]
      if (last) last.loading = false
      this.sending = false
    },
    scrollToBottom() {
      this.$nextTick(() => {
        this.scrollAnchor = `drawer-msg-${Math.max(this.messages.length - 1, 0)}`
      })
    }
  }
}
</script>

<style scoped>
.assistant-layer { position: fixed; inset: 0; z-index: 999; }
.assistant-mask { position: absolute; inset: 0; background: rgba(15, 23, 42, 0.35); }
.assistant-drawer {
  position: absolute;
  top: 0;
  right: 0;
  width: 86%;
  height: 100%;
  background: #f7f8fb;
  display: flex;
  flex-direction: column;
  box-shadow: -12rpx 0 40rpx rgba(15, 23, 42, 0.14);
  overflow: hidden;
  min-height: 0;
}
.assistant-head {
  padding: 72rpx 36rpx 28rpx;
  background: #ffffff;
  border-bottom: 1rpx solid #e8ecf3;
  display: flex;
  justify-content: space-between;
  gap: 24rpx;
}
.head-title { display: block; font-size: 34rpx; font-weight: 800; color: #1f2937; }
.head-subtitle { display: block; margin-top: 8rpx; font-size: 22rpx; color: #6b7280; line-height: 1.5; }
.close-btn {
  width: 64rpx;
  height: 64rpx;
  line-height: 60rpx;
  border-radius: 50%;
  background: #f1f5f9;
  color: #475569;
  border: none;
  font-size: 40rpx;
  padding: 0;
}
.tools-row { padding: 20rpx 28rpx 0; display: flex; gap: 16rpx; }
.mode-btn {
  padding: 12rpx 22rpx;
  border-radius: 14rpx;
  background: #ffffff;
  color: #64748b;
  font-size: 22rpx;
  font-weight: 700;
  border: 1rpx solid #e2e8f0;
}
.mode-btn.active { background: #eef2ff; color: #4f46e5; border-color: #c7d2fe; }
.prompt-scroll { padding: 18rpx 28rpx 8rpx; white-space: nowrap; box-sizing: border-box; }
.prompt-row { display: inline-flex; gap: 12rpx; }
.prompt-chip {
  padding: 12rpx 20rpx;
  background: #ffffff;
  border: 1rpx solid #e2e8f0;
  border-radius: 999rpx;
  font-size: 22rpx;
  color: #334155;
}
.chat-scroll {
  flex: 1;
  height: 0;
  min-height: 0;
  overflow-y: auto;
  padding: 24rpx 28rpx;
  box-sizing: border-box;
}
.welcome-card { background: #ffffff; border-radius: 24rpx; padding: 32rpx; border: 1rpx solid #e8ecf3; }
.welcome-title { display: block; color: #1f2937; font-size: 30rpx; font-weight: 800; }
.welcome-desc { display: block; margin-top: 10rpx; color: #64748b; font-size: 24rpx; line-height: 1.6; }
.message-row { display: flex; margin-bottom: 24rpx; }
.message-row.user { justify-content: flex-end; }
.message-bubble { max-width: 86%; padding: 22rpx 26rpx; border-radius: 22rpx; }
.assistant .message-bubble { background: #ffffff; color: #1f2937; border: 1rpx solid #e8ecf3; }
.user .message-bubble { background: #4f46e5; color: #ffffff; }
.message-text { font-size: 27rpx; line-height: 1.65; white-space: pre-wrap; }
.message-image { width: 100%; border-radius: 16rpx; margin-bottom: 14rpx; }
.typing-loader { display: flex; gap: 8rpx; padding-top: 8rpx; }
.typing-loader view { width: 10rpx; height: 10rpx; background: #94a3b8; border-radius: 50%; }
.image-preview { padding: 0 28rpx 16rpx; display: flex; align-items: center; gap: 16rpx; }
.preview-img { width: 110rpx; height: 110rpx; border-radius: 16rpx; }
.clear-image { color: #ef4444; font-size: 24rpx; font-weight: 700; }
.composer { padding: 20rpx 28rpx 48rpx; background: #ffffff; border-top: 1rpx solid #e8ecf3; }
.composer-input { width: 100%; min-height: 72rpx; max-height: 180rpx; font-size: 27rpx; color: #1f2937; }
.composer-actions { margin-top: 18rpx; display: flex; justify-content: space-between; gap: 18rpx; }
.tool-btn, .send-btn {
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 16rpx;
  font-size: 25rpx;
  font-weight: 800;
  border: none;
}
.tool-btn { flex: 1; background: #f1f5f9; color: #334155; }
.send-btn { flex: 1; background: #4f46e5; color: #ffffff; }
.scroll-bottom-pad { height: 30rpx; }
</style>
