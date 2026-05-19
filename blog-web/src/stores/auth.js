import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, register as apiRegister, me } from '../api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('blog_token') || '')
  const user = ref(null)
  const loading = ref(false)

  const isLoggedIn = computed(() => !!token.value)

  async function fetchMe() {
    if (!token.value) return
    try {
      user.value = await me()
    } catch {
      token.value = ''
      localStorage.removeItem('blog_token')
    }
  }

  async function login(phone, code) {
    loading.value = true
    try {
      const res = await apiLogin(phone, code)
      token.value = res.token
      localStorage.setItem('blog_token', res.token)
      await fetchMe()
      return res
    } finally {
      loading.value = false
    }
  }

  async function register(phone, code) {
    loading.value = true
    try {
      const res = await apiRegister(phone, code)
      token.value = res.token
      localStorage.setItem('blog_token', res.token)
      await fetchMe()
      return res
    } finally {
      loading.value = false
    }
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('blog_token')
  }

  return { token, user, loading, isLoggedIn, fetchMe, login, register, logout }
})
