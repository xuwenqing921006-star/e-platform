# T-014 测试报告

## 结论

PASS

## 范围

- 若依后台金融产品列表、查询、分页、详情、新增、编辑、删除。
- 产品导入模板 `GET /api/admin/products/import-template/download`。
- 服务端产品管理权限、固定银行、固定产品类型和 7 字段契约。
- ruoyi-ui 产品管理页面真实调用 `/api/admin/products*`。

## 自动验证

- `mvn -q -pl central-bank-business -am test`：PASS。
- `mvn -q test`：PASS。
- `mvn -q -pl ruoyi-admin -am package -DskipTests`：PASS。
- `npm.cmd run build:prod`（ruoyi-ui）：PASS，仅存在若依既有 bundle size warning。

## 覆盖点

- `AdminProductServiceTest` 覆盖产品管理权限、固定银行校验、7 字段保存、404、模板表头。
- `AdminProductControllerTest` 覆盖列表契约、详情契约、无 `reference_rate` 字段、403/404 响应和 xlsx 下载响应头。
- `PublicProductServiceTest` fake mapper 已同步后台 mapper 新增方法，确保公开产品查询回归不受影响。

## 外部服务边界

真实 MySQL 8 和 Redis 等待用户本地安装后联调。本轮自动验证继续使用 H2/local fallback，不宣称真实数据库完整联调完成。
