# T-017 若依操作日志真实闭环测试报告

## 结论

PASS

## 覆盖范围

- `GET /api/admin/audit-logs`：管理员按操作人、操作类型、时间范围分页查询操作日志。
- 权限：普通账号访问操作日志返回 403，响应文案为“仅管理员可查看操作日志”。
- 记录服务：复用若依 `sys_oper_log`，将日志映射为契约字段 `operator_name/operation_type/object_type/object_name/description/operated_at`。
- 敏感信息：记录动作时对 `password`、`new_password`、`JWT_SECRET`、`token` 等键值脱敏，不写入真实密码或密钥值。
- ruoyi-ui：新增 A10 操作日志页，只包含原型中的查询、重置和列表。

## 验证命令

```powershell
cd C:\Users\31333\Desktop\vibecoding\SDD_V7_1\Projects_Repo\central-bank-e-platform\backend
C:\Users\31333\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd -q -pl central-bank-business -am "-Dtest=AuditLogServiceTest,AdminAuditLogControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
C:\Users\31333\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd -q -pl central-bank-business -am test
C:\Users\31333\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd -q -pl ruoyi-admin -am -DskipTests package

cd C:\Users\31333\Desktop\vibecoding\SDD_V7_1\Projects_Repo\central-bank-e-platform\ruoyi-ui
npm.cmd run build:prod
```

## 验证结果

- 操作日志指定测试：通过。
- 后端业务模块全量测试：通过。
- ruoyi-admin 打包：通过。
- ruoyi-ui 生产构建：通过；存在若依模板已有的资源体积 warning，不影响本任务功能。

## 边界

- 本轮只做 A10 查询页和关键动作记录，不加入若依默认导出、清空、删除日志功能。
- 真实 MySQL 8 本轮仍按本地/H2 fallback 自动验证边界记录，未宣称真实数据库完整联调。
