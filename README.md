# Blog System

个人博客系统，前后端分离架构。前端含公开浏览端（blog-web）与管理后台（blog-admin），后端为 Spring Boot 单体服务。

## 项目结构

```
blog-system/
├── blog-server/     Spring Boot 后端 (Java 17 / MyBatis-Plus / Sa-Token)
├── blog-web/        Vue 3 公开博客前端 (游客浏览 + 登录评论)
├── blog-admin/      Vue 3 管理后台 (Element Plus / 模块化管理)
└── README.md
```

## 技术栈

| 层 | 技术 |
|---|---|
| 后端框架 | Spring Boot 3.3 + MyBatis-Plus 3.5 |
| 认证鉴权 | Sa-Token (手机验证码登录 / RBAC) |
| 数据库 | MySQL 8.x |
| 前端 | Vue 3 (Composition API) + Vue Router + Pinia |
| 后台 UI | Element Plus |
| 构建 | Vite |

## 后端模块划分

```
com.blogsystem
├── auth/            认证模块 (验证码 / 注册 / 登录 / 个人信息)
│   ├── controller/  AuthController
│   ├── service/     AuthService
│   ├── dto/         LoginUserVO, ProfileUpdateRequest, ...
│   ├── entity/      SysUser, SysRole, SysUserRole, SmsCodeLog
│   └── mapper/      MyBatis-Plus Mapper
├── admin/           管理端模块
│   ├── controller/  AdminController (用户管理)
│   └── service/     AdminService
├── content/         内容模块 (文章 / 分类 / 标签)
│   ├── controller/  ContentController
│   ├── service/     ContentService
│   ├── dto/         ArticleSaveRequest, CategorySaveRequest, TagSaveRequest
│   ├── entity/      Article, Category, Tag, ArticleTag
│   └── mapper/
├── comment/         评论模块
│   ├── controller/  CommentController
│   ├── service/     CommentService
│   ├── dto/         CommentVO, CommentSaveRequest
│   ├── entity/      Comment
│   └── mapper/
├── security/        权限实现 (Sa-Token StpInterface)
├── config/          配置 (CORS / MyBatis-Plus / Sa-Token)
└── common/          公共 (ApiResponse / GlobalExceptionHandler)
```

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+

### 1. 初始化数据库

执行 `blog-server/src/main/resources/db/schema.sql` 创建表结构和种子数据（管理员账号 + 角色权限）。

### 2. 配置数据库连接

编辑 `blog-server/src/main/resources/application.yml`，修改数据库地址与密码。

### 3. 启动后端

```bash
cd blog-server
mvn compile
mvn exec:java -Dexec.mainClass="com.blogsystem.BlogServerApplication"
```

后端默认运行在 `http://localhost:8080`。

### 4. 启动前端

```bash
# 公开博客
cd blog-web
npm install
npm run dev

# 管理后台
cd blog-admin
npm install
npm run dev
```

blog-web 默认 `http://localhost:5173`，blog-admin 默认 `http://localhost:5174`。

## 功能概览

### 公开博客 (blog-web)

- 游客模式：浏览文章列表与详情，阅读量统计
- 热门阅读：首页按阅读量展示热门文章
- 分类阅读：左侧栏按分类筛选文章
- 标签筛选：右侧栏按标签筛选文章
- 三栏自适配：分类(左) | 内容(中) | 标签(右)，随视口自动缩放
- 注册/登录：手机验证码登录
- 评论互动：登录后查看/发表/回复评论，展示昵称与时间
- 个人信息：`/profile` 编辑昵称和邮箱

### 管理后台 (blog-admin)

- 文章管理：新建/编辑（分类下拉 + 标签多选）/ 删除
- 评论审核：通过 / 隐藏评论
- 分类管理：新建 / 编辑 / 删除
- 标签管理：新建 / 编辑 / 删除
- 用户管理：分页查询 / 启用禁用 / 删除
- 明暗主题切换

## 开发说明

- 验证码开发环境返回 mock 码（6 位数字），无需真实短信服务
- 管理员账号：手机 `13800000000`，通过验证码登录
- 管理端接口需要 ADMIN 角色，前端路由守卫 + 后端 `@SaCheckRole("ADMIN")` 双重校验
- 文章阅读量在进入详情页时 +1，列表页不累加
- 评论列表仅在登录后可见
