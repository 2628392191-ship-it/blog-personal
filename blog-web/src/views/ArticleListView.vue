<template>
  <section class="page">
    <header class="hero">
      <p class="eyebrow">ESSAYS &amp; NOTES</p>
      <p class="hero-sub">
        这里分享个人在技术学习、系统涉及、开发历程的文章。
      </p>
      <div class="hero-line"></div>
    </header>

    <div v-if="loading" class="state-box">
      <p class="state-text">正在加载文章...</p>
    </div>

    <div v-else class="layout-with-sidebar">
      <aside class="sidebar" v-if="categories.length > 0">
        <nav class="category-nav">
          <h3 class="sidebar-title">分类</h3>
          <ul class="category-list">
            <li
              class="category-item"
              :class="{ active: !activeCategoryId }"
              @click="switchCategory(null)"
            >
              全部文章
              <span class="item-count">{{ totalCount }}</span>
            </li>
            <li
              v-for="category in categories"
              :key="category.id"
              class="category-item"
              :class="{ active: activeCategoryId === category.id }"
              @click="switchCategory(category.id)"
            >
              {{ category.name }}
            </li>
          </ul>
        </nav>
      </aside>

      <div class="main-area">
        <section v-if="!activeCategoryId && !activeTagId && hotList.length > 0" class="hot-section">
          <div class="section-head">
            <div>
              <p class="section-eyebrow">HOT READS</p>
              <h2>热门阅读</h2>
            </div>
            <span class="section-hint">按阅读量排序</span>
          </div>
          <div class="hot-grid">
            <article v-for="item in hotList" :key="`hot-${item.id}`" class="hot-card">
              <p class="hot-meta">
                <span>
                  <span v-if="item.isTop" class="top-badge">置顶</span>
                  {{ categoryName(item.categoryId) }}
                </span>
                <span>阅读 {{ item.viewCount || 0 }}</span>
              </p>
              <h3>
                <router-link :to="`/article/${item.id}`">{{ item.title }}</router-link>
              </h3>
              <img v-if="item.coverUrl" :src="item.coverUrl" class="card-cover" alt="cover" />
              <p class="hot-summary">{{ item.summary || '这篇文章还没有摘要，点击进入查看完整内容。' }}</p>
              <router-link class="read-more" :to="`/article/${item.id}`">
                继续阅读 <span class="arrow">&rarr;</span>
              </router-link>
            </article>
          </div>
        </section>

        <section v-if="activeCategoryId || activeTagId" class="feed-section">
          <div class="section-head compact">
            <div>
              <p class="section-eyebrow">CURRENT FEED</p>
              <h2>{{ activeCategoryId ? categoryName(activeCategoryId) : activeTagId ? '#' + tagMap[activeTagId] : '全部文章' }}</h2>
            </div>
            <span class="section-hint">{{ list.length }} 篇可读内容</span>
          </div>

          <template v-if="list.length > 0">
            <article v-for="(item, idx) in list" :key="item.id" class="card">
              <div class="card-index">
                <span class="index-num">{{ String(idx + 1 + (pageNum - 1) * pageSize).padStart(2, '0') }}</span>
              </div>
              <div class="card-body">
                <div class="meta-row">
                  <span class="meta-date">{{ item.publishTime || '待发布' }}</span>
                  <span v-if="item.isTop" class="top-badge">置顶</span>
                  <span class="meta-cat">{{ categoryName(item.categoryId) }}</span>
                  <span class="meta-views">阅读 {{ item.viewCount || 0 }}</span>
                </div>
                <h2>
                  <router-link :to="`/article/${item.id}`">{{ item.title }}</router-link>
                </h2>
                <img v-if="item.coverUrl" :src="item.coverUrl" class="card-cover" alt="cover" />
                <p class="card-summary">{{ item.summary || '这篇文章还没有摘要，点击进入查看完整内容。' }}</p>
                <router-link class="read-more" :to="`/article/${item.id}`">
                  继续阅读
                  <span class="arrow">&rarr;</span>
                </router-link>
              </div>
            </article>

            <div class="pager">
              <button :disabled="pageNum <= 1" @click="pageNum--; loadArticles()">
                <span class="pager-arrow">&larr;</span> 上一页
              </button>
              <span class="pager-info">第 {{ pageNum }} 页 / 共 {{ pages || 1 }} 页</span>
              <button :disabled="pageNum >= pages" @click="pageNum++; loadArticles()">
                下一页 <span class="pager-arrow">&rarr;</span>
              </button>
            </div>
          </template>

          <div v-else class="state-box narrow">
            <p class="state-title">当前分类暂无文章</p>
            <p class="state-text">可以先看看热门阅读，或者切换到左侧其他分类。</p>
          </div>
        </section>
      </div>

      <aside class="sidebar sidebar-right" v-if="tags.length > 0">
        <nav class="category-nav">
          <h3 class="sidebar-title">热门标签</h3>
          <ul class="category-list">
            <li
              v-for="tag in tags"
              :key="tag.id"
              class="category-item"
              :class="{ active: activeTagId === tag.id }"
              @click="switchTag(tag.id)"
            >
              {{ tag.name }}
            </li>
          </ul>
        </nav>
      </aside>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleList, categoryList, hotArticleList, tagList } from '../api'

