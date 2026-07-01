# 央行 E 平台部署注意事项

本文档用于测试环境、预生产环境和正式生产环境部署前检查。文档只记录配置字段名、流程和风险边界，不记录真实密码、JWT Secret、Token 或任何可还原片段。

## 1. 当前结论

项目已经具备容器化部署基础，可以先部署到测试或预生产环境演练；正式生产上线前仍必须完成域名、HTTPS、生产密钥、备份、账号清理和全链路验收。

不要把 cpolar、Vite dev server、Vue CLI dev server 当作正式部署方案。cpolar 只适合临时演示或验收，正式部署应使用域名、HTTPS、Nginx 和生产构建产物。

## 2. 部署组成

| 组件 | 路径 | 产物 | 说明 |
| --- | --- | --- | --- |
| H5 公众号端 | `frontend/` | `frontend/dist/` | 对外访问路径默认 `/h5/` |
| 若依后台 | `ruoyi-ui/` | `ruoyi-ui/dist/` | 对外访问路径默认 `/admin/` |
| 若依后端 | `backend/` | `ruoyi-admin.jar` 或后端镜像 | 提供后台接口、H5 公共接口、登录鉴权、附件访问 |
| MySQL | Docker 或服务器服务 | 持久化数据卷/实例 | 保存系统用户、菜单、业务数据 |
| Redis | Docker 或服务器服务 | 持久化数据卷/实例 | 支撑登录态、缓存、重复提交等 |
| Nginx | `deploy/nginx.conf` | 反向代理配置 | 统一承载静态资源和 API 代理 |

当前生产路由约定：

| 外部路径 | 目标 |
| --- | --- |
| `/h5/` | H5 静态资源 |
| `/admin/` | 后台静态资源 |
| `/api/` | H5 API，代理到后端并保留 `/api` 前缀 |
| `/prod-api/` | 后台 API，代理到后端并去掉 `/prod-api` 前缀 |
| `/` | 重定向到 `/h5/` |

## 3. 部署文件

| 文件 | 用途 |
| --- | --- |
| `.env.example` | 生产 Docker Compose 环境变量模板，只放占位符 |
| `backend/.env.example` | 后端 jar 直启环境变量模板，只放占位符 |
| `frontend/.env.example` | H5 构建和本地联调环境变量模板 |
| `ruoyi-ui/.env.example` | 若依后台构建和本地联调环境变量模板 |
| `docker-compose.prod.yml` | 生产/预生产参考编排，包含 MySQL、Redis、后端、Nginx |
| `deploy/nginx.conf` | Nginx 静态资源与 API 反向代理示例 |
| `backend/Dockerfile` | 后端运行镜像 |
| `backend/ruoyi-admin/src/main/resources/application-prod.yml` | 生产 profile 覆盖开发配置 |
| `docs/startup.md` | 本地启动、开发验收和构建命令 |

`docker-compose.yml` 仅用于本地 MySQL/Redis 开发，不作为生产部署配置。

## 4. 正式部署前必须确认

1. 域名已经确定，H5 和后台访问路径明确。
2. HTTPS 证书已准备，公网只开放 80/443。
3. MySQL、Redis、上传目录都有持久化和备份策略。
4. 生产密钥、数据库密码、Redis 密码、Druid 密码已经通过服务器环境变量或服务器本地 `.env` 提供。
5. 默认账号、测试账号、弱密码已清理或改强密码。
6. 真实 MySQL/Redis 环境已经完成后台和 H5 全链路验收。
7. 有回滚方案，包括旧镜像/旧 jar、旧静态资源、数据库备份和上传目录备份。

## 5. 必填环境变量

生产环境必须配置以下字段。可以放在服务器环境变量、部署平台 Secret、或仅服务器可读的 `.env` 文件中；不要提交到 Git。

```env
MYSQL_ROOT_PASSWORD=<required>
MYSQL_DATABASE=central_bank_e_platform
MYSQL_USER=central_bank
MYSQL_PASSWORD=<required>
REDIS_PASSWORD=<required>
APP_JWT_SECRET=<required>
DRUID_LOGIN_USERNAME=<required>
DRUID_LOGIN_PASSWORD=<required>
REFERER_ALLOWED_DOMAINS=<your-domain>
HTTP_PORT=80
```

仓库已提供 `.env.example`、`backend/.env.example`、`frontend/.env.example` 和 `ruoyi-ui/.env.example`。正式部署时只提交模板文件；真实 `.env`、`.env.production`、`.env.development` 等文件必须留在服务器或本机，并继续由 `.gitignore` 排除。

后端容器运行时会使用：

```env
DB_URL=jdbc:mysql://mysql:3306/central_bank_e_platform?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=GMT%2B8
DB_USERNAME=<mysql-user>
DB_PASSWORD=<mysql-password>
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_DATABASE=0
REDIS_PASSWORD=<redis-password>
APP_STORAGE_ROOT=/app/uploads
APP_JWT_SECRET=<jwt-secret>
APP_JWT_EXPIRE_MINUTES=30
```

如果前后端不在同一个域名下，还需要设置：

```env
APP_CORS_ALLOWED_ORIGINS=<https-origin-list>
REFERER_ALLOWED_DOMAINS=<domain-list>
```

同一个域名下使用 `/h5/`、`/admin/`、`/api/`、`/prod-api/` 时，通常不需要额外 CORS。

## 6. 构建顺序

在项目根目录执行。生产默认路径为 H5 `/h5/`、后台 `/admin/`。如改成独立域名根路径，需要同步调整 `VITE_PUBLIC_BASE` 和 `VUE_APP_PUBLIC_PATH`。

