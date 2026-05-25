import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import ArticleListView from '../views/ArticleListView.vue'
import ArticleDetailView from '../views/ArticleDetailView.vue'
import ProfileView from '../views/ProfileView.vue'
import AiChatView from '../views/AiChatView.vue'

const routes = [
  { path: '/login', component: LoginView, meta: { guest: true } },
  { path: '/', component: ArticleListView, meta: { guest: true } },
  { path: '/article/:id', component: ArticleDetailView, meta: { guest: true } },
  { path: '/profile', component: ProfileView },
  { path: '/ai', component: AiChatView }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

export default router
