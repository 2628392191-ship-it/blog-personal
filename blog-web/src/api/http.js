import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.PROD ? '' : 'http://localhost:8080',
  timeout: 8000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('blog_token')
  if (token) config.headers.Authorization = token
  return config
})

http.interceptors.response.use((res) => {
  const data = res.data
  if (data.code !== 0) {
    throw new Error(data.message || '请求失败')
  }
  return data.data
})

export default http
