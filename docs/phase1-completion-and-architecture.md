# 第一阶段（MVP）完成情况与系统架构文档

## 一、后端模块划分

```
com.blogsystem
├── auth/            认证模块（验证码 / 注册 / 登录 / 个人信息）
│   ├── controller/  AuthController
│   ├── service/     AuthService
│   ├── dto/         LoginUserVO, ProfileUpdateRequest, PasswordUpdateRequest, ...
│   ├── entity/      SysUser, SysRole, SysUserRole, SmsCodeLog
│   └── mapper/      MyBatis-Plus Mapper
├── admin/           管理端模块
│   ├── controller/  AdminController（用户管理：分页 / 状态开关 / 删除）
│   └── service/     AdminService
├── content/         内容模块（文章 / 分类 / 标签 / 点赞）
│   ├── controller/  ContentController
│   ├── service/     ContentService（含 Markdown→HTML 转换）
│   ├── dto/         ArticleSaveRequest, CategorySaveRequest, TagSaveRequest
│   ├── entity/      Article, Category, Tag, ArticleTag, ArticleLike
│   └── mapper/
├── comment/         评论模块
│   ├── controller/  CommentController
│   ├── service/     CommentService
│   ├── dto/         CommentVO, CommentSaveRequest
│   ├── entity/      Comment
│   └── mapper/
├── security/        权限实现（Sa-Token StpInterface）
├── config/          配置（CORS / MyBatis-Plus / Sa-Token / Knife4j）
└── common/          公共（ApiResponse / GlobalExceptionHandler）
```

---

## 二、数据库表（已落地）

| 表 | 说明 | 状态 |
|---|---|---|
| `sys_user` | 用户表 | ✅ |
| `sys_role` | 角色表 | ✅ |
| `sys_permission` | 权限表 | ✅ |
| `sys_user_role` | 用户-角色关联 | ✅ |
| `sys_role_permission` | 角色-权限关联 | ✅ |
| `sms_code_log` | 短信验证码记录 | ✅ |
| `article` | 文章表（含 content_html） | ✅ |
| `category` | 分类表 | ✅ |
| `tag` | 标签表 | ✅ |
| `article_tag` | 文章-标签关联 | ✅ |
| `article_like` | 文章点赞记录 | ✅ |
| `comment` | 评论表 | ✅ |

---

## 三、后端 API 清单

### 认证模块 `/api/auth`
| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| POST | `/sms-code` | 发送验证码（Mock） | 无 |
| POST | `/register` | 手机验证码注册 | 无 |
| POST | `/login` | 手机验证码登录 | 无 |
| GET | `/me` | 获取当前用户信息 | 登录 |
| PUT | `/profile` | 更新个人信息（昵称/邮箱/头像） | 登录 |
| PUT | `/password` | 修改密码 | 登录 |
| POST | `/logout` | 退出登录 | 登录 |

### 内容模块 `/api/content`
| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| POST | `/article` | 新增/更新文章 | 登录 |
| GET | `/article/list` | 文章分页（支持 status/categoryId/tagId 筛选） | 无 |
| GET | `/article/hot` | 热门文章（按阅读量降序） | 无 |
| GET | `/article/{id}` | 文章详情（阅读量 +1） | 无 |
| DELETE | `/article/{id}` | 删除文章（软删除） | 登录 |
| POST | `/article/{id}/like` | 切换点赞（返回当前是否已点赞） | 登录 |
| POST | `/category` | 新增/更新分类 | 登录 |
| GET | `/category/list` | 分类列表 | 无 |
| DELETE | `/category/{id}` | 删除分类 | 登录 |
| POST | `/tag` | 新增/更新标签 | 登录 |
| GET | `/tag/list` | 标签列表 | 无 |
| DELETE | `/tag/{id}` | 删除标签 | 登录 |

### 评论模块 `/api/comment`
| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| POST | `/` | 发表评论/回复 | 登录 |
| GET | `/article/{articleId}` | 文章可见评论列表 | 无 |
| GET | `/admin/list` | 管理端评论分页 | ADMIN |
| POST | `/admin/{id}/audit` | 审核评论（通过/隐藏） | ADMIN |
| DELETE | `/admin/{id}` | 删除评论 | ADMIN |

### 管理模块 `/api/admin`
| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| GET | `/users` | 用户分页（支持 status 筛选） | ADMIN |
| PUT | `/users/{id}/status` | 启用/禁用用户 | ADMIN |
| DELETE | `/users/{id}` | 删除用户（软删除） | ADMIN |

### 开发辅助
| 路径 | 说明 |
|---|---|
| `/doc.html` | Knife4j API 文档（OpenAPI 3.0） |
| `/v3/api-docs` | OpenAPI JSON |

---

## 四、前端功能清单

