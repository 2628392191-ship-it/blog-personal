import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.PROD ? 'http://47.88.48.16:8080' : 'http://localhost:8080',
  timeout: 8000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token) config.headers.Authorization = token
  return config
})

http.interceptors.response.use(
  (res) => {
    const data = res.data
    if (data.code !== 0) throw new Error(data.message || '请求失败')
    return data.data
  },
  (err) => {
    if (err.response?.status === 401 || err.response?.status === 403) {
      localStorage.removeItem('admin_token')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default http
