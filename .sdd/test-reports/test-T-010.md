# T-010 测试报告：H5 公开内容查询真实联调闭环

## 范围

- 实现 `GET /api/public/contents`，支持 `category`、`scope`、`county_code`、`page`、`page_size` 查询。
- 实现 `GET /api/public/contents/{id}`，返回文章详情和最多 3 个附件入口。
- 补充公开内容 DTO、分页 DTO、附件 DTO 和 H2/MySQL 最小 seed。
- H5 首页在 `VITE_USE_MOCK=false` 时隐藏 Mock 提示，并继续通过 `publicContentService` 命中 `/public/contents*`。

## 验证命令

```powershell
cd frontend
npm run typecheck
npm run test
npm run build
```

```powershell
cd backend
.\mvnw.cmd -q -pl central-bank-business -am test
.\mvnw.cmd -q test
```

## 验证结果

- 前端类型检查通过。
- 前端 Vitest 通过：6 个文件、48 个测试。
- 前端生产构建通过。
- `central-bank-business` 模块测试通过，覆盖 H2 seed、公开内容 service 和 MockMvc 路径测试。
- 后端 Maven 全量测试通过。
- MockMvc 已直接请求 `/api/public/contents` 和 `/api/public/contents/{id}`，验证 snake_case 字段、分页结构和 404 `data: null`。

## 边界说明

- 本轮使用 H2 seed 与 MockMvc 等价验证真实接口契约；真实 MySQL 8 未提供，不宣称真实数据库联调通过。
- 公开附件下载文件流仍归属 T-012，本轮只返回附件下载入口。
- H5 金融产品真实查询仍归属 T-011。
