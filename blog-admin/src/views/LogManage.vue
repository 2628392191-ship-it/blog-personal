<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h2 class="page-title">操作日志</h2>
        <p class="page-sub">管理员操作审计记录</p>
      </div>
      <el-select v-model="moduleFilter" placeholder="全部模块" @change="load" style="width:140px" clearable>
        <el-option label="文章管理" value="文章管理" />
        <el-option label="分类管理" value="分类管理" />
        <el-option label="标签管理" value="标签管理" />
        <el-option label="用户管理" value="用户管理" />
      </el-select>
    </header>

    <el-table :data="logs" size="large">
      <el-table-column prop="id" label="ID" width="64" />
      <el-table-column prop="module" label="模块" width="100" />
      <el-table-column prop="action" label="操作" width="140" />
      <el-table-column prop="content" label="描述" min-width="160" />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="createdAt" label="时间" width="160" />
    </el-table>

    <el-pagination v-if="pages > 1" v-model:current-page="pageNum" :page-size="10"
      :total="pages * 10" layout="prev, pager, next" @current-change="load" />
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'

const logs = ref([])
const pageNum = ref(1)
const pages = ref(1)
const moduleFilter = ref('')

const load = async () => {
  const params = { pageNum: pageNum.value, pageSize: 10 }
  if (moduleFilter.value) params.module = moduleFilter.value
  const res = await http.get('/api/admin/logs', { params })
  logs.value = res.records
  pages.value = res.pages || 1
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 1100px; }
.page-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: 24px; gap: 16px; flex-wrap: wrap;
}
.page-title { margin: 0 0 4px; font-size: 24px; font-weight: 700; color: var(--admin-text); }
.page-sub { margin: 0; font-size: 14px; color: var(--admin-muted); }
</style>
