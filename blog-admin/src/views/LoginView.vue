<template>
  <section class="page">
    <div class="login-card">
      <div class="card-header">
        <span class="header-dot"></span>
        <p class="eyebrow">BLOG ADMIN CONSOLE</p>
      </div>
      <h1>管理控制台</h1>
      <p class="sub">使用管理员账号登录以管理文章、分类、标签与评论审核。</p>

      <div class="form-area">
        <div class="field-stack">
          <label>ADMIN_PHONE</label>
          <input
            v-model="phone"
            type="tel"
            placeholder="13800000000"
            maxlength="11"
            @keyup.enter="focusCode"
          />
        </div>
        <div class="field-stack">
          <label>AUTH_CODE</label>
          <div class="code-row">
            <input
              ref="codeInput"
              v-model="code"
              placeholder="6 位验证码"
              maxlength="6"
              @keyup.enter="doLogin"
            />
            <button class="send-btn" @click="send" :disabled="sending">
              {{ sending ? 'SENDING...' : 'SEND_CODE' }}
            </button>
          </div>
        </div>
        <button class="login-btn" @click="doLogin" :disabled="loading">
          {{ loading ? 'AUTHENTICATING...' : 'ENTER_CONSOLE' }}
        </button>

        <div class="hint-box" v-if="mockCode">
          <span>DEV_MODE_MOCK_CODE</span>
          <strong>{{ mockCode }}</strong>
        </div>
        <p v-if="msg" class="msg" :class="{ error: isError }">{{ msg }}</p>
      </div>

      <div class="card-footer">
        <span class="footer-dot"></span>
        <span>SECURE_CONNECTION</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminAuthStore } from '../stores/auth'
import { sendCode } from '../api'

const router = useRouter()
const auth = useAdminAuthStore()

const phone = ref('13800000000')
const code = ref('')
const mockCode = ref('')
const msg = ref('')
const isError = ref(false)
const sending = ref(false)
const loading = ref(false)
const codeInput = ref(null)

const focusCode = () => { codeInput.value?.focus() }

const send = async () => {
  if (!phone.value || phone.value.length < 11) {
    msg.value = 'ERR: 请输入正确的手机号'
    isError.value = true
    return
  }
  sending.value = true
  try {
    const res = await sendCode(phone.value, 'LOGIN')
    mockCode.value = res.mockCode
    msg.value = '验证码已发送'
    isError.value = false
  } catch (e) {
    msg.value = `ERR: ${e.message}`
    isError.value = true
  } finally {
    sending.value = false
  }
}

const doLogin = async () => {
  if (!code.value) {
    msg.value = 'ERR: 请输入验证码'
    isError.value = true
    return
  }
  loading.value = true
  try {
    await auth.login(phone.value, code.value)
    router.push('/')
  } catch (e) {
    msg.value = `ERR: ${e.message}`
    isError.value = true
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
}

.login-card {
  width: min(480px, 100%);
  background: var(--admin-panel);
  backdrop-filter: blur(20px);
  border: 1px solid var(--admin-line-strong);
  box-shadow: var(--admin-shadow), 0 0 80px rgba(94, 200, 242, 0.04);
  position: relative;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 28px;
  border-bottom: 1px solid var(--admin-line);
}
.header-dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  background: var(--admin-accent);
  box-shadow: 0 0 10px rgba(94, 200, 242, 0.5);
}
.eyebrow {
  margin: 0;
  font-size: 11px;
  letter-spacing: .2em;
  color: var(--admin-accent);
  font-weight: 600;
}

.login-card h1 {
  margin: 28px 28px 0;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: .04em;
}
.sub {
  margin: 10px 28px 0;
  color: var(--admin-muted);
  font-size: 14px;
  line-height: 1.7;
}

.form-area {
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.field-stack { display: flex; flex-direction: column; gap: 6px; }
label {
  font-size: 11px;
  letter-spacing: .14em;
  color: var(--admin-muted);
  font-weight: 600;
}

input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid var(--admin-line-strong);
  background: var(--admin-input-bg);
  color: var(--admin-text);
  font-size: 15px;
  font-family: "SF Mono", "Consolas", monospace;
  letter-spacing: .04em;
  border-radius: var(--admin-radius);
  transition: border-color .18s ease, box-shadow .18s ease, background .18s ease;
}
input:focus {
  outline: none;
  border-color: var(--admin-accent);
  box-shadow: 0 0 0 3px rgba(94, 200, 242, 0.12);
}
input::placeholder { color: color-mix(in srgb, var(--admin-muted) 55%, transparent); }

.code-row { display: flex; gap: 10px; }
.code-row input { flex: 1; }

.send-btn {
  padding: 14px 18px;
  border: 1px solid var(--admin-line-strong);
  background: rgba(94, 200, 242, 0.08);
  color: var(--admin-accent);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: .08em;
  cursor: pointer;
  white-space: nowrap;
  border-radius: var(--admin-radius);
  transition: all .18s ease;
  font-family: "SF Mono", "Consolas", monospace;
}
.send-btn:hover:not(:disabled) { background: rgba(94, 200, 242, 0.16); border-color: var(--admin-accent); }
.send-btn:disabled { opacity: .4; cursor: not-allowed; }

.login-btn {
  padding: 16px;
  background: var(--admin-accent);
  color: #0a0e13;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: .12em;
  border: none;
  cursor: pointer;
  font-family: "SF Mono", "Consolas", monospace;
  border-radius: var(--admin-radius);
  transition: all .18s ease;
  box-shadow: 0 0 30px rgba(94, 200, 242, 0.18);
}
.login-btn:hover:not(:disabled) {
  background: #7ad8ff;
  box-shadow: 0 0 40px rgba(94, 200, 242, 0.3);
}
.login-btn:disabled { opacity: .5; cursor: not-allowed; }

.hint-box {
  padding: 14px 16px;
  border-left: 3px solid var(--admin-accent);
  background: rgba(94, 200, 242, 0.06);
  border-radius: var(--admin-radius);
}
.hint-box span { display: block; font-size: 10px; letter-spacing: .12em; color: var(--admin-muted); }
.hint-box strong { display: block; margin-top: 4px; font-size: 26px; color: var(--admin-accent); font-family: "SF Mono", "Consolas", monospace; }

.msg { margin: 0; font-size: 13px; color: var(--admin-success); font-family: "SF Mono", "Consolas", monospace; }
.msg.error { color: var(--admin-danger); }

.card-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  border-top: 1px solid var(--admin-line);
  font-size: 10px;
  letter-spacing: .12em;
  color: var(--admin-muted);
}
.footer-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: var(--admin-success);
}
</style>
