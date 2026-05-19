<template>
  <section class="page" v-if="auth.user">
    <header class="profile-hero">
      <router-link class="back-link" to="/">&larr; 返回首页</router-link>
      <p class="eyebrow">ACCOUNT SETTINGS</p>
      <h1>个人信息</h1>
      <p class="sub">管理你的账户资料与偏好设置。</p>
      <div class="hero-line"></div>
    </header>

    <div class="profile-card">
      <div class="avatar-section">
        <div class="avatar-placeholder">{{ avatarLetter }}</div>
        <p class="avatar-hint">头像功能即将上线</p>
      </div>

      <div class="fields">
        <div class="field">
          <label>手机号</label>
          <input :value="auth.user.phone" disabled />
          <span class="field-note">手机号不可修改</span>
        </div>
        <div class="field">
          <label>用户名</label>
          <input :value="auth.user.username" disabled />
        </div>
        <div class="field">
          <label>昵称</label>
          <input v-model="form.nickname" placeholder="设置昵称" />
        </div>
        <div class="field">
          <label>邮箱</label>
          <input v-model="form.email" type="email" placeholder="your@email.com" />
        </div>
      </div>

      <div class="actions">
        <button class="primary-btn" @click="save" :disabled="saving">
          {{ saving ? '保存中...' : '保存修改' }}
        </button>
      </div>

      <p v-if="msg" class="msg" :class="{ error: isError }">{{ msg }}</p>
    </div>
  </section>

  <section class="page state-box" v-else>
    <p class="state-text">请先登录</p>
    <router-link to="/login?redirect=/profile" class="login-link">前往登录</router-link>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { updateProfile } from '../api'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const saving = ref(false)
const msg = ref('')
const isError = ref(false)

const form = reactive({ nickname: '', email: '' })

const avatarLetter = computed(() => {
  const name = auth.user?.nickname || auth.user?.username || 'U'
  return name.charAt(0).toUpperCase()
})

onMounted(() => {
  if (auth.user) {
    form.nickname = auth.user.nickname || ''
    form.email = auth.user.email || ''
  }
})

const save = async () => {
  saving.value = true
  try {
    const updated = await updateProfile({
      nickname: form.nickname || null,
      email: form.email || null,
      avatar: null
    })
    auth.user = updated
    msg.value = '个人信息已更新'
    isError.value = false
    setTimeout(() => router.push('/'), 600)
  } catch (e) {
    msg.value = e.message
    isError.value = true
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.page { max-width: 720px; margin: 0 auto; padding: 56px 24px 72px; }

.profile-hero { margin-bottom: 36px; }
.back-link {
  display: inline-flex;
  margin-bottom: 18px;
  color: var(--web-accent-2);
  font-size: 15px;
  transition: color .18s ease;
}
.back-link:hover { color: var(--web-accent); }
.eyebrow {
  margin: 0 0 12px;
  font-size: 11px;
  letter-spacing: .22em;
  color: var(--web-accent);
}
.profile-hero h1 {
  margin: 0;
  font-size: clamp(38px, 5vw, 56px);
  line-height: .94;
}
.sub { margin: 12px 0 0; color: var(--web-muted); font-size: 16px; }
.hero-line { margin-top: 24px; width: 48px; height: 3px; background: var(--web-accent); }

.profile-card {
  padding: 36px 32px;
  border: 1px solid var(--web-line);
  background: var(--web-paper);
  box-shadow: var(--web-shadow);
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 28px;
  border-bottom: 1px solid var(--web-line);
}
.avatar-placeholder {
  width: 72px; height: 72px;
  border-radius: 50%;
  background: var(--web-accent-2);
  color: #fff;
  font-size: 32px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-hint { margin: 10px 0 0; color: var(--web-muted); font-size: 13px; }

.fields { display: flex; flex-direction: column; gap: 18px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field label {
  font-size: 12px;
  letter-spacing: .1em;
  color: var(--web-muted);
  font-weight: 600;
}
.field input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid var(--web-line);
  background: rgba(255,255,255,0.88);
  color: var(--web-ink);
  font-size: 15px;
  border-radius: var(--web-radius);
  transition: border-color .18s ease;
}
.field input:focus {
  outline: 2px solid rgba(159, 61, 34, 0.18);
  border-color: rgba(159, 61, 34, 0.32);
}
.field input:disabled {
  opacity: .55;
  background: rgba(0,0,0,0.02);
  cursor: not-allowed;
}
.field-note { font-size: 12px; color: var(--web-muted); }

.actions { margin-top: 28px; display: flex; justify-content: flex-end; }
.primary-btn {
  padding: 14px 28px;
  background: var(--web-accent);
  color: #fff9f3;
  border: none;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  border-radius: var(--web-radius);
  transition: all .18s ease;
  box-shadow: 0 8px 24px rgba(159, 61, 34, 0.18);
}
.primary-btn:hover:not(:disabled) { background: var(--web-accent-3); }
.primary-btn:disabled { opacity: .5; cursor: not-allowed; }

.msg { margin: 16px 0 0; font-size: 14px; color: var(--web-accent-2); }
.msg.error { color: #c0392b; }

.state-box { text-align: center; padding: 120px 24px; }
.state-text { color: var(--web-muted); font-size: 18px; }
.login-link { color: var(--web-accent-2); font-weight: 700; }
</style>
