# Bug 修复报告：后台路由权限与登录错误提示

## 用户原话

> /sdd-bugfix
> 1、我新建了一个账号，登录进去后菜单只有首页，应该是路由权限没配置好，我用ry这个账号登录进去菜单也不对，重新梳理角色权限及路由
> 2、我故意输错密码，给的提示是系统未知错误，请反馈给管理员，实际返回的{"code":500,"data":null,"message":"用户不存在/密码错误"}，把提示改成真实返回的提示，另外看看其他提示有没有类似问题。

## 经验回查

- 是否已有相关经验：是
- 相关经验：
  - `T-016`：账号管理不能直接复用若依系统用户字段，业务角色和办公室由 `cb_account_extension` 承载。
  - `T-020` 与历史 Bugfix：若依默认入口、菜单和后台可见能力必须进入审计。
  - `T-009`：认证失败和权限不足必须按真实 401/403 契约返回。
- 为什么仍然犯错：
  - 旧经验强调了业务扩展表和页面入口，但没有把“业务账号必须同步写入若依 `sys_user_role`”写成硬性规则。
  - 旧审计覆盖了菜单清理和接口契约，但没有覆盖新建账号后 `/getRouters` 的角色桥接。
  - 前端 request 封装仍沿用若依默认 `msg/errorCode` 优先级，没有按本项目统一响应字段 `message` 兜底所有异常分支。

## 问题分析

- 现象：
  - 新建业务账号登录后仅有首页。
  - `ry` 账号菜单集合不符合本项目业务角色口径。
  - 输错密码时页面显示“系统未知错误，请反馈给管理员”，没有展示后端返回的“用户不存在/密码错误”。
- 影响范围：
  - 后台登录后的菜单路由、按钮权限、账号管理创建流程。
  - 若依前端统一请求错误提示和下载错误提示。
  - 本地 Docker MySQL 中已存在的业务账号数据。
- 直接原因：
  - `AdminAccountService` 新建账号只写 `sys_user` 和 `cb_account_extension`，未写入若依 `sys_user_role`。
  - `/getRouters` 只读取若依角色菜单，未按本项目 `ADMIN/OFFICE_USER/office_code` 做业务菜单过滤。
  - 登录失败抛出的 `UserException/ServiceException` 没有稳定转换为契约化 401。
  - `ruoyi-ui/src/utils/request.js` 和下载插件优先使用默认错误文案，忽略了响应体 `message`。
- 根本原因：
  - 若依基座接入后，业务账号扩展模型与若依原生权限模型之间缺少明确桥接规则和回归审计。
  - 本项目统一响应字段是 `message`，但部分若依前端基础设施仍停留在 `msg` 字段口径。

## 修复方案

- 修改文件：
  - `backend/central-bank-business/src/main/java/com/centralbank/eplatform/service/AdminAccountService.java`
  - `backend/central-bank-business/src/main/java/com/centralbank/eplatform/mapper/CbAdminAccountMapper.java`
  - `backend/central-bank-business/src/main/resources/mapper/centralbank/CbAdminAccountMapper.xml`
  - `backend/central-bank-business/src/main/resources/sql/central_bank_admin_menu_cleanup.sql`
  - `backend/ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysLoginController.java`
  - `backend/ruoyi-framework/src/main/java/com/ruoyi/framework/web/exception/GlobalExceptionHandler.java`
  - `backend/ruoyi-framework/src/main/java/com/ruoyi/framework/web/service/SysLoginService.java`
  - `ruoyi-ui/src/utils/request.js`
  - `ruoyi-ui/src/plugins/download.js`
  - `backend/ruoyi-admin/src/test/java/com/ruoyi/web/security/SecurityContractTest.java`
  - `scripts/audit-mock-exit.mjs`
- 修改说明：
  - 新建业务账号时同步写入若依公共业务角色 `role_id=2`，删除业务账号时同步清理角色关系。
  - 当前库历史账号通过 SQL 补齐 `sys_user_role`，并确认缺失桥接数量为 0。
  - `/getInfo` 和 `/getRouters` 根据 `cb_account_extension` 重新收敛业务角色、按钮权限和菜单路由：
    - 管理员：内容管理、金融产品、账号管理、操作日志。
    - 货币信贷政策管理科办公室账号：内容管理、金融产品。
    - 其他办公室账号：内容管理。
  - 登录错误按 401 契约返回，前端优先展示响应体 `message`，下载插件同样优先使用 `message`。

## 验证方式

- 前端验证：
  - `npm.cmd run build:prod`
  - 结果：通过；仅保留若依模板资源体积 warning。
- 后端验证：
  - `.\mvnw.cmd -q -pl ruoyi-admin -am -Dtest=SecurityContractTest "-Dsurefire.failIfNoSpecifiedTests=false" test`
  - `.\mvnw.cmd -q -pl central-bank-business -am test`
  - `.\mvnw.cmd -q -pl ruoyi-admin -am -DskipTests package`
  - 结果：均通过。
- 审计验证：
  - `node scripts\audit-mock-exit.mjs`
  - 结果：通过。
- 真实接口抽样：
  - `GET http://localhost:8003/health` 返回 `{ code: 200, message: "success", data: { status: "UP" } }`
  - 错误账号登录返回 HTTP 401，body 为 `{ code: 401, data: null, message: "用户不存在/密码错误" }`
  - Docker MySQL 查询确认 `cb_account_extension` 中非管理员业务账号缺失 `role_id=2` 的数量为 0。

## experience.md 更新

- 新增/更新条目：新增“Bugfix: 后台业务账号路由权限与登录错误提示”。
- 后续避坑规则：
  - 业务账号落到若依登录主体时，必须同步 `sys_user_role`，不能只维护业务扩展表。
  - 菜单路由可见性必须同时经过若依菜单树和项目业务角色过滤。
  - 统一响应字段以 `message` 为准，前端错误处理不得让默认错误字典覆盖后端真实提示。
