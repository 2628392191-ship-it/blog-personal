<template>
  <div class="admin-shell" :class="{ 'no-sidebar': $route.path === '/login' }">
    <aside class="sidebar" v-if="$route.path !== '/login'">
      <div class="sidebar-brand" @click="$router.push('/articles')">
        <span class="brand-icon">&#9733;</span>
        <span class="brand-text">Blog Admin</span>
      </div>

      <nav class="sidebar-nav">
        <router-link to="/articles" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">&#9998;</span>
          <span>文章管理</span>
        </router-link>
        <router-link to="/comments" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">&#9993;</span>
          <span>评论审核</span>
        </router-link>
        <router-link to="/categories" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">&#9737;</span>
          <span>分类管理</span>
        </router-link>
        <router-link to="/tags" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">&#9733;</span>
          <span>标签管理</span>
        </router-link>
        <router-link to="/users" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon">&#9787;</span>
          <span>用户管理</span>
        </router-link>
      </nav>

      <div class="sidebar-foot">
        <button class="theme-toggle" @click="toggleTheme">
          {{ currentTheme === 'dark' ? '&#9728; 亮色' : '&#9790; 暗色' }}
        </button>
        <button v-if="auth.isLoggedIn" class="logout-btn" @click="handleLogout">退出</button>
      </div>
    </aside>

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
const currentTheme = ref('light')

const applyTheme = (theme) => {
  currentTheme.value = theme === 'dark' ? 'dark' : 'light'
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
  applyTheme(localStorage.getItem(THEME_KEY) || 'light')
})
</script>

<style>
:root {
  --admin-bg: #f5f7fb;
  --admin-surface: #fafbfd;
  --admin-panel: #ffffff;
  --admin-line: rgba(168, 184, 210, 0.22);
  --admin-line-strong: rgba(140, 156, 186, 0.35);
  --admin-text: #2e3852;
  --admin-muted: #8a95b0;
  --admin-accent: #6c9fd4;
  --admin-accent-2: #c4a0d8;
  --admin-accent-3: #f0a8b8;
  --admin-danger: #e87888;
  --admin-success: #78b89c;
  --admin-shadow: 0 4px 24px rgba(108, 130, 170, 0.10);
  --admin-shadow-lg: 0 12px 40px rgba(108, 130, 170, 0.14);
  --admin-radius: 10px;
  --admin-input-bg: rgba(248, 250, 253, 0.95);
  --admin-soft-accent: rgba(108, 159, 212, 0.08);
  --admin-soft-accent-strong: rgba(108, 159, 212, 0.16);
  --admin-soft-danger: rgba(232, 120, 136, 0.08);
  --admin-soft-success: rgba(120, 184, 156, 0.12);
  --admin-soft-warn: rgba(224, 176, 128, 0.14);
  --admin-table-head: rgba(108, 159, 212, 0.05);
  --admin-table-hover: rgba(108, 159, 212, 0.04);
  --admin-btn-text: #ffffff;
  --admin-sidebar-bg: #ffffff;
  --admin-sidebar-hover: rgba(108, 159, 212, 0.06);
  --admin-sidebar-active: rgba(108, 159, 212, 0.10);
  font-family: "Noto Sans SC", "Inter", -apple-system, sans-serif;
  color: var(--admin-text);
  background: var(--admin-bg);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

:root[data-admin-theme='dark'] {
  --admin-bg: #161b25;
  --admin-surface: #1c2230;
  --admin-panel: #1e2434;
  --admin-line: rgba(140, 160, 200, 0.12);
  --admin-line-strong: rgba(140, 160, 200, 0.20);
  --admin-text: #d8def0;
  --admin-muted: #7c869e;
  --admin-accent: #7ab4e0;
  --admin-accent-2: #b8a0d0;
  --admin-accent-3: #f0a0b0;
  --admin-danger: #e87888;
  --admin-success: #78b89c;
  --admin-shadow: 0 4px 28px rgba(0, 0, 0, 0.30);
  --admin-shadow-lg: 0 16px 48px rgba(0, 0, 0, 0.40);
  --admin-input-bg: rgba(30, 36, 52, 0.90);
  --admin-soft-accent: rgba(122, 180, 224, 0.08);
  --admin-soft-accent-strong: rgba(122, 180, 224, 0.15);
  --admin-soft-danger: rgba(232, 120, 136, 0.08);
  --admin-soft-success: rgba(120, 184, 156, 0.12);
  --admin-soft-warn: rgba(224, 176, 128, 0.12);
  --admin-table-head: rgba(122, 180, 224, 0.05);
  --admin-table-hover: rgba(122, 180, 224, 0.04);
  --admin-btn-text: #161b25;
  --admin-sidebar-bg: #1a202e;
  --admin-sidebar-hover: rgba(122, 180, 224, 0.06);
  --admin-sidebar-active: rgba(122, 180, 224, 0.10);
}

* { box-sizing: border-box; }
html, body, #app { min-height: 100%; }
body {
  margin: 0;
  background: var(--admin-bg);
  transition: background .25s ease, color .25s ease;
}
a { color: inherit; text-decoration: none; }
button { font: inherit; cursor: pointer; }
img { max-width: 100%; display: block; }

/* ---- Shell ---- */
.admin-shell {
  display: flex;
  min-height: 100vh;
}

/* ---- Sidebar ---- */
.sidebar {
  position: sticky;
  top: 0;
  width: 220px;
  min-height: 100vh;
  background: var(--admin-sidebar-bg);
  border-right: 1px solid var(--admin-line);
  display: flex;
  flex-direction: column;
  padding: 20px 14px;
  gap: 6px;
  z-index: 100;
  flex-shrink: 0;
}
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: opacity .2s;
}
.sidebar-brand:hover { opacity: .8; }
.brand-icon { font-size: 22px; color: var(--admin-accent-3); }
.brand-text { font-size: 17px; font-weight: 700; color: var(--admin-text); letter-spacing: .02em; }

