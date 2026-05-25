import http from './http'

export const sendCode = (phone, bizType) => http.post('/api/auth/sms-code', { phone, bizType })
export const login = (phone, code) => http.post('/api/auth/login', { phone, code })
export const me = () => http.get('/api/auth/me')

export const listArticles = (pageNum = 1, pageSize = 10, status) => http.get('/api/content/article/list', { params: { pageNum, pageSize, status } })
export const getArticle = (id) => http.get(`/api/content/article/${id}`)
export const saveArticle = (payload) => http.post('/api/content/article', payload)
export const deleteArticle = (id) => http.delete(`/api/content/article/${id}`)

export const deleteFile = (url) => http.delete('/api/file/delete', { params: { url } })

export const listCategories = () => http.get('/api/content/category/list')
export const saveCategory = (payload) => http.post('/api/content/category', payload)
export const deleteCategory = (id) => http.delete(`/api/content/category/${id}`)

export const listTags = () => http.get('/api/content/tag/list')
export const saveTag = (payload) => http.post('/api/content/tag', payload)
export const deleteTag = (id) => http.delete(`/api/content/tag/${id}`)

export const listAdminComments = (pageNum = 1, pageSize = 10, status) => http.get('/api/comment/admin/list', { params: { pageNum, pageSize, status } })
export const auditComment = (id, status) => http.post(`/api/comment/admin/${id}/audit`, null, { params: { status } })
export const deleteComment = (id) => http.delete(`/api/comment/admin/${id}`)

export const listUsers = (pageNum = 1, pageSize = 10, status) => http.get('/api/admin/users', { params: { pageNum, pageSize, status } })
export const updateUserStatus = (id, status) => http.put(`/api/admin/users/${id}/status`, null, { params: { status } })
export const deleteUser = (id) => http.delete(`/api/admin/users/${id}`)
