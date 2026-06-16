<template>
  <section class="page">
    <header class="page-header">
      <div class="header-left">
        <button class="back-btn" @click="handleCancel">
          <span class="back-arrow">&larr;</span>
          <span>返回文章列表</span>
        </button>
        <div>
          <h2 class="page-title">{{ isEdit ? '编辑文章' : '写文章' }}</h2>
          <p class="page-sub">{{ isEdit ? '修改已有文章的内容和设置' : '创建一篇新的博客文章' }}</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button size="large" @click="handleCancel">取消</el-button>
        <el-button type="primary" size="large" @click="submit" :loading="saving">
          {{ isEdit ? '保存修改' : '发布文章' }}
        </el-button>
      </div>
    </header>

    <div class="editor-wrapper editor-primary">
      <div class="toolbar" v-if="editor">
        <button class="tb-btn" @click="editor.chain().focus().toggleBold().run()" :class="{ active: editor.isActive('bold') }" title="加粗">B</button>
        <button class="tb-btn tb-italic" @click="editor.chain().focus().toggleItalic().run()" :class="{ active: editor.isActive('italic') }" title="斜体">I</button>
        <button class="tb-btn" @click="editor.chain().focus().toggleStrike().run()" :class="{ active: editor.isActive('strike') }" title="删除线">S</button>
        <span class="tb-sep"></span>
        <button class="tb-btn" @click="editor.chain().focus().toggleHeading({ level: 1 }).run()" :class="{ active: editor.isActive('heading', { level: 1 }) }">H1</button>
        <button class="tb-btn" @click="editor.chain().focus().toggleHeading({ level: 2 }).run()" :class="{ active: editor.isActive('heading', { level: 2 }) }">H2</button>
        <button class="tb-btn" @click="editor.chain().focus().toggleHeading({ level: 3 }).run()" :class="{ active: editor.isActive('heading', { level: 3 }) }">H3</button>
        <span class="tb-sep"></span>
        <button class="tb-btn" @click="editor.chain().focus().toggleBlockquote().run()" :class="{ active: editor.isActive('blockquote') }" title="引用">"</button>
        <button class="tb-btn" @click="editor.chain().focus().toggleCodeBlock().run()" :class="{ active: editor.isActive('codeBlock') }" title="代码块">&lt;/&gt;</button>
        <button class="tb-btn" @click="editor.chain().focus().toggleBulletList().run()" :class="{ active: editor.isActive('bulletList') }" title="无序列表">•</button>
        <button class="tb-btn" @click="editor.chain().focus().toggleOrderedList().run()" :class="{ active: editor.isActive('orderedList') }" title="有序列表">1.</button>
        <span class="tb-sep"></span>
        <button class="tb-btn" @click="setLink" title="插入链接">🔗</button>
        <button class="tb-btn" @click="triggerImageUpload" title="插入图片">🖼</button>
        <input ref="fileInput" type="file" accept="image/*" multiple hidden @change="onFileSelect" />
      </div>
      <div class="editor-body" @click="onEditorClick">
        <editor-content :editor="editor" />
      </div>
    </div>

    <div class="meta-bar">
      <div class="meta-item">
        <label>状态</label>
        <el-select v-model="form.status" size="large">
          <el-option label="草稿" :value="0" />
          <el-option label="发布" :value="1" />
        </el-select>
      </div>
      <div class="meta-divider" />
      <div class="meta-item">
        <label>分类</label>
        <el-select v-model="form.categoryId" placeholder="选择分类" clearable size="large">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </div>
      <div class="meta-divider" />
      <div class="meta-item meta-tags">
        <label>标签</label>
        <el-select v-model="form.tagIds" placeholder="选择标签" multiple clearable size="large">
          <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
      </div>
      <div class="meta-divider" />
      <div class="meta-switches">
        <div class="switch-item">
          <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" size="large" />
          <span>置顶</span>
        </div>
        <div class="switch-item">
          <el-switch v-model="form.isCommentEnabled" :active-value="1" :inactive-value="0" size="large" />
          <span>允许评论</span>
        </div>
      </div>
    </div>

    <fieldset class="form-section">
      <legend>基本信息</legend>
      <el-input v-model="form.title" placeholder="文章标题" size="large" class="title-input" />
      <el-input v-model="form.summary" placeholder="摘要（可选）" type="textarea" :rows="2" />
      <el-input v-model="form.coverUrl" placeholder="封面图片 URL（可选）" />
    </fieldset>

    <el-dialog v-model="imageDialog" title="编辑图片" width="440px">
      <div class="dialog-field">
        <label>图片地址</label>
        <el-input v-model="editingImageSrc" placeholder="图片 URL" size="large" />
      </div>
      <div class="dialog-field">
        <label>图片说明</label>
        <el-input v-model="editingImageCaption" placeholder="图片下方展示的说明文字（可选）" size="large" />
      </div>
      <div class="dialog-field">
        <label>宽度</label>
        <el-select v-model="editingImageWidth" size="large" style="width:100%">
          <el-option label="原始大小" value="" />
          <el-option label="25%" value="25%" />
          <el-option label="50%" value="50%" />
          <el-option label="75%" value="75%" />
          <el-option label="100%" value="100%" />
        </el-select>
      </div>
      <div class="dialog-preview" v-if="editingImageSrc">
        <img :src="editingImageSrc" :style="{ width: editingImageWidth || 'auto' }" alt="preview" />
      </div>
      <template #footer>
        <el-button @click="imageDialog = false">取消</el-button>
        <el-button type="primary" @click="updateImageSrc">确定</el-button>
      </template>
    </el-dialog>

    <!-- 图片预览 -->
    <Teleport to="body">
      <div class="preview-overlay" v-if="showPreview" @click="showPreview = false">
        <img :src="previewSrc" @click.stop alt="preview" />
        <button class="preview-close" @click="showPreview = false">&times;</button>
      </div>
    </Teleport>
  </section>
