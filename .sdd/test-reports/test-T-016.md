# T-016 若依后台账号管理真实闭环测试报告

## 结论

PASS

## 覆盖范围

- `GET /api/admin/accounts`：管理员分页查询账号，支持姓名/账号、办公室、角色筛选。
- `POST /api/admin/accounts`：新增账号写入若依 `sys_user` 与业务扩展 `cb_account_extension`，普通账号必须绑定办公室，重复账号返回 409。
- `GET/PUT/DELETE /api/admin/accounts/{id}`：详情、编辑、启停、办公室绑定、删除；当前登录账号不可删除。
- `POST /api/admin/accounts/{id}/reset-password`：管理员重置密码，保存加密后密码。
- ruoyi-ui：新增 A08 账号管理列表页、A09 新增/编辑账号页，字段限定为原型中的账号、姓名、角色、所属机构、初始/确认密码、账号状态。

## 验证命令

```powershell
cd C:\Users\31333\Desktop\vibecoding\SDD_V7_1\Projects_Repo\central-bank-e-platform\backend
.\mvnw.cmd -q -pl central-bank-business -am "-Dtest=AdminAccountServiceTest,AdminAccountControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
.\mvnw.cmd -q -pl central-bank-business -am test
.\mvnw.cmd -q -pl ruoyi-admin -am -DskipTests package

cd C:\Users\31333\Desktop\vibecoding\SDD_V7_1\Projects_Repo\central-bank-e-platform\ruoyi-ui
npm.cmd run build:prod
```

## 验证结果

- 后端账号管理指定测试：通过。
- 后端业务模块全量测试：通过。
- ruoyi-admin 打包：通过。
- ruoyi-ui 生产构建：通过；存在若依模板已有的资源体积 warning，不影响本任务功能。

## 边界

- 真实 MySQL 8 本轮仍按本地/H2 fallback 自动验证边界记录，未宣称真实数据库完整联调。
- 页面与接口不加入若依默认部门、岗位、邮箱、手机号等原型外字段。
- 浏览器插件本轮未暴露可调用打开/截图工具，页面可视化以 ruoyi-ui 构建通过和源码核对为准。
