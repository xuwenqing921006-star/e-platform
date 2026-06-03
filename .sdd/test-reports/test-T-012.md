# 测试报告：T-012 附件上传、删除与公开下载真实联调闭环

**测试时间**：2026-06-03 14:34
**Tester Agent ID**：tester-main-session

## 结果：PASS

## 验收标准逐条验证

| # | 标准 | 结果 | 说明 |
|---|------|------|------|
| 1 | 后台上传 PDF、Word、Excel 附件可返回契约字段。 | PASS | `AdminAttachmentController` 提供 `POST /api/admin/attachments`，`AttachmentStorageService` 校验扩展名与 MIME 类型，写入 `cb_attachment` 元数据并返回 `file_name`、`file_type`、`file_size`、`download_url`。 |
| 2 | 超过大小、格式不支持和超过 3 个附件返回清晰错误。 | PASS | Service 层限制单文件 20MB，并对不支持格式返回 400；`validateContentAttachmentLimit` 对内容附件数超过 3 个返回 400 和清晰中文错误。 |
| 3 | H5 附件下载接口返回真实文件流。 | PASS | `PublicAttachmentController` 提供 `GET /api/public/attachments/{id}/download`，通过 `AttachmentStorageService.downloadFile` 从配置目录读取真实文件流并设置下载响应头。 |

## 自动验证

| 命令 | 结果 |
|---|---|
| `backend .\mvnw.cmd -q -pl central-bank-business -am test` | PASS |
| `backend .\mvnw.cmd -q test` | PASS |
| `frontend npm run typecheck` | PASS |
| `frontend npm run test` | PASS，6 个测试文件、48 个测试通过 |
| `frontend npm run build` | PASS |
| `rg "sk-\|Bearer \|DB_PASSWORD=\|APP_JWT_SECRET=" backend\central-bank-business docs .sdd` | PASS；仅命中文档中的 Bearer 契约示例和空配置占位 |

## 联调边界

- 附件存储默认复用若依 `RuoYiConfig.getUploadPath()`，由 `APP_STORAGE_ROOT` 进入 `ruoyi.profile`，代码中不硬编码真实服务器路径。
- 自动测试使用 JUnit 临时目录完成真实文件写入、下载和删除验证，不触碰部署目录。
- 当前仍处于 `external_services_mode=local_fallback`；真实 MySQL 8 未提供，本轮不宣称真实 MySQL 完整联调通过。
- 每篇内容最多 3 个附件的校验已作为后端复用函数提供；真正内容保存、编辑和附件绑定属于 T-013，不在本轮过度宣称。

## 超出范围发现（不影响当前任务判定）

| # | 问题 | 所属模块 | 建议处理方式 |
|---|------|---------|------------|
| 1 | 若依后台内容发布页尚未接入真实内容 CRUD。 | T-013 后台内容管理 | 在 T-013 中把附件上传结果绑定到内容保存接口，并复用 `validateContentAttachmentLimit`。 |
