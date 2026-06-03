# 项目经验

> 当前项目长期有效的经验。
> Developer / Tester / Bugfix 在任务完成后维护本文件。

---

## Harness 系统经验摘要

新项目开始时，Developer / Tester / Bugfix 需要同时参考：

- 当前项目经验：`.sdd/experience.md`
- 系统级经验：`<SDD_V6>/memory/harness-experience.md`

---

（项目经验将在开发过程中追加）

### T-001: 前端双入口与 Mock 基础设施
- **陷阱**：Vitest 3.x 开发依赖命中 critical 安全公告；测试文件使用 Node API 时还需要显式安装 `@types/node` 并加入 TypeScript `types`。
- **经验**：基础设施任务也应先用结构与行为测试确认 RED，再补 Vue 路由、守卫、Axios Mock adapter 和集中 Mock dispatcher；Mock handler 返回前必须显式映射 endpoint DTO，不能直接透出内部实体。
- **避坑**：后续前端功能继续复用 `frontend/src/mocks/` 与单一 Axios 实例；新增 endpoint 时先对照 `docs/api-contracts.md` 定义独立响应 DTO，并保持 `VITE_USE_MOCK=true` 阶段不访问真实后端。
- **Retry 1 经验**：基础页只要展示业务数据，也必须通过 `services/` 调用单一 Axios 实例并消费契约 DTO，禁止在页面内临时硬编码 Mock 数组；后台布局使用移动优先的单栏默认值，再在桌面断点恢复固定侧栏和多栏概览；原型中的菜单、元信息和品牌图标应使用可复用图标组件，不能用文本字符占位。
- **[SYSTEM] 建议回传系统级经验**：基础设施任务的占位页同样需要执行集中 Mock、移动宽度收敛和导出原型图标结构检查，避免把可见页面误当作后续任务再补齐。
- **Retry 2 经验**：集中 Mock 检查不能只扫描页面文件；布局组件中的 header 用户名、状态标签和其他可见业务文案也必须纳入扫描。认证相关展示值由认证 store 统一提供，组件模板只消费 store getter；Mock-only 前缀不得进入最终可见文案。
- **[SYSTEM] 建议回传系统级经验**：Mock 阶段验收应递归扫描 `frontend/src/pages/**` 与 `frontend/src/components/**` 的 `[Mock]` 残留，并检查布局组件是否绕过 service/store 重复硬编码业务 fixture。
- **Retry 3 纠正**：Mock 阶段的集中管理与可见标识必须同时满足。`pages/**` 与 `components/**` 中禁止散落 `[Mock]` 业务字面量，但 `frontend/src/mocks/**` 返回给界面的可见 fixture 必须保留 `[Mock]` 或项目统一可见标识；只有切换真实接口退出 Mock 阶段后才能移除标识。
- **[SYSTEM] 建议回传系统级经验**：Mock 验收应做双向断言：展示组件无散落 fixture，集中 Mock fixture 或统一 banner 在 `VITE_USE_MOCK=true` 时存在可见标识。禁止把“清理组件硬编码”误实现为“删除全部 Mock 标识”。

### T-002: H5 首页栏目与 SAFE 浮动入口 Mock
- **陷阱**：H5 首页同时包含接口 Mock、固定县域配置和全局浮动入口，若把 Tab、服务队或 SAFE 地址直接写进页面，会重新引入分散业务数组；Vitest 挂载含 `src="/safe-logo.png"` 的 SFC 时还会把 public 路径误作模块文件读取。
- **经验**：栏目 Mock entity 与公开列表 DTO 分离，handler 返回前按 endpoint 显式映射；固定 Tab、四县服务队和 SAFE 地址分别收敛到 `src/config/`。SAFE Logo 路径使用配置变量动态绑定，可同时兼容 Vite 构建和 Vitest 挂载测试。
- **避坑**：无网络环境验证 SAFE 时只报告固定地址 fallback，不宣称外链真实可达；H5 详情隐藏规则放在全局入口判定函数，后续实现 H02/H03 时无需复制浮动入口控制逻辑。