### blog-web（公开博客 `:5173`）

| 功能 | 实现方式 |
|---|---|
| 三栏自适应布局 | CSS Grid `minmax(150px,200px) 1fr minmax(150px,200px)` + clamp() |
| 分类导航（左侧栏） | sticky 定位，点击替换路由 query，联动文章筛选 |
| 标签导航（右侧栏） | sticky 定位，点击替换路由 query，仅显示匹配文章 |
| 热门阅读 | 首页默认展示，按阅读量降序，显示封面/摘要/置顶标记 |
| 文章详情 | Markdown 渲染（`markdown-it`），阅读量自动 +1 |
| 封面图展示 | 列表卡片 + 详情页 header 均展示 coverUrl |
| 评论区 | 登录后可见，显示昵称 + 时间，支持回复 |
| 评论开关 | 根据 `isCommentEnabled` 控制评论区显隐 |
| 点赞 | 详情页 ♡/♥ 切换按钮，登录后可操作 |
| 个人信息 `/profile` | 昵称/邮箱编辑，修改密码 |
| 注册/登录 | 手机验证码流程 |
| 导航栏 | 已登录显示昵称下拉，未登录显示登录入口 |

### blog-admin（管理后台 `:5174`）

| 功能 | 实现方式 |
|---|---|
| 明暗主题切换 | `data-admin-theme` 属性 + localStorage 持久化 |
| 文章管理 | 新建/编辑/删除，表单含：标题/分类下拉/标签多选/摘要/封面URL/状态/置顶/评论开关 |
| Markdown 编辑器 | `bytemd`（原生 ESM，Vite 兼容） |
| 文章列表 | 分页 + 状态筛选，显示分类名/标签名 |
| 评论审核 | 通过/隐藏，分页 + 状态筛选 |
| 分类管理 | 新建/编辑/删除（name + slug） |
| 标签管理 | 新建/编辑/删除（name + slug） |
| 用户管理 | 分页 + 状态筛选，启用/禁用/删除 |

---

## 五、技术选型与关键决策

| 决策点 | 选择 | 原因 |
|---|---|---|
| Markdown 编辑器 | `bytemd` → 替代 `@kangc/v-md-editor` | bytemd 原生 ESM，Vite 完全兼容；v-md-editor CodeMirror 变体为 CJS，Vite 下 `Cannot read properties of undefined (reading 'prototype')` 白屏 |
| Markdown 渲染（前端） | `markdown-it` | 轻量、插件化、与 bytemd 输出一致 |
| Markdown→HTML（后端） | `flexmark-all` | Java 生态最成熟，保存时自动转换 contentMd → contentHtml |
| 点赞 | `article_like` 表 + 切换模式 | 同一用户再次点击取消点赞，防重复计数 |
| 启动方式 | `mvn exec:java` 替代 `spring-boot:run` | Windows 中文路径下 JAR 打包失败，exec:java 从编译 class 直接运行 |
| 验证码 | 开发环境返回 Mock 码 | 免短信服务依赖，6 位随机数字 |
| 权限模型 | Sa-Token + `@SaCheckRole("ADMIN")` | RBAC 表已建，权限接口已注册，当前用角色注解简化 |

---

## 六、第一阶段 DoD 验收清单

- [x] 认证：手机验证码注册/登录/个人信息/修改密码/退出
- [x] 文章：CRUD + 草稿/发布 + 分类/标签筛选 + 热门 + 阅读量
- [x] 分类标签：CRUD + 文章关联 + 前端三栏布局筛选
- [x] 评论：发表/回复/审核/隐藏/删除 + 昵称 + 时间
- [x] Markdown：bytemd 编辑 + markdown-it 渲染 + 后端 flexmark 转 HTML
- [x] 文章封面：admin 输入 + blog-web 展示
- [x] 文章置顶：admin switch + blog-web 置顶标记
- [x] 评论开关：admin switch + blog-web 条件显隐
- [x] 点赞：前后端 toggle-like + likeCount 同步
- [x] 用户管理：分页/筛选/启用禁用/删除
- [x] 个人信息：昵称/邮箱编辑 + 改密
- [x] API 文档：Knife4j `/doc.html`
- [x] 主题切换：blog-admin 明暗主题
- [x] 构建通过：后端 `mvn compile`，前端 `npm run build`

---

## 七、下一步（第二阶段）

依据 `personal_blog_architecture_and_database_design.md`：

1. **Redis 缓存** — 热门文章缓存、验证码频率限制
2. **文件上传** — 图片/文件上传 + `file_record` 表 + MinIO 扩展
3. **Docker 部署** — 容器化 blog-server + MySQL
4. **日志系统** — `operation_log` + `login_log` + `visit_log`
5. **权限系统完善** — 动态菜单 + `sys_permission` 前端路由守卫
