# 央行 E 平台开发计划

> 设计阶段与开发阶段的唯一进度文件。所有开发进度以本文档为准。
> 原型：`docs/prototypes/central-bank-e-platform.pen`
> 产品定义：`docs/PRD.md`
> 接口契约：`docs/api-contracts.md`

## 零、若依接入架构决策

> 2026-06-02 确认：后台与后端基础能力基于 Harness 级若依 Spring Boot 3 模板裁剪适配，模板路径为 `templates/fullstack-ruoyi-vue/`。

- 保留当前已完成的 H5 Vue 3/TypeScript 前端，不推倒重来。
- 项目 `backend_profile = "ruoyi-springboot3"`，后端从“从零初始化 Spring Boot”调整为“基于 `templates/fullstack-ruoyi-vue/` 裁剪到项目 `backend/`”，优先复用登录、权限、用户、角色、菜单、日志、代码生成和 MyBatis 基础设施。
- 若依后台管理端保留模板内 `ruoyi-ui` 既有结构；SDD 后续只在若依约定内新增业务菜单、权限、路由、接口和页面，不重新生成一套替代后台管理框架。
- API 契约仍以 `docs/api-contracts.md` 为准。若依默认返回字段、权限结构或路由命名与契约不同，必须在后端适配到本项目约定的 `{ code, message, data }` 格式。
- 若依源码不得写入 `harness-core/`，不得作为 SDD 核心规则维护；业务代码只能写入当前项目目录。

## 一、功能清单总览

| 编号 | 功能名称 | 一句话描述 | 对应页面 | 优先级 | 状态 |
| --- | --- | --- | --- | --- | --- |
| F01 | H5 首页栏目 | 公开浏览金融服务和乡村振兴栏目 | H01-A 至 H01-E | MVP | 待开发 |
| F02 | H5 文章详情 | 阅读富文本正文并下载附件 | H02 | MVP | 待开发 |
| F03 | H5 产品详情 | 查看 7 项助企金融产品信息 | H03 | MVP | 待开发 |
| F04 | SAFE 浮动入口 | 展开、收起并跳转数字外管平台 | H01-A 至 H01-E | MVP | 待开发 |
| F05 | 后台登录与概览 | 登录、保持状态、查看工作概览 | A01、A02 | MVP | 待开发 |
| F06 | 后台内容管理 | 查询、发布、编辑、预览和删除内容 | A03、A04、A12 | MVP | 待开发 |
| F07 | 后台产品管理 | 查询、维护、下载模板和导入产品 | A05、A06、A07 | MVP | 待开发 |
| F08 | 后台账号管理 | 管理账号、状态和密码重置 | A08、A09 | MVP | 待开发 |
| F09 | 操作日志 | 查询关键后台操作 | A10 | MVP | 待开发 |
| F10 | 修改本人密码 | 已登录用户修改自己的密码 | A11 | MVP | 待开发 |
| F11 | 真实接口联调 | 前端退出默认 Mock，逐模块接入 Spring Boot | 全部页面 | MVP | 待开发 |
| F12 | E2E 与交付 | 全链路回归并输出启动文档 | 全部页面 | MVP | 待开发 |

## 二、数据契约摘要

完整接口契约见 `docs/api-contracts.md`。

### 2.1 统一响应格式

成功：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

错误：

```json
{
  "code": 400,
  "message": "错误描述",
  "data": null
}
```

