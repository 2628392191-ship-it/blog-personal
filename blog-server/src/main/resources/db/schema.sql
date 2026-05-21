CREATE DATABASE IF NOT EXISTS blog_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE blog_system;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(20) NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) DEFAULT NULL,
    avatar VARCHAR(255) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    last_login_time DATETIME DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_user_phone (phone),
    UNIQUE KEY uk_sys_user_username (username),
    KEY idx_sys_user_email (email)
);

CREATE TABLE IF NOT EXISTS sms_code_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(20) NOT NULL,
    biz_type VARCHAR(20) NOT NULL,
    code_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    sent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status TINYINT NOT NULL DEFAULT 0,
    request_ip VARCHAR(50) DEFAULT NULL,
    KEY idx_sms_phone_biz (phone, biz_type),
    KEY idx_sms_expires_at (expires_at)
);

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(50) NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(255) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_role_code (role_code)
);

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    perm_code VARCHAR(100) NOT NULL,
    perm_name VARCHAR(100) NOT NULL,
    perm_type VARCHAR(20) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    path VARCHAR(255) DEFAULT NULL,
    component VARCHAR(255) DEFAULT NULL,
    icon VARCHAR(100) DEFAULT NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_perm_code (perm_code)
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_perm (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_category_slug (slug)
);

CREATE TABLE IF NOT EXISTS tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    color VARCHAR(20) DEFAULT NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_tag_slug (slug)
);

CREATE TABLE IF NOT EXISTS article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    author_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(500) DEFAULT NULL,
    content_md LONGTEXT NOT NULL,
    content_html LONGTEXT DEFAULT NULL,
    cover_url VARCHAR(255) DEFAULT NULL,
    category_id BIGINT DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    is_top TINYINT NOT NULL DEFAULT 0,
    is_comment_enabled TINYINT NOT NULL DEFAULT 1,
    view_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    collect_count INT NOT NULL DEFAULT 0,
    publish_time DATETIME DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_article_category_id (category_id),
    KEY idx_article_status (status),
    KEY idx_article_publish_time (publish_time),
    KEY idx_article_is_top (is_top)
);

CREATE TABLE IF NOT EXISTS article_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    UNIQUE KEY uk_article_tag (article_id, tag_id)
);

CREATE TABLE IF NOT EXISTS article_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    UNIQUE KEY uk_article_user (article_id, user_id)
);

CREATE TABLE IF NOT EXISTS comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT NOT NULL DEFAULT 0,
    reply_to_user_id BIGINT DEFAULT NULL,
    content VARCHAR(1000) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    ip VARCHAR(50) DEFAULT NULL,
    user_agent VARCHAR(500) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_comment_article_id (article_id),
    KEY idx_comment_parent_id (parent_id)
);

INSERT INTO sys_role (role_code, role_name, status, remark)
VALUES ('ADMIN', '管理员', 1, '系统管理员'), ('USER', '普通用户', 1, '普通注册用户')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

INSERT INTO sys_permission (perm_code, perm_name, perm_type, parent_id, path, component, icon, sort, status)
VALUES ('admin:user:list', '后台用户列表', 'api', 0, '/api/admin/users', '', '', 1, 1),
       ('comment:admin:list', '后台评论列表', 'api', 0, '/api/comment/admin/list', '', '', 2, 1),
       ('comment:admin:audit', '后台评论审核', 'api', 0, '/api/comment/admin/{id}/audit', '', '', 3, 1),
       ('comment:admin:delete', '后台评论删除', 'api', 0, '/api/comment/admin/{id}', '', '', 4, 1)
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name), status = VALUES(status);

INSERT INTO category (name, slug, description, sort, status, deleted)
VALUES ('前端工程', 'frontend-engineering', '围绕 Vue、React 与现代前端体系的实践记录', 1, 1, 0),
       ('后端架构', 'backend-architecture', '服务设计、接口治理与系统演进', 2, 1, 0),
       ('数据库札记', 'database-notes', 'MySQL、索引、事务与数据建模', 3, 1, 0),
       ('开发随笔', 'developer-notes', '效率工具、调试经验与工程反思', 4, 1, 0)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), sort = VALUES(sort), status = VALUES(status), deleted = VALUES(deleted);

INSERT INTO tag (name, slug, color, sort, status, deleted)
VALUES ('Vue 3', 'vue-3', '#42b883', 1, 1, 0),
       ('Spring Boot', 'spring-boot', '#6db33f', 2, 1, 0),
       ('MySQL', 'mysql', '#00758f', 3, 1, 0),
       ('架构设计', 'architecture-design', '#7d7bff', 4, 1, 0),
       ('调试复盘', 'debug-retrospective', '#f59e0b', 5, 1, 0)
ON DUPLICATE KEY UPDATE name = VALUES(name), color = VALUES(color), sort = VALUES(sort), status = VALUES(status), deleted = VALUES(deleted);

INSERT INTO sys_user (phone, username, password, nickname, status, deleted)
VALUES ('13800000000', 'admin', '21232f297a57a5a743894a0e4a801fc3', '超级管理员', 1, 0)
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), status = VALUES(status), deleted = VALUES(deleted);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
         JOIN sys_role r ON r.role_code = 'ADMIN'
WHERE u.username = 'admin'
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
         JOIN sys_permission p ON p.perm_code IN ('admin:user:list', 'comment:admin:list', 'comment:admin:audit', 'comment:admin:delete')
WHERE r.role_code = 'ADMIN'
ON DUPLICATE KEY UPDATE permission_id = VALUES(permission_id);

-- 增量 DDL（已有数据库执行）
-- ALTER TABLE article ADD COLUMN content_html LONGTEXT DEFAULT NULL AFTER content_md;
