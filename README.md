# Blog System

个人博客系统，前后端分离架构。前端含公开浏览端（blog-web）与管理后台（blog-admin），后端为 Spring Boot 单体服务。

## 项目结构

```
blog-system/
├── blog-server/     Spring Boot 后端 (Java 17 / MyBatis-Plus / Sa-Token)
├── blog-web/        Vue 3 公开博客前端 (Vite 5 / markdown-it)
├── blog-admin/      Vue 3 管理后台 (Element Plus / Tiptap)
├── docs/            项目文档
│   ├── phase1-progress.md
│   ├── phase2-progress.md
│   ├── extend.md
│   ├── ai-chat-module.md
│   └── troubleshooting-and-lessons-learned.md
└── README.md
```

## 技术栈

| 层 | 技术 |
|---|---|
| 后端框架 | Spring Boot 3.3 + MyBatis-Plus 3.5 + Spring AOP |
| 认证鉴权 | Sa-Token 1.38 (JWT + Redis 混合模式 / 13 条权限码 RBAC) |
| 数据库 | MySQL 8.x |
| 缓存 | Redis (热门文章 ZSet / 验证码限流 / AI 记忆 / 登录凭证) |
| Markdown→HTML | Flexmark (后端) + markdown-it (前端) |
| AI 对话 | Spring AI + DashScope Qwen-Max (SSE 流式 + RAG 博客知识库) |
| API 文档 | Knife4j (/doc.html) |
| 前端框架 | Vue 3 (Composition API) + Vue Router + Pinia |
| 后台 UI | Element Plus |
| 富文本编辑器 | Tiptap (WYSIWYG, 替代 ByTemd) |
| 构建 | Vite 5 |
| 部署 | Dockerfile + docker-compose (MySQL / Redis / blog-server / Nginx) |

## 后端模块划分

```
com.blogsystem
├── auth/            认证模块 (验证码 / 注册 / 登录 / 个人信息 / 改密)
│   ├── controller/  AuthController
│   ├── service/     AuthService
│   ├── dto/         LoginUserVO, ProfileUpdateRequest, PasswordUpdateRequest
│   ├── entity/      SysUser, SysRole, SysUserRole, SmsCodeLog
│   └── mapper/      MyBatis-Plus Mapper
├── admin/           管理端模块
│   ├── controller/  AdminController (用户管理 / 日志查询)
│   └── service/     AdminService
├── content/         内容模块 (文章 / 分类 / 标签 / 热门文章 / 点赞)
│   ├── controller/  ContentController
│   ├── service/     ContentService
│   ├── dto/         ArticleSaveRequest, CategorySaveRequest, TagSaveRequest
│   ├── entity/      Article, Category, Tag, ArticleTag
│   └── mapper/
├── comment/         评论模块 (发表 / 回复 / 审核)
│   ├── controller/  CommentController
│   ├── service/     CommentService
│   ├── dto/         CommentVO, CommentSaveRequest
│   ├── entity/      Comment
│   └── mapper/
├── file/            文件模块 (上传 / 删除 / file_record 表)
│   ├── controller/  FileController
│   ├── service/     FileService
│   ├── entity/      FileRecord
│   └── mapper/      FileRecordMapper
├── ai/              AI 模块 (SSE 流式对话 / RAG / Redis 记忆)
│   ├── controller/  AiController
│   ├── service/     AiService
│   ├── config/      AiConfig / RedisChatMemory
│   └── dto/         ChatRequest
├── log/             日志模块 (操作日志 / 登录日志 / @OpLog AOP)
│   ├── controller/
│   ├── aspect/      LogAspect
│   ├── annotation/  OpLog
│   ├── entity/      OperationLog, LoginLog
│   └── mapper/
├── security/        权限实现 (StpInterfaceImpl / TokenRedisInterceptor)
├── config/          配置 (CORS / MyBatis-Plus / Sa-Token / Jackson / Cache / WebMvc)
└── common/          公共 (ApiResponse / GlobalExceptionHandler)
```

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+

### 1. 初始化数据库

执行 `blog-server/src/main/resources/db/schema.sql` 创建表结构和种子数据（管理员账号 + 角色 + 13 条权限）。

### 2. 配置

编辑 `blog-server/src/main/resources/application-dev.yml`：
- 数据库连接（host / port / username / password）
- Redis 连接（host / port / database）
- 上传目录（`blog.upload.dir`）
- JWT 密钥（`sa-token.jwt-secret-key`）

### 3. 启动后端