</template>

<script setup>
import { h, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useEditor, EditorContent, NodeViewWrapper, VueNodeViewRenderer } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Image from '@tiptap/extension-image'
import Placeholder from '@tiptap/extension-placeholder'
import Link from '@tiptap/extension-link'
import { deleteFile, getArticle, listCategories, listTags, saveArticle } from '../api'
import http from '../api/http'

const route = useRoute()
const router = useRouter()
const saving = ref(false)
const categories = ref([])
const tags = ref([])
const originalHtml = ref('')
const uploadedUrls = new Set()
const fileInput = ref(null)

const articleId = route.params.id ? Number(route.params.id) : null
const isEdit = !!articleId

const form = reactive({
  id: null, title: '', summary: '', coverUrl: '', contentMd: '',
  categoryId: null, status: 1, isTop: 0, isCommentEnabled: 1, tagIds: []
})

// ---- Resizable image node view with preview ----
const previewSrc = ref('')
const showPreview = ref(false)
const ResizableImageVue = {
  props: ['node', 'updateAttributes'],
  setup(props) {
    const imgRef = ref(null)
    const resizing = ref(false)
    let startX = 0, startW = 0

    const onResizeStart = (e) => {
      e.preventDefault(); e.stopPropagation()
      resizing.value = true
      startX = e.clientX
      startW = imgRef.value?.clientWidth || 200
      document.addEventListener('mousemove', onResizeMove)
      document.addEventListener('mouseup', onResizeEnd)
    }
    const onResizeMove = (e) => {
      if (!imgRef.value) return
      const parentW = imgRef.value.parentElement?.parentElement?.clientWidth || 800
      const newW = startW + (e.clientX - startX)
      const pct = Math.max(10, Math.min(100, Math.round((newW / parentW) * 100)))
      props.updateAttributes({ width: pct + '%' })
    }
    const onResizeEnd = () => {
      resizing.value = false
      document.removeEventListener('mousemove', onResizeMove)
      document.removeEventListener('mouseup', onResizeEnd)
    }
    const onPreview = () => {
      previewSrc.value = props.node.attrs.src
      showPreview.value = true
    }
    const caption = props.node.attrs.caption || ''
    return () => h(NodeViewWrapper, { class: 'image-node', 'data-drag-handle': '' }, [
      h('div', {
        class: 'image-resize-wrap',
        style: { width: props.node.attrs.width || 'auto', maxWidth: '100%' }
      }, [
        h('img', {
          ref: imgRef,
          src: props.node.attrs.src,
          class: 'resizable-image',
          onClick: onPreview,
          title: '点击放大预览'
        }),
        h('div', { class: 'resize-handle', onMousedown: onResizeStart, title: '拖动调整大小' })
      ]),
      h('figcaption', { class: 'image-caption' }, caption || '')
    ])
  }
}

