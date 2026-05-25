# 个人博客系统整体架构与数据库设计

# 一、项目定位

构建一个：

> 前后端分离 + 支持 AI 扩展 + 可长期迭代的个人博客系统

项目目标：

- 支持长期维护
- 支持 AI 功能扩展
- 可用于面试项目展示
- 具备真实互联网项目架构
- 后期可升级为 CMS / AI 知识库平台

---

# 二、系统整体架构

```text
                ┌─────────────┐
                │   前端 Vue   │
                └──────┬──────┘
                       │ HTTP
                ┌──────▼──────┐
                │ Spring Boot │
                └──────┬──────┘
         ┌─────────────┼─────────────┐
         │             │             │
    ┌────▼────┐   ┌────▼────┐   ┌────▼────┐
    │ MySQL   │   │ Redis   │   │ MinIO   │
    └─────────┘   └─────────┘   └─────────┘
```

后期可扩展：

```text
AI服务
Elasticsearch
RabbitMQ
Docker
Nginx
```

---

# 三、推荐技术栈

# 前端技术栈

| 技术 | 说明 |
|---|---|
| Vue3 | 前端框架 |
| TypeScript | 类型安全 |
| Vite | 构建工具 |
| Pinia | 状态管理 |
| Vue Router | 路由管理 |
| Element Plus | 后台 UI |
| TailwindCSS | 前台样式 |
| Axios | 网络请求 |
| Markdown 编辑器 | md-editor-v3 |

---

# 后端技术栈

| 技术 | 说明 |
|---|---|
| Spring Boot | 后端核心框架 |
| MyBatis Plus | ORM 框架 |
| MySQL | 数据库 |
| Redis | 缓存 |
| JWT | 登录鉴权 |
| Sa-Token / Spring Security | 权限认证 |
| Lombok | 简化代码 |
| Knife4j | API 文档 |

---

# 部署技术

| 技术 | 说明 |
|---|---|
| Linux | 云服务器 |
| Docker | 容器化部署 |
| Nginx | 反向代理 |
| MinIO | 文件存储 |

---

# 四、系统模块设计

# 1. 用户与认证模块

负责登录、权限、身份认证。

## 功能列表

- 用户登录
- 用户退出
- JWT 鉴权
- 用户信息维护
- 修改密码
- 角色权限控制

---

# 2. 文章内容模块

博客核心模块。

## 功能列表

- 发布文章
- 编辑文章
- 删除文章
- Markdown 编辑
- 文章封面
- 草稿箱
- 发布状态管理
- 阅读量统计
- 置顶文章
- 点赞与收藏

---

# 3. 分类与标签模块

用于内容组织。

## 功能列表

- 分类管理
- 标签管理
- 分类筛选
- 标签筛选
- 文章标签关联

---

# 4. 评论模块

实现博客互动。

## 功能列表

- 一级评论
- 评论回复
- 评论审核
- 评论开关
- 敏感词过滤（后期）

---

# 5. 文件与图片模块

用于管理博客资源。

## 功能列表

- 图片上传
- 文件上传
- 文件管理
- 图床支持
- MinIO/OSS扩展

---

# 6. 后台管理模块

后台系统核心。

## 功能列表

- 文章管理
- 分类管理
- 标签管理
- 评论管理
- 用户管理
- 系统配置

---

# 7. 系统配置模块

动态管理网站配置。

## 功能列表

- 网站标题
- 网站描述
- 首页公告
- SEO 配置
- 是否允许评论
- 是否开放注册
- AI 开关

---

# 8. 日志与统计模块

用于系统审计与数据分析。

## 功能列表

- 登录日志
- 操作日志
- 阅读统计
- 访问统计
- 后台行为审计

---

# 五、扩展模块设计

# 1. 搜索模块

## 功能列表

- 标题搜索
- 内容搜索
- 标签搜索
- 分类搜索

## 技术方案

第一阶段：MySQL LIKE

第二阶段：Elasticsearch

---

# 2. AI 扩展模块

## 功能列表

- AI 摘要生成
- AI 标签生成
- AI 问答助手
- AI 写作辅助

---

# 3. 通知模块

## 功能列表

- 评论通知
- 邮件通知
- 系统通知

---

# 六、推荐项目结构

```text
blog-system
├── blog-web         前台博客
├── blog-admin       后台管理
├── blog-server      后端服务
├── blog-common      公共模块
└── docs             项目文档
```

---

# 七、数据库设计

# 1. 用户表：sys_user

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| username | varchar(50) | 用户名 |
| password | varchar(255) | 密码 |
| nickname | varchar(50) | 昵称 |
| avatar | varchar(255) | 头像 |
| email | varchar(100) | 邮箱 |
| phone | varchar(20) | 手机号 |
| status | tinyint | 状态 |
| last_login_time | datetime | 最后登录时间 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |
| deleted | tinyint | 逻辑删除 |

索引建议：

- username 唯一索引
- email 普通索引

---

# 2. 角色表：sys_role

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| role_code | varchar(50) | 角色编码 |
| role_name | varchar(50) | 角色名称 |
| status | tinyint | 状态 |
| remark | varchar(255) | 备注 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |
| deleted | tinyint | 逻辑删除 |

---

# 3. 权限表：sys_permission

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| perm_code | varchar(100) | 权限标识 |
| perm_name | varchar(100) | 权限名称 |
| perm_type | varchar(20) | menu/button/api |
| parent_id | bigint | 父权限 |
| path | varchar(255) | 菜单路径 |
| component | varchar(255) | 前端组件路径 |
| icon | varchar(100) | 图标 |
| sort | int | 排序 |
| status | tinyint | 状态 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

---

