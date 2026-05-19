import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'

const routes = [
  { path: '/login', component: LoginView, meta: { guest: true } },
  { path: '/', component: DashboardView, meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('admin_token')
  if (to.meta.requiresAuth && !token) {
    return '/login'
  }
  if (to.path === '/login' && token) {
    return '/'
  }
})

export default router