### T-003: H5 文章详情 Mock
- **陷阱**：Pencil MCP 可能因未连接运行中的 Pencil 应用而不可用；此时只能按 `docs/prototypes/exports/README.md` 映射到 H02 导出图并结合 PRD 回退，不能用 shell 读取 `.pen`。
- **经验**：文章详情需要独立于列表 DTO 定义 `PublicContentDetailData`，Mock 内部实体可包含 `office_code`、分流字段和超过 3 个附件，但 `/api/public/contents/{id}` handler 返回前必须显式映射契约字段并截断附件。
- **避坑**：富文本图片由详情页事件委托处理，页面只消费 service 返回的 HTML，不把业务正文散落在页面；SAFE 详情隐藏规则继续复用 `shouldShowSafeEntry`，新增 `/h5/contents/:id` 时要保留该回归测试。

### T-004: H5 产品详情 Mock
- **陷阱**：产品详情的“7 个业务字段”与接口契约中的响应字段名必须同时核对；页面不应把参考利率、贷款额度、期限等 Excel 或后台常见字段顺手带入。
- **经验**：公开产品详情需要独立于列表 DTO 定义 `PublicProductDetailData`，Mock 内部实体可保留 `bank_code`，但 `/api/public/products/{id}` handler 返回前必须显式映射为契约 DTO；详情页会展示的每个 Mock 字符串字段也要带 `[Mock]` 标识。
- **避坑**：产品列表卡片只做摘要展示并链接到 `/h5/products/:id`；H03 页面只消费 `publicProductService`，业务 fixture 继续集中在 `frontend/src/mocks/`，详情页隐藏 SAFE 入口仍复用全局路径规则。
### T-005: 后台登录与工作概览 Mock
- **陷阱**：基础设施阶段的登录 Mock 不能只检查账号密码字段存在，否则错误密码也会被当作登录成功；工作概览也不能直接把全部内容实体映射为最近发布列表。
- **经验**：认证请求通过独立 `authService` 调用统一 Axios 实例，Store 只负责保存 Mock Token 和用户状态；概览 handler 按 `GET /api/admin/dashboard/summary` 显式构造 DTO，并把最近发布限制为原型中的三条摘要。
- **避坑**：快捷入口需要使用路由链接指向后续页面；Mock 可见数据继续集中在 `frontend/src/mocks/` 并保留 `[Mock]` 标识，页面与组件不散落 fixture。

### T-006: 后台内容管理 Mock
- **陷阱**：后台内容列表与详情使用不同 DTO；列表若直接复用内部内容实体会把 `attachments` 或附件个数泄露到 A03。Pencil MCP 未连接时仍需使用导出图映射核对 A03、A04、A12，不能直接读取 `.pen`。
- **经验**：后台内容查询、新增、详情、编辑、删除和后台附件上传、删除分别按 endpoint 显式映射 DTO；附件 handler 同时校验最多 3 个、单个不超过 20MB，以及 PDF、Word、Excel 格式。页面仅通过 `adminContentService` 与 `attachmentService` 消费集中 Mock。
- **避坑**：A03 表格固定只展示标题、分类、发布机构、发布时间和操作；富文本正文留在集中 Mock 和编辑器中，页面不散落业务 fixture。编辑页移除附件时同时更新 Mock 绑定关系与表单 `attachment_ids`。
- **Retry 1 经验**：后台列表即使 Mock 已返回 `page/page_size/total`，页面也必须提供上一页、下一页或页码入口，并在切页时重新请求列表；A04 富文本工具栏需要对齐原型的链接、图片、表格入口，Mock 阶段交互可通过运行时插入可见片段验证。
- **避坑**：办公室下拉值、保存 payload 和详情回显名称要以 `GET /api/admin/options` 契约为单一来源；`CURRENCY_GOLD_SILVER` 不能简写为 `CURRENCY_GOLD`，Mock 名称映射必须覆盖全部可提交办公室编码。
- **[SYSTEM] 建议回传系统级经验**：固定选项必须以 `api-contracts.md` 或统一 options DTO 为单一来源；Mock 名称映射需要覆盖全部可提交值，并通过逐项保存后详情回显测试验证。