const ResizableImageExt = Image.extend({
  inline: true,
  group: 'inline',
  addAttributes() {
    return { ...this.parent?.(), width: { default: null }, caption: { default: '' } }
  },
  addNodeView() {
    return VueNodeViewRenderer(ResizableImageVue)
  }
})

const editor = useEditor({
  extensions: [
    StarterKit.configure({ heading: { levels: [1, 2, 3] } }),
    ResizableImageExt,
    Placeholder.configure({ placeholder: '开始写作，拖拽或粘贴图片即可上传...' }),
    Link.configure({ openOnClick: false, HTMLAttributes: { target: '_blank', rel: 'noopener' } })
  ],
  editorProps: {
    handleDrop: (view, event) => {
      const files = event.dataTransfer?.files
      if (files?.length) {
        handleImageFiles(Array.from(files))
        return true
      }
      return false
    },
    handlePaste: (view, event) => {
      const items = event.clipboardData?.items
      if (!items) return false
      const imageFiles = []
      for (const item of items) {
        if (item.type.startsWith('image/')) imageFiles.push(item.getAsFile())
      }
      if (imageFiles.length) {
        handleImageFiles(imageFiles)
        return true
      }
      // 粘贴纯文本 URL → 自动转为链接
      const text = event.clipboardData?.getData('text/plain')?.trim()
      if (text && /^https?:\/\/\S+$/.test(text)) {
        event.preventDefault()
        editor.value.chain().focus().insertContent(`<a href="${text}" target="_blank" rel="noopener">${text}</a> `).run()
        return true
      }
      return false
    }
  }
})

const triggerImageUpload = () => { fileInput.value?.click() }

const onFileSelect = (e) => {
  const files = e.target.files
  if (files?.length) handleImageFiles(Array.from(files))
  e.target.value = ''
}

const handleImageFiles = async (files) => {
  for (const file of files) {
    if (!file.type.startsWith('image/')) continue
    try {
      const fd = new FormData()
      fd.append('file', file)
      fd.append('subDir', 'articles')
      const data = await http.post('/api/file/upload', fd)
      uploadedUrls.add(data.url)
      editor.value.chain().focus().setImage({ src: data.url }).insertContent(' ').run()
    } catch (e) {
      ElMessage.error(`图片上传失败: ${e.message}`)
    }
  }
}

const setLink = () => {
  const url = window.prompt('输入链接地址:', 'https://')
  if (!url || !editor.value) return
  const { empty } = editor.value.state.selection
  if (empty) {
    // 无选中文字：插入链接文字并在后面加空格，后续输入不受影响
    editor.value.chain().focus().insertContent(`<a href="${url}" target="_blank" rel="noopener">${url}</a>&nbsp;`).run()
  } else {
    editor.value.chain().focus().setLink({ href: url }).run()
  }
}

const imageDialog = ref(false)
const editingImageSrc = ref('')
const editingImageWidth = ref('')
const editingImageCaption = ref('')
let editingImagePos = null

