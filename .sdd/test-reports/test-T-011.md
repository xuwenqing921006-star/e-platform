# 测试报告：T-011 H5 公开金融产品查询真实联调闭环

**测试时间**：2026-06-03 14:05
**Tester Agent ID**：tester-main-session

## 结果：PASS

## 验收标准逐条验证

| # | 标准 | 结果 | 说明 |
|---|------|------|------|
| 1 | H5 产品列表真实分页加载，列表只显示银行、产品名称、类型。 | PASS | `PublicProductListItem` 仅包含 `id`、`bank_name`、`product_name`、`product_type`；`PublicProductControllerTest` 验证列表 JSON 和分页字段，并断言列表不包含 `admission_conditions`。`publicProductService.ts` 在真实模式调用 `/public/products`。 |
| 2 | H5 产品详情严格展示 7 个产品字段，不出现参考利率等额外字段。 | PASS | `PublicProductDetailData` 仅映射 `bank_name`、`product_name`、`product_type`、`admission_conditions`、`product_intro`、`business_manager`、`contact_info`，测试断言 `reference_rate`、`loan_amount`、`updated_at` 不存在。 |
| 3 | 页面不显示 [Mock] 或 Mock-only 文案。 | PASS | 前端 `api.ts` 在 `VITE_USE_MOCK=false` 时不启用 mock adapter；H5 首页 Mock 提示由 `import.meta.env.VITE_USE_MOCK !== 'false'` 控制，真实模式隐藏。 |

## 自动验证

| 命令 | 结果 |
|---|---|
| `backend .\mvnw.cmd -q -pl central-bank-business -am test` | PASS |
| `backend .\mvnw.cmd -q test` | PASS |
| `frontend npm run typecheck` | PASS |
| `frontend npm run test` | PASS，6 个测试文件、48 个测试通过 |
| `frontend npm run build` | PASS |

## 联调边界

- `frontend/.env` 使用 `VITE_API_BASE_URL=/api`，`frontend/vite.config.ts` 配置 `/api` 与 `/ws` 代理，默认后端目标 `http://localhost:8099`。
- 当前项目状态为 `external_services_mode=local_fallback`；真实 MySQL 8 未提供，本轮不宣称真实 MySQL 完整联调通过。
- 完整 112 条金融产品初始化仍属于 B12/T-019；T-011 验证接口能力、分页契约、详情字段收敛和 H5 真实接口路径。

## 超出范围发现（不影响当前任务判定）

| # | 问题 | 所属模块 | 建议处理方式 |
|---|------|---------|------------|
| 1 | `docs/Plan.md` 总览表仍有旧的“待开发”状态文案。 | SDD 进度文档 | 后续可在专门的 SDD 文档同步任务中统一修正，不影响 T-011 代码闭环。 |