### T-007: 若依全栈基座接入
- **陷阱**：若依后端模板默认 `application-druid.yml` 会在启动期连接本地 MySQL；仅验证 `/health` 时如果不隔离 druid/MyBatis/Mapper 扫描，短时启动会因数据库不可达提前退出。
- **经验**：基础设施裁剪应先用结构脚本观察 RED，再复制模板并复跑 GREEN；健康检查可使用独立 `health` profile 做数据库无关启动基线，真实业务 profile 仍保留若依默认 druid/MySQL 边界。
- **避坑**：运行单模块 Maven 测试时带 `-pl ruoyi-admin -am`，并在指定单测时给无匹配测试的上游模块设置 `-Dsurefire.failIfNoSpecifiedTests=false`；PowerShell 中带点的 `-D` 参数需要加引号。
- **Retry 1 陷阱**：`health` profile 禁用 Mapper 扫描后，默认 Spring Security filter chain 仍会在首次请求时懒加载 `UserDetailsServiceImpl`，进而要求 `SysUserMapper`。此时 controller 上的 `@Anonymous` 尚不足以保证 `/health` 可访问，错误转发还会被 `/error` 的认证失败响应覆盖。
- **Retry 1 经验**：完整业务 profile 在 `SecurityConfig` 中显式放行 `/health` 与 `/error`；`health` profile 使用独立最小安全链，只放行 `/health` 与 `/error`、拒绝其余请求，并排除依赖业务 Mapper 的 `UserDetailsServiceImpl`。
- **Retry 1 避坑**：健康检查不能只写 standalone MockMvc 测试。使用 `@SpringBootTest`、`@AutoConfigureMockMvc` 和 `@ActiveProfiles("health")` 覆盖真实 filter chain，同时断言 health profile 下后台业务路径返回 403。
- **[SYSTEM] 建议回传系统级经验**：数据库无关 health profile 必须同时隔离数据源、Mapper 扫描和依赖 Mapper 的认证服务；公开健康端点需要显式 allowlist，并用真实 Security filter chain 测试验证。

### Bugfix: H5 详情页字号过大
- **触发**：用户反馈“服务指引详情页、政策宣传详情页、助企通道详情页里的文字都太大了”。
- **根因**：H5 详情页沿用了偏桌面/后台的大字号：文章标题 28px、正文 20px、产品标题 32px、产品字段正文 20px 且高字重，移动端阅读压迫感明显。
- **已有经验回查**：已有 `T-003` 和 `T-004` 约束详情页数据契约、富文本消费方式和 SAFE 隐藏规则，但没有约束移动端详情页字号。
- **为什么仍然犯错**：验收测试只覆盖了契约字段和页面元素，没有把详情页移动端字号作为可执行断言。
- **修复**：收敛 `frontend/src/styles/global.css` 中 H5 文章详情和产品详情的标题、正文、字段标签、卡片间距和图标尺寸；补充详情页字号回归测试。
- **避坑规则**：H5 详情页必须按移动端阅读规格验收，正文建议 16px 左右，主标题约 21-22px；不得把后台/桌面页面的 28px+ 标题和 20px+ 正文字号直接套到手机详情页。

