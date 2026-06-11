<template>
  <section class="page">
    <div class="login-card">
      <div class="card-header">
        <span class="header-icon">&#9733;</span>
        <span class="header-text">管理控制台</span>
      </div>

      <div class="card-body">
        <div class="tab-row">
          <button class="tab-btn" :class="{ active: mode === 'sms' }" @click="mode = 'sms'">短信登录</button>
          <button class="tab-btn" :class="{ active: mode === 'pwd' }" @click="mode = 'pwd'">密码登录</button>
        </div>

        <div class="field-stack">
          <label>手机号</label>
          <el-input v-model="phone" placeholder="输入管理员手机号" size="large" />
        </div>

        <template v-if="mode === 'sms'">
          <div class="field-stack">
            <label>验证码</label>
            <div class="code-row">
              <el-input ref="codeInputRef" v-model="code" placeholder="6 位验证码" size="large" @keyup.enter="doLogin" />
              <el-button @click="send" :loading="sending" class="send-btn">获取验证码</el-button>
            </div>
          </div>
        </template>

        <template v-if="mode === 'pwd'">
          <div class="field-stack">
            <label>密码</label>
            <el-input v-model="pwd" type="password" placeholder="输入密码" size="large" @keyup.enter="doLogin" />
          </div>
        </template>

        <el-button type="primary" size="large" @click="doLogin" :loading="loading" class="login-btn">
          登录控制台
        </el-button>

        <div class="hint-box" v-if="mockCode">
          <span>开发环境验证码</span>
          <strong>{{ mockCode }}</strong>
        </div>
        <p v-if="msg" class="msg" :class="{ error: isError }">{{ msg }}</p>
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

const mode = ref('sms')
const phone = ref('13800000000')
const code = ref('')
const pwd = ref('')
const mockCode = ref('')
const msg = ref('')
const isError = ref(false)
const sending = ref(false)
const loading = ref(false)
const codeInputRef = ref(null)

const send = async () => {
  if (!phone.value || phone.value.length < 11) {
    msg.value = '请输入正确的手机号'
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
    msg.value = e.message
    isError.value = true
  } finally { sending.value = false }
}

const doLogin = async () => {
  msg.value = ''; isError.value = false
  loading.value = true
  try {
    if (mode.value === 'pwd') {
      if (!pwd.value) { msg.value = '请输入密码'; isError.value = true; return }
      await auth.loginByPassword(phone.value, pwd.value)
    } else {
      if (!code.value) { msg.value = '请输入验证码'; isError.value = true; return }
      await auth.login(phone.value, code.value)
    }
    router.push('/')
  } catch (e) {
    msg.value = e.message; isError.value = true
  } finally { loading.value = false }
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
  width: min(420px, 100%);
  background: var(--admin-panel);
  border: 1px solid var(--admin-line);
  border-radius: var(--admin-radius);
  box-shadow: var(--admin-shadow-lg);
  overflow: hidden;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 28px;
  border-bottom: 1px solid var(--admin-line);
  background: var(--admin-soft-accent);
}
.header-icon { font-size: 18px; color: var(--admin-accent-3); }
.header-text { font-size: 15px; font-weight: 600; color: var(--admin-text); }

.card-body { padding: 28px; display: flex; flex-direction: column; gap: 16px; }
.tab-row { display: flex; gap: 8px; }
.tab-btn {
  flex: 1; padding: 10px; font-size: 14px;
  border: 1px solid var(--admin-line);
  background: var(--admin-bg); color: var(--admin-muted);
  cursor: pointer; border-radius: 8px; transition: all .2s;
}
.tab-btn.active { background: var(--admin-accent); color: #fff; border-color: var(--admin-accent); font-weight: 700; }
.tab-btn:not(.active):hover { border-color: var(--admin-accent); color: var(--admin-accent); }
.field-stack { display: flex; flex-direction: column; gap: 6px; }
.field-stack label { font-size: 13px; color: var(--admin-muted); font-weight: 600; }
.code-row { display: flex; gap: 10px; }
.code-row > :first-child { flex: 1; }
.send-btn { white-space: nowrap; }

.login-btn { width: 100%; }

.hint-box {
  padding: 12px 16px;
  border-left: 4px solid var(--admin-accent);
  background: var(--admin-soft-accent);
  border-radius: 8px;
}
.hint-box span { display: block; font-size: 11px; color: var(--admin-muted); }
.hint-box strong { display: block; margin-top: 4px; font-size: 24px; color: var(--admin-accent); }

.msg { margin: 0; font-size: 14px; color: var(--admin-success); }
.msg.error { color: var(--admin-danger); }
</style>