.sidebar-nav { display: flex; flex-direction: column; gap: 2px; flex: 1; }

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  font-size: 14px;
  color: var(--admin-muted);
  border-radius: 8px;
  transition: all .22s ease;
  font-weight: 500;
  border-left: 3px solid transparent;
}
.nav-item:hover {
  background: var(--admin-sidebar-hover);
  color: var(--admin-text);
}
.nav-item--active {
  background: var(--admin-sidebar-active);
  color: var(--admin-accent);
  border-left-color: var(--admin-accent);
  font-weight: 600;
}
.nav-icon { font-size: 16px; width: 20px; text-align: center; }

.sidebar-foot {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-top: 12px;
  border-top: 1px solid var(--admin-line);
}
.theme-toggle, .logout-btn {
  padding: 8px 14px;
  font-size: 13px;
  border: none;
  background: transparent;
  color: var(--admin-muted);
  border-radius: 8px;
  transition: all .2s;
  text-align: left;
}
.theme-toggle:hover { background: var(--admin-soft-accent); color: var(--admin-accent); }
.logout-btn:hover { background: var(--admin-soft-danger); color: var(--admin-danger); }

/* ---- Main Content ---- */
.main-content {
  flex: 1;
  min-width: 0;
  padding: 32px 36px 64px;
  position: relative;
  z-index: 1;
}
.no-sidebar .main-content { padding: 0; }

/* ---- Transitions ---- */
.page-fade-enter-active, .page-fade-leave-active {
  transition: opacity .22s ease, transform .22s ease;
}
.page-fade-enter-from { opacity: 0; transform: translateY(8px); }
.page-fade-leave-to { opacity: 0; transform: translateY(-6px); }

