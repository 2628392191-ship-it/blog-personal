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
        <p class="footer-copy">&copy; {{ year }} Jerry L-served.</p>
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
  --web-bg: #f0f5ff;
  --web-paper: #ffffff;
  --web-ink: #2c3e50;
  --web-muted: #7b8da0;
  --web-line: rgba(100, 140, 200, 0.15);
  --web-accent: #4a90d9;
  --web-accent-2: #5bb89e;
  --web-accent-3: #f08ca0;
  --web-shadow: 0 4px 24px rgba(74, 144, 217, 0.10);
  --web-shadow-lg: 0 16px 48px rgba(74, 144, 217, 0.14);
  --web-radius: 12px;
  --web-font-display: "ZCOOL KuaiLe", "Noto Sans SC", sans-serif;
  --web-font-body: "Noto Sans SC", "Inter", -apple-system, sans-serif;
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
    radial-gradient(ellipse at 20% 10%, rgba(74, 144, 217, 0.12), transparent 45%),
    radial-gradient(ellipse at 80% 60%, rgba(240, 140, 160, 0.10), transparent 40%),
    radial-gradient(ellipse at 50% 90%, rgba(91, 184, 158, 0.08), transparent 35%),
    linear-gradient(180deg, #eef4ff 0%, #f0f5ff 50%, #f8f4ff 100%);
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

/* ---- Cloud decorations ---- */
.web-glow {
  position: fixed;
  pointer-events: none;
  z-index: 0;
}
.web-glow-a {
  width: 260px; height: 90px;
  top: 8%; left: 5%;
  background: rgba(74, 144, 217, 0.10);
  border-radius: 90px;
  box-shadow:
    60px -30px 0 20px rgba(74, 144, 217, 0.08),
    140px -15px 0 10px rgba(74, 144, 217, 0.06);
}
.web-glow-a::after {
  content: '';
  position: absolute;
  width: 100px; height: 100px;
  right: -30px; top: -50px;
  background: radial-gradient(circle, rgba(240, 140, 160, 0.12) 0%, transparent 70%);
  border-radius: 50%;
}
.web-glow-b {
  width: 200px; height: 70px;
  right: 8%; top: 55%;
  background: rgba(91, 184, 158, 0.09);
  border-radius: 80px;
  box-shadow:
    -50px -25px 0 15px rgba(91, 184, 158, 0.07),
    -110px 10px 0 8px rgba(74, 144, 217, 0.05);
}

/* ---- Top Navigation ---- */
.top-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(240, 245, 255, 0.70);
  backdrop-filter: blur(24px) saturate(1.4);
  -webkit-backdrop-filter: blur(24px) saturate(1.4);
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
  font-family: var(--web-font-display);
  font-size: 22px;
  color: var(--web-accent);
  transition: transform .2s ease;
}
.nav-brand:hover { transform: scale(1.04); }
.brand-mark {
  font-size: 26px;
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
  transition: all .2s ease;
  padding: 6px 12px;
  border-radius: 999px;
}
.nav-link:hover, .nav-link.router-link-exact-active {
  color: var(--web-accent);
  background: rgba(74, 144, 217, 0.08);
}
.nav-user {
  font-size: 14px;
  color: var(--web-muted);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  transition: color .2s ease;
}
.nav-user:hover { color: var(--web-accent); }
.nav-btn {
  padding: 7px 18px;
  font-size: 14px;
  border: 1px solid var(--web-line);
  background: rgba(255,255,255,0.60);
  color: var(--web-ink);
  cursor: pointer;
  transition: all .22s ease;
  border-radius: 999px;
}
.nav-btn:hover { background: rgba(255,255,255,0.90); transform: scale(1.04); }
.nav-btn--accent {
  background: var(--web-accent-3);
  color: #fff;
  border-color: var(--web-accent-3);
}
.nav-btn--accent:hover { background: #e47890; transform: scale(1.06); }

/* ---- Main Content ---- */
.main-content {
  flex: 1;
  position: relative;
  z-index: 1;
}

/* ---- Page Transitions ---- */
.page-fade-enter-active, .page-fade-leave-active {
  transition: opacity .28s ease, transform .28s ease;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(16px) scale(.98);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(.99);
}

/* ---- Footer ---- */
.site-footer {
  position: relative;
  z-index: 1;
  border-top: 1px solid var(--web-line);
  padding: 24px;
  margin-top: 64px;
  opacity: .7;
}
.footer-inner {
  max-width: 1040px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 13px;
  color: var(--web-muted);
}
.footer-brand {
  display: flex;
  align-items: center;
  gap: 6px;
}
.footer-brand .brand-mark { font-size: 16px; color: var(--web-accent-3); }
.footer-copy { margin: 0; }

@media (max-width: 720px) {
  .nav-inner { height: 50px; }
  .nav-brand .brand-text { display: none; }
  .footer-inner { flex-direction: column; text-align: center; }
}
</style>
