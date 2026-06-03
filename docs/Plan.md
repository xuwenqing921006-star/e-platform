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
| P01 | H01-A 服务指引列表 | 一级 Tab 左侧乡村振兴、右侧金融服务且默认选中乡村振兴；所有 H01 原型统一展示乡村振兴高亮态；二级 Tab、文章卡片、加载更多、空状态、重试 | `GET /api/public/contents` | 点击卡片到 H02 | 待开发 |
| P02 | H01-B 政策宣传列表 | 政策宣传 Tab、倒序文章卡片、加载更多 | `GET /api/public/contents` | 点击卡片到 H02 | 待开发 |
| P03 | H01-C 助企通道列表 | 产品统计、银行机构、产品名称、类型、加载更多 | `GET /api/public/products` | 点击卡片到 H03 | 待开发 |
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
| B08 | Excel 导入产品 | B07 | `/api/admin/products/import/*` | 待开发 |
| B09 | 后台账号管理 | B01、B02 | `/api/admin/accounts*` | 待开发 |
| B10 | 操作日志 | B02 | `GET /api/admin/audit-logs` | 待开发 |
| B11 | 工作概览 | B06、B07、B09、B10 | `GET /api/admin/dashboard/summary` | 待开发 |
| B12 | 初始数据 | B01、B07、B09 | 管理员、112 条产品、演示内容 | 待开发 |
| B13 | 真实前后端联调 | B02 至 B12 | 所有接口 | 待开发 |
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

- [ ] 补充分层实现思路。
- [ ] 固化 14 个办公室、17 个银行、2 个产品类型和 4 个县域编码。
- [ ] 实现后台选项接口。
- [ ] 编写接口测试。
- [ ] 自动验证通过。

### B02 登录认证与本人密码

- [ ] 补充分层实现思路。
- [ ] 实现 JWT 登录、当前用户和修改密码。
- [ ] 实现后台路由保护和权限上下文。
- [ ] 前端切换到真实认证接口验证。
- [ ] 自动验证通过。

### B03 公开 H5 内容查询

- [ ] 补充分层实现思路。
- [ ] 实现市级与县域服务指引自动分流。
- [ ] 实现文章列表、详情和分页。
- [ ] 前端 H5 内容页面切到真实接口验证。
- [ ] 自动验证通过。

### B04 公开 H5 产品查询

- [ ] 补充分层实现思路。
- [ ] 实现产品摘要列表和 7 字段详情。
- [ ] 确认响应不包含参考利率等额外字段。
- [ ] 前端 H5 产品页面切到真实接口验证。
- [ ] 自动验证通过。

### B05 本地附件存储

- [ ] 补充分层实现思路。
- [ ] 实现上传、格式校验、大小校验、删除和公开下载。
- [ ] 使用临时测试目录完成自动化验证。
- [ ] 自动验证通过。

### B06 后台内容管理

- [ ] 补充分层实现思路。
- [ ] 实现办公室权限隔离、县域分类限制、富文本过滤、CRUD 和日志。
- [ ] 前端内容管理切到真实接口验证。
- [ ] 自动验证通过。

### B07 后台产品管理

- [ ] 补充分层实现思路。
- [ ] 实现产品 CRUD、固定银行校验和模板下载。
- [ ] 产品模型严格限制为 7 个业务字段。
- [ ] 前端产品管理切到真实接口验证。
- [ ] 自动验证通过。

### B08 Excel 导入产品

- [ ] 补充分层实现思路。
- [ ] 实现 xlsx 校验、错误行反馈、一次性导入 Token 和确认提交。
- [ ] 验证 `阳光惠农贷` 特殊行。
- [ ] 前端导入页切到真实接口验证。
- [ ] 自动验证通过。

### B09 后台账号管理

- [ ] 补充分层实现思路。
- [ ] 实现账号 CRUD、状态、办公室绑定和密码重置。
- [ ] 前端账号页切到真实接口验证。
- [ ] 自动验证通过。

### B10 操作日志

- [ ] 补充分层实现思路。
- [ ] 实现关键动作记录和管理员分页查询。
- [ ] 前端日志页切到真实接口验证。
- [ ] 自动验证通过。

### B11 工作概览

- [ ] 补充分层实现思路。
- [ ] 实现统计指标和最近发布内容。
- [ ] 前端概览页切到真实接口验证。
- [ ] 自动验证通过。

### B12 初始数据

- [ ] 补充分层实现思路。
- [ ] 创建管理员和演示账号初始化策略。
- [ ] 导入 112 条产品：涉农 52 条，小微 60 条。
- [ ] 验证固定银行与特殊行。
- [ ] 自动验证通过。

### B13 真实前后端联调

- [ ] 全部业务页面使用 `VITE_USE_MOCK=false` 验证。
- [ ] 页面不得显示 `[Mock]` 标识或 Mock-only 文案。
- [ ] 证明浏览器请求命中真实后端接口。
- [ ] 自动验证通过。

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
  -> 112 条产品与初始账号数据
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
