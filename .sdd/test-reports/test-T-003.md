# 测试报告：T-003 H5 文章详情 Mock

**测试时间**：2026-06-02 17:19:37 +08:00
**Tester Agent ID**：019e879d-9b67-7983-9ba1-cce48769b739

## 结果：PASS

## 验收范围

- 仅验证 T-003 的 acceptanceCriteria 与 technicalChecks。
- T-002 已通过但未提交的工作区改动未作为 T-003 FAIL 依据。
- Mock 阶段未启动 Playwright、Puppeteer、Cypress，未检查真实后端，未要求真实文件流。
- Pencil MCP 调用失败，按任务规则回退到 `docs/prototypes/exports/e9h2Ky.png`（H02 文章详情导出图）和 `docs/PRD.md`。

## 验收标准逐条验证

| # | 标准 | 结果 | 说明 |
|---|------|------|------|
| 1 | 用户从 H5 文章卡片进入详情页，预期看到完整标题、发布办公室、发布时间、富文本正文和附件列表。 | PASS | `H5LandingPage.vue` 的文章卡片路由到 `/h5/contents/${item.id}`；`router/index.ts` 注册 `/h5/contents/:id`；`H5ArticleDetailPage.vue` 通过 `getPublicContentDetail` 展示标题、办公室、日期、`rich_text_html` 和附件列表。 |
| 2 | 用户点击正文中的长图片，预期可以查看原图。 | PASS | Mock 正文包含 `/article-credit-service-long.svg` 长图；详情页对 `.article-rich-text` 做图片点击委托，打开 `.article-image-viewer` 原图查看层，并提供关闭按钮。 |
| 3 | 用户查看文章详情，预期页面最多展示 3 个附件下载入口，且不强制在 H5 内预览 PDF、Word 或 Excel。 | PASS | Mock 内部实体包含 4 个附件用于边界验证，但 `getPublicContentDetail` DTO 映射和页面渲染均限制 `slice(0, 3)`；下载入口使用 `href` 与 `download` 属性呈现，没有 PDF/Word/Excel 内嵌预览逻辑。 |

## Technical Checks

| # | 检查项 | 结果 | 说明 |
|---|------|------|------|
| 1 | `npm run typecheck`、`npm run test`、`npm run build` 通过。 | PASS | 三项均通过；另按本轮要求执行 `npm run lint`、`npm audit --audit-level=high`、`git diff --check`，均通过。 |
| 2 | 文章详情 Mock 响应与 `GET /api/public/contents/{id}` 契约一致。 | PASS | `PublicContentDetailData` 字段为 `id/title/category/office_name/published_at/rich_text_html/attachments`；附件字段为 `id/file_name/file_type/file_size/download_url`，与 `docs/api-contracts.md` 一致；404 返回 `code/status=404`、`message=内容不存在`、`data=null`。 |
| 3 | Mock 阶段只验证下载入口呈现，不要求真实文件流。 | PASS | 仅静态核对附件 `href`、`download` 属性和 Mock DTO，未请求真实附件流。 |
| 4 | SAFE 详情页隐藏规则保持。 | PASS | `shouldShowSafeEntry` 对 `/h5/contents/` 与 `/h5/products/` 详情路径返回隐藏；详情页测试也断言 `.safe-entry` 不存在。 |

## 原型核对

- Pencil MCP：调用 `batch_get` 失败，错误为无法连接运行中的 Pencil app。
- 回退依据：`docs/prototypes/exports/README.md` 标注 `e9h2Ky.png` 为 H02 文章详情；导出图包含返回顶栏、`服务指引`、标题、办公室、日期、正文、`附件下载` 和附件下载项。
- 实现核对：详情页结构与 H02 主要信息架构一致；Mock 阶段增加长图以覆盖“点击正文长图片查看原图”的 T-003 验收点。

## 实际命令摘要

| 命令 | 结果 |
|---|---|
| `npm run typecheck` | PASS |
| `npm run test` | PASS，3 个测试文件、26 个测试通过 |
| `npm run lint` | PASS |
| `npm run build` | PASS |
| `npm audit --audit-level=high` | PASS，0 vulnerabilities |
| `git diff --check` | PASS，仅有 Git CRLF 提示，无 whitespace error |

## 结论

T-003 满足验收标准和技术检查，判定 PASS。