const route = useRoute()
const router = useRouter()

const list = ref([])
const hotList = ref([])
const categories = ref([])
const tags = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const pages = ref(1)
const loading = ref(false)
const total = ref(0)
const activeCategoryId = ref(null)
const activeTagId = ref(null)

const categoryMap = computed(() => Object.fromEntries(categories.value.map(item => [item.id, item.name])))
const tagMap = computed(() => Object.fromEntries(tags.value.map(item => [item.id, item.name])))

const totalCount = computed(() => total.value)

const categoryName = (categoryId) => {
  if (!categoryId) return '未分类'
  return categoryMap.value[categoryId] || `分类 #${categoryId}`
}

const syncFiltersFromRoute = () => {
  activeCategoryId.value = route.query.category ? Number(route.query.category) : null
  activeTagId.value = route.query.tag ? Number(route.query.tag) : null
}

const loadArticles = async () => {
  loading.value = true
  try {
    const page = await articleList(pageNum.value, pageSize.value, activeCategoryId.value || undefined, activeTagId.value || undefined)
    list.value = page.records
    pages.value = page.pages || 1
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

const loadMeta = async () => {
  const [categoryRes, tagRes, hotRes] = await Promise.all([
    categoryList(),
    tagList(),
    hotArticleList(6)
  ])
  categories.value = categoryRes
  tags.value = tagRes
  hotList.value = hotRes
}

const buildQuery = () => {
  const q = {}
  if (activeCategoryId.value) q.category = String(activeCategoryId.value)
  if (activeTagId.value) q.tag = String(activeTagId.value)
  return q
}

const switchCategory = async (categoryId) => {
  pageNum.value = 1
  activeCategoryId.value = categoryId
  await router.replace({ path: '/', query: buildQuery() })
}

const switchTag = async (tagId) => {
  pageNum.value = 1
  activeTagId.value = activeTagId.value === tagId ? null : tagId
  await router.replace({ path: '/', query: buildQuery() })
}

watch(() => route.query, async () => {
  syncFiltersFromRoute()
  await loadArticles()
}, { deep: true })

onMounted(async () => {
  syncFiltersFromRoute()
  await loadMeta()
  await loadArticles()
})
</script>

<style scoped>
.page { max-width: 1280px; margin: 0 auto; padding: 56px 20px 80px; }

.hero { margin-bottom: 48px; max-width: 960px; }
.eyebrow {
  margin: 0 0 18px;
  font-size: 13px;
  letter-spacing: .20em;
  color: var(--web-accent-3);
  font-weight: 600;
}
.hero h1 {
  margin: 0;
  font-family: var(--web-font-display);
  font-size: clamp(48px, 7vw, 96px);
  line-height: 1.1;
  letter-spacing: .04em;
  background: linear-gradient(135deg, var(--web-accent) 0%, var(--web-accent-3) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.hero-sub {
  margin: 20px 0 0;
  color: var(--web-muted);
  font-size: 18px;
  line-height: 1.8;
  max-width: 56ch;
}
.hero-line {
  margin-top: 32px;
  width: 60px;
  height: 4px;
  border-radius: 2px;
  background: linear-gradient(90deg, var(--web-accent-3), var(--web-accent));
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 20px;
  margin-bottom: 22px;
}
.section-head.compact { margin-bottom: 16px; }
.section-eyebrow {
  margin: 0 0 6px;
  font-size: 11px;
  letter-spacing: .22em;
  color: var(--web-accent-3);
}
.section-head h2 {
  margin: 0;
  font-family: var(--web-font-display);
  font-size: clamp(28px, 4vw, 42px);
  line-height: 1.1;
  color: var(--web-accent);
}
.section-hint { color: var(--web-muted); font-size: 14px; }

.layout-with-sidebar {
  display: grid;
  grid-template-columns: minmax(150px, 200px) 1fr minmax(150px, 200px);
  gap: clamp(18px, 3vw, 40px);
  align-items: start;
}

.sidebar {
  position: sticky;
  top: 80px;
  min-width: 0;
  max-height: calc(100vh - 100px);
  overflow-y: auto;
}

.sidebar-title {
  margin: 0 0 12px;
  font-family: var(--web-font-display);
  font-size: 15px;
  font-weight: 700;
  color: var(--web-accent-3);
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.category-item {
  padding: 8px 14px;
  border-radius: 999px;
  cursor: pointer;
  font-size: 14px;
  color: var(--web-muted);
  transition: all .22s ease;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.category-item:hover {
  background: rgba(74, 144, 217, 0.08);
  color: var(--web-accent);
  transform: translateX(3px);
}

.category-item.active {
  background: linear-gradient(135deg, rgba(74, 144, 217, 0.12), rgba(240, 140, 160, 0.10));
  color: var(--web-accent);
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(74, 144, 217, 0.10);
}

.item-count {
  font-size: 12px;
  color: var(--web-muted);
  background: rgba(74, 144, 217, 0.08);
  padding: 2px 8px;
  border-radius: 999px;
  font-weight: 400;
}

.main-area {
  min-width: 0;
}

.hot-section,
.feed-section { margin-bottom: 44px; }

.hot-grid {
  display: flex;
  flex-direction: column;
  gap: 18px;
  max-width: 640px;
}
.hot-card {
  padding: 24px 24px;
  background: var(--web-paper);
  border: 1px solid var(--web-line);
  border-left: 4px solid var(--web-accent-3);
  border-radius: var(--web-radius);
  box-shadow: var(--web-shadow);
  display: flex;
  flex-direction: column;
  transition: all .25s ease;
}
.hot-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--web-shadow-lg);
  border-color: var(--web-accent-3);
}
.hot-meta {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin: 0 0 10px;
  font-size: 12px;
  color: var(--web-muted);
}
.hot-card h3 {
  margin: 0 0 10px;
  font-size: 22px;
  line-height: 1.3;
}
.hot-card h3 a:hover { color: var(--web-accent-3); }
.hot-summary {
  margin: 0 0 auto;
  color: var(--web-muted);
  font-size: 14px;
  line-height: 1.75;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.hot-card .read-more {
  margin-top: 14px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: var(--web-accent-2);
}
.hot-card .read-more:hover { color: var(--web-accent-3); }
.hot-card .read-more .arrow {
  display: inline-block;
  transition: transform .25s ease;
}
.hot-card:hover .read-more .arrow { transform: translateX(4px); }

.top-badge {
  display: inline-block;
  padding: 1px 8px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .06em;
  background: var(--web-accent);
  color: #fff;
  border-radius: 3px;
  margin-right: 6px;
}
.card-cover {
  width: 100%;
  max-height: 260px;
  object-fit: cover;
  margin: 10px 0;
  border-radius: var(--web-radius);
  border: 1px solid var(--web-line);
}

.section-head-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.category-small {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.category-chip {
  padding: 5px 14px;
  font-size: 12px;
  border: 1px solid rgba(47, 93, 80, 0.18);
  background: rgba(255,255,255,0.6);
  color: var(--web-muted);
  border-radius: 999px;
  cursor: pointer;
  transition: all .18s ease;
}
.category-chip:hover,
.category-chip.active {
  background: var(--web-accent-2);
  border-color: var(--web-accent-2);
  color: #fffaf5;
}

.state-box { padding: 64px 0; text-align: center; }
.state-box.narrow { padding: 36px 0 12px; }
.state-title { margin: 0; font-size: 28px; color: var(--web-ink); }
.state-text { margin: 12px 0 0; color: var(--web-muted); font-size: 16px; }

.card {
  display: grid;
  grid-template-columns: 60px 1fr;
  gap: 20px;
  padding: 28px 20px;
  border: 1px solid transparent;
  border-radius: var(--web-radius);
  background: var(--web-paper);
  box-shadow: var(--web-shadow);
  margin-bottom: 12px;
  transition: all .25s ease;
}
.card:hover {
  transform: translateX(4px);
  border-color: var(--web-accent-3);
  box-shadow: var(--web-shadow-lg);
}

.card-index { padding-top: 2px; }
.index-num {
  font-size: 18px;
  color: var(--web-accent);
  font-family: var(--web-font-display);
}

.meta-row {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.meta-date, .meta-views { font-size: 13px; color: var(--web-muted); }
.meta-cat {
  font-size: 12px;
  color: var(--web-accent);
  font-weight: 600;
  padding: 3px 12px;
  background: rgba(74, 144, 217, 0.08);
  border-radius: 999px;
}

.card h2 {
  margin: 0 0 10px;
  font-size: clamp(26px, 3.5vw, 40px);
  line-height: 1.2;
}
.card h2 a { transition: color .2s ease; }
.card h2 a:hover { color: var(--web-accent-3); }

.card-summary {
  margin: 0;
  color: var(--web-muted);
  font-size: 15px;
  line-height: 1.7;
  max-width: 58ch;
}

.read-more {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
  font-size: 14px;
  font-weight: 700;
  color: var(--web-accent);
  transition: color .2s ease;
}
.read-more .arrow {
  display: inline-block;
  transition: transform .25s ease;
}
.read-more:hover { color: var(--web-accent-3); }
.card:hover .read-more .arrow { transform: translateX(4px); }

.pager {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 48px;
}
.pager button {
  padding: 10px 22px;
  border: 1px solid var(--web-line);
  background: rgba(255,255,255,0.80);
  color: var(--web-ink);
  font-size: 14px;
  cursor: pointer;
  border-radius: 999px;
  transition: all .22s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}
.pager button:hover:not(:disabled) {
  background: var(--web-accent-3);
  color: #fff;
  border-color: var(--web-accent-3);
  transform: scale(1.04);
}
.pager button:disabled { opacity: .35; cursor: not-allowed; }
.pager-info { font-size: 14px; color: var(--web-muted); }

@media (min-width: 1100px) {
  .hot-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    max-width: none;
  }
}

@media (max-width: 860px) {
  .hot-grid { max-width: none; }
  .layout-with-sidebar {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  .sidebar {
    position: static;
    max-height: none;
  }
  .sidebar-right { order: 3; }
  .category-list, .sidebar-right .category-list {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 6px;
  }
  .category-item {
    padding: 6px 12px;
    font-size: 13px;
  }
  .item-count { display: none; }
  .sidebar-title { margin-bottom: 10px; font-size: 14px; }
}

@media (max-width: 720px) {
  .hero h1 { font-size: 40px; }
  .card { grid-template-columns: 1fr; gap: 8px; }
  .card-index { display: none; }
  .hero-sub { font-size: 16px; }
  .section-head { flex-direction: column; align-items: flex-start; }
}
</style>
