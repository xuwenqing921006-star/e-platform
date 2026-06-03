# 央行 E 平台启动与交付检查

> 本文档只记录字段名、端口、命令和验证边界，不记录真实密码、JWT Secret、Token 或任何可还原片段。

## 1. 组件与端口

| 组件 | 路径 | Agent 验证端口 | 用户验收端口 | 说明 |
| --- | --- | --- | --- | --- |
| H5 公众号端 | `frontend/` | `5199` | `5175` | Vue 3 + Vite，默认真实接口路径，保留显式 Mock 测试模式 |
| 若依后端 | `backend/ruoyi-admin` | `8099` | `8003` | Spring Boot 3，`health` profile 可不连接数据库短启动 |
| 若依后台 | `ruoyi-ui/` | `5199` | `5175` | Vue 2 + Element UI，经 `/dev-api` 或 `/prod-api` 代理到后端 |
| MySQL | 外部服务 | `3306` | 按用户环境 | 真实业务 profile 必需 |
| Redis | 外部服务 | `6379` | 按用户环境 | 若依登录态、验证码、限流和缓存能力依赖 |

## 2. 环境要求

- JDK 17+，使用 `java -version` 和 `javac -version` 确认。
- Node.js 与 npm，依赖已分别落在 `frontend/node_modules` 与 `ruoyi-ui/node_modules`。
- MySQL 8：真实业务 profile 必需；仓库不提供已配置密码的数据库。
- Redis：真实业务 profile 建议启动；`health` profile 不需要 Redis。
- 本地附件目录：由 `APP_STORAGE_ROOT` 指向，部署时纳入备份。
- Docker：项目根目录提供 `docker-compose.yml`，可一键启动本地 MySQL 8 与 Redis 7。compose 使用环境变量插值和 local-only placeholder，不在文档或 `.sdd/**` 中记录真实密码。

## 3. 配置字段

### 3.1 H5 `frontend`

`frontend/.env` 和 `frontend/.env.example` 应保持：

```env
VITE_API_BASE_URL=/api
VITE_USE_MOCK=false
VITE_BACKEND_PROXY_TARGET=http://localhost:8099
```

说明：

- `VITE_API_BASE_URL` 固定使用相对路径 `/api`，避免本地 CORS。
- `VITE_USE_MOCK=false` 是默认交付路径；测试文件需要 Mock 时使用 `frontend/.env.test`。
- `VITE_BACKEND_PROXY_TARGET` 在用户验收时临时改到 `http://localhost:8003`。

### 3.2 若依后端 `backend`

真实业务 profile 使用以下字段，值只放在本地环境或 `.env` 类本地 secret 文件中：

```env
DB_URL=jdbc:mysql://localhost:3306/central_bank_e_platform?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=GMT%2B8
DB_USERNAME=<mysql-user>
DB_PASSWORD=<mysql-password>
APP_STORAGE_ROOT=<absolute-upload-directory>
APP_JWT_SECRET=<local-secret-not-committed>
```

Redis 使用 `spring.data.redis.host`、`spring.data.redis.port`、`spring.data.redis.database`、`spring.data.redis.password`。当前默认指向 `localhost:6379` 且密码留空；如测试环境 Redis 需要认证，通过本地启动参数或本地配置覆盖，不能提交真实值。

### 3.3 若依后台 `ruoyi-ui`

开发环境：

```env
VUE_APP_BASE_API=/dev-api
VUE_APP_BACKEND_PROXY_TARGET=http://localhost:8099
```

生产构建：

```env
VUE_APP_BASE_API=/prod-api
```

说明：

- `ruoyi-ui/vue.config.js` 会把 `/dev-api` 代理到 `VUE_APP_BACKEND_PROXY_TARGET`，默认 `http://localhost:8099`。
- 生产部署时由 Nginx 将 `/prod-api` 反向代理到若依后端，并按部署规则去掉代理前缀。

## 4. 后端启动

### 4.1 数据库无关健康检查

此路径只验证 Spring Boot 进程、路由和 `/health`，不代表 MySQL/Redis 真实联调。

```powershell
cd backend
.\mvnw.cmd -q -pl ruoyi-admin -am -DskipTests package
java -jar .\ruoyi-admin\target\ruoyi-admin.jar --spring.profiles.active=health --server.port=8099
```

