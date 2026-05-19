<template>
  <section class="page" v-if="article">
    <header class="article-hero">
      <router-link class="back-link" to="/">&larr; 返回列表</router-link>
      <p class="eyebrow">LONGFORM ENTRY</p>
      <h1>{{ article.title }}</h1>
      <div class="hero-meta">
        <span>{{ article.publishTime || '待发布' }}</span>
        <span>阅读 {{ article.viewCount || 0 }}</span>
      </div>
      <div class="hero-line"></div>
    </header>

    <section class="article-body">
      <div class="content" v-html="renderedMarkdown"></div>
    </section>

    <section class="comment-card">
      <div class="comment-head">
        <h2>评论区</h2>
        <span v-if="auth.isLoggedIn">{{ comments.length }} 条互动</span>
      </div>

      <!-- Guest mode: login to view comments -->
      <div v-if="!auth.isLoggedIn" class="guest-banner">
        <p>登录后可查看和发表评论。</p>
        <router-link :to="`/login?redirect=/article/${article.id}`" class="login-cta">立即登录 / 注册</router-link>
      </div>

      <!-- Logged in: full comment section -->
      <template v-else>
        <div class="composer">
          <textarea v-model="content" placeholder="写下你的看法..." rows="3"></textarea>
          <button class="primary-btn" @click="submitComment">发布评论</button>
        </div>

        <p v-if="msg" class="msg" :class="{ error: isError }">{{ msg }}</p>

        <ul class="comment-list" v-if="comments.length > 0">
          <li v-for="c in comments" :key="c.id" class="comment-item">
            <div class="comment-body">
              <div class="comment-left">
                <span class="comment-avatar">{{ (c.nickname || '用户')[0] }}</span>
              </div>
              <div class="comment-main">
                <div class="comment-meta">
                  <span class="comment-author">{{ c.nickname || '用户' }}</span>
                  <span class="comment-time">{{ fmtTime(c.createdAt) }}</span>
                </div>
                <p class="comment-text">{{ c.content }}</p>
                <div class="reply-row">
                  <input v-model="replyContent[c.id]" :placeholder="`回复 ${c.nickname || '用户'}...`" />
                  <button class="ghost-btn" @click="submitReply(c)">回复</button>
                </div>
              </div>
            </div>
          </li>
        </ul>

        <div v-else class="empty-comments">
          <p>暂无评论，成为第一个参与讨论的人。</p>
        </div>
      </template>
    </section>
  </section>

  <section class="page state-box" v-else>
    <p class="state-text">加载中...</p>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { addComment, articleDetail, commentList } from '../api'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const auth = useAuthStore()
const article = ref(null)
const comments = ref([])
const content = ref('')
const msg = ref('')
const isError = ref(false)
const replyContent = ref({})

