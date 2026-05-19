<template>
  <section class="page">
    <div class="auth-card">
      <div class="auth-intro">
        <span class="intro-icon">&#9670;</span>
        <p class="eyebrow">JOIN THE CONVERSATION</p>
        <h1>加入博客社区</h1>
        <p class="lead">
          注册账号后即可在文章下方发表评论、参与讨论。我们使用手机验证码登录，无需记忆密码。
        </p>
        <div class="feature-list">
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>阅读所有公开文章</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>发表评论与回复</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>收藏喜欢的文章</span>
          </div>
        </div>
      </div>

      <div class="auth-form">
        <div class="tab-row">
          <button
            class="tab-btn"
            :class="{ active: mode === 'login' }"
            @click="mode = 'login'"
          >登录</button>
          <button
            class="tab-btn"
            :class="{ active: mode === 'register' }"
            @click="mode = 'register'"
          >注册</button>
        </div>

        <div class="field-stack">
          <label>手机号</label>
          <input
            v-model="phone"
            type="tel"
            placeholder="输入手机号"
            maxlength="11"
          />
        </div>

        <div class="field-stack">
          <label>验证码</label>
          <div class="code-row">
            <input
              v-model="code"
              placeholder="6 位验证码"
              maxlength="6"
            />
            <button class="send-btn" @click="send" :disabled="sending">
              {{ sending ? '发送中...' : '获取验证码' }}
            </button>
          </div>
        </div>

        <button class="submit-btn" @click="handleSubmit" :disabled="auth.loading">
          {{ auth.loading ? '处理中...' : (mode === 'login' ? '登录' : '注册') }}
        </button>

        <div class="hint-box" v-if="mockCode">
          <span>开发环境验证码</span>
          <strong>{{ mockCode }}</strong>
        </div>

        <p v-if="msg" class="msg" :class="{ error: isError }">{{ msg }}</p>

        <p class="back-link">
          <router-link to="/">&larr; 返回文章列表</router-link>
        </p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { sendCode } from '../api'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const mode = ref('login')
const phone = ref('13900000000')
const code = ref('')
const mockCode = ref('')
const msg = ref('')
const isError = ref(false)
const sending = ref(false)

const send = async () => {
  if (!phone.value || phone.value.length < 11) {
    msg.value = '请输入正确的手机号'
    isError.value = true
    return
  }
  sending.value = true
  try {
    const bizType = mode.value === 'login' ? 'LOGIN' : 'REGISTER'
    const res = await sendCode(phone.value, bizType)
    mockCode.value = res.mockCode
    msg.value = '验证码已发送'
    isError.value = false
  } catch (e) {
    msg.value = e.message
    isError.value = true
  } finally {
    sending.value = false
  }
}

const handleSubmit = async () => {
  if (!code.value) {
    msg.value = '请输入验证码'
    isError.value = true
    return
  }
  try {
    if (mode.value === 'login') {
      await auth.login(phone.value, code.value)
    } else {
      await auth.register(phone.value, code.value)
    }
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    msg.value = e.message
    isError.value = true
  }
}
</script>

<style scoped>
.page {
  min-height: calc(100vh - 56px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

.auth-card {
  width: min(880px, 100%);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  background: var(--web-paper);
  border: 1px solid var(--web-line);
  box-shadow: var(--web-shadow-lg);
}

.auth-intro {
  padding: 48px 40px;
  background: linear-gradient(160deg, rgba(159, 61, 34, 0.04) 0%, rgba(47, 93, 80, 0.06) 100%);
  border-right: 1px solid var(--web-line);
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.intro-icon { font-size: 36px; color: var(--web-accent); margin-bottom: 18px; }
.eyebrow {
  margin: 0;
  font-size: 11px;
  letter-spacing: .2em;
  color: var(--web-accent);
}
.auth-intro h1 {
  margin: 10px 0 16px;
  font-size: 42px;
  line-height: 1.05;
  letter-spacing: -.01em;
}
.lead {
  margin: 0 0 28px;
  color: var(--web-muted);
  font-size: 16px;
  line-height: 1.75;
}

.feature-list { display: flex; flex-direction: column; gap: 12px; }
.feature-item { display: flex; align-items: center; gap: 10px; font-size: 15px; color: var(--web-ink); }
.feature-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: var(--web-accent-2);
  flex-shrink: 0;
}

.auth-form {
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.tab-row {
  display: flex;
  gap: 0;
  margin-bottom: 12px;
  border: 1px solid var(--web-line);
}
.tab-btn {
  flex: 1;
  padding: 14px;
  font-size: 16px;
  border: none;
  background: transparent;
  color: var(--web-muted);
  cursor: pointer;
  transition: all .18s ease;
}
.tab-btn.active { background: var(--web-accent); color: #fff9f3; font-weight: 700; }
.tab-btn:not(.active):hover { color: var(--web-ink); background: rgba(0,0,0,0.03); }

.field-stack { display: flex; flex-direction: column; gap: 6px; }
label { font-size: 13px; color: var(--web-muted); letter-spacing: .06em; }

input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid var(--web-line);
  background: rgba(255,255,255,0.9);
  color: var(--web-ink);
  font-size: 16px;
  border-radius: var(--web-radius);
  transition: border-color .18s ease, outline .18s ease;
}
input:focus {
  outline: 2px solid rgba(159, 61, 34, 0.18);
  border-color: rgba(159, 61, 34, 0.32);
}

.code-row { display: flex; gap: 10px; }
.code-row input { flex: 1; }

.send-btn {
  padding: 14px 18px;
  border: 1px solid var(--web-line);
  background: rgba(255,255,255,0.9);
  color: var(--web-accent-2);
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
  border-radius: var(--web-radius);
  transition: all .18s ease;
}
.send-btn:hover:not(:disabled) { background: var(--web-accent-2); color: #fff; border-color: var(--web-accent-2); }
.send-btn:disabled { opacity: .5; cursor: not-allowed; }

.submit-btn {
  padding: 16px;
  background: var(--web-accent);
  color: #fff9f3;
  font-size: 17px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  border-radius: var(--web-radius);
  transition: all .18s ease;
  box-shadow: 0 8px 24px rgba(159, 61, 34, 0.22);
}
.submit-btn:hover:not(:disabled) { background: var(--web-accent-3); transform: translateY(-1px); }
.submit-btn:disabled { opacity: .6; cursor: not-allowed; }

.hint-box {
  padding: 14px 16px;
  border-left: 3px solid var(--web-accent-2);
  background: rgba(47, 93, 80, 0.08);
  border-radius: var(--web-radius);
}
.hint-box span { display: block; font-size: 11px; letter-spacing: .12em; color: var(--web-muted); }
.hint-box strong { display: block; margin-top: 4px; font-size: 24px; color: var(--web-accent-2); }

.msg { margin: 0; font-size: 14px; color: var(--web-accent-2); }
.msg.error { color: #c0392b; }

.back-link { margin: 0; font-size: 14px; color: var(--web-muted); }
.back-link a:hover { color: var(--web-ink); }

@media (max-width: 700px) {
  .auth-card { grid-template-columns: 1fr; }
  .auth-intro { border-right: none; border-bottom: 1px solid var(--web-line); padding: 36px 28px; }
  .auth-form { padding: 36px 28px; }
  .auth-intro h1 { font-size: 32px; }
}
</style>
