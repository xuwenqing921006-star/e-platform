# 央行 E 平台线上全链路测试报告

测试时间：2026-07-04
测试环境：`https://xczx.hljfinance.com`
测试入口：H5 `/h5/`，后台 `/admin/login`
测试账号：使用用户提供的后台测试账号，密码不写入文档。

## 测试结论

当前系统核心 H5 公开接口、后台登录、后台授权业务接口、产品模板下载、产品 Excel 导入校验均可用。H5 首页、文章详情、产品详情可渲染，文章图片资源可访问。

正式上线前仍建议修复或确认以下问题：

1. `/h5` 无尾斜杠访问返回 404，`/h5/` 正常。
2. SAFE 外链当前网络下不可达，点击可能提示打开失败。
3. H5 内容数据不完整：当前只有肇州县 1 条服务指引；其他县域、金融服务指引、政策宣传为空。
4. 后台子路由直接打开/刷新存在被重定向到登录页的风险，需要复核 Nginx history fallback 和后台路由配置。
5. H5 产品详情长正文仍偏密，移动端阅读体验可以继续优化。

## 测试方式

- HTTP 状态测试：`curl`
- 真实浏览器渲染截图：Chrome headless
- 后台真实登录与菜单探测：Chrome DevTools Protocol
- 接口权限矩阵：公开接口、未登录接口、授权后台接口
- 文件能力：模板下载、Excel 导入校验

本轮没有提交新增、修改、删除业务数据；产品导入只执行了“校验”，没有执行“提交导入”。

## 入口测试

| 项目 | 结果 | 说明 |
| --- | --- | --- |
| `https://xczx.hljfinance.com/h5` | 不通过 | 返回 404 |
| `https://xczx.hljfinance.com/h5/` | 通过 | H5 首页正常渲染 |
| `https://xczx.hljfinance.com/admin/login` | 通过 | 后台登录页正常渲染 |
| `/admin/prod-api/captchaImage` | 通过 | 返回 `captchaEnabled=false` |
| `/prod-api/captchaImage` | 不通过 | 返回 404；如有旧前端仍请求根路径会出问题 |

## H5 测试

| 功能 | 结果 | 说明 |
| --- | --- | --- |
| H5 首页渲染 | 通过 | 首屏显示乡村振兴、金融服务、县域切换、服务指引卡片 |
| 乡村振兴服务指引 | 部分通过 | 肇州县有 1 条内容，肇源县/林甸县/杜蒙县为空 |
| 金融服务服务指引 | 部分通过 | 接口正常，但当前无数据 |
| 政策宣传 | 部分通过 | 接口正常，但当前无数据 |
| 助企通道产品列表 | 通过 | 产品列表接口返回 112 条 |
| 产品详情 | 通过 | 产品名称、银行、产品类型、准入条件、产品介绍、联系方式可加载 |
| 手机号拨打链接 | 通过 | 前端构建中已生成 `tel:` 链接 |
| 文章详情 | 通过 | 标题、科室、日期、正文图片正常显示 |
| 文章图片资源 | 通过 | `/admin/prod-api/profile/upload/...png` 返回 200 `image/png` |
| 文章附件 | 未覆盖到数据 | 当前测试文章详情未返回附件 |
| 图片放大查看 | 静态确认 | 前端构建中存在图片查看器和关闭按钮逻辑，本轮未做手势缩放自动化 |
| 禁止页面双指缩放 | 静态确认 | H5 HTML 含 `maximum-scale=1.0, user-scalable=no`，构建中只允许图片查看器内触摸缩放 |

## 后台测试

| 功能 | 结果 | 说明 |
| --- | --- | --- |
| 登录页渲染 | 通过 | 显示账号、密码、记住密码、登录按钮 |
| 后台登录 | 通过 | 浏览器表单登录后进入 `/admin/index` |
| 工作概览 | 通过 | 显示已发布内容 1、金融产品 112、后台账号 1、今日操作 427 |
| 菜单显示 | 通过 | 首页、内容管理、金融产品、账号管理、操作日志、修改密码入口可见 |
| 退出登录弹窗 | 通过 | 点击管理员/退出登录可出现“确定注销并退出系统吗？”确认弹窗 |
| 内容管理接口 | 通过 | 授权后 `/h5/api/admin/contents` 返回 200 |
| 金融产品接口 | 通过 | 授权后 `/h5/api/admin/products` 返回 200 |
| 账号管理接口 | 通过 | 授权后 `/h5/api/admin/accounts` 返回 200 |
| 操作日志接口 | 通过 | 授权后 `/h5/api/admin/audit-logs` 返回 200 |
| 后台选项接口 | 通过 | 授权后 `/h5/api/admin/options` 返回 200 |
| 未登录拦截 | 通过 | 未带 token 请求后台接口返回 401 |
| 子路由直达/刷新 | 需修复或复核 | 直接访问 `/admin/centralbank/content` 等子路由时出现重定向登录风险 |

