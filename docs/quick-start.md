# blog-system 第一阶段本地联调说明

## 1. 准备数据库
1. 创建 MySQL 数据库并执行：
   - `blog-server/src/main/resources/db/schema.sql`
2. 默认会初始化：
   - 管理员账号：`admin`
   - 手机号：`13800000000`
   - 角色：`ADMIN`

## 2. 启动后端
在 `blog-server` 目录执行：

```bash
mvn spring-boot:run
```

默认端口：`8080`

## 3. 认证接口联调
### 3.1 发送验证码（Mock）
```bash
curl -X POST "http://localhost:8080/api/auth/sms-code" \
  -H "Content-Type: application/json" \
  -d '{"phone":"13900000000","bizType":"REGISTER"}'
```
返回里会有 `mockCode`。

### 3.2 注册
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"phone":"13900000000","code":"这里填mockCode"}'
```

### 3.3 登录
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"phone":"13900000000","code":"这里填mockCode"}'
```

记下返回中的 `token`。

### 3.4 获取当前用户
```bash
curl "http://localhost:8080/api/auth/me" \
  -H "Authorization: 上一步token"
```

## 4. 内容接口联调（示例）
```bash
curl -X POST "http://localhost:8080/api/content/category" \
  -H "Content-Type: application/json" \
  -H "Authorization: token" \
  -d '{"name":"后端","slug":"backend"}'
```

## 5. 评论与后台接口联调（示例）
```bash
curl "http://localhost:8080/api/comment/admin/list" \
  -H "Authorization: 管理员token"
```