```bash
cd blog-server
mvn compile
mvn exec:java -Dexec.mainClass="com.blogsystem.BlogServerApplication"
```

后端默认运行在 `http://localhost:8080`，API 文档 `http://localhost:8080/doc.html`。

### 4. 启动前端

```bash
# 公开博客 (http://localhost:5173)
cd blog-web
npm install
npx vite --port 5173

# 管理后台 (http://localhost:5174)
cd blog-admin
npm install --legacy-peer-deps
npx vite --port 5174
```

## 功能概览

### 公开博客 (blog-web)

- 游客浏览：文章列表 / 详情 / 阅读量统计 / 热门文章
- 分类 + 标签筛选：三栏布局自适应视口
- 手机验证码注册/登录
- 评论互动：发表 / 回复 / 昵称 + 头像
- 个人信息：`/profile` 编辑昵称 / 邮箱 / 头像
- 点赞：toggle 模式，防止重复点赞
- 图片自适应展示

### 管理后台 (blog-admin)

- **文章管理**：WYSIWYG 编辑器 (Tiptap)，支持：
  - 拖拽/粘贴图片自动上传
  - 图片拖拽缩放 + 说明文字
  - Bold / Italic / H1-H3 / 引用 / 代码 / 列表 / 链接
  - 图片点击编辑 URL
- **评论审核**：通过 / 隐藏 / 删除
- **分类管理**：新建 / 编辑 / 删除
- **标签管理**：新建 / 编辑 / 删除
- **用户管理**：分页 / 启用禁用 / 删除 / 头像同步
- **操作日志**：@OpLog AOP 自动记录 + 管理端查看
- **明暗双主题**：localStorage 持久化
- **管理员头像+昵称**：侧边栏同步用户信息
- **Redis 会话管理**：删除 Redis 凭证即时生效

### 后端能力

- 13 条权限码 RBAC (`@SaCheckPermission`)
- JWT + Redis 混合会话：JWT 负责跨服务验证，Redis 负责会话撤销
- 验证码限流（手机号 60s / IP 每天 10 次）
- 热门文章 ZSet 缓存（ZREVRANGE + selectBatchIds）
- AI 流式对话（SSE + Qwen-Max + 博客文章 RAG + Redis 记忆）
- 文件上传（本地存储 + file_record 表 + MD5 校验）
- @OpLog AOP 自动记录操作日志
- 全局异常处理（区分 400/401/403/500）
- Docker Compose 一键部署（MySQL + Redis + blog-server + Nginx）

## 配置速查

### Sa-Token 当前配置

```yaml
sa-token:
  token-name: Authorization
  timeout: 86400           # 24 小时
  is-concurrent: true      # 同一账号可多处登录
  is-share: false          # 每次登录独立 token
  jwt-secret-key: ${sa-token.jwt-secret-key}
```

### 权限码 (13 条)

```
admin:user:list / update / delete
admin:log:list
content:article:write / delete
content:category:write / delete
content:tag:write / delete
comment:admin:list / audit / delete
```

### 关键 API

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/auth/login` | POST | 手机验证码登录 |
| `/api/auth/me` | GET | 当前用户信息 |
| `/api/content/article/list` | GET | 文章分页 |
| `/api/content/article/{id}` | GET | 文章详情 |
| `/api/content/article` | POST | 新建/更新文章 |
| `/api/file/upload` | POST | 文件上传 (subDir 参数) |
| `/api/file/delete` | DELETE | 删除文件 + DB 记录 |
| `/api/ai/chat` | POST | AI 流式对话 (SSE) |

## 开发说明

- 验证码开发环境返回固定 mock 码，无需短信服务
- 管理员手机 `13800000000`，通过验证码登录
- 管理端 token 存 `localStorage`，有效期由 Redis 控制
- Redis 中删除 `Authorization:login:token:{token}` 可立即撤销登录态
- 文章 `contentMd` 字段当前存储 HTML（Tiptap 输出），旧文章 Markdown 仍兼容
- 旧 ByTemd 编辑器已由 Tiptap 替换，图文分离时代结束

## 相关文档

- `docs/phase1-progress.md` — 第一阶段完成情况
- `docs/phase2-progress.md` — 第二阶段进度 (Redis / 日志 / 权限 / Docker / AI)
- `docs/extend.md` — 第三阶段扩展 (编辑器重构 / 图片管理)
- `docs/ai-chat-module.md` — AI 模块集成
- `docs/troubleshooting-and-lessons-learned.md` — 全阶段问题记录