## 产品导入测试

| 功能 | 结果 | 说明 |
| --- | --- | --- |
| 下载模板 | 通过 | `/h5/api/admin/products/import-template/download` 返回 200，文件大小约 3636 bytes |
| Excel 导入校验 | 通过 | 使用 `financial-product-import-template (3).xlsx` 校验成功 |
| 导入校验结果 | 通过 | `total_count=112`，`valid_count=112`，`invalid_count=0` |
| 导入提交 | 未执行 | 为避免线上数据重复，未执行 commit |

## API 路径测试

当前线上前端构建的 API base 为：

```text
/h5/api
```

测试结果：

| 路径 | 结果 | 说明 |
| --- | --- | --- |
| `/h5/api/public/products` | 通过 | H5 公开产品接口 |
| `/h5/api/public/contents` | 通过 | H5 公开内容接口 |
| `/h5/api/admin/**` | 通过 | 带若依 token 后可访问 |
| `/api/**` | 不通过 | 根路径 API 返回 404 |
| `/prod-api/**` | 不通过 | 根路径 prod-api 返回 404 |
| `/admin/prod-api/**` | 通过 | 若依登录与后台基础接口可用 |

建议：部署文档和 Nginx 配置中明确这三类路径的职责，避免后续前端包误构建到 `/api` 或 `/prod-api`。

## SAFE 外链测试

配置外链：

```text
http://zwfw.safe.gov.cn/asone/
```

测试结果：

| 地址 | 结果 |
| --- | --- |
| `http://zwfw.safe.gov.cn/asone/` | 超时 |
| `https://zwfw.safe.gov.cn/asone/` | 连接被重置 |

建议：

1. 确认外管平台是否允许从微信/H5 内打开。
2. 优先使用官方确认可访问的 HTTPS 地址。
3. H5 增加外链打开失败提示或复制链接兜底。

## UI 观察

1. H5 首页视觉整体可用，移动端首屏无明显重叠。
2. H5 文章详情图片已被限制在卡片宽度内，未再超出边框。
3. H5 产品详情文字较长，阅读密度偏高，部分段落靠近右侧边界，建议继续优化行高、段落间距、正文最大宽度和换行。
4. 后台工作概览页面视觉正常，指标卡和快捷操作清晰。

## 问题清单与修改建议

### P1：修复 `/h5` 无尾斜杠 404

现象：`https://xczx.hljfinance.com/h5` 返回 404，`/h5/` 正常。
建议：Nginx 增加：

```nginx
location = /h5 {
    return 301 /h5/;
}
```

### P1：复核后台子路由刷新/直达

现象：自动化直达 `/admin/centralbank/content`、`/admin/centralbank/product` 等路径时被重定向到登录页。
建议：

1. 后台 SPA history fallback 全部回落到 `/admin/index.html` 或后台实际入口。
2. 检查 token 存储、路由守卫和 Nginx rewrite 是否一致。
3. 人工复测：登录后进入内容管理，刷新页面，应保持当前页面而不是回登录页。

### P1：补齐 H5 内容数据

现象：政策宣传、金融服务指引、多个县域服务指引为空。
建议：正式发布前至少补齐每个栏目 1-3 条真实内容，否则用户会看到大量空态。

### P2：SAFE 外链不可达

现象：HTTP 超时，HTTPS 被重置。
建议：确认官方可访问地址；如外链不可控，增加“复制链接/稍后再试”兜底。

### P2：继续优化 H5 产品详情正文

现象：长文本可读性一般。
建议：正文 `line-height` 保持 1.7 左右，段落间距 10-12px，卡片内左右 padding 保持 16px，长连续文本启用 `overflow-wrap:anywhere`。

## 附件与证据

本次测试生成的截图和结果文件位于：

```text
.sdd/test-reports/runtime/
```

关键文件：

- `h5-home.png`
- `h5-product-detail.png`
- `h5-article-detail.png`
- `admin-login.png`
- `admin-index-after-login.png`
- `api-test-results.json`
- `cdp-admin-menu-test-results.json`
