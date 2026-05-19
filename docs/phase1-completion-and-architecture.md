# 第一阶段（MVP）完成情况与当前系统整体架构

## 一、第一阶段已完成内容

### 1. 后端工程与基础设施
- 已创建后端项目：`blog-system/blog-server`
- 技术栈：Spring Boot 3 + MyBatis-Plus + Sa-Token（JWT）+ MySQL
- 已完成统一响应与全局异常处理：
  - `blog-server/src/main/java/com/blogsystem/common/ApiResponse.java`
  - `blog-server/src/main/java/com/blogsystem/common/GlobalExceptionHandler.java`
- 已完成 Sa-Token 拦截与角色接口适配：
  - `blog-server/src/main/java/com/blogsystem/config/SaTokenConfigure.java`
  - `blog-server/src/main/java/com/blogsystem/config/StpInterfaceImpl.java`

### 2. 数据库（MVP 子集 + RBAC）
- 已完成建库脚本：`blog-server/src/main/resources/db/schema.sql`
- 已落地核心表：
  - 认证：`sys_user`、`sms_code_log`
  - RBAC：`sys_role`、`sys_permission`、`sys_user_role`、`sys_role_permission`
  - 内容：`article`、`category`、`tag`、`article_tag`
  - 互动：`comment`
- 已完成初始化数据：
  - 角色：`ADMIN`、`USER`
  - 管理员账号：`admin`（phone: `13800000000`）
  - 管理员角色绑定与后台权限绑定

### 3. 认证与验证码流程（开发期 Mock）
- 已完成接口：
  - `POST /api/auth/sms-code`
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `GET /api/auth/me`
  - `POST /api/auth/logout`
- 已实现手机号验证码登录注册（符合“手机验证码流程”要求）

### 4. 内容模块（文章 + 分类 + 标签）
- 已完成接口：
  - 文章：`POST /api/content/article`、`GET /api/content/article/list`、`GET /api/content/article/{id}`、`DELETE /api/content/article/{id}`
  - 分类：`POST /api/content/category`、`GET /api/content/category/list`、`DELETE /api/content/category/{id}`
  - 标签：`POST /api/content/tag`、`GET /api/content/tag/list`、`DELETE /api/content/tag/{id}`
- 已支持文章与标签关联保存（`article_tag`）

### 5. 评论与后台管理基础能力
- 已完成评论接口：
  - `POST /api/comment`
  - `GET /api/comment/article/{articleId}`
  - `GET /api/comment/admin/list`
  - `POST /api/comment/admin/{id}/audit`
  - `DELETE /api/comment/admin/{id}`
- 已完成后台用户管理基础接口：
  - `GET /api/admin/users`（ADMIN）

### 6. 前端工程骨架与页面主链路
- 已创建前台项目：`blog-system/blog-web`
  - 已实现：登录/注册页、文章列表页、文章详情页、发表评论
- 已创建后台项目：`blog-system/blog-admin`
  - 已实现：管理员登录页、文章管理、分类管理、标签管理、评论审核页
- 两个前端项目均已 `npm run build` 通过

### 7. 联调与测试资产
- 已新增：`blog-system/docs/quick-start.md`
- 已新增：`blog-system/docs/postman-blog-system-mvp.json`
- 包含建库、启动后端、认证与核心接口联调示例，及可导入 Postman 集合

---

## 二、当前系统整体架构（实现态）

```text
                ┌──────────────────────┐
                │      blog-web        │
                │  (Vue3 + Vite 前台)  │
                └──────────┬───────────┘
                           │ HTTP/JSON
                ┌──────────▼───────────┐
                │     blog-server      │
                │ Spring Boot + JWT鉴权 │
                └───────┬───────┬──────┘
                        │       │
               ┌────────▼───┐   │
               │   MySQL    │   │
               │ 业务数据/RBAC│   │
               └────────────┘   │
                                 │
                ┌────────────────▼───┐
                │     blog-admin     │
                │  (Vue3 + Vite 后台) │
                └────────────────────┘
```

### 模块分层
- Controller 层：对外 REST 接口
- Service 层：业务逻辑（认证、内容、评论、后台）
- Mapper 层：MyBatis-Plus 数据访问
- DB 层：MySQL（含 RBAC 表）

### 鉴权与权限
- 登录态：Sa-Token Token
- 角色校验：`@SaCheckRole("ADMIN")`
- 角色来源：`sys_user_role -> sys_role`

---

## 三、与第一阶段目标对照

- 登录注册（手机验证码）：✅ 已实现（Mock）
- 文章 CRUD：✅ 已实现基础 CRUD
- 分类标签：✅ 已实现
- 评论系统：✅ 已实现发布、查询、后台审核删除
- Markdown 编辑链路：✅ 已支持 `content_md` 存储与展示
- 后台管理：✅ 已有登录与管理页主链路

---

## 四、第一阶段 DoD 验收清单（逐条打勾）

- [x] 认证：可发送手机验证码（Mock）、可手机号+验证码注册登录、可获取当前用户、可退出登录  
  证据：`/api/auth/sms-code`、`/api/auth/register`、`/api/auth/login`、`/api/auth/me`、`/api/auth/logout`

- [x] 文章：支持新增/编辑/删除，草稿与发布状态区分，前台可查看发布列表与详情  
  证据：`/api/content/article`、`/api/content/article/list`（支持 `status/pageNum/pageSize`）、`/api/content/article/{id}`、`DELETE /api/content/article/{id}`

- [x] 分类标签：可维护分类与标签，文章可关联标签  
  证据：`/api/content/category`、`/api/content/category/list`、`/api/content/tag`、`/api/content/tag/list`，以及 `article_tag` 表关联保存

- [x] 评论：支持评论与回复，支持后台审核/隐藏与删除  
  证据：`/api/comment`（支持 `parentId/replyToUserId`）、`/api/comment/article/{articleId}`、`/api/comment/admin/list`（支持分页筛选）、`/api/comment/admin/{id}/audit`、`/api/comment/admin/{id}`

- [x] Markdown：文章以 `content_md` 存储并在前台详情展示  
  证据：`article.content_md` 字段；`blog-web/src/views/ArticleDetailView.vue`

- [x] 后台管理：具备登录、文章管理、分类管理、标签管理、评论审核主链路  
  证据：`blog-admin/src/views/LoginView.vue`、`blog-admin/src/views/DashboardView.vue`

- [x] 可运行性：后端编译通过，前后端构建通过  
  证据：`mvn -DskipTests compile` 成功；`blog-web` 与 `blog-admin` 的 `npm run build` 成功

---

## 五、当前遗留与下一步建议（第一阶段收尾）

1. 安全与稳定性增强
- 验证码发送频率限制（同手机号/同IP）
- 接口参数边界校验补强
- 管理员初始密码改造（避免默认值）

2. 交互完善
- 后台表单校验与更细粒度错误提示
- 评论树形展示优化（当前已支持回复数据写入）

3. 测试完善
- 增加后端单元测试与集成测试
- 增加前端关键页面的端到端回归脚本
