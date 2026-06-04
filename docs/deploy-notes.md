# 央行 E 平台部署注意事项

本文档用于上线前检查和部署准备。文档只记录配置字段名、部署步骤和风险边界，不记录真实密码、JWT Secret、Token 或任何可还原片段。

## 1. 推荐部署形态

当前项目由三部分组成：

| 模块 | 路径 | 部署产物 | 说明 |
| --- | --- | --- | --- |
| 若依后端 | `backend/` | `ruoyi-admin.jar` 或后端镜像 | 提供后台接口、H5 公共接口、附件访问、登录鉴权 |
| 若依后台 | `ruoyi-ui/` | `dist/` | 后台管理端静态资源 |
| H5 公众号端 | `frontend/` | `dist/` | 公众号/H5 静态资源 |
| 基础服务 | MySQL / Redis | Docker 或服务器服务 | MySQL 保存业务数据，Redis 支撑登录态和缓存 |

推荐先上线测试环境，再上线生产环境。生产环境建议使用 Nginx 统一接入 HTTPS，并只对外开放 80/443。

## 2. 新增部署文件

本项目已补充以下部署文件：

| 文件 | 用途 |
| --- | --- |
| `backend/ruoyi-admin/src/main/resources/application-prod.yml` | 生产 profile 覆盖开发配置，启动时放在 profile 列表最后 |
| `backend/Dockerfile` | 后端 jar 运行镜像 |
| `deploy/nginx.conf` | Nginx 静态资源与接口反向代理示例 |
| `docker-compose.prod.yml` | 测试/生产参考 compose |

`docker-compose.yml` 仍作为本地开发 MySQL/Redis 使用，`docker-compose.prod.yml` 面向部署参考。

## 3. 上线前必须配置的环境变量

生产环境必须由服务器环境变量、部署平台 Secret、或仅服务器可读的 `.env` 文件提供以下字段：

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

后端容器会使用以下运行字段：

```env
DB_URL=jdbc:mysql://mysql:3306/central_bank_e_platform?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=GMT%2B8
DB_USERNAME=<mysql-user>
DB_PASSWORD=<mysql-password>
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=<redis-password>
APP_STORAGE_ROOT=/app/uploads
APP_JWT_SECRET=<jwt-secret>
```

禁止把真实密码、JWT Secret、Token 写入 `docs/**`、`.sdd/**`、README、测试报告或 Git 提交内容。

## 4. 构建顺序

上线构建建议按以下顺序执行：

默认部署路径为：

```text
H5: /h5/
后台: /admin/
```

已在 `frontend/vite.config.ts` 与 `ruoyi-ui/vue.config.js` 中配置生产资源路径。若改成独立域名根路径部署，需要同步调整 `VITE_PUBLIC_BASE` 与 `VUE_APP_PUBLIC_PATH`。

```powershell
cd C:\Users\31333\Desktop\vibecoding\SDD_V7_1\Projects_Repo\central-bank-e-platform

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

完成后应存在：

```text
frontend/dist/
ruoyi-ui/dist/
backend/ruoyi-admin/target/ruoyi-admin.jar
```

## 5. Nginx 路由约定

当前 `deploy/nginx.conf` 使用以下路由：

| 外部路径 | 目标 |
| --- | --- |
| `/h5/` | H5 公众号端静态资源 |
| `/admin/` | 若依后台静态资源 |
| `/api/` | H5 接口反向代理到后端，保留 `/api` 前缀 |
| `/prod-api/` | 后台接口反向代理到后端，去掉 `/prod-api` 前缀 |
| `/` | 重定向到 `/h5/` |

若后续要改成两个独立域名，也可以保留同样的接口代理规则，只把静态资源拆到不同 server block。

## 6. 数据库注意事项

`docker-compose.prod.yml` 会在 MySQL 首次创建数据卷时执行初始化 SQL。注意：

- 初始化 SQL 只在空数据卷首次启动时执行。
- 生产环境不要删除 `mysql_data` 卷，否则会丢数据。
- 已上线后新增字段或菜单，应使用迁移 SQL，不要重新初始化数据库。
- 上线前必须准备数据库备份策略，至少包含每日备份和恢复演练。

## 7. 附件与上传文件

后端上传目录由 `APP_STORAGE_ROOT` 控制。容器部署时对应 `app_uploads` 卷。

上线前确认：

- 上传目录可写。
- 上传目录不会随容器重建丢失。
- 上传目录纳入备份。
- Nginx `client_max_body_size` 与后端上传大小一致或更大。

## 8. 安全收口

上线前必须完成以下检查：

- MySQL `3306` 不暴露公网。
- Redis `6379` 不暴露公网，并设置密码。
- Druid 管理台设置强账号密码；如不需要，进一步关闭访问。
- Swagger/OpenAPI 默认关闭。
- 后端以 `druid,prod` profile 启动，不使用开发 profile。
- `APP_JWT_SECRET` 使用强随机值。
- 后台默认账号、测试账号、弱密码全部清理。
- 只保留业务需要的菜单和角色权限。
- 服务器开启 HTTPS，公众号/H5 域名使用有效证书。

## 9. 部署启动参考

在已完成构建且已配置生产环境变量后，可参考：

```powershell
cd C:\Users\31333\Desktop\vibecoding\SDD_V7_1\Projects_Repo\central-bank-e-platform
docker compose -f docker-compose.prod.yml config
docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml ps
```

检查 Redis：

```powershell
docker compose -f docker-compose.prod.yml exec redis redis-cli -a $env:REDIS_PASSWORD ping
```

检查后端日志：

```powershell
docker compose -f docker-compose.prod.yml logs -f backend
```

访问路径：

```text
http://<domain-or-ip>/h5/
http://<domain-or-ip>/admin/
```

## 10. 生产验收清单

上线后至少验证以下链路：

- 后台登录、退出、记住密码文案展示。
- 非管理员账号菜单、路由、快捷入口权限正确。
- 内容新增、编辑、发布、下架、删除。
- 四县办公室只能发布服务指引的前后端规则。
- 金融产品新增、编辑、多联系人展示。
- H5 首页、乡村振兴、金融产品、详情页返回逻辑。
- 上传附件可访问，重启后文件仍存在。
- 错误密码提示展示真实后端 message。
- 操作日志和账号管理符合预期。

## 11. 暂不建议直接生产上线的原因

当前项目已具备部署基础，但上线生产前仍建议先走测试环境，因为：

- 业务功能仍在持续开发，菜单和权限刚完成多轮修正。
- H5 与后台都需要真实 MySQL/Redis 下的完整人工验收。
- 生产域名、HTTPS、公众号配置、服务器备份策略尚未在仓库内确认。
- 默认账号、测试数据、初始化菜单需要按最终运营角色再清理一次。