# 4. 用户角色关联表：sys_user_role

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| user_id | bigint | 用户ID |
| role_id | bigint | 角色ID |

---

# 5. 角色权限关联表：sys_role_permission

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| role_id | bigint | 角色ID |
| permission_id | bigint | 权限ID |

---

# 6. 文章表：article

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| author_id | bigint | 作者ID |
| title | varchar(200) | 标题 |
| summary | varchar(500) | 摘要 |
| content_md | longtext | Markdown 内容 |
| content_html | longtext | HTML 内容 |
| cover_url | varchar(255) | 封面图 |
| category_id | bigint | 分类ID |
| status | tinyint | 状态 |
| is_top | tinyint | 是否置顶 |
| is_comment_enabled | tinyint | 是否允许评论 |
| view_count | int | 阅读量 |
| like_count | int | 点赞量 |
| collect_count | int | 收藏量 |
| publish_time | datetime | 发布时间 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |
| deleted | tinyint | 逻辑删除 |

索引建议：

- category_id
- status
- publish_time
- is_top

---

# 7. 分类表：category

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| name | varchar(50) | 分类名称 |
| slug | varchar(100) | URL别名 |
| description | varchar(255) | 描述 |
| sort | int | 排序 |
| status | tinyint | 状态 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |
| deleted | tinyint | 逻辑删除 |

---

# 8. 标签表：tag

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| name | varchar(50) | 标签名称 |
| slug | varchar(100) | URL别名 |
| color | varchar(20) | 标签颜色 |
| sort | int | 排序 |
| status | tinyint | 状态 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |
| deleted | tinyint | 逻辑删除 |

---

# 9. 文章标签关联表：article_tag

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| article_id | bigint | 文章ID |
| tag_id | bigint | 标签ID |

---

# 10. 评论表：comment

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| article_id | bigint | 文章ID |
| user_id | bigint | 评论用户ID |
| parent_id | bigint | 父评论ID |
| reply_to_user_id | bigint | 回复目标用户ID |
| content | varchar(1000) | 评论内容 |
| status | tinyint | 审核状态 |
| ip | varchar(50) | IP 地址 |
| user_agent | varchar(500) | 浏览器信息 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |
| deleted | tinyint | 逻辑删除 |

---

# 11. 文件表：file_record

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| file_name | varchar(255) | 文件名 |
| file_url | varchar(500) | 文件访问地址 |
| file_path | varchar(500) | 存储路径 |
| file_type | varchar(50) | 文件类型 |
| file_size | bigint | 文件大小 |
| storage_type | varchar(20) | 存储类型 |
| md5 | varchar(64) | 文件指纹 |
| uploader_id | bigint | 上传人 |
| created_at | datetime | 创建时间 |

---

# 12. 系统配置表：sys_config

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| config_key | varchar(100) | 配置键 |
| config_value | text | 配置值 |
| config_type | varchar(50) | 配置类型 |
| remark | varchar(255) | 备注 |
| updated_at | datetime | 更新时间 |

配置示例：

```text
site.title
site.description
site.allow_register
site.allow_comment
ai.enabled
ai.api_key
```

---

# 13. 操作日志表：operation_log

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| user_id | bigint | 操作人 |
| module | varchar(50) | 模块 |
| action | varchar(100) | 行为 |
| content | varchar(500) | 操作描述 |
| request_data | text | 请求数据 |
| ip | varchar(50) | IP |
| user_agent | varchar(500) | 浏览器信息 |
| created_at | datetime | 创建时间 |

---

# 14. 登录日志表：login_log

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| user_id | bigint | 用户ID |
| username | varchar(50) | 用户名 |
| login_status | tinyint | 登录状态 |
| login_type | varchar(20) | 登录方式 |
| ip | varchar(50) | IP |
| user_agent | varchar(500) | 浏览器信息 |
| fail_reason | varchar(255) | 失败原因 |
| created_at | datetime | 登录时间 |

---

# 15. 访问统计表：visit_log

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| article_id | bigint | 文章ID |
| visitor_id | varchar(100) | 访客标识 |
| ip | varchar(50) | IP |
| referer | varchar(500) | 来源页 |
| user_agent | varchar(500) | 浏览器信息 |
| visit_time | datetime | 访问时间 |

---

# 16. AI任务记录表：ai_task_log

| 字段 | 类型 | 说明 |
|---|---|---|
| id | bigint | 主键 |
| task_type | varchar(50) | 任务类型 |
| source_id | bigint | 来源ID |
| input_text | longtext | 输入内容 |
| output_text | longtext | 输出内容 |
| status | tinyint | 状态 |
| created_at | datetime | 创建时间 |

---

# 八、推荐开发阶段

# 第一阶段（MVP）

## 核心功能

- 登录注册
- 文章 CRUD
- 分类标签
- 评论系统
- Markdown 编辑器
- 后台管理

## 周期

2~3 周

---

# 第二阶段（系统增强）

## 功能

- Redis 缓存
- 文件上传
- Docker 部署
- 日志系统
- 权限系统

## 周期

2 周

---

# 第三阶段（高级功能）

## 功能

- Elasticsearch 搜索
- RabbitMQ
- 邮件系统
- CDN 优化

## 周期

2~4 周

---

# 第四阶段（AI模块）

## 功能

- AI 摘要
- AI 标签生成
- AI 问答助手
- RAG 知识库

---

# 九、推荐开发顺序

正确顺序：

1. 数据库设计
2. 后端接口开发
3. 前端页面开发
4. 后台管理开发
5. Redis 与 Docker
6. AI 功能扩展

---

# 十、项目未来升级方向

该项目后期可升级为：

- AI 知识库系统
- CMS 内容管理系统
- 技术社区平台
- AI 写作平台
- 个人品牌网站

