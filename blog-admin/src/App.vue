<template>
  <div class="admin-shell">
    <div class="admin-grid"></div>

    <nav class="top-bar">
      <div class="bar-inner">
        <div class="bar-brand">
          <span class="brand-dot"></span>
          <span class="brand-text">BLOG ADMIN</span>
        </div>
        <div class="bar-actions">
          <span class="bar-hint">{{ auth.isLoggedIn ? '控制台' : '登录入口' }}</span>
          <button class="theme-btn" @click="toggleTheme">
            {{ currentTheme === 'dark' ? '切换亮色' : '切换暗色' }}
          </button>
          <button v-if="auth.isLoggedIn" class="bar-btn" @click="handleLogout">退出登录</button>
        </div>
      </div>
    </nav>

    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminAuthStore } from './stores/auth'

const router = useRouter()
const auth = useAdminAuthStore()

const THEME_KEY = 'admin_theme'
const currentTheme = ref('dark')

const applyTheme = (theme) => {
  currentTheme.value = theme === 'light' ? 'light' : 'dark'
  document.documentElement.setAttribute('data-admin-theme', currentTheme.value)
  localStorage.setItem(THEME_KEY, currentTheme.value)
}

const toggleTheme = () => {
  applyTheme(currentTheme.value === 'dark' ? 'light' : 'dark')
}

const handleLogout = () => {
  auth.logout()
  router.push('/login')
}

onMounted(() => {
  applyTheme(localStorage.getItem(THEME_KEY) || 'dark')
})
</script>

