# Bug 修复报告：金融产品联系人按钮、提交 500 与 H5 多联系人展示

## 用户原话

> /sdd-bugfix
> 1、新增金融产品页面，点击新增业务经办人后<button data-v-d8df2eac="" type="button" class="el-button el-button--danger el-button--medium is-plain is-circle"><!----><i class="el-icon-minus"></i><!----></button>这个按钮被挤变形了。
> 2、新增金融产品页面，点击确定按钮后、编辑产品后点击确认，response返回{
>     "code": 500,
>     "data": null,
>     "message": "Handler dispatch failed: java.lang.NoClassDefFoundError: com/fasterxml/jackson/core/util/InternalJacksonUtil"
> }
> 另外你看一下可添加多个联系人后，对应的H5页面的联系人联系电话是不也要对应增加，样式也要修改一下？

- 触发时间：2026-06-04 09:40
- 当前项目阶段：T-021 后本地验收 Bugfix
- 相关页面/接口/功能：若依后台金融产品新增/编辑、`POST/PUT /api/admin/products`、H5 产品详情

## 经验回查

- 是否已有相关经验：是
- 相关经验：
  - `T-014: 若依后台金融产品管理与模板下载真实闭环`
  - `Bugfix: 后台登录、县域内容分类、多联系人和 H5 返回上下文`
- 为什么仍然犯错：
  - 旧经验只约束了“多组联系人不扩展产品字段”，没有把按钮固定尺寸、H5 成对展示、Jackson 运行时依赖三件套写成可执行检查。
  - 上一轮打包验证没有覆盖当前运行 jar 是否为最新 fat jar，导致旧运行产物可能继续暴露 Jackson 运行时错配。

## 问题分析

- 现象：
  - 若依产品表单新增联系人后，减号圆按钮被网格列和 Element 默认按钮间距挤压。
  - 新增/编辑金融产品提交时报 `InternalJacksonUtil` 缺失。
  - H5 产品详情只把经办人和联系方式分别作为多行文本展示，不能直观看出多组联系人对应关系。
- 影响范围：
  - 后台金融产品新增/编辑体验与提交成功率。
  - H5 产品详情多联系人阅读体验。
- 直接原因：
  - 表单网格把两个按钮固定在 `36px` 列内，且未清理相邻按钮默认 `margin-left`。
  - `ruoyi-common` 只显式声明 `jackson-databind`，运行产物若出现旧 core 或未重新打包，Jackson 反序列化会缺 `InternalJacksonUtil`。
  - H5 未按换行顺序把 `business_manager` 与 `contact_info` 配对渲染。
- 根本原因：
  - 多联系人变更没有同步建立 UI 布局、运行时依赖和 H5 展示的回归测试。

## 修复方案

- 修改文件：
  - `ruoyi-ui/src/views/centralbank/product/index.vue`
  - `backend/ruoyi-common/pom.xml`
  - `backend/ruoyi-admin/src/test/java/com/ruoyi/web/security/SecurityContractTest.java`
  - `backend/central-bank-business/src/test/java/com/centralbank/eplatform/controller/AdminProductControllerTest.java`
  - `frontend/src/pages/h5/H5ProductDetailPage.vue`
  - `frontend/src/utils/productContacts.ts`
  - `frontend/src/styles/global.css`
  - `frontend/tests/h5-product-detail.spec.ts`
  - `scripts/audit-mock-exit.mjs`
  - `docs/PRD.md`
- 修改说明：
  - 若依产品表单把加减按钮放入固定动作区，按钮固定 32px，并清除默认相邻按钮外边距。
  - `ruoyi-common` 显式声明 `jackson-databind`、`jackson-core`、`jackson-annotations`，测试检查 `InternalJacksonUtil` 可加载。
  - 后台 Controller 测试覆盖 `contacts` 数组请求体解析。
  - H5 产品详情新增联系人拆分工具，多组联系人按行配对展示；仍只使用原契约的 `business_manager` 与 `contact_info` 两个字段。
  - 审计脚本新增联系人按钮布局、H5 配对展示和 Jackson 依赖回归检查。

## 验证方式

- 前端验证：
  - `frontend`: `npx.cmd vitest run --maxWorkers=1`
  - `frontend`: `npm.cmd run typecheck`
  - `frontend`: `npm.cmd run build`
  - `ruoyi-ui`: `npm.cmd run build:prod`
- 后端验证：
  - `backend`: `.\mvnw.cmd -q -pl central-bank-business -am test`
  - `backend`: `.\mvnw.cmd -q -pl ruoyi-admin -am -Dtest=SecurityContractTest "-Dsurefire.failIfNoSpecifiedTests=false" test`
  - `backend`: `.\mvnw.cmd -q -pl ruoyi-admin -am -DskipTests package`
  - 打包产物确认包含 `BOOT-INF/lib/jackson-core-2.19.4.jar`
  - 新 jar 启动后 `GET /health` 返回 200
- 审计：
  - `node scripts/audit-mock-exit.mjs`

## experience.md 更新

- 新增条目：`Bugfix: 金融产品多联系人按钮、Jackson 运行时依赖与 H5 配对展示`
- 后续避坑规则：
  - 图标按钮进入动态表单时必须固定尺寸和容器，不依赖 Element 默认圆形按钮尺寸。
  - Jackson 运行期三件套必须显式同版本，并验证 fat jar 中的 `BOOT-INF/lib`。
  - 多组联系人继续压缩进原两个契约字段时，H5 必须按行配对展示，不新增产品字段。
