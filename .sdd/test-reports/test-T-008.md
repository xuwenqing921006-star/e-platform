# T-008 测试报告：若依业务数据层、固定选项与初始化

## 范围

- 新增 `backend/central-bank-business` 业务模块。
- 建立内容、附件、金融产品、账号扩展的 Domain、Mapper 与 Mapper XML。
- 提供 `GET /api/admin/options` 对应的固定选项服务与控制器。
- 提供 MySQL schema/seed 脚本和 H2 fallback schema/seed 测试脚本。

## 验证命令

```powershell
cd backend
.\mvnw.cmd -q -pl central-bank-business -am test
.\mvnw.cmd -q test
```

## 验证结果

- `central-bank-business` 模块测试通过。
- 后端 Maven 全量测试通过。
- H2 fallback 中已执行建表与最小 seed，验证 `cb_content`、`cb_attachment`、`cb_financial_product`、`cb_account_extension` 均可初始化。
- 固定选项验证覆盖：2 个内容分类、2 个产品类型、14 个办公室、17 个银行。

## 边界说明

- 真实 MySQL 8 连接信息尚未提供，本轮只验证 H2 fallback 与 MySQL 脚本可提交。
- 完整管理员/演示账号初始化和 112 条产品导入属于 T-019，本轮只放置基础 seed 骨架。