另开终端验证：

```powershell
Invoke-RestMethod http://127.0.0.1:8099/health
```

预期结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "UP"
  }
}
```

### 4.2 真实业务 profile

启动前先准备：

1. MySQL 8 已创建业务库并执行 `backend/sql/ry_20260417.sql`、`backend/sql/quartz.sql`、`backend/central-bank-business/src/main/resources/sql/central_bank_schema_mysql.sql`、`backend/central-bank-business/src/main/resources/sql/central_bank_seed_mysql.sql` 和对应菜单 SQL；使用项目 Docker Compose 时首次启动会按文件挂载顺序自动执行。
2. Redis 已启动并可从后端访问。
3. `APP_STORAGE_ROOT` 指向可读写目录。
4. `APP_JWT_SECRET` 已在本地配置，未提交。

PowerShell 示例：

```powershell
cd backend
$env:DB_URL='jdbc:mysql://localhost:3306/central_bank_e_platform?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=GMT%2B8'
$env:DB_USERNAME='<mysql-user>'
$env:DB_PASSWORD='<mysql-password>'
$env:APP_STORAGE_ROOT='<absolute-upload-directory>'
$env:APP_JWT_SECRET='<local-secret-not-committed>'
java -jar .\ruoyi-admin\target\ruoyi-admin.jar --server.port=8099
```

用户验收端口改为：

```powershell
java -jar .\ruoyi-admin\target\ruoyi-admin.jar --server.port=8003
```

## 5. H5 启动

Agent 验证：

```powershell
cd frontend
npm.cmd run dev -- --host 127.0.0.1 --port 5199
```

用户验收：

```powershell
cd frontend
$env:VITE_BACKEND_PROXY_TARGET='http://localhost:8003'
npm.cmd run dev -- --host 127.0.0.1 --port 5175
```

构建：

```powershell
cd frontend
npm.cmd run typecheck
npm.cmd run lint
npm.cmd run test
npm.cmd run build
```

核心路径：

- `http://127.0.0.1:5199/h5`
- `http://127.0.0.1:5199/h5/contents/<id>`
- `http://127.0.0.1:5199/h5/products/<id>`

## 6. 若依后台启动

Agent 验证：

```powershell
cd ruoyi-ui
$env:VUE_APP_BACKEND_PROXY_TARGET='http://localhost:8099'
$env:port='5199'
npm.cmd run dev
```

用户验收：

```powershell
cd ruoyi-ui
$env:VUE_APP_BACKEND_PROXY_TARGET='http://localhost:8003'
$env:port='5175'
npm.cmd run dev
```

构建：

```powershell
cd ruoyi-ui
npm.cmd run build:prod
```

核心路径：

- `http://127.0.0.1:5199/login`
- `http://127.0.0.1:5199/index`
- 内容管理、金融产品、Excel 导入、账号管理、操作日志、修改密码等菜单需在真实 MySQL 菜单 SQL 初始化后验证。

## 7. Docker 一键启动 MySQL / Redis

项目根目录提供 `docker-compose.yml`，用于本地启动 MySQL 8 和 Redis 7：

```powershell
cd C:\Users\31333\Desktop\vibecoding\SDD_V7_1\Projects_Repo\central-bank-e-platform

# 可选：如需替换 compose 内 local-only placeholder，只在当前终端或本地 .env 中设置。
$env:MYSQL_ROOT_PASSWORD='<local-only-root-password>'
$env:MYSQL_DATABASE='central_bank_e_platform'
$env:MYSQL_USER='central_bank'
$env:MYSQL_PASSWORD='<local-only-db-password>'

docker compose config
docker compose up -d mysql redis
```

首次初始化顺序由 compose 的 `/docker-entrypoint-initdb.d/NN-*.sql` 挂载名确定：

1. `backend/sql/ry_20260417.sql`
2. `backend/sql/quartz.sql`
3. `backend/central-bank-business/src/main/resources/sql/central_bank_schema_mysql.sql`
4. `backend/central-bank-business/src/main/resources/sql/central_bank_seed_mysql.sql`
5. `central_bank_content_menu.sql`
6. `central_bank_product_menu.sql`
7. `central_bank_account_menu.sql`
8. `central_bank_audit_log_menu.sql`

Docker 启动后验证：

