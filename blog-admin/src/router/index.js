import { createRouter, createWebHashHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import Dashboard from '../views/Dashboard.vue'
import ArticleManage from '../views/ArticleManage.vue'
import ArticleEdit from '../views/ArticleEdit.vue'
import CommentManage from '../views/CommentManage.vue'
import CategoryManage from '../views/CategoryManage.vue'
import TagManage from '../views/TagManage.vue'
import UserManage from '../views/UserManage.vue'
import LogManage from '../views/LogManage.vue'

const routes = [
  { path: '/login', component: LoginView, meta: { guest: true } },
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', component: Dashboard, meta: { requiresAuth: true } },
  { path: '/articles', component: ArticleManage, meta: { requiresAuth: true } },
  { path: '/article/new', component: ArticleEdit, meta: { requiresAuth: true } },
  { path: '/article/:id/edit', component: ArticleEdit, meta: { requiresAuth: true } },
  { path: '/comments', component: CommentManage, meta: { requiresAuth: true } },
  { path: '/categories', component: CategoryManage, meta: { requiresAuth: true } },
  { path: '/tags', component: TagManage, meta: { requiresAuth: true } },
  { path: '/users', component: UserManage, meta: { requiresAuth: true } },
  { path: '/logs', component: LogManage, meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('admin_token')
  if (to.meta.requiresAuth && !token) return '/login'
  if (to.path === '/login' && token) return '/'
})

export default router