const fmtTime = (raw) => {
  if (!raw) return ''
  const d = new Date(raw)
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' }) + ' ' +
         d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const renderedMarkdown = computed(() => {
  if (!article.value?.contentMd) return ''
  let md = article.value.contentMd
  md = md.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  md = md.replace(/^### (.+)$/gm, '<h3>$1</h3>')
  md = md.replace(/^## (.+)$/gm, '<h2>$1</h2>')
  md = md.replace(/^# (.+)$/gm, '<h1>$1</h1>')
  md = md.replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
  md = md.replace(/`([^`]+)`/g, '<code>$1</code>')
  md = md.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  md = md.replace(/\*(.+?)\*/g, '<em>$1</em>')
  md = md.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img alt="$1" src="$2" />')
  md = md.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>')
  md = md.replace(/^> (.+)$/gm, '<blockquote>$1</blockquote>')
  md = md.replace(/^- (.+)$/gm, '<li>$1</li>')
  md = md.replace(/^(\d+)\. (.+)$/gm, '<li>$1. $2</li>')
  md = md.replace(/\n{2,}/g, '</p><p>')
  md = '<p>' + md + '</p>'
  return md
})

const load = async () => {
  const id = route.params.id
  article.value = await articleDetail(id)
  comments.value = await commentList(id)
}

const submitComment = async () => {
  if (!content.value.trim()) return
  try {
    await addComment({ articleId: Number(route.params.id), content: content.value })
    content.value = ''
    comments.value = await commentList(route.params.id)
    msg.value = '评论成功'
    isError.value = false
  } catch (e) {
    msg.value = e.message
    isError.value = true
  }
}

const submitReply = async (comment) => {
  const rc = replyContent.value[comment.id]
  if (!rc || !rc.trim()) return
  try {
    await addComment({ articleId: Number(route.params.id), parentId: comment.id, replyToUserId: comment.userId, content: rc })
    replyContent.value[comment.id] = ''
    comments.value = await commentList(route.params.id)
    msg.value = '回复成功'
    isError.value = false
  } catch (e) {
    msg.value = e.message
    isError.value = true
  }
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 960px; margin: 0 auto; padding: 56px 24px 72px; }

.article-hero { margin-bottom: 36px; }
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
.article-hero h1 {
  margin: 0;
  font-size: clamp(38px, 5vw, 72px);
  line-height: .94;
  letter-spacing: -.01em;
}
.summary {
  margin-top: 16px;
  color: var(--web-muted);
  font-size: 18px;
  line-height: 1.7;
  max-width: 58ch;
}
.hero-meta {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-top: 14px;
  color: var(--web-muted);
  font-size: 14px;
  letter-spacing: .04em;
}
.hero-line {
  margin-top: 28px;
  width: 48px;
  height: 3px;
  background: var(--web-accent);
}

.article-body {
  margin: 32px 0 48px;
  padding: 36px 32px;
  border: 1px solid var(--web-line);
  background: var(--web-paper);
  box-shadow: var(--web-shadow);
}
.content { line-height: 1.9; color: var(--web-ink); font-size: 17px; }
.content :deep(h1) { font-size: 36px; margin: 32px 0 16px; line-height: 1.15; }
.content :deep(h2) { font-size: 28px; margin: 28px 0 14px; line-height: 1.2; }
.content :deep(h3) { font-size: 22px; margin: 24px 0 12px; line-height: 1.25; }
.content :deep(p) { margin: 0 0 20px; }
.content :deep(pre) {
  background: rgba(0,0,0,0.04);
  padding: 20px 24px;
  overflow-x: auto;
  font-size: 15px;
  line-height: 1.6;
  border-left: 3px solid var(--web-accent);
  margin: 20px 0;
}
.content :deep(code) {
  font-family: "Consolas", "Monaco", monospace;
  font-size: 0.9em;
  background: rgba(0,0,0,0.04);
  padding: 2px 6px;
  border-radius: 3px;
}
.content :deep(pre code) { background: none; padding: 0; }
.content :deep(blockquote) {
  border-left: 3px solid var(--web-accent-2);
  padding: 8px 20px;
  margin: 20px 0;
  color: var(--web-muted);
  font-style: italic;
}
.content :deep(li) { margin: 6px 0; }
.content :deep(a) { color: var(--web-accent-2); text-decoration: underline; }
.content :deep(strong) { font-weight: 700; }
.content :deep(img) { max-width: 100%; margin: 16px 0; }

.comment-card {
  border: 1px solid var(--web-line);
  background: var(--web-paper);
  box-shadow: var(--web-shadow);
  padding: 32px;
}
.comment-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.comment-head h2 { margin: 0; font-size: 28px; }
.comment-head span { color: var(--web-muted); font-size: 14px; }

.guest-banner {
  padding: 24px;
  text-align: center;
  background: rgba(47, 93, 80, 0.04);
  border: 1px dashed var(--web-line);
  border-radius: var(--web-radius);
  margin-bottom: 16px;
}
.guest-banner p { margin: 0 0 12px; color: var(--web-muted); font-size: 16px; }
.login-cta {
  display: inline-block;
  padding: 12px 24px;
  background: var(--web-accent-2);
  color: #fff;
  font-weight: 700;
  border-radius: var(--web-radius);
  transition: all .18s ease;
}
.login-cta:hover { background: var(--web-accent); transform: translateY(-1px); }

.composer { display: flex; flex-direction: column; gap: 12px; margin-bottom: 18px; }
.composer textarea {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid var(--web-line);
  background: rgba(255,255,255,0.9);
  color: var(--web-ink);
  font-size: 15px;
  border-radius: var(--web-radius);
  resize: vertical;
  min-height: 80px;
}
.composer textarea:focus {
  outline: 2px solid rgba(159, 61, 34, 0.18);
  border-color: rgba(159, 61, 34, 0.32);
}
.composer button { align-self: flex-end; min-width: 100px; }

input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--web-line);
  background: rgba(255,255,255,0.88);
  color: var(--web-ink);
  font-size: 15px;
  border-radius: var(--web-radius);
}
input:focus {
  outline: 2px solid rgba(159, 61, 34, 0.18);
  border-color: rgba(159, 61, 34, 0.32);
}

button {
  cursor: pointer;
  padding: 12px 18px;
  border: 1px solid var(--web-line);
  font-size: 14px;
  border-radius: var(--web-radius);
  transition: all .18s ease;
}
button:hover { transform: translateY(-1px); }

.primary-btn {
  background: var(--web-accent);
  color: #fff9f3;
  border-color: var(--web-accent);
  font-weight: 700;
  box-shadow: 0 8px 24px rgba(159, 61, 34, 0.18);
}
.primary-btn:hover { background: var(--web-accent-3); }

.ghost-btn { background: rgba(255,255,255,0.7); color: var(--web-ink); }
.ghost-btn:hover { background: rgba(255,255,255,0.95); }

.msg { margin: 12px 0; font-size: 14px; color: var(--web-accent-2); }
.msg.error { color: #c0392b; }

.empty-comments { padding: 32px 0; text-align: center; color: var(--web-muted); }

.comment-list { list-style: none; padding: 0; margin: 0; }
.comment-item {
  border-top: 1px solid var(--web-line);
  padding: 20px 0;
}
.comment-body { display: flex; gap: 14px; align-items: flex-start; }
.comment-left { flex-shrink: 0; }
.comment-avatar {
  width: 36px; height: 36px;
  border-radius: 50%;
  background: var(--web-accent-2);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.comment-main { flex: 1; min-width: 0; }
.comment-meta { display: flex; gap: 10px; align-items: baseline; margin-bottom: 6px; }
.comment-author { font-weight: 600; font-size: 14px; color: var(--web-ink); }
.comment-time { font-size: 12px; color: var(--web-muted); }
.comment-text { margin: 0 0 10px; line-height: 1.7; font-size: 15px; color: var(--web-ink); }

.reply-row {
  margin-top: 4px;
  display: flex;
  gap: 10px;
}
.reply-row input { flex: 1; }

.state-box { text-align: center; padding: 120px 24px; }
.state-text { color: var(--web-muted); font-size: 18px; }

@media (max-width: 720px) {
  .comment-head { flex-direction: column; align-items: flex-start; gap: 6px; }
  .reply-row { margin-left: 0; }
  .article-body { padding: 24px 20px; }
  .comment-card { padding: 24px 20px; }
}
</style>
