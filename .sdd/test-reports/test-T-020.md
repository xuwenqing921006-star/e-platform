# 测试报告：T-020 全部页面退出 Mock 联调审计

**测试时间**：2026-06-04 00:35:48 +08:00
**Tester Agent ID**：codex-tester-real

## 结果：PASS

## 验收标准逐条验证

| # | 标准 | 结果 | 说明 |
|---|------|------|------|
| 1 | VITE_USE_MOCK=false 时 H5 页面命中真实 Spring Boot API。 | PASS | `frontend/.env` 与 `.env.example` 均为 `VITE_USE_MOCK=false`、`VITE_API_BASE_URL=/api`；`frontend/src/services/api.ts` 仅在 `VITE_USE_MOCK === 'true'` 时启用 mock adapter；`publicContentService` 与 `publicProductService` 分别请求 `/public/contents*`、`/public/products*`，经 baseURL 命中 `/api/public/*`。 |
| 2 | 若依后台业务页面命中真实后端接口。 | PASS | `ruoyi-ui/src/api/centralbank/content.js`、`product.js`、`account.js`、`auditLog.js`、`dashboard.js` 均调用 `/api/admin/*`；审计脚本对 centralbank API 路径检查通过。 |
| 3 | 所有业务页面无 [Mock] 或等价 Mock-only 文案。 | PASS | `node scripts/audit-mock-exit.mjs` 通过；源码扫描未在 H5 页面/config/service、若依登录/布局/settings/main 与 centralbank API 中发现 `[Mock]`、Mock-only、MockJs、源码/文档/捐赠/默认密码等可见残留。集中 Mock fixture 仍存在，仅供 `.env.test` 和显式 `VITE_USE_MOCK=true` 使用。 |

## 技术检查

| # | 检查 | 结果 | 说明 |
|---|------|------|------|
| 1 | frontend npm run typecheck、test、build 通过。 | PASS | `npm.cmd run typecheck` 通过；`npm.cmd run test` 通过，6 个测试文件、48 条测试；`npm.cmd run build` 通过。额外执行 `npm.cmd run lint` 通过。 |
| 2 | backend Maven 测试和短时启动通过。 | PASS | `.\mvnw.cmd -q -pl central-bank-business -am test` 通过；`.\mvnw.cmd -q -pl ruoyi-admin -am -DskipTests package` 通过；`java -jar ruoyi-admin/target/ruoyi-admin.jar --spring.profiles.active=health --server.port=8099` 短时启动后 `GET /health` 返回 200 且 `status=UP`。 |
| 3 | ruoyi-ui 构建或 lint 通过。 | PASS | `npm.cmd run build:prod` 通过；仅有 Vue CLI 资源体积 warning，不影响本任务验收。 |

## Developer 产出检查

| 项 | 结果 | 说明 |
|---|------|------|
| 文件存在性 | PASS | Developer 列出的新增/修改文件均存在。 |
| 删除文件合理性 | PASS | `ruoyi-ui/src/components/RuoYi/Doc/index.vue` 与 `ruoyi-ui/src/components/RuoYi/Git/index.vue` 不存在；删除方向符合清理若依默认源码/文档入口要求。 |
| 敏感信息 | PASS | 对 Developer 产出文件与配置示例扫描，未发现真实密码、JWT Secret、数据库密码、Bearer Token 或 sk 类密钥；只存在字段名、空占位和测试/Mock 阶段默认口令字样。 |
| 可复现性 | PASS | 修复 `.sdd/tasks.json` 中 T-020 `notes` 后缺少逗号的问题，并完成状态更新；修复前 Node JSON 解析报 `Expected ',' or '}' after property value`。 |

## 验证命令摘要

| 命令 | 结果 |
|---|---|
| `node scripts/audit-mock-exit.mjs` | PASS |
| `frontend npm.cmd run typecheck` | PASS |
| `frontend npm.cmd run lint` | PASS |
| `frontend npm.cmd run test` | PASS，6 files / 48 tests |
| `frontend npm.cmd run build` | PASS |
| `backend .\mvnw.cmd -q -pl central-bank-business -am test` | PASS |
| `backend .\mvnw.cmd -q -pl ruoyi-admin -am -DskipTests package` | PASS |
| `backend java -jar ruoyi-admin/target/ruoyi-admin.jar --spring.profiles.active=health --server.port=8099` + `/health` | PASS |
| `ruoyi-ui npm.cmd run build:prod` | PASS，存在资源体积 warning |

## 外部服务边界

MySQL 8 在项目任务定义中仍为 fallback 状态，本轮未宣称真实 MySQL 联调完成；短时启动使用 health profile 验证 Spring Boot 进程与健康检查路径。JWT 签名密钥与本地附件目录未发现真实敏感值泄露。

## 超出范围发现（不影响当前任务判定）

| # | 问题 | 所属模块 | 建议处理方式 |
|---|------|---------|------------|
| 1 | `frontend/dist` 中仍能检索到集中 Mock fixture 字符串，原因是 `api.ts` 顶层导入 mock dispatcher；真实默认路径 adapter 为 `undefined`，页面不会展示这些 fixture。 | frontend 构建优化 | 后续交付阶段如要求生产包完全不含 Mock fixture，可改为 `VITE_USE_MOCK=true` 时动态导入 mock dispatcher。 |
| 2 | `frontend/public/article-credit-service-long.svg` 自身含 `[Mock]` 文案；真实后端 seed 未引用该资源，本轮业务页面不会展示。 | frontend public asset | 最终静态资源清理阶段可删除或替换该 Mock 示例图。 |
