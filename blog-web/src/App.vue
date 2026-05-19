<template>
  <div class="web-shell">
    <div class="web-glow web-glow-a"></div>
    <div class="web-glow web-glow-b"></div>

    <nav class="top-nav">
      <div class="nav-inner">
        <router-link to="/" class="nav-brand">
          <span class="brand-mark">&#9670;</span>
          <span class="brand-text">Essays</span>
        </router-link>
        <div class="nav-links">
          <router-link to="/" class="nav-link">文章</router-link>
          <template v-if="auth.isLoggedIn">
            <router-link to="/profile" class="nav-user">{{ auth.user?.nickname || auth.user?.username }}</router-link>
            <button class="nav-btn" @click="auth.logout()">退出</button>
          </template>
          <router-link v-else to="/login" class="nav-btn nav-btn--accent">登录</router-link>
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

    <footer class="site-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <span class="brand-mark">&#9670;</span>
          <span>Essays — 写给长期主义者的个人博客</span>
        </div>
        <p class="footer-copy">&copy; {{ year }} All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const year = computed(() => new Date().getFullYear())

onMounted(() => {
  auth.fetchMe()
})
</script>

<style>
:root {
  --web-bg: #f4efe6;
  --web-paper: rgba(255, 251, 245, 0.88);
  --web-ink: #1f1a17;
  --web-muted: #7a6f67;
  --web-line: rgba(76, 58, 44, 0.14);
  --web-accent: #9f3d22;
  --web-accent-2: #2f5d50;
  --web-accent-3: #c75b3a;
  --web-shadow: 0 2px 24px rgba(75, 52, 34, 0.08);
  --web-shadow-lg: 0 24px 80px rgba(75, 52, 34, 0.13);
  --web-radius: 4px;
  --web-font-display: "Georgia", "Times New Roman", "Noto Serif SC", serif;
  --web-font-body: "Georgia", "Times New Roman", "Noto Serif SC", serif;
  font-family: var(--web-font-body);
  color: var(--web-ink);
  background: var(--web-bg);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

* { box-sizing: border-box; }

body {
  margin: 0;
  background:
    radial-gradient(ellipse at 0% 0%, rgba(159, 61, 34, 0.10), transparent 40%),
    radial-gradient(ellipse at 100% 30%, rgba(47, 93, 80, 0.09), transparent 35%),
    linear-gradient(180deg, #f8f2e8 0%, #efe7db 100%);
  background-attachment: fixed;
}

a { color: inherit; text-decoration: none; }
button, input, select, textarea { font: inherit; color: inherit; }

img { max-width: 100%; display: block; }

.web-shell {
  position: relative;
  min-height: 100vh;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
}

.web-glow {
  position: fixed;
  width: 32rem;
  height: 32rem;
  border-radius: 999px;
  filter: blur(80px);
  opacity: 0.30;
  pointer-events: none;
  z-index: 0;
}
.web-glow-a { background: rgba(159, 61, 34, 0.18); top: -10rem; left: -8rem; }
.web-glow-b { background: rgba(47, 93, 80, 0.14); right: -10rem; top: 18rem; }

/* ---- Top Navigation ---- */
.top-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(244, 239, 230, 0.72);
  backdrop-filter: blur(20px) saturate(1.2);
  -webkit-backdrop-filter: blur(20px) saturate(1.2);
  border-bottom: 1px solid var(--web-line);
}
.nav-inner {
  max-width: 1040px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: .02em;
  color: var(--web-ink);
}
.brand-mark {
  font-size: 24px;
  color: var(--web-accent);
  line-height: 1;
}
.nav-links {
  display: flex;
  align-items: center;
  gap: 18px;
}
.nav-link {
  font-size: 15px;
  color: var(--web-muted);
  transition: color .18s ease;
  padding: 6px 0;
  border-bottom: 2px solid transparent;
}
.nav-link:hover, .nav-link.router-link-exact-active {
  color: var(--web-ink);
  border-bottom-color: var(--web-accent);
}
.nav-user {
  font-size: 14px;
  color: var(--web-muted);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  transition: color .18s ease;
}
.nav-user:hover { color: var(--web-accent); }
.nav-btn {
  padding: 8px 18px;
  font-size: 14px;
  border: 1px solid var(--web-line);
  background: rgba(255,255,255,0.55);
  color: var(--web-ink);
  cursor: pointer;
  transition: all .18s ease;
  border-radius: var(--web-radius);
}
.nav-btn:hover { background: rgba(255,255,255,0.85); transform: translateY(-1px); }
.nav-btn--accent {
  background: var(--web-accent);
  color: #fff9f3;
  border-color: var(--web-accent);
}
.nav-btn--accent:hover { background: var(--web-accent-3); }

/* ---- Main Content ---- */
.main-content {
  flex: 1;
  position: relative;
  z-index: 1;
}

/* ---- Page Transitions ---- */
.page-fade-enter-active, .page-fade-leave-active {
  transition: opacity .24s ease, transform .24s ease;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ---- Footer ---- */
.site-footer {
  position: relative;
  z-index: 1;
  border-top: 1px solid var(--web-line);
  padding: 32px 24px;
  margin-top: 64px;
}
.footer-inner {
  max-width: 1040px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.footer-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--web-muted);
}
.footer-brand .brand-mark { font-size: 18px; }
.footer-copy {
  margin: 0;
  font-size: 13px;
  color: var(--web-muted);
}

@media (max-width: 720px) {
  .nav-inner { height: 50px; }
  .nav-brand .brand-text { display: none; }
  .footer-inner { flex-direction: column; text-align: center; }
}
</style>