```powershell
docker compose ps
docker compose exec redis redis-cli ping
$mysqlRootPassword = $env:MYSQL_ROOT_PASSWORD
if (-not $mysqlRootPassword) { $mysqlRootPassword = 'central-bank-local-root-password' }
"show databases;" | docker compose exec -T mysql mysql -uroot "-p$mysqlRootPassword" -N
"show tables; select count(*) from sys_user; select count(*) from cb_financial_product;" | docker compose exec -T mysql mysql -uroot "-p$mysqlRootPassword" -N central_bank_e_platform
```

后端连接本地 Docker MySQL 时：

```powershell
$env:DB_URL='jdbc:mysql://localhost:3306/central_bank_e_platform?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=GMT%2B8'
$env:DB_USERNAME='central_bank'
$env:DB_PASSWORD='<local-only-db-password>'
```

- Docker：仅管理本项目 compose project 下的 `mysql`、`redis` 服务和命名卷；不要删除用户其他容器或卷。
- MySQL：完整业务验收必须连真实 MySQL 8。Docker 首次启动会自动执行若依基础 SQL、Quartz、央行业务 schema、seed 和菜单 SQL；自动化测试中的 H2 fallback 只证明 mapper/service/contract 兼容，不证明真实 MySQL 已联调。
- Redis：`health` profile 不依赖 Redis；真实登录、验证码、在线用户、重复提交、限流和缓存相关路径依赖 Redis。若 Redis 未启动，不要宣称后台完整链路可用。
- 附件目录：自动测试可用临时目录；真实部署必须把 `APP_STORAGE_ROOT` 指向持久化目录并纳入备份。
- SAFE：固定地址为 `http://zwfw.safe.gov.cn/asone/`；无网络时只验证 H5 链接地址和详情页隐藏规则，不声明外站可达。

## 8. 验证步骤

### 8.1 静态与构建门禁

```powershell
Get-Content .sdd\tasks.json -Raw | ConvertFrom-Json | Out-Null
node scripts/audit-mock-exit.mjs
docker compose config
docker compose up -d mysql redis
docker compose exec redis redis-cli ping
$mysqlRootPassword = $env:MYSQL_ROOT_PASSWORD
if (-not $mysqlRootPassword) { $mysqlRootPassword = 'central-bank-local-root-password' }
"show tables; select count(*) from sys_user; select count(*) from cb_financial_product;" | docker compose exec -T mysql mysql -uroot "-p$mysqlRootPassword" -N central_bank_e_platform

cd frontend
npm.cmd run typecheck
npm.cmd run lint
npm.cmd run test
npm.cmd run build

cd ..\backend
.\mvnw.cmd -q -pl central-bank-business -am test
.\mvnw.cmd -q -pl ruoyi-admin -am -DskipTests package

cd ..\ruoyi-ui
npm.cmd run build:prod
```

### 8.2 短启动健康检查

```powershell
cd backend
java -jar .\ruoyi-admin\target\ruoyi-admin.jar --spring.profiles.active=health --server.port=8099
Invoke-RestMethod http://127.0.0.1:8099/health
```

通过后停止 Java 进程。

### 8.3 轻量真实路径检查

在后端 `health` profile 启动时：

1. 启动 H5 dev server，打开 `/h5`，确认页面渲染并向 `/api/public/*` 真实路径发起请求；由于 health profile 不提供业务 API，业务请求可能返回错误，这是预期的 fallback 边界。
2. 启动 `ruoyi-ui` dev server，打开 `/login`，确认页面渲染、无默认若依品牌和默认账号密码提示；完整登录需要真实 MySQL + Redis。
3. 若已有真实 MySQL/Redis 和本地 secret 配置，再执行登录、概览、内容、产品、导入、账号、日志和 H5 详情全链路；否则报告“真实 MySQL/Redis 未自动验证”。

## 9. 交付前脱敏要求

- `docs/**`、`.sdd/**`、README、测试报告和日志摘要只允许出现字段名或占位符。
- 不允许出现真实 `DB_PASSWORD`、`APP_JWT_SECRET`、JWT、Bearer Token、API Key 或可还原片段。
- `node scripts/audit-mock-exit.mjs` 会检查 `docs/startup.md` 是否存在、是否覆盖启动边界，并扫描常见敏感片段。