const onEditorClick = (e) => {
  const nodeWrap = e.target.closest('.image-node')
  if (!nodeWrap || !editor.value) return
  const img = nodeWrap.querySelector('img')
  if (!img) return

  // Find the node position
  const { view } = editor.value
  const pos = view.posAtDOM(nodeWrap, 0)
  const node = view.state.doc.nodeAt(pos)
  if (node?.type.name === 'image') {
    editingImagePos = pos
    editingImageSrc.value = node.attrs.src
    editingImageWidth.value = node.attrs.width || ''
    editingImageCaption.value = node.attrs.caption || ''
    imageDialog.value = true
  }
}

const updateImageSrc = () => {
  if (editingImagePos != null && editor.value) {
    editor.value.chain().focus().setNodeSelection(editingImagePos).updateAttributes('image', {
      src: editingImageSrc.value,
      width: editingImageWidth.value || null,
      caption: editingImageCaption.value || null
    }).run()
  }
  imageDialog.value = false
  editingImagePos = null
}

watch(() => editor.value?.getHTML(), (html) => {
  if (html && editor.value) form.contentMd = html
})

const handleCancel = async () => {
  if (uploadedUrls.size > 0) {
    const tasks = [...uploadedUrls].map(url => deleteFile(url).catch(() => {}))
    await Promise.all(tasks)
    uploadedUrls.clear()
  }
  router.push('/articles')
}

