<template>
  <section class="page">
    <el-tabs v-model="activeTab" class="main-tabs">
      <el-tab-pane label="文章管理" name="articles">
        <div class="module">
          <div class="module-toolbar">
            <el-button type="primary" @click="showArticleForm = !showArticleForm">
              {{ showArticleForm ? '收起表单' : '新建文章' }}
            </el-button>
            <el-select
              v-model="articleStatusFilter"
              placeholder="筛选状态"
              @change="loadArticles"
              style="width: 140px"
            >
              <el-option label="全部状态" :value="''" />
              <el-option label="草稿" :value="0" />
              <el-option label="已发布" :value="1" />
            </el-select>
          </div>

          <div class="article-form" v-if="showArticleForm">
            <div class="form-row-3">
              <el-input v-model="articleForm.title" placeholder="文章标题" />
              <el-select
                v-model="articleForm.categoryId"
                placeholder="选择分类"
                clearable
              >
                <el-option
                  v-for="cat in categories"
                  :key="cat.id"
                  :label="cat.name"
                  :value="cat.id"
                />
              </el-select>
            </div>
            <el-select
              v-model="articleForm.tagIds"
              placeholder="选择标签"
              multiple
              clearable
            >
              <el-option
                v-for="tag in tags"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
              />
            </el-select>
            <el-input v-model="articleForm.summary" placeholder="摘要" />
            <el-input
              v-model="articleForm.contentMd"
              placeholder="Markdown 内容"
              type="textarea"
              :rows="6"
            />
            <div class="form-row-3">
              <el-select
                v-model="articleForm.status"
                placeholder="发布状态"
                style="width: 120px"
              >
                <el-option label="草稿" :value="0" />
                <el-option label="发布" :value="1" />
              </el-select>
              <el-button type="primary" @click="submitArticle">
                {{ editingId ? '更新文章' : '发布文章' }}
              </el-button>
              <el-button v-if="editingId" @click="cancelEdit">取消编辑</el-button>
            </div>
          </div>

          <el-table :data="articles" stripe style="width: 100%" size="medium">
            <el-table-column prop="id" label="ID" width="64" />
            <el-table-column prop="title" label="标题" min-width="200">
              <template #default="{ row }">
                <span class="article-title">{{ row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <span class="status-tag" :class="row.status === 1 ? 'published' : 'draft'">
                  {{ row.status === 1 ? '已发布' : '草稿' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" width="140" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="editArticle(row)">编辑</el-button>
                <el-button type="danger" size="small" @click="removeArticle(row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="articlePages > 1"
            v-model:current-page="articlePageNum"
            :page-size="articlePageSize"
            :total="articlePages * articlePageSize"
            layout="prev, pager, next"
            @current-change="loadArticles"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="评论审核" name="comments">
        <div class="module">
          <div class="module-toolbar">
            <el-select
              v-model="commentStatusFilter"
              placeholder="筛选状态"
              @change="loadComments"
              style="width: 140px"
            >
              <el-option label="全部状态" :value="''" />
              <el-option label="已隐藏" :value="0" />
              <el-option label="已显示" :value="1" />
            </el-select>
          </div>

          <el-table :data="comments" stripe style="width: 100%" size="medium">
            <el-table-column prop="id" label="ID" width="64" />
            <el-table-column prop="content" label="评论内容" min-width="280">
              <template #default="{ row }">
                <div class="comment-content">
                  <span class="comment-article-ref">[A:{{ row.articleId }}]</span>
                  {{ row.content }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <span class="status-tag" :class="row.status === 1 ? 'published' : 'draft'">
                  {{ row.status === 1 ? '显示' : '隐藏' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="audit(row.id, 1)">通过</el-button>
                <el-button size="small" @click="audit(row.id, 0)">隐藏</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="commentPages > 1"
            v-model:current-page="commentPageNum"
            :page-size="commentPageSize"
            :total="commentPages * commentPageSize"
            layout="prev, pager, next"
            @current-change="loadComments"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="分类管理" name="categories">
        <div class="module">
          <div class="inline-form">
            <el-input v-model="categoryForm.name" placeholder="分类名称" style="flex:1" />
            <el-input v-model="categoryForm.slug" placeholder="slug" style="flex:1" />
            <el-button type="primary" @click="submitCategory">
              {{ editingCatId ? '更新分类' : '新增分类' }}
            </el-button>
            <el-button v-if="editingCatId" @click="cancelCatEdit">取消</el-button>
          </div>

          <el-table :data="categories" stripe style="width: 100%; margin-top: 16px" size="medium">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="slug" label="Slug" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="editCategory(row)">编辑</el-button>
                <el-button type="danger" size="small" @click="removeCategory(row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="用户管理" name="users">
        <div class="module">
          <div class="module-toolbar">
            <el-select
              v-model="userStatusFilter"
              placeholder="筛选状态"
              @change="loadUsers"
              style="width: 140px"
            >
              <el-option label="全部状态" :value="''" />
              <el-option label="正常" :value="1" />
              <el-option label="已禁用" :value="0" />
            </el-select>
          </div>

          <el-table :data="users" stripe style="width: 100%" size="medium">
            <el-table-column prop="id" label="ID" width="64" />
            <el-table-column prop="phone" label="手机号" width="140" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="nickname" label="昵称" min-width="140" />
            <el-table-column prop="email" label="邮箱" min-width="160" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <span class="status-tag" :class="row.status === 1 ? 'published' : 'draft'">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="注册时间" width="140" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="toggleUser(row)">
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button type="danger" size="small" @click="removeUser(row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="userPages > 1"
            v-model:current-page="userPageNum"
            :page-size="userPageSize"
            :total="userPages * userPageSize"
            layout="prev, pager, next"
            @current-change="loadUsers"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="标签管理" name="tags">
        <div class="module">
          <div class="inline-form">
            <el-input v-model="tagForm.name" placeholder="标签名称" style="flex:1" />
            <el-input v-model="tagForm.slug" placeholder="slug" style="flex:1" />
            <el-button type="primary" @click="submitTag">
              {{ editingTagId ? '更新标签' : '新增标签' }}
            </el-button>
            <el-button v-if="editingTagId" @click="cancelTagEdit">取消</el-button>
          </div>

          <el-table :data="tags" stripe style="width: 100%; margin-top: 16px" size="medium">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="slug" label="Slug" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="editTag(row)">编辑</el-button>
                <el-button type="danger" size="small" @click="removeTag(row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  auditComment, deleteArticle, deleteCategory, deleteTag, deleteUser,
  listAdminComments, listArticles, listCategories, listTags, listUsers,
  saveArticle, saveCategory, saveTag, updateUserStatus
} from '../api'

const activeTab = ref('articles')
const showArticleForm = ref(false)
const editingId = ref(null)

const articles = ref([])
const categories = ref([])
const tags = ref([])
const comments = ref([])

const articlePageNum = ref(1)
const articlePageSize = ref(10)
const articlePages = ref(1)
const articleStatusFilter = ref('')

const commentPageNum = ref(1)
const commentPageSize = ref(10)
const commentPages = ref(1)
const commentStatusFilter = ref('')

const users = ref([])
const userPageNum = ref(1)
const userPageSize = ref(10)
const userPages = ref(1)
const userStatusFilter = ref('')

const articleForm = reactive({
  id: null, title: '', summary: '', contentMd: '',
  categoryId: null, status: 1, isTop: 0,
  isCommentEnabled: 1, tagIds: []
})
const editingCatId = ref(null)
const editingTagId = ref(null)

const categoryForm = reactive({ id: null, name: '', slug: '' })
const tagForm = reactive({ id: null, name: '', slug: '' })

const loadArticles = async () => {
  const page = await listArticles(
    articlePageNum.value, articlePageSize.value,
    articleStatusFilter.value === '' ? undefined : Number(articleStatusFilter.value)
  )
  articles.value = page.records
  articlePages.value = page.pages || 1
}

const loadComments = async () => {
  const page = await listAdminComments(
    commentPageNum.value, commentPageSize.value,
    commentStatusFilter.value === '' ? undefined : Number(commentStatusFilter.value)
  )
  comments.value = page.records
  commentPages.value = page.pages || 1
}

const loadUsers = async () => {
  const page = await listUsers(
    userPageNum.value, userPageSize.value,
    userStatusFilter.value === '' ? undefined : Number(userStatusFilter.value)
  )
  users.value = page.records
  userPages.value = page.pages || 1
}

const toggleUser = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateUserStatus(row.id, newStatus)
    await loadUsers()
    ElMessage.success(newStatus === 1 ? '用户已启用' : '用户已禁用')
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const removeUser = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此用户？', '确认', { type: 'warning' })
    await deleteUser(id)
    await loadUsers()
    ElMessage.success('用户已删除')
  } catch { /* cancelled */ }
}

const loadAll = async () => {
  await loadArticles()
  categories.value = await listCategories()
  tags.value = await listTags()
  await loadComments()
  await loadUsers()
}

const resetArticleForm = () => {
  Object.assign(articleForm, {
    id: null, title: '', summary: '', contentMd: '',
    categoryId: null, status: 1, isTop: 0,
    isCommentEnabled: 1, tagIds: []
  })
}

const editArticle = (row) => {
  Object.assign(articleForm, {
    id: row.id, title: row.title, summary: row.summary,
    contentMd: row.contentMd, categoryId: row.categoryId,
    status: row.status, isTop: row.isTop,
    isCommentEnabled: row.isCommentEnabled, tagIds: row.tagIds || []
  })
  editingId.value = row.id
  showArticleForm.value = true
}

const cancelEdit = () => {
  editingId.value = null
  showArticleForm.value = false
  resetArticleForm()
}

const submitArticle = async () => {
  const wasEditing = !!editingId.value
  try {
    await saveArticle(articleForm)
    editingId.value = null
    resetArticleForm()
    showArticleForm.value = false
    await loadArticles()
    ElMessage.success(wasEditing ? '文章已更新' : '文章创建成功')
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const removeArticle = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此文章？', '确认', { type: 'warning' })
    await deleteArticle(id)
    await loadArticles()
    ElMessage.success('文章已删除')
  } catch { /* cancelled */ }
}

const resetCategoryForm = () => {
  Object.assign(categoryForm, { id: null, name: '', slug: '' })
}

const editCategory = (row) => {
  Object.assign(categoryForm, { id: row.id, name: row.name, slug: row.slug })
  editingCatId.value = row.id
}

const cancelCatEdit = () => {
  editingCatId.value = null
  resetCategoryForm()
}

const submitCategory = async () => {
  const wasEditing = !!editingCatId.value
  try {
    await saveCategory(categoryForm)
    editingCatId.value = null
    resetCategoryForm()
    categories.value = await listCategories()
    ElMessage.success(wasEditing ? '分类已更新' : '分类创建成功')
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const removeCategory = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此分类？', '确认', { type: 'warning' })
    await deleteCategory(id)
    categories.value = await listCategories()
    ElMessage.success('分类已删除')
  } catch { /* cancelled */ }
}

const resetTagForm = () => {
  Object.assign(tagForm, { id: null, name: '', slug: '' })
}

const editTag = (row) => {
  Object.assign(tagForm, { id: row.id, name: row.name, slug: row.slug })
  editingTagId.value = row.id
}

const cancelTagEdit = () => {
  editingTagId.value = null
  resetTagForm()
}

const submitTag = async () => {
  const wasEditing = !!editingTagId.value
  try {
    await saveTag(tagForm)
    editingTagId.value = null
    resetTagForm()
    tags.value = await listTags()
    ElMessage.success(wasEditing ? '标签已更新' : '标签创建成功')
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const removeTag = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此标签？', '确认', { type: 'warning' })
    await deleteTag(id)
    tags.value = await listTags()
    ElMessage.success('标签已删除')
  } catch { /* cancelled */ }
}

const audit = async (id, status) => {
  try {
    await auditComment(id, status)
    await loadComments()
    ElMessage.success(status === 1 ? '评论已通过' : '评论已隐藏')
  } catch (e) {
    ElMessage.error(e.message)
  }
}

onMounted(loadAll)
</script>

<style scoped>
.page {
  max-width: 1240px;
  margin: 0 auto;
  padding: 28px 24px 64px;
}

.main-tabs {
  background: var(--admin-panel);
  border: 1px solid var(--admin-line-strong);
  box-shadow: var(--admin-shadow);
  padding: 0 24px;
  border-radius: var(--admin-radius);
}

.module { padding: 8px 0 24px; }

.module-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.article-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
  margin-bottom: 16px;
  background: var(--admin-soft-accent);
  border: 1px solid var(--admin-line);
  border-radius: var(--admin-radius);
}
.form-row-3 {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
}

.inline-form {
  display: flex;
  gap: 12px;
  align-items: center;
}

.article-title { font-weight: 600; color: var(--admin-text); }
.comment-content { color: var(--admin-text); line-height: 1.5; }
.comment-article-ref {
  color: var(--admin-accent);
  font-size: 12px;
  margin-right: 6px;
}

.status-tag {
  display: inline-block;
  padding: 2px 10px;
  font-size: 12px;
  border-radius: 999px;
  font-weight: 600;
  letter-spacing: .04em;
}
.status-tag.published { background: var(--admin-soft-success); color: var(--admin-success); }
.status-tag.draft { background: var(--admin-soft-warn); color: #c9882c; }

@media (max-width: 860px) {
  .inline-form { flex-direction: column; }
  .form-row-3 { grid-template-columns: 1fr; }
  .module-toolbar { flex-direction: column; align-items: stretch; }
  .main-tabs { padding: 0 12px; }
}
</style>