/* ---- Element Plus Overrides ---- */
.el-table {
  background: transparent !important;
  color: var(--admin-text) !important;
  --el-table-border-color: var(--admin-line) !important;
}
.el-table th.el-table__cell {
  background: var(--admin-table-head) !important;
  color: var(--admin-muted) !important;
  font-weight: 600 !important;
  font-size: 12px !important;
  letter-spacing: .06em !important;
  border-bottom: 1px solid var(--admin-line) !important;
}
.el-table td.el-table__cell { border-bottom: 1px solid var(--admin-line) !important; }
.el-table tr:hover > td { background: var(--admin-table-hover) !important; }
.el-table__inner-wrapper::before { display: none !important; }

.el-pagination { justify-content: center !important; margin-top: 24px !important; }
.el-pagination button, .el-pager li {
  background: transparent !important;
  color: var(--admin-muted) !important;
  border: 1px solid var(--admin-line) !important;
  border-radius: 8px !important;
}
.el-pager li.is-active {
  background: var(--admin-accent) !important;
  color: var(--admin-btn-text) !important;
  border-color: var(--admin-accent) !important;
}

.el-input__wrapper, .el-textarea__inner, .el-select__wrapper {
  background: var(--admin-input-bg) !important;
  box-shadow: 0 0 0 1px var(--admin-line-strong) inset !important;
  border-radius: 8px !important;
}
.el-input__inner, .el-textarea__inner, .el-select__placeholder,
.el-select__selected-item { color: var(--admin-text) !important; }

.el-select-dropdown, .el-popper.is-light {
  background: var(--admin-panel) !important;
  border: 1px solid var(--admin-line-strong) !important;
  box-shadow: var(--admin-shadow-lg) !important;
  border-radius: 10px !important;
}
.el-select-dropdown__item:hover, .el-select-dropdown__item.is-hovering,
.el-select-dropdown__item.selected {
  background: var(--admin-soft-accent) !important;
  color: var(--admin-text) !important;
  border-radius: 6px !important;
}

.el-message {
  background: var(--admin-panel) !important;
  border: 1px solid var(--admin-line) !important;
  box-shadow: var(--admin-shadow) !important;
  border-radius: 10px !important;
}
.el-message__content { color: var(--admin-text) !important; }

.el-dialog, .el-message-box, .el-drawer {
  background: var(--admin-panel) !important;
  border: 1px solid var(--admin-line) !important;
  box-shadow: var(--admin-shadow-lg) !important;
}
.el-dialog__title, .el-message-box__title, .el-message-box__message,
.el-drawer__title { color: var(--admin-text) !important; }
.el-dialog__header, .el-message-box__header {
  border-bottom: 1px solid var(--admin-line) !important;
}

.el-button--primary {
  background: var(--admin-accent) !important;
  border-color: var(--admin-accent) !important;
  color: var(--admin-btn-text) !important;
  border-radius: 8px !important;
}
.el-button--danger {
  background: transparent !important;
  border-color: var(--admin-danger) !important;
  color: var(--admin-danger) !important;
  border-radius: 8px !important;
}
.el-button--danger:hover { background: var(--admin-soft-danger) !important; }
.el-button { border-radius: 8px !important; }

.el-switch {
  --el-switch-on-color: var(--admin-accent) !important;
  --el-switch-off-color: var(--admin-line-strong) !important;
}

.el-tag {
  border-radius: 6px !important;
  border: none !important;
}

@media (max-width: 860px) {
  .sidebar { width: 180px; padding: 16px 10px; }
  .main-content { padding: 24px 18px 48px; }
}
@media (max-width: 640px) {
  .admin-shell { flex-direction: column; }
  .sidebar { position: static; width: 100%; min-height: auto; padding: 12px 14px; flex-direction: row; flex-wrap: wrap; align-items: center; gap: 8px; }
  .sidebar-brand { margin-bottom: 0; }
  .sidebar-nav { flex-direction: row; flex: none; }
  .sidebar-foot { flex-direction: row; border-top: none; padding-top: 0; }
  .main-content { padding: 20px 14px 40px; }
}
</style>