const submit = async () => {
  saving.value = true
  try {
    if (editor.value) form.contentMd = editor.value.getHTML()
    await saveArticle({ ...form })

    const oldUrls = extractImageUrls(originalHtml.value)
    const newUrls = extractImageUrls(form.contentMd)
    const removedUrls = oldUrls.filter(u => !newUrls.includes(u))
    for (const url of removedUrls) {
      try { await deleteFile(url) } catch { /* ignore */ }
    }

    uploadedUrls.clear()
    ElMessage.success(isEdit ? '文章已更新' : '文章已发布')
    router.push('/articles')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

const extractImageUrls = (html) => {
  if (!html) return []
  const urls = []
  const re = /<img[^>]+src="([^"]*\/uploads\/[^"]+)"/g
  let m
  while ((m = re.exec(html)) !== null) {
    const src = m[1]
    // 去掉可能的前缀域名，只保留 /uploads/... 路径
    const path = src.replace(/^https?:\/\/[^/]+/, '')
    urls.push(path)
  }
  return urls
}

onMounted(async () => {
  const [cats, tgs] = await Promise.all([listCategories(), listTags()])
  categories.value = cats
  tags.value = tgs

  if (articleId) {
    const article = await getArticle(articleId)
    Object.assign(form, {
      id: article.id, title: article.title, summary: article.summary || '',
      coverUrl: article.coverUrl || '', contentMd: article.contentHtml || article.contentMd || '',
      categoryId: article.categoryId, status: article.status,
      isTop: article.isTop ?? 0, isCommentEnabled: article.isCommentEnabled ?? 1,
      tagIds: article.tagIds || []
    })
    setTimeout(() => {
      if (editor.value) {
        editor.value.commands.setContent(article.contentHtml || `<p>${article.contentMd || ''}</p>`)
      }
    }, 50)
  } else {
    setTimeout(() => {
      if (editor.value) editor.value.commands.setContent('')
    }, 50)
  }
})

onBeforeUnmount(() => {
  editor.value?.destroy()
  if (uploadedUrls.size > 0) {
    const token = localStorage.getItem('admin_token')
    for (const url of uploadedUrls) {
      const apiUrl = `${http.defaults.baseURL || ''}/api/file/delete?url=${encodeURIComponent(url)}`
      fetch(apiUrl, { method: 'DELETE', keepalive: true, headers: { Authorization: token } }).catch(() => {})
    }
  }
})
</script>

<style scoped>
.page { max-width: 1200px; padding-bottom: 64px; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 16px;
  flex-wrap: wrap;
}
.header-left { display: flex; flex-direction: column; gap: 10px; }
.page-title { margin: 0; font-size: 24px; font-weight: 700; color: var(--admin-text); }
.page-sub { margin: 0; font-size: 14px; color: var(--admin-muted); }
.header-actions { display: flex; gap: 10px; align-items: center; }

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  font-size: 13px;
  border: 1px solid var(--admin-line-strong);
  background: transparent;
  color: var(--admin-muted);
  border-radius: 8px;
  transition: all .2s;
}
.back-btn:hover { background: var(--admin-soft-accent); color: var(--admin-accent); }
.back-arrow { font-size: 15px; }

.meta-bar {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 20px;
  border: 1px solid var(--admin-line);
  border-radius: var(--admin-radius);
  background: var(--admin-panel);
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.meta-item { display: flex; align-items: center; gap: 8px; }
.meta-item label { font-size: 13px; font-weight: 600; color: var(--admin-muted); white-space: nowrap; }
.meta-tags { min-width: 180px; }
.meta-divider { width: 1px; height: 28px; background: var(--admin-line); flex-shrink: 0; }
.meta-switches { display: flex; align-items: center; gap: 18px; }
.switch-item { display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--admin-muted); white-space: nowrap; }

.form-section {
  border: 1px solid var(--admin-line);
  border-radius: var(--admin-radius);
  padding: 20px;
  display: flex; flex-direction: column; gap: 12px;
  background: var(--admin-panel);
  margin-bottom: 16px;
}
.form-section legend {
  font-size: 13px; font-weight: 700; color: var(--admin-accent); padding: 0 8px;
  letter-spacing: .06em;
}
.title-input :deep(.el-input__inner) { font-size: 18px; font-weight: 600; }

.editor-wrapper {
  display: flex; flex-direction: column;
  border: 1px solid var(--admin-line);
  border-radius: var(--admin-radius);
  overflow: hidden;
  background: var(--admin-panel);
  margin-bottom: 16px;
}
.editor-primary {
  min-height: 550px;
  height: 65vh;
  max-height: 80vh;
}

.toolbar {
  display: flex; align-items: center; gap: 2px;
  padding: 8px 12px;
  border-bottom: 1px solid var(--admin-line);
  background: var(--admin-table-head);
  flex-shrink: 0;
  flex-wrap: wrap;
}
.tb-btn {
  width: 32px; height: 32px;
  border: none; background: transparent;
  color: var(--admin-muted); font-size: 14px; font-weight: 600;
  border-radius: 6px; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all .15s;
}
.tb-btn:hover { background: var(--admin-soft-accent); color: var(--admin-accent); }
.tb-btn.active { background: var(--admin-soft-accent-strong); color: var(--admin-accent); }
.tb-italic { font-style: italic; }
.tb-sep { width: 1px; height: 20px; background: var(--admin-line); margin: 0 4px; }

.editor-body { flex: 1; min-height: 0; overflow: hidden auto; }

.editor-body :deep(.tiptap) {
  padding: 24px 28px;
  min-height: 100%;
  outline: none;
  font-size: 16px;
  line-height: 1.8;
  color: var(--admin-text);
}
.editor-body :deep(.tiptap p.is-editor-empty:first-child::before) {
  content: attr(data-placeholder);
  color: var(--admin-muted);
  pointer-events: none;
  float: left;
  height: 0;
}
.editor-body :deep(.tiptap h1) { font-size: 28px; margin: 20px 0 10px; line-height: 1.3; }
.editor-body :deep(.tiptap h2) { font-size: 22px; margin: 18px 0 8px; line-height: 1.3; }
.editor-body :deep(.tiptap h3) { font-size: 18px; margin: 16px 0 6px; line-height: 1.3; }
.editor-body :deep(.tiptap p) { margin: 0 0 16px; }
.editor-body :deep(.tiptap pre) {
  background: var(--admin-table-head); padding: 16px 20px;
  border-radius: 8px; font-size: 14px; overflow-x: auto; margin: 16px 0;
}
.editor-body :deep(.tiptap code) {
  background: var(--admin-table-head); padding: 2px 6px; border-radius: 4px; font-size: 0.9em;
}
.editor-body :deep(.tiptap blockquote) {
  border-left: 3px solid var(--admin-accent); padding: 8px 16px; margin: 16px 0;
  color: var(--admin-muted); background: var(--admin-soft-accent); border-radius: 0 6px 6px 0;
}
.editor-body :deep(.tiptap ul), .editor-body :deep(.tiptap ol) { padding-left: 24px; margin: 12px 0; }
.editor-body :deep(.tiptap li) { margin: 4px 0; }
.editor-body :deep(.tiptap a) { color: var(--admin-accent); text-decoration: underline; }
.editor-body :deep(.tiptap hr) { border: none; border-top: 1px solid var(--admin-line); margin: 24px 0; }

/* Image node wrapper */
.editor-body :deep(.image-node) {
  display: inline-block;
  vertical-align: middle;
  max-width: 100%;
  margin: 4px;
}
.editor-body :deep(.image-resize-wrap) {
  display: inline-block;
  position: relative;
  max-width: 100%;
  overflow: visible;
  border-radius: 6px;
}
.editor-body :deep(.resizable-image) {
  display: block;
  width: 100%;
  height: auto;
  cursor: zoom-in;
  transition: outline .15s;
  border-radius: 6px;
}
.editor-body :deep(.image-node:hover .resizable-image),
.editor-body :deep(.image-node.ProseMirror-selectednode .resizable-image) {
  outline: 2px solid var(--admin-accent);
  outline-offset: 2px;
}
.editor-body :deep(.resize-handle) {
  position: absolute;
  right: -4px; bottom: -4px;
  width: 20px; height: 20px;
  background: var(--admin-accent);
  clip-path: polygon(100% 0, 100% 100%, 0 100%);
  border-radius: 0 0 6px 0;
  cursor: nwse-resize;
  opacity: 0;
  transition: opacity .15s;
  z-index: 2;
}
.editor-body :deep(.image-node:hover .resize-handle),
.editor-body :deep(.image-node.ProseMirror-selectednode .resize-handle) {
  opacity: 0.8;
}

/* ---- image preview overlay ---- */
.preview-overlay {
  position: fixed; inset: 0; z-index: 9999;
  background: rgba(0,0,0,0.85);
  display: flex; align-items: center; justify-content: center;
  cursor: zoom-out;
}
.preview-overlay img {
  max-width: 90vw; max-height: 90vh;
  object-fit: contain; border-radius: 4px;
  box-shadow: 0 4px 32px rgba(0,0,0,0.4);
}
.preview-close {
  position: absolute; top: 20px; right: 20px;
  background: rgba(255,255,255,0.15); border: none;
  color: #fff; font-size: 32px; width: 44px; height: 44px;
  border-radius: 50%; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background .2s;
}
.preview-close:hover { background: rgba(255,255,255,0.3); }
.editor-body :deep(.image-caption) {
  text-align: center;
  font-size: 13px;
  color: var(--admin-muted);
  margin-top: 6px;
  padding: 0 4px;
  line-height: 1.5;
  font-style: italic;
  min-height: 0;
}

.dialog-field { margin-bottom: 16px; }
.dialog-field label { display: block; margin-bottom: 6px; font-size: 13px; font-weight: 600; color: var(--admin-muted); }
.dialog-preview { margin-top: 12px; }
.dialog-preview img { max-width: 100%; border-radius: 6px; }

@media (max-width: 900px) {
  .meta-bar { gap: 10px; }
  .meta-divider { display: none; }
  .toolbar { gap: 0; padding: 6px 8px; }
  .editor-primary { height: 50vh; min-height: 400px; }
}
@media (max-width: 640px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 8px; }
  .meta-bar { flex-direction: column; gap: 8px; }
  .form-section { flex-direction: column; }
  .toolbar { gap: 0; padding: 4px 6px; overflow-x: auto; }
}
</style>
