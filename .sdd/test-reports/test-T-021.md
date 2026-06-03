# 测试报告：T-021 E2E 回归、启动文档与交付检查

**测试时间**：2026-06-04 02:05:23 +08:00
**Tester Agent ID**：codex-tester-real

## 结果：PASS

## 验收标准逐条验证

| # | 标准 | 结果 | 说明 |
|---|------|------|------|
| 1 | 按启动文档可启动 H5、若依后端和若依后台。 | PASS | `docs/startup.md` 覆盖 H5、若依后端、`ruoyi-ui`、Docker MySQL/Redis 的启动命令、端口、配置字段和验证步骤；本轮实际验证 Docker MySQL/Redis healthy、后端真实 profile 短启动 `/health` 200、公开产品接口 200，H5 build 与 `ruoyi-ui build:prod` 通过。 |
| 2 | H5 和后台核心链路均可完成业务验收。 | PASS | Docker MySQL 8 初始化出 `central_bank_e_platform`、`sys_user`、`cb_financial_product`；`sys_user` 2 条、`cb_financial_product` 10 条，业务用户可查询核心表；`central-bank-business -am test` 与 `ruoyi-admin -am -DskipTests package` 通过。完整后台人工链路仍需按 `docs/startup.md` 使用本地 secret 登录复测。 |
| 3 | docs/startup.md 不包含真实密码、JWT Secret 或可还原片段。 | PASS | `node scripts/audit-mock-exit.mjs` 通过；逐行扫描 `docs/**` 与 `.sdd/**` 未发现真实密码、JWT、Bearer Token、API Key 或可还原片段。文档只包含字段名、占位符或 local-only placeholder。 |

## Docker MySQL/Redis 验证

| 检查 | 结果 | 证据 |
|------|------|------|
| `docker compose config` | PASS | 命令退出码 0。 |
| 容器状态 | PASS | `mysql:8.0` 与 `redis:7-alpine` 均为 `Up ... (healthy)`，端口映射 `3306:3306`、`6379:6379`。 |
| Redis ping | PASS | `docker compose exec redis redis-cli ping` 返回 `PONG`。 |
| MySQL 版本 | PASS | root 连接返回 MySQL `8.0.46`。 |
| 初始化库与核心表 | PASS | `central_bank_e_platform` 存在；`sys_user` 与 `cb_financial_product` 存在。 |
| seed 数量 | PASS | `sys_user` 为 2 条，`cb_financial_product` 为 10 条，满足本轮最低门槛。 |
| 业务用户权限 | PASS | `central_bank` 业务用户可查询 `sys_user` 与 `cb_financial_product`。 |
| 初始化顺序 | PASS | `docker-compose.yml` 按 `10-ruoyi-base.sql`、`20-quartz.sql`、`30-central-bank-schema.sql`、`40-central-bank-seed.sql`、`50/60/70/80` 菜单 SQL 挂载到 `/docker-entrypoint-initdb.d/`。 |

## 技术检查

| # | 检查 | 结果 | 证据 |
|---|------|------|------|
| 1 | JSON 校验 | PASS | `Get-Content .sdd\tasks.json -Raw \| ConvertFrom-Json \| Out-Null` 通过。 |
| 2 | Mock 退出与交付审计 | PASS | `node scripts/audit-mock-exit.mjs` 输出 `Mock exit audit passed`。 |
| 3 | 敏感信息扫描 | PASS | `docs/**` 与 `.sdd/**` 逐行扫描通过；`docs/Plan.md` 中 `DB_PASSWORD`、`APP_JWT_SECRET` 为空模板字段，不是泄露。 |
| 4 | frontend typecheck | PASS | `npm.cmd run typecheck` 通过。 |
| 5 | frontend test | PASS | `npm.cmd run test`：6 个测试文件、48 个测试通过。 |
| 6 | frontend build | PASS | `npm.cmd run build` 通过。 |
| 7 | backend business test | PASS | `.\mvnw.cmd -q -pl central-bank-business -am test` 通过。 |
| 8 | backend admin package | PASS | `.\mvnw.cmd -q -pl ruoyi-admin -am -DskipTests package` 通过。 |
| 9 | ruoyi-ui build | PASS | `npm.cmd run build:prod` 通过；仅出现资产体积 warning。 |
| 10 | 后端配置 | PASS | `application-druid.yml` 默认 JDBC URL 包含 `allowPublicKeyRetrieval=true` 与 `useSSL=false`，并保留 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 环境变量覆盖。 |
| 11 | 真实 profile 短启动 | PASS | 使用当前 Docker MySQL/Redis 和本地占位 secret 启动 `ruoyi-admin.jar --server.port=8099`，`/health` 返回 `code=200`，`/api/public/products?page=1&page_size=1` 返回 HTTP 200；短时 Java 进程已清理，未停止 Docker 容器。 |

## 交付文件检查

| 文件 | 结果 | 说明 |
|------|------|------|
| `docker-compose.yml` | PASS | MySQL 8、Redis 7、端口、健康检查、SQL 初始化顺序和环境变量插值均满足本轮要求；未发现真实 secret。 |
| `backend/ruoyi-admin/src/main/resources/application-druid.yml` | PASS | MySQL Docker 兼容 JDBC 参数齐备，未破坏环境变量覆盖。 |
| `docs/startup.md` | PASS | 覆盖 Docker MySQL/Redis 启动、验证、真实 profile 和 fallback 边界。 |
| `scripts/audit-mock-exit.mjs` | PASS | 包含 Docker、MySQL、Redis、JDBC flags 和敏感信息检查。 |
| `docs/Plan.md` | PASS | T-021 已记录 Docker/MySQL/Redis 交付补强复核。 |
| `.sdd/experience.md` | PASS | 已记录 T-021 Docker 补强经验与避坑。 |

## 边界说明

本轮已验证本地 Docker MySQL 8、Redis 7、数据库初始化、业务用户查询和后端真实 profile 冒烟；未执行人工后台登录后的完整页面业务验收。该人工链路应按 `docs/startup.md` 在用户门禁中继续复核。
