import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, loginByPassword as apiLoginByPassword, me as apiMe } from '../api'

export const useAdminAuthStore = defineStore('adminAuth', () => {
  const K = 'admin_token'
  const token = ref(localStorage.getItem(K) || '')
  const user = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  async function loginByPassword(phone, password) {
    const res = await apiLoginByPassword(phone, password)
    token.value = res.token
    localStorage.setItem(K, res.token)
    await fetchMe()
    return res
  }

  async function login(phone, code) {
    const res = await apiLogin(phone, code)
    token.value = res.token
    localStorage.setItem(K, res.token)
    await fetchMe()
    return res
  }

  async function fetchMe() {
    try { user.value = await apiMe() } catch { user.value = null }
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem(K)
  }

  return { token, user, isLoggedIn, login, loginByPassword, logout, fetchMe }
})
