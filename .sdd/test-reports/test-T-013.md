# 测试报告：T-013 若依后台内容管理真实闭环

**测试时间**：2026-06-03 15:27
**Tester Agent ID**：tester-main-session

## 结果：PASS

## 验收标准逐条验证

| # | 标准 | 结果 | 说明 |
|---|------|------|------|
| 1 | 若依后台内容列表支持查询、分页、查看、编辑、删除，不展示附件个数列。 | PASS | `AdminContentController` 提供 `/api/admin/contents*`；`AdminContentListItem` 只包含标题、分类、办公室、发布时间等列表字段；ruoyi-ui 表格未展示附件个数。 |
| 2 | 发布和编辑内容支持分类、办公室、富文本和最多 3 个附件。 | PASS | `AdminContentRequest` 按契约接收 `title/category/office_code/rich_text_html/attachment_ids`；保存时绑定附件，超过 3 个复用 `AttachmentStorageService` 校验。 |
| 3 | 办公室隔离和县域权限在服务端校验。 | PASS | `AdminContentServiceTest` 覆盖管理员查询、普通办公室强制本办公室、跨办公室修改 403、县域办公室只能发布服务指引。 |

## 自动验证

| 命令 | 结果 |
|---|---|
| `backend mvn -q -pl central-bank-business -am test` | PASS |
| `backend mvn -q test` | PASS |
| `backend mvn -q -pl ruoyi-admin -am package -DskipTests` | PASS |
| `ruoyi-ui npm run build:prod` | PASS；仅有若依模板既有包体积 warning |
| `frontend npm run typecheck` | PASS |
| `frontend npm run test` | PASS，6 个测试文件、48 个测试通过 |
| `frontend npm run build` | PASS |

## 联调边界

- 当前仍处于 `external_services_mode=local_fallback`；真实 MySQL 8 未提供，本轮不宣称真实 MySQL 完整联调通过。
- ruoyi-ui 菜单与按钮权限提供 `central_bank_content_menu.sql`，实际菜单 SQL 执行属于部署/初始化流程。
- 操作日志查询与审计闭环属于 T-017；本轮不提前宣称日志闭环完成。
- Maven wrapper 在当前 PowerShell 环境下启动脚本异常，本轮使用 wrapper 缓存中的 Maven 3.9.16 执行同等命令。

## 超出范围发现（不影响当前任务判定）

| # | 问题 | 所属模块 | 建议处理方式 |
|---|------|---------|------------|
| 1 | 真实 MySQL 8 与正式菜单 SQL 尚未在目标数据库执行。 | 部署/初始化 | T-019/T-021 阶段执行初始化和启动文档联调时处理。 |
