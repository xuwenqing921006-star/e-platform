# 测试报告：T-001 前端双入口与 Mock 基础设施

**测试时间**：2026-06-02 14:25:56 +08:00
**Tester Agent ID**：019e8614-4f24-7eb0-97ad-adb5e3a1a238
**Developer Agent ID**：019e86ef-650b-7ad0-8479-d375356e64ee
**复验轮次**：第 3 次自动修复复验

## 结果：PASS

## 验收边界

- 当前任务为 `frontend`，且 `frontendIntegration.required=false`。
- 严格按 Mock 阶段边界验证：未启动 Playwright、Puppeteer、Cypress 或浏览器自动化；未启动或检查真实后端；未做跨会话测试。
- Pencil MCP 仍无法连接运行中的 Pencil 应用。按既定规则使用 PNG 导出与 `docs/PRD.md` 回退；本轮没有业务页面视觉改动，原型对齐修复保持有效。未使用 shell、Read、grep 或脚本读取 `.pen`。

## 历史 FAIL 复验

| # | 历史问题 | 结果 | 说明 |
|---|----------|------|------|
| 1 | 后台移动宽度布局不可正常适配 | PASS | 移动优先单栏与桌面增强规则保持存在。 |
| 2 | Dashboard 页面硬编码 Mock 指标和最近内容 | PASS | Dashboard 继续通过统一 Axios service 读取 `/admin/dashboard/summary`。 |
| 3 | 基础页面未严格对齐高保真原型 | PASS | 可复用 SVG 图标、header 用户区、指标卡、快捷入口和 H5 元信息图标保持存在。 |
| 4 | 后台 header 直接硬编码 `[Mock] 系统管理员` | PASS | `frontend/src/components/admin/AdminLayout.vue:39` 继续读取 `authStore.displayName`。 |
| 5 | Mock 阶段可见标识被移除 | PASS | `frontend/src/mocks/index.ts:30`、`:38`、`:46`、`:124` 已恢复集中可见 `[Mock]` 标识。 |
| 6 | 递归扫描未同时覆盖集中 fixture 可辨识性 | PASS | 自动测试同时验证组件无散落标识与集中 fixture 在 Mock 阶段返回可见标识。 |

## 验收标准逐条验证

| # | 标准 | 结果 | 说明 |
|---|------|------|------|
| 1 | 用户打开 `/h5/` 和 `/admin/login`，预期看到可访问的 H5 入口和后台登录入口。 | PASS | 路由保持注册；`npm run build` 通过。 |
| 2 | 用户在手机宽度和桌面宽度打开页面，预期看到 H5 卡片布局和后台基础布局均能正常适配。 | PASS | 移动端单栏与 `1280px`、`1440px`、`1920px` 桌面断点保持存在。 |
| 3 | 用户访问后台受保护页面但未登录，预期页面跳转到 `/admin/login`。 | PASS | 后台父路由 `requiresAuth` 与统一守卫保持存在；对应自动测试通过。 |

## Technical Checks

| # | 检查项 | 结果 | 说明 |
|---|--------|------|------|
| 1 | `npm run typecheck`、`npm run test`、`npm run build` 通过。 | PASS | 三条命令均实际执行成功；额外执行的 `npm run lint` 与 `npm audit --audit-level=high` 也通过。Vitest 结果为 `11 passed`。 |
| 2 | Mock 数据集中位于 `frontend/src/mocks/`，格式与 `docs/api-contracts.md` 一致。 | PASS | 页面与组件无散落 `[Mock]` 业务字面量；四个可见标识集中位于 `frontend/src/mocks/index.ts`，DTO 字段仍为契约子集。`frontend.md:253` 与 `frontend.md:258` 同时满足。 |
| 3 | Mock 阶段不调用真实后端 API。 | PASS | `frontend/.env:2` 为 `VITE_USE_MOCK=true`；Axios 本地 adapter 保持启用。未执行真实后端检查。 |
| 4 | 响应式布局覆盖移动端与 `1280px`、`1440px`、`1920px` 桌面宽度。 | PASS | 移动端收敛和桌面断点保持存在。 |

## 代码质量与安全检查

| 检查项 | 结果 | 说明 |
|--------|------|------|
| Developer 修改文件 | PASS | 本轮清单中的文件均存在。 |
| `TODO/FIXME/HACK` | PASS | 对源码与测试静态扫描未发现。 |
| 敏感值泄露 | PASS | 静态模式扫描未发现真实 API Key、Token、私钥或可还原 secret。 |
| 集中 Mock | PASS | `pages/**` 与 `components/**` 无散落 `[Mock]`；集中 fixture 命中四处可见标识。 |
| Header 数据源 | PASS | `AdminLayout.vue` 从 `authStore.displayName` 读取用户展示名。 |

## 实际执行命令摘要

```text
git status --short
rg -n '\[Mock\]' frontend/src/pages frontend/src/components
rg -n '\[Mock\]' frontend/src/mocks frontend/src/services frontend/src/stores
rg -n 'TODO|FIXME|HACK' frontend/src frontend/tests frontend/.env frontend/.env.example frontend/vite.config.ts
rg -n --glob '!*.pen' --glob '!package-lock.json' '<敏感值模式>' frontend .sdd docs
rg -n '<Store、集中 Mock 测试、路由、Axios adapter、代理、响应式关键模式>' frontend
npm run typecheck
npm run test
npm run lint
npm run build
npm audit --audit-level=high
```