### Bugfix: H5 首页移除加载更多按钮
- **触发**：用户在浏览器评论中指出“加载更多”按钮不需要，并确认改为滚动到底自动加载。
- **根因**：早期 PRD/Plan 和测试仍要求 H5 列表支持显式加载更多，页面实现按旧口径保留了按钮；仅删除按钮会导致后续分页数据不可见。
- **已有经验回查**：已有 `T-002` 约束 H5 首页配置和页面行为集中管理，但没有覆盖用户验收后按钮去留的文档同步要求。
- **为什么仍然犯错**：按钮去留属于产品口径调整；如果只改页面不改 PRD/Plan 和测试，后续开发容易按旧验收点恢复按钮。
- **修复**：移除 `H5LandingPage.vue` 的加载更多按钮，保留分页接口并改为滚动接近底部自动加载下一页；首屏内容未撑满视口时自动补拉下一页；更新 H5 首页测试，并同步修改 `docs/PRD.md` 与 `docs/Plan.md`。
- **避坑规则**：用户验收确认按钮、入口或文案去留时，必须同时确认替代交互；移除分页按钮时要保留滚动自动加载或明确只展示固定数量，并额外验证短页/大屏无滚动条时也能继续加载；测试中同时断言入口不存在和替代加载路径可用。

### T-008: 若依业务数据层、固定选项与初始化
- **陷阱**：新增若依业务模块时，如果包名不在 `RuoYiApplication` 扫描范围或 `MapperScan` 范围内，模块能编译但运行时 controller/mapper 不会生效。
- **经验**：业务模块使用独立 Maven 子模块 `central-bank-business`，同时在 `ruoyi-admin` 引入依赖，并把 `com.centralbank.eplatform` 加入 Spring 扫描和 MyBatis mapper 扫描。
- **避坑**：真实 MySQL 未提供时，只能声明 H2 fallback 验证通过；测试必须真实执行 H2 schema/seed，不能只检查 SQL 文件存在。
- **后续边界**：T-008 只落基础表结构、固定选项和最小 seed；完整账号与 112 条金融产品数据仍归 T-019。
### T-009: 若依安全、契约响应与 H5 公开路由适配
- **陷阱**：若依 `ServletUtils.renderString` 默认会把 HTTP 状态码设为 200；认证失败和权限不足如果只在 body 中写 `code`，前端按 HTTP status 判断时会失真。
- **经验**：为 `ServletUtils.renderString` 增加带 status 的重载，并在 `AuthenticationEntryPointImpl` 与专用 `AccessDeniedHandler` 中分别写入真实 401/403。
- **陷阱**：Fastjson 手写响应默认会省略 map 中的 null 值，即使 `AjaxResult` 内部已经包含 `data: null`，序列化后也可能丢失契约字段。
- **经验**：安全过滤器中使用 `JSONWriter.Feature.WriteMapNullValue` 输出 401/403，保证 `{ code, message, data }` 结构稳定。
- **避坑**：`spring-boot:run` 带 `-am` 时可能在父工程执行并找不到 main class；短启动验证优先用 `-pl ruoyi-admin -am -DskipTests package` 打 jar，再 `java -jar` 指定 profile 和端口。
- **后续边界**：T-009 只完成安全链、契约响应和配置入口适配；完整 `/api/auth/*` 登录、当前用户、本人改密和具体业务 API 联调留给后续闭环任务。
### T-010: H5 公开内容查询真实联调闭环
- **陷阱**：Java `LocalDateTime` 直接交给 Jackson 可能与前端契约中的 `published_at` 字符串不一致；公开 H5 DTO 应在 service 层明确格式化为 `+08:00` ISO 字符串。
- **经验**：公开接口 DTO 独立于数据库 domain，列表只暴露摘要字段，详情才暴露富文本和附件；附件在 service 层截断到最多 3 个，避免 mapper 或页面重复实现限制。
- **陷阱**：前端切到 `VITE_USE_MOCK=false` 后，如果页面仍无条件渲染 Mock 提示，即使数据来自真实接口也会失败。
- **避坑**：Mock 提示、Mock banner 和 Mock-only 文案必须绑定 mock mode；真实接口联调任务除了 service 路径，还要检查页面可见文案。
- **后续边界**：真实 MySQL 未提供时，只能用 H2 seed 与 MockMvc 验证契约路径；不要宣称真实数据库已联调。附件下载文件流仍归 T-012。
