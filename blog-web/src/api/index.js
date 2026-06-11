import http from './http'

export const sendCode = (phone, bizType) => http.post('/api/auth/sms-code', { phone, bizType })
export const register = (phone, code, password, confirmPassword) => http.post('/api/auth/register', { phone, code, password, confirmPassword })
export const login = (phone, code) => http.post('/api/auth/login', { phone, code })
export const loginByPassword = (phone, password) => http.post('/api/auth/login/password', { phone, password })
export const resetPassword = (phone, code, newPassword) => http.post('/api/auth/password/reset', { phone, code, newPassword })
export const me = () => http.get('/api/auth/me')

export const articleList = (pageNum = 1, pageSize = 10, categoryId, tagId) => http.get('/api/content/article/list', {
  params: { status: 1, pageNum, pageSize, categoryId, tagId }
})
export const hotArticleList = (limit = 6) => http.get('/api/content/article/hot', { params: { limit } })
export const categoryList = () => http.get('/api/content/category/list')
export const tagList = () => http.get('/api/content/tag/list')
export const articleDetail = (id) => http.get(`/api/content/article/${id}`)

export const updateProfile = (payload) => http.put('/api/auth/profile', payload)
export const changePassword = (payload) => http.put('/api/auth/password', payload)
export const uploadFile = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return http.post('/api/file/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}

export const toggleLike = (id) => http.post(`/api/content/article/${id}/like`)

export const commentList = (articleId) => http.get(`/api/comment/article/${articleId}`)
export const addComment = (payload) => http.post('/api/comment', payload)
