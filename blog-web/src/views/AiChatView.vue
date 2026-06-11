<template>
  <section class="page">
    <div class="chat-panel">
      <div class="chat-header">
        <h2>AI 技术助手</h2>
        <p>小J 在线 · 基于 Qwen-Max</p>
        <button class="clear-btn" @click="clearMemory" :disabled="!sessionId">清除记忆</button>
      </div>

      <div class="chat-body" ref="chatBody">
        <div v-if="messages.length === 0" class="welcome">
          <span class="welcome-icon">&#9733;</span>
          <h3>你好，我是小J</h3>
          <p>我是 Javerry 的 AI 技术助手，可以回答编程、架构、数据库等问题。</p>
          <div class="suggestions">
            <button v-for="s in suggestions" :key="s" @click="sendMessage(s)">{{ s }}</button>
          </div>
        </div>

        <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
          <img v-if="m.role === 'user' && auth.user?.avatar" :src="auth.user.avatar" class="msg-avatar" />
          <div v-else class="msg-avatar">{{ m.role === 'user' ? 'U' : 'J' }}</div>
          <div class="msg-content">
            <div class="msg-role">{{ m.role === 'user' ? (auth.user?.nickname || '你') : '小J' }}</div>
            <div class="msg-text" v-html="renderMd(m.content)" :class="{ streaming: m.streaming }"></div>
          </div>
        </div>

        <div v-if="loading" class="msg assistant">
          <div class="msg-avatar">J</div>
          <div class="msg-content"><div class="msg-text">思考中...</div></div>
        </div>
      </div>

      <div class="chat-input">
        <textarea
          v-model="input"
          @keydown.enter.exact.prevent="send"
          placeholder="输入技术问题，Enter 发送，Shift+Enter 换行"
          rows="2"
          :disabled="loading"
        ></textarea>
        <button class="send-btn" @click="send" :disabled="loading || !input.trim()">发送</button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'

const auth = useAuthStore()
const router = useRouter()
const md = new MarkdownIt({ html: false, breaks: true })

const messages = ref([])
const input = ref('')
const loading = ref(false)
const sessionId = ref('')
const chatBody = ref(null)

const suggestions = [
  'Java 中 HashMap 的实现原理？',
  '如何优化 MySQL 查询性能？',
  'Vue 3 的 Composition API 有什么优势？',
  '微服务架构的优缺点是什么？'
]

const renderMd = (text) => DOMPurify.sanitize(md.render(text || ''))

const scrollDown = async () => {
  await nextTick()
  if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
}

const sendMessage = (msg) => {
  input.value = msg
  send()
}

const send = async () => {
  const text = input.value.trim()
  if (!text || loading.value) return
  input.value = ''
  const userMsg = { role: 'user', content: text }
  messages.value.push(userMsg)
  const aiMsg = { role: 'assistant', content: '', streaming: true }
  messages.value.push(aiMsg)
  await scrollDown()

  loading.value = true
  try {
    const token = localStorage.getItem('blog_token')
    const resp = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: token || '' },
      body: JSON.stringify({ message: text, sessionId: sessionId.value })
    })
    const reader = resp.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const line of lines) {
        if (line.startsWith('data:')) {
          aiMsg.content += line.substring(5)
        }
      }
      await nextTick()
      await scrollDown()
    }
    if (buffer.startsWith('data:')) {
      aiMsg.content += buffer.substring(5)
    }
  } catch {
    aiMsg.content = '请求失败，请稍后再试。'
  } finally {
    aiMsg.streaming = false
    loading.value = false
  }
}

const clearMemory = async () => {
  try {
    const token = localStorage.getItem('blog_token')
    await fetch(`/api/ai/memory/${sessionId.value}`, {
      method: 'DELETE', headers: { Authorization: token || '' }
    })
    messages.value = []
    sessionId.value = ''
  } catch { /* ignore */ }
}

onMounted(() => {
  if (!auth.isLoggedIn) router.push('/login?redirect=/ai')
  sessionId.value = 'sess_' + Date.now()
})
</script>

<style scoped>
.page { max-width: 800px; margin: 0 auto; padding: 32px 24px 64px; display: flex; flex-direction: column; min-height: calc(100vh - 120px); }

.chat-panel {
  flex: 1; display: flex; flex-direction: column;
  background: rgba(0, 0, 0, 0.3); backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px); border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: var(--web-radius); box-shadow: var(--web-shadow);
  overflow: hidden;
}

