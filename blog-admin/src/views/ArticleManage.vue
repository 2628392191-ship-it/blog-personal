<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h2 class="page-title">文章管理</h2>
        <p class="page-sub">撰写、编辑和管理所有文章内容</p>
      </div>
      <div class="header-actions">
        <el-select v-model="statusFilter" placeholder="全部状态" @change="load" style="width:130px" size="large">
          <el-option label="全部" value="" />
          <el-option label="已发布" :value="1" />
          <el-option label="草稿" :value="0" />
        </el-select>
        <el-button type="primary" size="large" @click="$router.push('/article/new')">写文章</el-button>
      </div>
    </header>

    <el-table :data="articles" style="width:100%" size="large">
      <el-table-column prop="id" label="ID" width="64" />
      <el-table-column label="标题" min-width="220">
        <template #default="{ row }">
          <div class="cell-title">
            <img v-if="row.coverUrl" :src="row.coverUrl" class="cell-cover" />
            <span class="cell-text">{{ row.title }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <span class="cell-status" :class="row.status === 1 ? 'pub' : 'draft'">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column label="标签" width="150">
        <template #default="{ row }">
          <div class="cell-tags" v-if="row.tagNames?.length">
            <span class="cell-tag" v-for="t in row.tagNames" :key="t">{{ t }}</span>
          </div>
          <span v-else class="cell-none">—</span>
        </template>
      </el-table-column>
      <el-table-column label="阅读" width="70" prop="viewCount" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button size="small" text @click="$router.push(`/article/${row.id}/edit`)">编辑</el-button>
          <el-button size="small" text type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-if="pages > 1" v-model:current-page="pageNum" :page-size="10"
      :total="pages * 10" layout="prev, pager, next" @current-change="load" />
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteArticle, listArticles } from '../api'

const articles = ref([])
const pageNum = ref(1)
const pages = ref(1)
const statusFilter = ref('')

const remove = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此文章？', '确认', { type: 'warning' })
    await deleteArticle(id)
    await load()
    ElMessage.success('文章已删除')
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error(e?.message || '删除失败')
  }
}

const load = async () => {
  const page = await listArticles(pageNum.value, 10, statusFilter.value === '' ? undefined : Number(statusFilter.value))
  articles.value = page.records
  pages.value = page.pages || 1
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.page { max-width: 1100px; overflow-x: auto; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  gap: 16px;
  flex-wrap: wrap;
}
.page-title { margin: 0 0 4px; font-size: 24px; font-weight: 700; color: var(--admin-text); }
.page-sub { margin: 0; font-size: 14px; color: var(--admin-muted); }
.header-actions { display: flex; gap: 10px; align-items: center; }

.cell-title { display: flex; align-items: center; gap: 10px; }
.cell-cover { width: 40px; height: 28px; object-fit: cover; border-radius: 4px; border: 1px solid var(--admin-line); }
.cell-text { font-weight: 600; }
.cell-status {
  display: inline-block; padding: 2px 10px; font-size: 12px; border-radius: 999px; font-weight: 600; letter-spacing: .04em;
}
.cell-status.pub { background: var(--admin-soft-success); color: var(--admin-success); }
.cell-status.draft { background: var(--admin-soft-warn); color: #c9882c; }
.cell-tags { display: flex; flex-wrap: wrap; gap: 4px; }
.cell-tag {
  padding: 1px 8px; font-size: 11px; background: var(--admin-soft-accent); color: var(--admin-accent);
  border-radius: 999px; font-weight: 500;
}
.cell-none { color: var(--admin-muted); font-size: 13px; }
</style>