```powershell
cd frontend
npm.cmd run typecheck
npm.cmd run lint
npm.cmd run test
npm.cmd run build

cd ..\ruoyi-ui
npm.cmd run build:prod

cd ..\backend
.\mvnw.cmd -q -pl ruoyi-admin -am -DskipTests package
```

构建前确认：

- `frontend/.env` 中 `VITE_PUBLIC_BASE=/h5/`、`VITE_API_BASE_URL=/api`。
- `ruoyi-ui/.env.production` 中 `VUE_APP_PUBLIC_PATH=/admin/`、`VUE_APP_BASE_API=/prod-api`。
- 后端生产环境变量来自服务器 `.env` 或部署平台 Secret，不写入 Git。

构建完成后确认：

```text
frontend/dist/
ruoyi-ui/dist/
backend/ruoyi-admin/target/ruoyi-admin.jar
```

## 7. Docker Compose 部署流程

先在服务器上准备只供部署使用的环境变量或 `.env` 文件，再执行：

```powershell
docker compose -f docker-compose.prod.yml config
docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml ps
```

`docker compose -f docker-compose.prod.yml config` 如果提示缺少 `MYSQL_PASSWORD`、`REDIS_PASSWORD`、`APP_JWT_SECRET` 等变量，说明还不能部署。不要用空密码绕过。

检查服务：

```powershell
docker compose -f docker-compose.prod.yml logs -f backend
docker compose -f docker-compose.prod.yml exec redis redis-cli -a <redis-password> ping
docker compose -f docker-compose.prod.yml exec mysql mysql -uroot -p -e "show databases;"
```

访问入口：

```text
https://<domain>/h5/
https://<domain>/admin/
```

## 8. 数据库注意事项

`docker-compose.prod.yml` 会在 MySQL 首次创建空数据卷时自动执行初始化 SQL：

1. `backend/sql/ry_20260417.sql`
2. `backend/sql/quartz.sql`
3. `backend/central-bank-business/src/main/resources/sql/central_bank_schema_mysql.sql`
4. `backend/central-bank-business/src/main/resources/sql/central_bank_seed_mysql.sql`
5. `central_bank_content_menu.sql`
6. `central_bank_product_menu.sql`
7. `central_bank_account_menu.sql`
8. `central_bank_audit_log_menu.sql`
9. `central_bank_admin_menu_cleanup.sql`

注意：

- 初始化 SQL 只在空数据卷第一次启动时执行。
- 已上线后不要删除 `mysql_data` 卷，否则会丢失正式数据。
- 后续新增字段、菜单或初始化数据，应使用迁移 SQL，不要重新初始化整个库。
- 正式上线前至少完成一次备份和恢复演练。

## 9. 上传文件与附件

后端上传目录由 `APP_STORAGE_ROOT` 控制。容器部署时对应 `app_uploads` 卷。

上线前确认：

- 上传目录可写。
- 上传目录不会随容器重建丢失。
- 上传目录纳入备份。
- Nginx `client_max_body_size` 与后端上传大小一致或更大。
- H5 文章图片、后台富文本图片、附件下载在重启后仍可访问。

## 10. 安全收口清单

上线前必须完成：

- MySQL `3306` 不暴露公网。
- Redis `6379` 不暴露公网，并设置强密码。
- Druid 管理台设置强账号密码；不需要时关闭。
- Swagger/OpenAPI 在生产环境关闭。
- 后端以 `druid,prod` profile 启动，不使用开发 profile。
- `APP_JWT_SECRET` 使用强随机值。
- 默认账号、测试账号、弱密码全部清理。
- 只保留业务需要的菜单和角色权限。
- HTTPS 证书有效，HTTP 自动跳转 HTTPS。
- 不把真实密码、JWT、Token、API Key 写入 `docs/**`、`.sdd/**`、README、日志摘要或 Git 提交。

## 11. 生产验收清单

上线后至少验证：

- H5 首页可打开，县区切换、乡村振兴、金融产品入口正常。
- H5 内容详情图片可展示，点击图片可放大查看。
- H5 银行产品详情经办人和手机号显示正常，手机号可点击拨号。
- 后台登录、退出、修改密码正常。
- 后台右上角用户昵称能跟随账号姓名修改刷新。
- 内容新增、编辑、发布、下架、删除正常。
- 富文本图片不溢出弹窗，H5 可正常展示上传图片。
- 附件上传、下载、重启后访问正常。
- 金融产品新增、编辑、多联系人展示正常。
- 账号管理、权限菜单、操作日志正常。
- 错误密码、无权限、登录失效等错误提示展示真实后端 message。

## 12. 回滚与应急

上线前准备：

- 上一个可用后端镜像或 `ruoyi-admin.jar`。
- 上一个可用 `frontend/dist` 和 `ruoyi-ui/dist`。
- MySQL 上线前备份。
- 上传目录上线前备份。
- 当前生产环境变量备份。

出现问题时优先回滚应用产物；涉及数据库结构变更时，先评估是否需要恢复数据库备份，不要直接删除生产卷。

## 13. 当前仍需人工确认

正式生产前还需要确认：

- 生产域名。
- HTTPS 证书来源和续期方式。
- 服务器规格、磁盘容量和备份目录。
- MySQL/Redis 是使用容器还是云服务。
- 公众号/H5 最终访问域名是否需要备案、白名单或微信侧配置。
- 默认账号和初始化数据是否符合最终运营要求。
