# T-018 若依工作概览真实闭环测试报告

## 结论

PASS

## 覆盖范围

- `GET /api/admin/dashboard/summary`：返回已发布内容数、金融产品数、后台账号数、今日操作数和最近 3 条发布内容。
- Service：组合内容、产品、账号和操作日志 mapper，今日操作按北京时间当天 00:00:00 至 23:59:59 统计。
- 契约字段：响应使用 `published_content_count/product_count/account_count/today_operation_count/recent_contents`。
- ruoyi-ui：首页替换为 A02 工作概览，删除若依默认框架介绍、技术选型、联系方式和捐赠内容。

## 验证命令

```powershell
cd C:\Users\31333\Desktop\vibecoding\SDD_V7_1\Projects_Repo\central-bank-e-platform\backend
C:\Users\31333\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd -q -pl central-bank-business -am "-Dtest=AdminDashboardServiceTest,AdminDashboardControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
C:\Users\31333\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd -q -pl central-bank-business -am test
C:\Users\31333\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd -q -pl ruoyi-admin -am -DskipTests package

cd C:\Users\31333\Desktop\vibecoding\SDD_V7_1\Projects_Repo\central-bank-e-platform\ruoyi-ui
npm.cmd run build:prod
```

## 验证结果

- 工作概览指定测试：通过。
- 后端业务模块全量测试：通过。
- ruoyi-admin 打包：通过。
- ruoyi-ui 生产构建：通过；存在若依模板已有的资源体积 warning，不影响本任务功能。

## 边界

- 本轮只实现 A02 工作概览真实统计和最近发布内容，不新增原型外图表、运营入口或若依默认首页内容。
- 真实 MySQL 8 本轮仍按 H2/local fallback 验证 mapper 与契约；完整真实数据库联调留给后续任务。
