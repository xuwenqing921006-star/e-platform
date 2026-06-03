# 测试报告：T-002 H5 首页栏目与 SAFE 浮动入口 Mock

**测试时间**：2026-06-02 14:56 Asia/Shanghai
**Tester Agent ID**：019e871a-a27b-7663-96c0-2e3907177769

## 结果：PASS

## 验收标准逐条验证

| # | 标准 | 结果 | 说明 |
|---|------|------|------|
| 1 | 用户打开 H5 首页，预期顶部直接显示乡村振兴和金融服务一级 Tab，默认高亮乡村振兴，不显示独立平台头部。 | PASS | `frontend/src/config/h5Home.ts` 固定一级 Tab 顺序；`frontend/src/pages/h5/H5LandingPage.vue` 默认 `primaryTab='RURAL'`，页面顶部直接渲染 Tab，无独立平台头部。 |
| 2 | 用户在 H5 首页切换县域、三级文字 Tab 和金融服务栏目，预期本页面展示对应 Mock 列表、固定服务队信息、空状态或重试入口。 | PASS | 页面通过集中配置与 service 切换县域、三级文字 Tab、金融栏目；存在列表、加载更多、四县固定服务队、空态和失败重试入口。`npm run test` 覆盖产品加载更多、杜蒙县空态与服务队展示。 |
| 3 | 用户点击右下角 SAFE Logo，预期入口向左展开；点击关闭按钮后向右收起；点击展开区域后跳转到固定 SAFE 地址。 | PASS | `H5SafeEntry.vue` 通过右侧固定容器扩宽实现向左展开，关闭后收起；链接与 `window.open` 均使用固定 `SAFE_PORTAL_URL`。Vitest 覆盖展开、关闭、跳转和弹窗受阻反馈。 |

## Technical Checks

| # | 检查项 | 结果 | 说明 |
|---|--------|------|------|
| 1 | `npm run typecheck`、`npm run test`、`npm run build` 通过。 | PASS | 独立执行均退出码 0；测试为 2 个测试文件、21/21 用例通过。 |
| 2 | H5 首页数据来自 `frontend/src/mocks/` 或前端固定配置，不调用真实后端 API。 | PASS | 页面仅调用 `publicContentService.ts` 与 `publicProductService.ts`；单一 Axios 实例在 Mock 阶段使用 `mockAdapter`。业务列表集中在 `frontend/src/mocks/index.ts`，Tab 与服务队集中在 `frontend/src/config/h5Home.ts`。 |
| 3 | SAFE 地址固定为 `http://zwfw.safe.gov.cn/asone/`，无密钥；无网络时仅校验链接地址。 | PASS | 固定地址集中在 `frontend/src/config/safePortal.ts`，未使用密钥。短时网络探测返回 HTTP 200，并跳转到 SAFE 自身 URL。 |
| 4 | SAFE 外部地址失败时反馈清晰，不静默吞错。 | PASS | `window.open` 受阻时显示“数字外管平台打开失败，请稍后重试。”；Vitest 已覆盖失败反馈。 |
| 5 | 详情页不渲染 SAFE 浮动入口。 | PASS | `shouldShowSafeEntry()` 排除 `/h5/contents/*` 与 `/h5/products/*`；Vitest 覆盖两类详情路径。 |

## 原型与规范核对

- Pencil MCP 已发现，但运行中的 Pencil 应用不可连接；按规则回退到 `docs/prototypes/exports/README.md` 和 PNG。
- 已核对 `vdLBB.png`、`c3rs6D.png`、`EojJm.png`、`FCCjg.png`、`IBsKk.png`：一级 Tab、金融栏目、县域栏目、三级文字 Tab、列表卡片、服务队和 SAFE 右下角入口结构一致。
- Mock 阶段额外显示的 `[Mock]` 标识符合 `harness-core/dev-standards/frontend.md` 强制要求。
- Mock handler 在返回前显式映射 endpoint DTO；内部实体附加字段未泄漏到公开列表 DTO。
- 页面和组件未散落列表或服务队业务 fixture；未发现 `TODO`、`FIXME`、`HACK` 或真实敏感值泄露。

## 实际命令摘要

| 命令 | 结果 |
|------|------|
| `npm run typecheck` | PASS |
| `npm run test` | PASS，21/21 |
| `npm run lint` | PASS |
| `npm run build` | PASS |
| `git diff --check` | PASS，仅输出 Git 的 LF/CRLF 提示 |
| `curl.exe --location --max-time 5 http://zwfw.safe.gov.cn/asone/` | PASS，HTTP 200，最终地址仍属于 SAFE |
| `npm audit --audit-level=high` | 外部网络阻塞：连续两次访问 npm registry 审计端点时 TLS 握手前断连；不影响本任务判定 |

## Mock 阶段边界

- 未启动 Playwright、Puppeteer 或 Cypress。
- 未启动或检查真实后端。
- 未执行跨会话同步或持久化验证。
