# T-015 若依 Excel 导入金融产品真实闭环测试报告

## 结论

PASS

## 覆盖范围

- `POST /api/admin/products/import/validate`：xlsx 上传、总数/有效数/无效数、错误行字段、非 xlsx 415。
- `POST /api/admin/products/import/commit`：按 `import_token` 写入有效产品、跳过无效行、重复提交返回 409。
- 固定银行与产品类型：支持 Excel 中的中文展示名；`阳光惠农贷` 特殊行按固定银行项通过。
- ruoyi-ui：产品列表新增原型已有的“Excel 导入”入口；新增 A07 导入页，展示上传、校验汇总、错误行表格、返回产品列表和确认导入。

## 验证命令

```powershell
cd C:\Users\31333\Desktop\vibecoding\SDD_V7_1\Projects_Repo\central-bank-e-platform\backend
.\mvnw.cmd -q -pl central-bank-business -am "-Dtest=AdminProductServiceTest,AdminProductControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
.\mvnw.cmd -q -pl central-bank-business -am test
.\mvnw.cmd -q -pl ruoyi-admin -am -DskipTests package

cd C:\Users\31333\Desktop\vibecoding\SDD_V7_1\Projects_Repo\central-bank-e-platform\ruoyi-ui
npm.cmd run build:prod
```

## 验证结果

- 后端指定测试：通过。
- 后端业务模块全量测试：通过。
- ruoyi-admin 打包：通过。
- ruoyi-ui 生产构建：通过；存在若依模板已有的资源体积 warning，不影响本任务功能。

## 边界

- 真实 MySQL 8 本轮仍按本地/H2 fallback 自动验证边界记录，未宣称真实数据库完整联调。
- 页面字段严格保持 7 个业务字段；未新增参考利率、贷款额度、期限等原型外字段。
- 浏览器插件本轮未暴露可调用打开/截图工具，页面可视化以 ruoyi-ui 构建通过和源码核对为准。