.chat-header {
  display: flex; align-items: center; gap: 12px;
  padding: 16px 20px; border-bottom: 1px solid var(--web-line);
  background: var(--admin-soft-accent);
}
.chat-header h2 { margin: 0; font-size: 18px; }
.chat-header p { margin: 0; font-size: 13px; color: rgba(255, 255, 255, 0.5); flex: 1; }
.clear-btn {
  padding: 6px 14px; font-size: 12px; border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.08); color: rgba(255, 255, 255, 0.5);
  border-radius: 999px; cursor: pointer; transition: all .2s;
}
.clear-btn:hover { border-color: var(--web-accent-3); color: var(--web-accent-3); }

.chat-body { flex: 1; overflow-y: auto; padding: 20px; }

.welcome { text-align: center; padding: 40px 0; }
.welcome-icon { font-size: 48px; color: var(--web-accent-3); }
.welcome h3 { margin: 12px 0 8px; font-size: 22px; }
.welcome p { color: rgba(255, 255, 255, 0.5); margin: 0 0 20px; }
.suggestions { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.suggestions button {
  padding: 8px 16px; font-size: 13px; border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.08); color: rgba(255, 255, 255, 0.85);
  border-radius: 999px; cursor: pointer; transition: all .2s;
}
.suggestions button:hover { border-color: var(--web-accent); color: var(--web-accent); }

.msg { display: flex; gap: 12px; margin-bottom: 20px; }
.msg.user { flex-direction: row-reverse; }
.msg-avatar {
  width: 36px; height: 36px; border-radius: 50%; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 700; color: #fff;
  object-fit: cover;
}
.msg.assistant .msg-avatar { background: linear-gradient(135deg, var(--web-accent), var(--web-accent-3)); }
.msg.user .msg-avatar { background: var(--web-muted); }
.msg-content { max-width: 75%; }
.msg-role { font-size: 12px; color: rgba(255, 255, 255, 0.5); margin-bottom: 4px; }
.msg.user .msg-role { text-align: right; }
.msg-text { font-size: 15px; line-height: 1.7; color: rgba(255, 255, 255, 0.85); }
.msg-text.streaming::after { content: '▍'; animation: blink .8s infinite; }
@keyframes blink { 50% { opacity: 0; } }

/* markdown styles in chat */
.msg-text :deep(pre) {
  background: rgba(255, 255, 255, 0.05); padding: 12px 16px; overflow-x: auto;
  font-size: 13px; line-height: 1.5; border-left: 3px solid var(--web-accent);
  border-radius: 0 6px 6px 0; margin: 10px 0;
}
.msg-text :deep(code) {
  font-family: "Consolas", "Monaco", monospace; font-size: .9em;
  background: rgba(255, 255, 255, 0.06); padding: 2px 6px; border-radius: 3px;
}
.msg-text :deep(pre code) { background: none; padding: 0; }
.msg-text :deep(p) { margin: 6px 0; }
.msg-text :deep(li) { margin: 4px 0; }

.chat-input {
  display: flex; gap: 10px; padding: 14px 20px;
  border-top: 1px solid var(--web-line); background: rgba(255, 255, 255, 0.03);
}
.chat-input textarea {
  flex: 1; padding: 12px 14px; border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(0, 0, 0, 0.3); backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px); color: rgba(255, 255, 255, 0.85);
  font-size: 15px; border-radius: var(--web-radius); resize: none;
}
.chat-input textarea:focus {
  outline: 2px solid rgba(74, 144, 217, 0.15); border-color: var(--web-accent);
}
.send-btn {
  padding: 12px 24px; background: var(--web-accent-3); color: #fff;
  border: none; font-size: 15px; font-weight: 700; border-radius: 999px;
  cursor: pointer; transition: all .22s; align-self: flex-end;
}
.send-btn:hover:not(:disabled) { background: #e47890; transform: scale(1.04); }
.send-btn:disabled { opacity: .4; cursor: not-allowed; }
@media (max-width: 640px) {
  .page { padding: 16px 10px 40px; }
  .msg-content { max-width: 90%; }
  .chat-header { padding: 12px 14px; }
  .chat-body { padding: 14px; }
  .chat-input { padding: 10px 14px; }
}
</style>
