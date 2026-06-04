# Bug 修复报告：后台内容办公室锁定与首页快捷入口权限

## 用户原话

> /sdd-bugfix
> 1、后台新增内容页面，如果登录的用户不是管理员，则默认选中对应办公室，不可修改
> 2、后台首页，快捷操作模块，如果用户没有对应的功能权限，直接不可点击，不要点击进去后报404了。

## 经验回查

- 是否已有相关经验：是
- 相关经验：
  - `T-013`：普通办公室账号只能管理本办公室内容，县域办公室只能发布服务指引，权限不能只靠前端。
  - `T-018`：工作概览只保留项目确认的统计、最近发布和快捷入口。
  - `Bugfix: 后台业务账号路由权限与登录错误提示`：业务菜单可见性必须同时经过若依角色菜单和项目业务角色过滤。
- 为什么仍然犯错：
  - 旧经验强调了服务端权限和菜单过滤，但没有把“表单默认值/禁用态”和“快捷入口禁用态”写成前端可执行验收项。
  - `/getInfo` 未暴露当前账号的业务扩展信息，前端无法可靠判断非管理员所属办公室。
  - 首页快捷操作使用无条件路由跳转，依赖路由权限兜底，导致用户体验变成点击后 404。

## 问题分析

- 现象：
  - 非管理员进入新增内容页面仍可修改发布办公室。
  - 没有金融产品权限的用户仍能点击“新增金融产品”“导入产品表格”等快捷入口，随后进入 404。
- 影响范围：
  - 若依后台 `内容管理` 新增/编辑弹窗。
  - 若依后台 `工作概览` 快捷操作模块。
  - `/getInfo` 当前用户上下文。
- 直接原因：
  - 内容页面只根据选中的办公室限制分类，没有根据当前登录账号锁定办公室。
  - 首页快捷入口没有读取按钮权限。
  - 当前用户 Store 未保存业务账号扩展。
- 根本原因：
  - 权限体验只做到了菜单/服务端拦截，没有延伸到表单默认值、字段禁用态和入口可点击状态。

## 修复方案

- 修改文件：
  - `backend/ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysLoginController.java`
  - `backend/ruoyi-admin/src/test/java/com/ruoyi/web/security/SecurityContractTest.java`
  - `ruoyi-ui/src/store/modules/user.js`
  - `ruoyi-ui/src/store/getters.js`
  - `ruoyi-ui/src/views/centralbank/content/index.vue`
  - `ruoyi-ui/src/views/index.vue`
  - `scripts/audit-mock-exit.mjs`
  - `docs/PRD.md`
  - `docs/Plan.md`
  - `docs/api-contracts.md`
  - `docs/澄清文档/admin-content-dashboard-permission/01-alignment.md`
- 修改说明：
  - `/getInfo` 返回 `account_extension`，包含当前业务角色、办公室编码、办公室名称和启用状态。
  - Vuex 保存 `accountExtension` 并提供 getter。
  - 内容新增/编辑弹窗中，非管理员按当前账号办公室默认选中并禁用办公室下拉；管理员保持可选。
  - 首页快捷操作按 `centralbank:content:add`、`centralbank:product:add`、`centralbank:product:import` 判断是否可用；无权限时按钮禁用并阻止跳转。
  - 补充契约、PRD、Plan、澄清文档和审计项。

## 验证方式

- 后端契约测试：
  - `.\mvnw.cmd -q -pl ruoyi-admin -am -Dtest=SecurityContractTest "-Dsurefire.failIfNoSpecifiedTests=false" test`
  - 结果：通过。测试日志中的 403 为既有权限测试主动触发。
- 审计脚本：
  - `node scripts\audit-mock-exit.mjs`
  - 结果：通过。
- 前端构建：
  - `npm.cmd run build:prod`
  - 结果：通过；仅保留若依模板资源体积 warning。
- 后端运行：
  - 已重打包并重启本地 `8003`。
  - `GET http://localhost:8003/health` 返回 `{ code: 200, message: "success", data: { status: "UP" } }`。

## experience.md 更新

- 新增/更新条目：新增“Bugfix: 后台内容办公室锁定与首页快捷入口权限”。
- 后续避坑规则：
  - 权限控制不能只靠菜单隐藏和路由拦截；表单默认值、字段禁用态、快捷入口可点击状态也要进入验收。
  - 当前用户上下文必须包含业务账号扩展，否则前端不能实现办公室级体验约束。
  - 快捷入口必须按按钮权限禁用，不能让用户点击后再由 404 或 403 兜底。