分页：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [],
    "total": 0,
    "page": 1,
    "page_size": 20
  }
}
```

### 2.2 核心字段边界

产品列表仅返回：

```json
{
  "id": 2001,
  "bank_name": "农业银行",
  "product_name": "惠农e贷",
  "product_type": "AGRICULTURAL"
}
```

产品详情和产品编辑仅包含 7 个业务字段：

```text
银行机构、产品名称、类型、准入条件、产品介绍、业务经办人、联系方式
```

内容管理列表不展示附件个数。金融产品管理列表不展示参考利率。

## 二点五、外部服务与测试权限清单

> 进入自动化开发前必须确认。真实 Key、密码和 Secret 不写入本文档或任何 `docs/**`、`.sdd/**` 可读产物。

| 服务 | 用途 | 配置字段 | MVP 必需 | Tester 完整联调权限 | 缺失时策略 | 状态 |
| --- | --- | --- | --- | --- | --- | --- |
| MySQL 8 | 业务数据持久化 | `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` | 是 | 本地或测试环境数据库 | Agent 开发阶段可使用隔离 H2，真实 MySQL 联调前需补充 | 待确认 |
| 本地附件目录 | 存储上传附件 | `APP_STORAGE_ROOT` | 是 | 可读写测试目录 | 使用项目内临时测试目录验证 | 待确认 |
| JWT 签名密钥 | 后台登录 Token | `APP_JWT_SECRET` | 是 | 本地测试 Secret | 本地开发使用不提交的测试值 | 待确认 |
| SAFE 外部地址 | H5 外链跳转 | 固定地址，无密钥 | 是 | 可访问互联网 | 无网络时只验证链接地址 | 已确认 |
| 微信公众号菜单 | 正式入口配置 | 公众号后台权限 | 否 | 正式发布前配置 | 本地直接访问 H5 路径 | 不进入本地开发 |

本项目不调用付费 AI、短信、邮件、地图或第三方对象存储服务。

## 三、前端开发清单

### 3.1 基础设施

| 编号 | 工作项 | 说明 | 状态 |
| --- | --- | --- | --- |
| P00-01 | 初始化 Vue 项目 | Vue 3、TypeScript、Vite、Pinia、Vue Router、Axios | 待开发 |
| P00-02 | 配置代理 | `/api` 指向 `VITE_BACKEND_PROXY_TARGET`，默认 `http://localhost:8099` | 待开发 |
| P00-03 | 统一 API 封装 | 单一 Axios 实例、Bearer Token、401 处理 | 待开发 |
| P00-04 | Mock 层 | `frontend/src/mocks/` 集中维护，与接口契约完全一致 | 待开发 |
| P00-05 | 双入口路由 | `/h5/*` 公开，`/admin/*` 后台，后台统一守卫 | 待开发 |
| P00-06 | 公共样式 | 主色 `#1556D1`、移动端卡片、后台侧边栏、响应式规则 | 待开发 |

### 3.2 H5 页面

| 编号 | 页面 | 涉及功能 | Mock 数据来源 | 跳转 | 状态 |
| --- | --- | --- | --- | --- | --- |
| P01 | H01-A 服务指引列表 | 一级 Tab 左侧乡村振兴、右侧金融服务且默认选中乡村振兴；所有 H01 原型统一展示乡村振兴高亮态；二级 Tab、文章卡片、空状态、重试；不展示加载更多按钮，滚动接近底部或首屏未撑满视口时自动加载下一页 | `GET /api/public/contents` | 点击卡片到 H02 | 待开发 |
| P02 | H01-B 政策宣传列表 | 政策宣传 Tab、倒序文章卡片；不展示加载更多按钮，滚动接近底部或首屏未撑满视口时自动加载下一页 | `GET /api/public/contents` | 点击卡片到 H02 | 待开发 |
| P03 | H01-C 助企通道列表 | 产品统计、银行机构、产品名称、类型；不展示加载更多按钮，滚动接近底部或首屏未撑满视口时自动加载下一页 | `GET /api/public/products` | 点击卡片到 H03 | 待开发 |
| P04 | H01-D 县域服务指引 | 四县二级 Tab、三级文字 Tab、县域文章列表 | `GET /api/public/contents` | 点击卡片到 H02 | 待开发 |
| P05 | H01-E 县域服务队 | 固定服务队介绍、成员、职务、电话 | 前端固定配置 | 返回或切换 Tab | 待开发 |
| P06 | H02 文章详情 | 正文、图片、附件下载、返回上下文 | `GET /api/public/contents/{id}` | 返回 H01 | 待开发 |
| P07 | H03 产品详情 | 身份卡、详情卡、7 个产品字段 | `GET /api/public/products/{id}` | 返回 H01-C | 待开发 |
| P08 | SAFE 浮动入口 | Logo、展开、关闭、固定地址跳转、详情页隐藏 | 前端固定配置 | 外部地址 | 待开发 |

### 3.3 WEB 管理后台页面

> 架构切换说明：A01 至 A12 后台管理页面后续在若依 `ruoyi-ui` 中实现。当前 `frontend/` 中已完成的 A01 至 A04/A12 Mock 页面仅作为需求和交互参考，不再继续扩展自研后台 Mock。`frontend/` 后续只保留公众号 H5 端。

| 编号 | 页面 | 涉及功能 | Mock 数据来源 | 跳转 | 状态 |
| --- | --- | --- | --- | --- | --- |
| P09 | A01 登录 | 账号、密码、记住登录状态、错误提示 | `POST /api/auth/login` | 登录后到 A02 | 待开发 |
| P10 | A02 工作概览 | 指标卡、最近发布、快捷入口 | `GET /api/admin/dashboard/summary` | 到 A03、A05、A07 | 待开发 |
| P11 | A03 内容管理列表 | 查询、分页、查看、编辑、删除，不展示附件个数 | `GET /api/admin/contents` | 到 A04、A12 | 待开发 |
| P12 | A04 发布内容 | 标题、分类、办公室、富文本、最多 3 个附件 | 内容 CRUD、附件接口 | 保存后回 A03 | 待开发 |
| P13 | A05 金融产品列表 | 查询、分页、查看、编辑、删除、下载模板、Excel 导入，不展示参考利率 | 产品 CRUD、模板接口 | 到 A06、A07 | 待开发 |
| P14 | A06 新增金融产品 | 严格维护 7 个产品字段 | 产品 CRUD | 保存后回 A05 | 待开发 |
| P15 | A07 Excel 导入 | 上传、校验、错误行、提交导入 | 导入校验和提交接口 | 完成后回 A05 | 待开发 |
| P16 | A08 账号管理列表 | 查询、新增、编辑、删除、重置密码 | 账号 CRUD | 到 A09 | 待开发 |
| P17 | A09 新增账号 | 账号、姓名、角色、办公室、初始密码、状态 | 账号 CRUD、选项接口 | 保存后回 A08 | 待开发 |
| P18 | A10 操作日志 | 查询条件、分页列表 | `GET /api/admin/audit-logs` | 无 | 待开发 |
| P19 | A11 修改密码 | 当前密码、新密码、确认密码 | `POST /api/auth/change-password` | 当前页 | 待开发 |
| P20 | A12 内容详情预览 | 分类、标题、发布信息、正文、附件 | `GET /api/admin/contents/{id}` | 返回 A03 或编辑 | 待开发 |

### 3.4 前端文件结构

```text
frontend/
  src/
    assets/
    components/
      h5/
      admin/
      common/
    config/
    mocks/
    pages/
      h5/
      admin/
    router/
    services/
    stores/
    styles/
    types/
    utils/
```

### 3.5 前端自动验收标准

- [ ] H5 与后台全部页面路由可访问。
- [ ] 页面布局与 Pencil 原型一致。
- [ ] Mock 数据集中位于 `frontend/src/mocks/`。
- [ ] Mock 响应字段与 `docs/api-contracts.md` 一致。
- [ ] H5 产品列表没有利率字段，产品详情没有额外字段。
- [ ] 内容管理列表没有附件个数列。
- [ ] 金融产品管理列表没有参考利率列，存在下载模板按钮。
- [ ] `npm run typecheck` 通过。
- [ ] `npm run test` 通过。
- [ ] `npm run build` 通过。
- [ ] Agent/Tester 已完成浏览器页面检查。

## 四、后端开发清单

### 4.1 Java 环境

- **JDK**：进入开发阶段后由 Agent 执行 `java -version` 和 `javac -version`，必须为 17+。
- **构建工具**：基于若依 Maven 多模块工程裁剪，路径 `backend/mvnw.cmd`；如若依原包缺少 Wrapper，由 Agent 在 `backend/` 内补齐。
- **后端启动端口**：Agent 自动验证 `8099`，用户验收 `8003`。
- [ ] Agent 已确认 `java` / `javac` 均为 17+。
- [ ] Agent 已确认 Maven Wrapper 可执行。

### 4.2 后端任务

| 编号 | 功能名称 | 依赖 | 对应接口 | 状态 |
| --- | --- | --- | --- | --- |
| B00 | Spring Boot 基础设施 | 无 | `GET /health` | 待开发 |
| B01 | 固定配置与选项 | B00 | `GET /api/admin/options` | 待开发 |
| B02 | 登录认证与本人密码 | B00、B01 | `/api/auth/*` | 待开发 |
| B03 | 公开 H5 内容查询 | B00、B01 | `GET /api/public/contents*` | 待开发 |
| B04 | 公开 H5 产品查询 | B00、B01 | `GET /api/public/products*` | 待开发 |
| B05 | 本地附件存储 | B00、B02 | `/api/admin/attachments*`、公开下载 | 待开发 |
| B06 | 后台内容管理 | B01、B02、B05 | `/api/admin/contents*` | 待开发 |
| B07 | 后台产品管理 | B01、B02、B04 | `/api/admin/products*`、模板下载 | 待开发 |
| B08 | Excel 导入产品 | B07 | `/api/admin/products/import/*` | 已完成 |
| B09 | 后台账号管理 | B01、B02 | `/api/admin/accounts*` | 已完成 |
| B10 | 操作日志 | B02 | `GET /api/admin/audit-logs` | 已完成 |
| B11 | 工作概览 | B06、B07、B09、B10 | `GET /api/admin/dashboard/summary` | 已完成 |
| B12 | 初始数据 | B01、B07、B09 | 管理员、10 条真实样本产品、演示内容 | 已完成 |
| B13 | 真实前后端联调 | B02 至 B12 | 所有接口 | 已完成 |
| B14 | E2E 与交付文档 | B13 | `docs/startup.md` | 待开发 |

### 4.3 后端文件结构

```text
backend/
  pom.xml
  mvnw
  mvnw.cmd
  .mvn/wrapper/
  ruoyi-admin/
  ruoyi-common/
  ruoyi-framework/
  ruoyi-system/
  ruoyi-generator/
  ruoyi-quartz/
  central-bank-business/
    src/main/java/com/centralbank/eplatform/
      controller/
      domain/
      mapper/
      service/
      dto/
      config/
    src/main/resources/
      mapper/
ruoyi-ui/
  package.json
  src/
frontend/
  # 公众号 H5 端，后续不再承载后台管理扩展
```

### 4.4 后端实现规则

- 使用 Java 17+、Spring Boot 3.x 和若依后端基础能力；若若依原始版本为 Spring Boot 2.x，进入 B00 时必须先升级或确认兼容策略，不得降级本项目技术栈。
- 使用若依 MyBatis/MyBatis-Plus 风格的数据访问与 SQL 初始化/迁移能力；不再强制使用 Spring Data JPA 或 Flyway。
- 运行时使用 MySQL 8；自动化测试优先使用若依兼容的隔离测试数据库或 H2 fallback，若兼容性不足，必须在测试报告中明确标记降级边界。
- 真实密钥仅进入本地 secret 配置。
- Controller、Service、Mapper、Domain、DTO 分层明确。
- DTO 与 Domain 分离，禁止直接把持久化对象作为公开接口响应。
- 普通账号办公室隔离与产品管理权限必须在服务端校验。
- 富文本保存前执行白名单过滤。
- 附件目录通过 `APP_STORAGE_ROOT` 配置。
- 每完成一个后端业务功能，立即验证前端在 `VITE_USE_MOCK=false` 下命中真实接口。

## 五、功能详情

> 开发任意后端功能前，必须使用 `feature-plan` 流程在对应标题下补充分层实现思路、测试命令与验收结果。

### B00 Spring Boot 基础设施

- [ ] 补充分层实现思路。
- [ ] 从 `templates/fullstack-ruoyi-vue/` 裁剪后端模块与 `ruoyi-ui` 到项目工程，保留若依基础权限、用户、日志与配置能力。
- [ ] 补齐 Maven Wrapper、Java 17+、Spring Boot 3.x 兼容策略和本项目业务模块 `central-bank-business`。
- [ ] 增加契约适配层、统一响应、全局异常、CORS、测试配置和 `GET /health`。
- [ ] 执行 Maven 测试。
- [ ] 短时启动后执行 `curl http://localhost:8099/health`。
- [ ] 自动验证通过。

### B01 固定配置与选项

- [x] 补充分层实现思路。
- 分层实现思路：
  - Maven：新增 `backend/central-bank-business` 子模块，父级仍使用若依根 `pom.xml`，由 `ruoyi-admin` 引入该模块。
  - Domain/Mapper：按 MyBatis 风格建立内容、附件、金融产品、账号扩展等业务表 domain 与 mapper，占位到后续真实 CRUD 可复用。
  - SQL 初始化：提供 MySQL 运行脚本与 H2 fallback 脚本；自动化测试只执行 H2 脚本，避免触碰真实 MySQL。
  - 固定选项：在 service 层集中维护内容分类、产品类型、县域、办公室、银行机构选项，DTO 与 `GET /api/admin/options` 契约字段对齐。
  - Controller：提供 `GET /api/admin/options`，统一返回 `{ code, message, data }`，认证/放行策略留给 T-009 统一适配。
  - 测试：用 service 单测校验固定选项覆盖范围，用 H2 JDBC 测试校验 schema/seed 可重复初始化。
- [x] 固化 14 个办公室、17 个银行、2 个产品类型和 4 个县域编码。
- [x] 实现后台选项接口。
- [x] 编写接口测试。
- [x] 自动验证通过：`.\mvnw.cmd -q -pl central-bank-business -am test`、`.\mvnw.cmd -q test`。

### B02 登录认证与本人密码

- [x] 补充分层实现思路。
- 分层实现思路：
  - Security：复用若依 JWT 过滤器与 Spring Security 链，显式放行 `/health`、`/api/public/**` 和登录验证码等公开路径，`/api/admin/**` 默认要求认证。
  - 响应契约：调整若依 `AjaxResult`，把 `msg` 统一为 `message`，错误响应也返回 `data: null`。
  - 401/403：认证失败由 `AuthenticationEntryPointImpl` 返回 `{ code: 401, message, data: null }`；权限不足由专用 `AccessDeniedHandler` 返回 `{ code: 403, message, data: null }`。
  - 配置：`token.secret` 读取 `APP_JWT_SECRET`，上传目录读取 `APP_STORAGE_ROOT`，数据库连接读取 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`。
  - 测试：直接验证 401/403 handler 输出结构、AjaxResult 字段结构、安全链源码公开/受保护路径声明和配置文件敏感值占位。
- [ ] 实现 JWT 登录、当前用户和修改密码。
- [x] 实现后台路由保护和权限上下文基础适配。
- [ ] 前端切换到真实认证接口验证。
- [ ] 自动验证通过。
- T-009 验收：已适配 `/health`、`/api/public/**` 放行与 `/api/admin/**` 认证要求；401/403 返回 `{ code, message, data }`，JWT、数据库与附件目录配置从本地环境变量读取。验证命令：`backend/.mvnw.cmd -q test`、`backend/.mvnw.cmd -q -pl ruoyi-admin -am -DskipTests package`、health profile jar 短启动与敏感值扫描。

### B03 公开 H5 内容查询

- [x] 补充分层实现思路。
- 分层实现思路：
  - Mapper：在 `CbContentMapper` 中补充公开列表 count/list 查询，按 `category`、`scope`、`county_code` 过滤并按发布时间倒序分页。
  - Service：`PublicContentService` 负责参数校验、DTO 映射、`published_at` +08:00 字符串格式化和附件最多 3 个截断。
  - Controller：`PublicContentController` 提供 `GET /api/public/contents` 与 `GET /api/public/contents/{id}`，400/404 返回统一 `{ code, message, data }`。
  - 前端：现有 `publicContentService` 在 `VITE_USE_MOCK=false` 下通过 Axios 命中 `/api/public/contents*`，H5 Mock 提示仅 Mock 模式显示。
  - 测试：用 H2 seed、service 单测、MockMvc 路径测试和前端源码/构建验证真实接口切换边界。
- [x] 实现市级与县域服务指引自动分流。
- [x] 实现文章列表、详情和分页。
- [x] 前端 H5 内容页面切到真实接口验证。
- [x] 自动验证通过：`frontend npm run typecheck/test/build`、`backend .\mvnw.cmd -q -pl central-bank-business -am test`、`backend .\mvnw.cmd -q test`。

### B04 公开 H5 产品查询

- [x] 补充分层实现思路。
- 分层实现思路：
  - Mapper：在 `CbFinancialProductMapper` 中补充公开产品分页查询，列表只取 `id`、`bank_name`、`product_name`、`product_type` 摘要字段。
  - Service：`PublicProductService` 负责分页参数校验、摘要 DTO 映射和详情 DTO 映射；详情严格限制为产品 ID + 已确认 7 个业务字段。
  - Controller：`PublicProductController` 提供 `GET /api/public/products` 与 `GET /api/public/products/{id}`，400/404 返回统一 `{ code, message, data }`。
  - 前端：现有 `publicProductService` 在 `VITE_USE_MOCK=false` 下通过 Axios 命中 `/api/public/products*`；H5 Mock 提示仅 Mock 模式显示。
  - 测试：用 service 单测、MockMvc 路径测试、前端 typecheck/test/build 和后端全量 Maven 测试验证契约。当前完整 112 条产品来源已确认，B12/T-019 本轮仅抽取 10 条真实样本初始化。
- [x] 实现产品摘要列表和 7 字段详情。
- [x] 确认响应不包含参考利率等额外字段。
- [x] 前端 H5 产品页面切到真实接口验证。
- [x] 自动验证通过：`frontend npm run typecheck/test/build`、`backend .\mvnw.cmd -q -pl central-bank-business -am test`、`backend .\mvnw.cmd -q test`。

### B05 本地附件存储

- [x] 补充分层实现思路。
- 分层实现思路：
  - 配置：继续复用若依 `RuoYiConfig.getUploadPath()`，由 `APP_STORAGE_ROOT` 注入本地附件根目录，不在代码中硬编码真实路径。
  - Mapper：扩展 `CbAttachmentMapper` 的插入、查询和删除能力，附件元数据写入 `cb_attachment`。
  - Service：`AttachmentStorageService` 校验单文件 20MB、PDF/Word/Excel 扩展名与 MIME 类型，保存真实文件到配置目录，生成公开下载 URL，并提供每篇内容最多 3 个附件的复用校验。
  - Controller：`AdminAttachmentController` 实现后台上传和删除；`PublicAttachmentController` 实现公开下载文件流。
  - 测试：使用 JUnit 临时目录验证真实文件写入、下载和删除，不触碰真实附件目录；用 MockMvc 验证契约响应。
- [x] 实现上传、格式校验、大小校验、删除和公开下载。
- [x] 使用临时测试目录完成自动化验证。
- [x] 自动验证通过：`frontend npm run typecheck/test/build`、`backend .\mvnw.cmd -q -pl central-bank-business -am test`、`backend .\mvnw.cmd -q test`。

### B06 后台内容管理

- [x] 补充分层实现思路。
- 分层实现思路：
  - 后端：沿用 `central-bank-business` 业务模块，新增 `AdminContentController` 与 `AdminContentService`，不改若依登录、JWT、菜单加载或安全核心。
  - Mapper：扩展 `CbContentMapper` 的后台列表、创建、编辑、删除能力，并扩展 `CbAttachmentMapper` 的内容附件绑定/解绑能力。
  - 权限：Service 层基于若依登录 userId 查询 `cb_account_extension`，管理员可管理全部内容；普通办公室账号只能管理本办公室内容；县域办公室只能发布服务指引。
  - 前端：`ruoyi-ui/src/views/centralbank/content/index.vue` 使用若依 Vue2 + Element UI 页面模式，真实调用 `/api/admin/contents*`、`/api/admin/attachments*` 与 `/api/admin/options`。
  - 菜单：提供 `central_bank_content_menu.sql` 用于挂载若依菜单与按钮权限；真实菜单 SQL 执行归部署/初始化流程。
  - 边界：操作日志查询闭环归 T-017，本轮不提前实现审计日志页面。
- [x] 实现办公室权限隔离、县域分类限制、富文本过滤、CRUD 和附件绑定。
- [x] 前端内容管理切到真实接口验证。
- [x] 自动验证通过：backend Maven test/package、ruoyi-ui build、H5 frontend typecheck/test/build。

### B07 后台产品管理

- 分层实现思路（T-014）：
  - Domain/Mapper：复用 `CbFinancialProduct`，在 `CbFinancialProductMapper` 增加后台分页查询、按名称/银行/类型筛选、插入、更新和删除。
  - Service：新增 `AdminProductService`，统一校验分页、固定银行、产品类型和 7 个业务字段；产品管理权限限定管理员或货币信贷政策管理科账号。
  - Controller：新增 `/api/admin/products*` CRUD，并按契约提供 `GET /api/admin/products/import-template/download` xlsx 模板流。
  - ruoyi-ui：新增 `centralbank/product` API 与页面，列表不展示参考利率，表单只维护银行机构、产品名称、类型、准入条件、产品介绍、业务经办人、联系方式。
  - 测试：用 service/controller 单测覆盖权限、契约字段、模板表头和无参考利率字段；执行后端 Maven 测试、ruoyi-ui 构建和 ruoyi-admin 打包。
- [x] 补充分层实现思路。
- [x] 实现产品 CRUD、固定银行校验和模板下载。
- [x] 产品模型严格限制为 7 个业务字段。
- [x] 前端产品管理切到真实接口验证。
- [x] 自动验证通过：backend Maven test/package、ruoyi-ui build。

### B08 Excel 导入产品

- 分层实现思路（T-015）：
  - Service：新增金融产品导入服务，只解析 xlsx 首个工作表的 7 个确认业务字段，校验必填、固定银行和产品类型；`阳光惠农贷` 按固定银行特殊项通过，不新增参考利率等字段。
  - Token：校验通过的数据以一次性 `import_token` 暂存，确认提交后写入通过校验的产品并立即失效，重复提交返回 409。
  - Controller：提供 `POST /api/admin/products/import/validate` 和 `POST /api/admin/products/import/commit`，响应字段严格对齐 `docs/api-contracts.md`。
  - ruoyi-ui：新增 Excel 导入页，按 A07 原型展示上传、校验汇总、错误行表格、返回产品列表和确认导入；产品列表只增加原型已有的 Excel 导入入口。
  - 测试：先覆盖 xlsx 校验统计、错误行、固定银行、`阳光惠农贷` 特殊行、一次性提交和接口响应，再执行 backend Maven 与 ruoyi-ui 构建验证。
- [x] 补充分层实现思路。
- [x] 实现 xlsx 校验、错误行反馈、一次性导入 Token 和确认提交。
- [x] 验证 `阳光惠农贷` 特殊行。
- [x] 前端导入页切到真实接口验证。
- [x] 自动验证通过：`backend .\mvnw.cmd -q -pl central-bank-business -am test`、`backend .\mvnw.cmd -q -pl ruoyi-admin -am -DskipTests package`、`ruoyi-ui npm run build:prod`。

### B09 后台账号管理

- 分层实现思路（T-016）：
  - Mapper：复用若依 `sys_user` 作为登录账号主体，扩展 `cb_account_extension` 的分页查询、创建、更新、删除和启停字段维护，不复制若依系统用户管理页面的无关字段。
  - Service：新增后台账号服务，只允许管理员管理账号；普通账号必须绑定固定办公室，管理员账号不强制绑定办公室；禁止删除当前登录账号。
  - Controller：提供 `/api/admin/accounts*` CRUD 与 `/reset-password`，响应字段严格对齐 `docs/api-contracts.md`。
  - ruoyi-ui：新增账号管理列表与新增/编辑页面，按 A08/A09 只展示原型字段；不加入部门、岗位、邮箱、手机号等若依默认字段。
  - 测试：覆盖管理员权限、普通账号办公室绑定、重复账号、当前账号不可删除、重置密码、接口契约和前端构建。
- [x] 补充分层实现思路。
- [x] 实现账号 CRUD、状态、办公室绑定和密码重置。
- [x] 前端账号页切到真实接口验证。
- [x] 自动验证通过：`backend .\mvnw.cmd -q -pl central-bank-business -am test`、`backend .\mvnw.cmd -q -pl ruoyi-admin -am -DskipTests package`、`ruoyi-ui npm.cmd run build:prod`。

### B10 操作日志

- 分层实现思路（T-017）：
  - Mapper：复用若依 `sys_oper_log` 作为日志落点，新增本项目查询 mapper，将若依字段映射为 `operator_name/operation_type/object_type/object_name/description/operated_at`。
  - Service：只允许管理员查询；按操作人、类型、时间范围分页；记录关键动作时统一脱敏密码、JWT Secret、token 等敏感字段。
  - Controller：提供 `GET /api/admin/audit-logs`，响应严格对齐 `docs/api-contracts.md`。
  - ruoyi-ui：新增 A10 操作日志页，只保留原型中的查询、重置和列表，不加入若依默认导出、清空、删除等额外功能。
  - 测试：覆盖查询过滤、权限 403、契约响应、记录动作和敏感信息脱敏。
- [x] 补充分层实现思路。
- [x] 实现操作日志查询与记录服务。
- [x] 前端日志页切到真实接口验证。
- [x] 自动验证通过：`backend mvn -q -pl central-bank-business -am test`、`backend mvn -q -pl ruoyi-admin -am -DskipTests package`、`ruoyi-ui npm.cmd run build:prod`。

### B11 工作概览

- 分层实现思路（T-018）：
  - Service：组合内容、产品、账号和操作日志 mapper，计算已发布内容数、金融产品数、后台账号数、当日操作数和最近 3 条内容。
  - Controller：提供 `GET /api/admin/dashboard/summary`，响应字段严格对齐 `docs/api-contracts.md`。
  - ruoyi-ui：替换若依默认首页为 A02 工作概览，删除框架介绍、技术选型、联系方式、捐赠等无关内容，只展示统计、最近发布内容和快捷入口。
  - 测试：覆盖契约响应、真实 mapper 计数、最近内容时间格式和前端构建。
- [x] 补充分层实现思路。
- [x] 实现统计指标和最近发布内容。
- [x] 前端概览页切到真实接口验证。
- [x] 自动验证通过：`backend mvn -q -pl central-bank-business -am test`、`backend mvn -q -pl ruoyi-admin -am -DskipTests package`、`ruoyi-ui npm.cmd run build:prod`。

### B12 初始数据

- [x] 补充分层实现思路。
- 分层实现思路：
  - 真实产品来源为用户提供的 `全市银行机构涉农、小微信贷产品汇总表.xlsx`，该来源共有 112 条产品。
  - 本轮 T-019 按用户确认只抽取 10 条真实样本进行初始化闭环：涉农 5 条、小微 5 条；不声明 112 条全量已初始化。
  - seed 脚本必须可重复执行，采用固定 ID 与 `MERGE`/幂等插入策略；测试环境使用 H2 版本，运行环境使用 MySQL 版本。
  - seed 中不得写真实密码、JWT Secret 或生产数据库连接信息。
- [x] 建立基础业务表 schema 与最小 seed，并通过 H2 fallback 初始化测试。
- [x] 创建管理员和演示账号初始化策略。
- [x] 导入 10 条真实样本产品：涉农 5 条，小微 5 条。
- [x] 验证固定银行与 `阳光惠农贷` 特殊样本。
- [x] 完整初始数据自动验证通过。

### B13 真实前后端联调

- [x] 补充分层实现思路。
- 分层实现思路（T-020）：
  - 审计脚本：新增 `scripts/audit-mock-exit.mjs`，检查 H5 默认环境、Mock adapter 开关、H5 可见页面/config、若依后台登录/首页/布局/业务 API 的 Mock-only 与若依默认内容残留。
  - H5：`frontend/.env` 与 `.env.example` 默认 `VITE_USE_MOCK=false`；`frontend/src/services/api.ts` 仅在 `VITE_USE_MOCK=true` 时启用集中 Mock adapter；页面层不再保留 Mock 横幅文案。
  - 若依后台：移除导航栏源码/文档入口、默认若依标题/版权、登录页默认账号密码与 MockJs 上线提示；`api/centralbank/**` 继续命中 `/api/admin/*` Spring Boot API。
  - 测试边界：前端单测通过 `.env.test` 显式启用集中 Mock fixture，避免影响默认真实验收路径。
- [x] 全部业务页面默认真实路径静态审计完成：`node scripts/audit-mock-exit.mjs`。
- [x] 页面与业务入口无 `[Mock]` 或等价 Mock-only 可见文案残留。
- [x] H5、后端业务模块、`ruoyi-admin` 与 `ruoyi-ui` 自动验证通过。
- [x] Tester 复核完成：H5 service 默认经 `/api` 命中 `/api/public/*`，若依 centralbank service 命中 `/api/admin/*`；已通过后端 health profile 短启动验证 Spring Boot 进程路径，真实 MySQL 环境未在本轮宣称。

### B14 E2E 与交付文档

- [ ] 执行 H5 和后台全链路回归。
- [ ] 执行前端 typecheck、test、build。
- [ ] 执行后端 Maven 测试和短时启动检查。
- [ ] 输出 `docs/startup.md`。
- [ ] 自动验证通过。
- [ ] 用户最终验收通过。

## 六、开发顺序

### 阶段 1：H5 与已完成后台 Mock 参考

```text
P00 基础设施
  -> P01-P08 H5
  -> P09-P12/P20 自研后台 Mock 参考（已到 T-006 为止）
  -> 停止继续扩展自研后台 Mock
```

### 阶段 2：若依全栈基座

```text
T-007 若依 Spring Boot 3 + ruoyi-ui 基座接入
  -> T-008 业务数据层、固定选项与初始化
  -> T-009 若依安全、接口契约与 H5 公开路由适配
```

### 阶段 3：逐功能闭环

```text
H5 公开内容
  -> H5 公开产品
  -> 附件上传与公开下载
  -> 若依后台内容管理
  -> 若依后台产品管理
  -> 若依 Excel 导入
  -> 若依账号管理
  -> 若依操作日志
  -> 若依工作概览
  -> 10 条真实样本产品与初始账号数据
  -> 全部页面退出 Mock 审计
```

### 阶段 4：E2E 与交付

```text
B14 全链路回归
  -> docs/startup.md
  -> 用户最终验收
```

## 七、门禁与自动验证

### 7.1 前端自动验证

```powershell
cd frontend
npm run typecheck
npm run test
npm run build
```

### 7.2 后端自动验证

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8099"
curl http://localhost:8099/health
```

### 7.3 用户验收端口

```text
后端：http://localhost:8003
前端：http://localhost:5175
```

## 八、配置模板字段

### 8.1 前端

```env
VITE_API_BASE_URL=/api
VITE_USE_MOCK=true
VITE_BACKEND_PROXY_TARGET=http://localhost:8099
```

### 8.2 后端

```env
DB_URL=jdbc:mysql://localhost:3306/central_bank_e_platform
DB_USERNAME=
DB_PASSWORD=
APP_STORAGE_ROOT=
APP_JWT_SECRET=
```