<style>
:root {
  --admin-bg: #0a0e13;
  --admin-surface: #111820;
  --admin-panel: rgba(18, 24, 34, 0.92);
  --admin-panel-hover: rgba(24, 32, 44, 0.94);
  --admin-line: rgba(120, 160, 220, 0.10);
  --admin-line-strong: rgba(120, 160, 220, 0.18);
  --admin-text: #e4ecfa;
  --admin-muted: #7e8ca0;
  --admin-accent: #5ec8f2;
  --admin-accent-2: #8b8af0;
  --admin-danger: #f07070;
  --admin-success: #5ec8a0;
  --admin-shadow: 0 4px 32px rgba(0, 0, 0, 0.45);
  --admin-radius: 6px;
  --admin-body-gradient:
    radial-gradient(ellipse at 50% 0%, rgba(94, 200, 242, 0.08), transparent 40%),
    radial-gradient(ellipse at 80% 20%, rgba(139, 138, 240, 0.07), transparent 35%),
    linear-gradient(180deg, #0a0e13 0%, #111820 100%);
  --admin-grid-line: rgba(120, 160, 220, 0.04);
  --admin-topbar-bg: rgba(10, 14, 19, 0.78);
  --admin-input-bg: rgba(0, 0, 0, 0.28);
  --admin-soft-accent: rgba(94, 200, 242, 0.08);
  --admin-soft-accent-strong: rgba(94, 200, 242, 0.16);
  --admin-soft-danger: rgba(240, 112, 112, 0.08);
  --admin-soft-success: rgba(94, 200, 160, 0.15);
  --admin-soft-warn: rgba(240, 184, 112, 0.15);
  --admin-table-head: rgba(120, 160, 220, 0.06);
  --admin-table-hover: rgba(120, 160, 220, 0.04);
  --admin-btn-text: #0a0e13;
  font-family: "SF Mono", "Consolas", "PingFang SC", "Microsoft YaHei", monospace;
  color: var(--admin-text);
  background: var(--admin-bg);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

:root[data-admin-theme='light'] {
  --admin-bg: #edf2f8;
  --admin-surface: #f7fbff;
  --admin-panel: rgba(255, 255, 255, 0.92);
  --admin-panel-hover: rgba(247, 251, 255, 0.98);
  --admin-line: rgba(84, 112, 150, 0.12);
  --admin-line-strong: rgba(84, 112, 150, 0.22);
  --admin-text: #152033;
  --admin-muted: #617086;
  --admin-accent: #1677d2;
  --admin-accent-2: #6f63d9;
  --admin-danger: #d84f5f;
  --admin-success: #2b8a66;
  --admin-shadow: 0 10px 32px rgba(27, 45, 78, 0.12);
  --admin-body-gradient:
    radial-gradient(ellipse at 20% 0%, rgba(22, 119, 210, 0.10), transparent 40%),
    radial-gradient(ellipse at 80% 10%, rgba(111, 99, 217, 0.10), transparent 30%),
    linear-gradient(180deg, #eef4fb 0%, #dfe8f5 100%);
  --admin-grid-line: rgba(84, 112, 150, 0.06);
  --admin-topbar-bg: rgba(244, 248, 252, 0.82);
  --admin-input-bg: rgba(255, 255, 255, 0.94);
  --admin-soft-accent: rgba(22, 119, 210, 0.08);
  --admin-soft-accent-strong: rgba(22, 119, 210, 0.16);
  --admin-soft-danger: rgba(216, 79, 95, 0.10);
  --admin-soft-success: rgba(43, 138, 102, 0.14);
  --admin-soft-warn: rgba(201, 136, 44, 0.16);
  --admin-table-head: rgba(22, 119, 210, 0.06);
  --admin-table-hover: rgba(22, 119, 210, 0.05);
  --admin-btn-text: #ffffff;
}

* { box-sizing: border-box; }
html, body, #app { min-height: 100%; }
body {
  margin: 0;
  background: var(--admin-body-gradient);
  background-attachment: fixed;
  transition: background .2s ease, color .2s ease;
}
a { color: inherit; text-decoration: none; }
button, input, select, textarea { font: inherit; color: inherit; }
img { max-width: 100%; display: block; }

.admin-shell {
  position: relative;
  min-height: 100vh;
  overflow-x: hidden;
}

.admin-grid {
  position: fixed;
  inset: 0;
  background-image:
    linear-gradient(var(--admin-grid-line) 1px, transparent 1px),
    linear-gradient(90deg, var(--admin-grid-line) 1px, transparent 1px);
  background-size: 40px 40px;
  mask-image: linear-gradient(180deg, rgba(255,255,255,0.5) 0%, transparent 80%);
  pointer-events: none;
  z-index: 0;
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--admin-topbar-bg);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border-bottom: 1px solid var(--admin-line);
}
.bar-inner {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 24px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.bar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: .14em;
  color: var(--admin-accent);
}
.brand-dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  background: var(--admin-accent);
  box-shadow: 0 0 10px color-mix(in srgb, var(--admin-accent) 55%, transparent);
}
.bar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.bar-hint {
  font-size: 13px;
  color: var(--admin-muted);
  letter-spacing: .06em;
}
.bar-btn,
.theme-btn {
  padding: 6px 16px;
  font-size: 13px;
  border: 1px solid var(--admin-line-strong);
  background: rgba(255,255,255,0.04);
  color: var(--admin-muted);
  cursor: pointer;
  border-radius: var(--admin-radius);
  transition: all .18s ease;
}
.theme-btn {
  background: var(--admin-soft-accent);
  color: var(--admin-accent);
}
.theme-btn:hover {
  border-color: var(--admin-accent);
  background: var(--admin-soft-accent-strong);
}
.bar-btn:hover {
  border-color: var(--admin-danger);
  color: var(--admin-danger);
  background: var(--admin-soft-danger);
}

.main-content {
  position: relative;
  z-index: 1;
}

.page-fade-enter-active, .page-fade-leave-active {
  transition: opacity .2s ease, transform .2s ease;
}
.page-fade-enter-from { opacity: 0; transform: translateY(8px); }
.page-fade-leave-to { opacity: 0; transform: translateY(-6px); }

.el-tabs__header { margin-bottom: 0 !important; }
.el-tabs__nav-wrap::after { display: none !important; }
.el-tabs__item {
  color: var(--admin-muted) !important;
  font-size: 14px !important;
  height: 44px !important;
  line-height: 44px !important;
  padding: 0 20px !important;
}
.el-tabs__item.is-active { color: var(--admin-accent) !important; }
.el-tabs__active-bar { background-color: var(--admin-accent) !important; height: 2px !important; }

.el-table {
  background: transparent !important;
  color: var(--admin-text) !important;
}
.el-table th.el-table__cell {
  background: var(--admin-table-head) !important;
  color: var(--admin-muted) !important;
  font-weight: 600 !important;
  font-size: 12px !important;
  letter-spacing: .08em !important;
  border-bottom: 1px solid var(--admin-line) !important;
}
.el-table td.el-table__cell { border-bottom: 1px solid var(--admin-line) !important; }
.el-table tr:hover > td { background: var(--admin-table-hover) !important; }
.el-table__inner-wrapper::before { display: none !important; }

.el-pagination {
  justify-content: center !important;
  margin-top: 20px !important;
}
.el-pagination button, .el-pager li {
  background: rgba(255,255,255,0.04) !important;
  color: var(--admin-muted) !important;
  border: 1px solid var(--admin-line) !important;
}
.el-pager li.is-active {
  background: var(--admin-accent) !important;
  color: var(--admin-btn-text) !important;
  border-color: var(--admin-accent) !important;
}

.el-input__wrapper,
.el-textarea__inner,
.el-select__wrapper {
  background: var(--admin-input-bg) !important;
  box-shadow: 0 0 0 1px var(--admin-line-strong) inset !important;
  color: var(--admin-text) !important;
}
.el-input__inner,
.el-textarea__inner,
.el-select__selected-item,
.el-select__placeholder {
  color: var(--admin-text) !important;
}
.el-select-dropdown__item,
.el-select-dropdown__empty,
.el-select-dropdown__loading,
.el-select-dropdown__wrap {
  color: var(--admin-text) !important;
}
.el-select-dropdown,
.el-popper.is-light {
  background: var(--admin-panel) !important;
  border-color: var(--admin-line-strong) !important;
  box-shadow: var(--admin-shadow) !important;
}
.el-select-dropdown__item.is-hovering,
.el-select-dropdown__item.hover,
.el-select-dropdown__item.selected {
  background: var(--admin-soft-accent) !important;
}

.el-message {
  background: var(--admin-panel) !important;
  border: 1px solid var(--admin-line-strong) !important;
  box-shadow: var(--admin-shadow) !important;
}
.el-message--success { border-color: color-mix(in srgb, var(--admin-success) 50%, transparent) !important; }
.el-message--error { border-color: color-mix(in srgb, var(--admin-danger) 50%, transparent) !important; }

.el-dialog,
.el-message-box {
  background: var(--admin-panel) !important;
  border: 1px solid var(--admin-line-strong) !important;
  box-shadow: var(--admin-shadow) !important;
}
.el-dialog__header,
.el-message-box__header { border-bottom: 1px solid var(--admin-line) !important; }
.el-dialog__title,
.el-message-box__title,
.el-message-box__content,
.el-message-box__message,
.el-message__content { color: var(--admin-text) !important; }

.el-button--primary { background: var(--admin-accent) !important; border-color: var(--admin-accent) !important; color: var(--admin-btn-text) !important; }
.el-button--danger { background: var(--admin-soft-danger) !important; border-color: var(--admin-danger) !important; color: var(--admin-danger) !important; }

@media (max-width: 860px) {
  .bar-inner { height: auto; padding-top: 10px; padding-bottom: 10px; align-items: flex-start; }
  .bar-actions { flex-wrap: wrap; justify-content: flex-end; }
}
</style>
