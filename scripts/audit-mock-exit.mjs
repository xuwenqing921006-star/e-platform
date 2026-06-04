import { existsSync, readFileSync } from 'node:fs'
import { join } from 'node:path'

const root = process.cwd()
const violations = []

const read = (relativePath) => readFileSync(join(root, relativePath), 'utf8')

function requireFile(filePath) {
  if (!existsSync(join(root, filePath))) {
    violations.push(`${filePath}: missing required delivery document`)
    return false
  }
  return true
}

function requireEnvValue(filePath, key, expected) {
  const content = read(filePath)
  const match = content.match(new RegExp(`^${key}\\s*=\\s*(.+)$`, 'm'))
  if (!match) {
    violations.push(`${filePath}: missing ${key}`)
    return
  }
  const actual = match[1].trim()
  if (actual !== expected) {
    violations.push(`${filePath}: ${key} must be ${expected}, got ${actual}`)
  }
}

function rejectPatterns(filePath, checks) {
  const content = read(filePath)
  for (const [label, pattern] of checks) {
    if (pattern.test(content)) {
      violations.push(`${filePath}: contains ${label}`)
    }
  }
}

function requirePatterns(filePath, checks) {
  const content = read(filePath)
  for (const [label, pattern] of checks) {
    if (!pattern.test(content)) {
      violations.push(`${filePath}: missing ${label}`)
    }
  }
}

function rejectSensitiveContent(filePath) {
  rejectPatterns(filePath, [
    ['OpenAI-style API key', /\bsk-[A-Za-z0-9_-]{16,}\b/],
    ['Bearer token value', /\bBearer\s+[A-Za-z0-9._-]{12,}\b/i],
    ['JWT token value', /\beyJ[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\b/],
    [
      'secret config value',
      /^[\t -]*(?:DB_PASSWORD|APP_JWT_SECRET|JWT_SECRET|token\.secret|REDIS_PASSWORD|MYSQL_ROOT_PASSWORD|MYSQL_PASSWORD)\s*[:=]\s*(?!\s*['"]?(?:$|<[^>\r\n]+>|\$\{[^}\r\n]+}|占位|留空|空|未填写|已配置|由环境变量|不写入|配置字段|placeholder|null|N\/A|N-A|无|central-bank-local-[A-Za-z-]+)['"]?\s*$).+/im,
    ],
  ])
}

requireEnvValue('frontend/.env', 'VITE_USE_MOCK', 'false')
requireEnvValue('frontend/.env.example', 'VITE_USE_MOCK', 'false')
requireEnvValue('frontend/.env', 'VITE_API_BASE_URL', '/api')
requireEnvValue('frontend/.env.example', 'VITE_API_BASE_URL', '/api')

rejectPatterns('frontend/src/services/api.ts', [
  ['Mock as implicit default', /VITE_USE_MOCK\s*!==\s*['"]false['"]/],
])

for (const filePath of [
  'frontend/src/pages/h5/H5LandingPage.vue',
  'frontend/src/pages/h5/H5ArticleDetailPage.vue',
  'frontend/src/pages/h5/H5ProductDetailPage.vue',
  'frontend/src/components/h5/H5SafeEntry.vue',
  'frontend/src/config/h5Home.ts',
]) {
  rejectPatterns(filePath, [
    ['visible [Mock] marker', /\[Mock\]/],
    ['visible simulated-data notice', /当前页面使用模拟数据/],
    ['Mock notice as implicit default', /VITE_USE_MOCK\s*!==\s*['"]false['"]/],
  ])
}

requirePatterns('frontend/src/styles/global.css', [
  ['H5 card overflow protection', /\.h5-content-card[\s\S]+overflow:\s*hidden/],
  ['H5 product card overflow protection', /\.h5-product-card[\s\S]+overflow:\s*hidden/],
  ['H5 hard-wrap protection', /overflow-wrap:\s*anywhere/],
])

requirePatterns('frontend/src/pages/h5/H5LandingPage.vue', [
  ['H5 detail links preserve back context', /buildH5DetailBackQuery[\s\S]+contentDetailRoute[\s\S]+productDetailRoute/],
])

requirePatterns('frontend/src/pages/h5/H5ProductDetailPage.vue', [
  ['H5 product contacts paired display', /productContacts[\s\S]+product-contact-list[\s\S]+product-contact-item/],
])

for (const filePath of [
  'frontend/src/pages/h5/H5ArticleDetailPage.vue',
  'frontend/src/pages/h5/H5ProductDetailPage.vue',
]) {
  requirePatterns(filePath, [
    ['H5 detail back target resolver', /resolveH5BackTarget\(route\.query\)/],
  ])
}

for (const filePath of [
  'ruoyi-ui/.env.development',
  'ruoyi-ui/.env.production',
  'ruoyi-ui/.env.staging',
  'ruoyi-ui/src/settings.js',
  'ruoyi-ui/src/layout/components/Navbar.vue',
  'ruoyi-ui/src/views/login.vue',
  'ruoyi-ui/src/views/register.vue',
]) {
  rejectPatterns(filePath, [
    ['RuoYi default branding', /若依|RuoYi|ruoyi/i],
    ['RuoYi source/doc links', /源码地址|文档地址|y_project|doc\.ruoyi\.vip/],
    ['Mock-only prompt', /MockJs|mock api|mock-server/i],
    ['default demo password', /admin123/],
  ])
}

for (const filePath of [
  'ruoyi-ui/src/views/login.vue',
  'ruoyi-ui/src/views/register.vue',
  'ruoyi-ui/src/api/login.js',
]) {
  rejectPatterns(filePath, [
    ['captcha UI or API usage', /验证码|captchaEnabled|getCodeImg|captchaImage|validCode/],
  ])
}

requirePatterns('backend/ruoyi-framework/src/main/java/com/ruoyi/framework/web/service/SysLoginService.java', [
  ['username-password login signature', /login\(String username,\s*String password\)/],
])

requirePatterns('backend/ruoyi-framework/src/main/java/com/ruoyi/framework/web/exception/GlobalExceptionHandler.java', [
  ['login user exception uses 401 contract', /@ExceptionHandler\(UserException\.class\)[\s\S]+HttpStatus\.UNAUTHORIZED/],
])

rejectPatterns('backend/ruoyi-framework/src/main/java/com/ruoyi/framework/web/service/SysLoginService.java', [
  ['captcha validation in login service', /validateCaptcha|selectCaptchaEnabled|CAPTCHA_CODE_KEY|CaptchaException|CaptchaExpireException/],
])

requirePatterns('backend/sql/ry_20260417.sql', [
  ['captcha disabled in base seed', /'sys\.account\.captchaEnabled',\s+'false'/],
])

requirePatterns('backend/central-bank-business/src/main/java/com/centralbank/eplatform/service/AdminAccountService.java', [
  ['central bank accounts bind RuoYi role', /CENTRAL_BANK_COMMON_ROLE_ID[\s\S]+insertUserRole\(account\.getId\(\),\s*CENTRAL_BANK_COMMON_ROLE_ID\)/],
])

requirePatterns('backend/central-bank-business/src/main/resources/mapper/centralbank/CbAdminAccountMapper.xml', [
  ['account mapper writes user role bridge', /insert into sys_user_role\(user_id,\s*role_id\)/],
])

requirePatterns('backend/ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysLoginController.java', [
  ['central bank menu filtering', /filterCentralBankMenus[\s\S]+centralBankPermissions[\s\S]+MONETARY_CREDIT/],
])

requirePatterns('ruoyi-ui/src/utils/request.js', [
  ['contract message preferred in normal responses', /res\.data\.message\s*\|\|\s*res\.data\.msg/],
  ['contract message preferred in axios error responses', /error\.response\s*&&\s*error\.response\.data[\s\S]+message/],
])

requirePatterns('ruoyi-ui/src/plugins/download.js', [
  ['download errors prefer contract message', /rspObj\.message\s*\|\|\s*rspObj\.msg/],
])

requirePatterns('ruoyi-ui/src/views/centralbank/content/index.vue', [
  ['office-first content form rule', /请先选择办公室/],
  ['county office category narrowing', /formContentCategories[\s\S]+SERVICE_GUIDE[\s\S]+isCountyOffice/],
])

requirePatterns('backend/central-bank-business/src/main/java/com/centralbank/eplatform/service/AdminContentService.java', [
  ['county office category normalization', /categoryForOffice[\s\S]+SERVICE_GUIDE/],
])

requirePatterns('ruoyi-ui/src/views/centralbank/product/index.vue', [
  ['dynamic product contacts', /form\.contacts[\s\S]+addContact[\s\S]+removeContact/],
  ['product contact limit five', /contacts\.length\s*<\s*5|最多添加 5 组/],
  ['stable product contact actions wrapper', /product-contact-actions[\s\S]+product-contact-action/],
  ['fixed product contact action button size', /product-contact-action[\s\S]+width:\s*32px[\s\S]+height:\s*32px/],
])

requirePatterns('backend/central-bank-business/src/main/java/com/centralbank/eplatform/dto/AdminProductRequest.java', [
  ['admin product contacts request', /List<ProductContact>\s+contacts/],
])

requirePatterns('backend/central-bank-business/src/main/java/com/centralbank/eplatform/service/AdminProductService.java', [
  ['product contact max validation', /MAX_CONTACTS\s*=\s*5[\s\S]+最多添加 5 组/],
])

requirePatterns('backend/central-bank-business/src/main/resources/sql/central_bank_schema_mysql.sql', [
  ['product contact storage length', /business_manager varchar\(500\)[\s\S]+contact_info varchar\(500\)/],
])

rejectPatterns('ruoyi-ui/src/main.js', [
  ['Mock-only prompt', /MockJs|mock api|mock-server/i],
])

rejectPatterns('ruoyi-ui/src/layout/components/Navbar.vue', [
  ['RuoYi global header search', /HeaderSearch|<search\b|header-search/i],
  ['RuoYi fullscreen tool', /Screenfull|screenfull/i],
  ['RuoYi layout size selector', /SizeSelect|布局大小|size-select/i],
  ['RuoYi notice tool', /HeaderNotice|消息通知|header-notice/i],
  ['RuoYi lock screen entry', /锁定屏幕|lockScreen/i],
  ['RuoYi personal-center entry', /个人中心/],
  ['RuoYi layout-setting entry', /布局设置|setLayout/],
])

rejectPatterns('ruoyi-ui/src/layout/index.vue', [
  ['RuoYi layout settings drawer', /<settings\b|Settings/],
])

requirePatterns('ruoyi-ui/src/settings.js', [
  ['settings panel disabled', /showSettings:\s*false/],
  ['tags view disabled', /tagsView:\s*false/],
])

if (requireFile('docker-compose.yml')) {
  requirePatterns('docker-compose.yml', [
    ['MySQL 8 service', /image:\s*mysql:8(?:\.0)?/i],
    ['Redis 7 service', /image:\s*redis:7/i],
    ['MySQL localhost port 3306', /["']?3306:3306["']?/],
    ['Redis localhost port 6379', /["']?6379:6379["']?/],
    ['MySQL environment interpolation', /MYSQL_ROOT_PASSWORD:\s*\$\{MYSQL_ROOT_PASSWORD:-[^}\r\n]+}/],
    ['MySQL business database', /MYSQL_DATABASE:\s*\$\{MYSQL_DATABASE:-central_bank_e_platform}/],
    ['ordered RuoYi base SQL', /10-ruoyi-base\.sql/],
    ['ordered Quartz SQL', /20-quartz\.sql/],
    ['ordered central-bank schema SQL', /30-central-bank-schema\.sql/],
    ['ordered central-bank seed SQL', /40-central-bank-seed\.sql/],
    ['ordered central-bank menu SQL', /50-central-bank-content-menu\.sql[\s\S]+60-central-bank-product-menu\.sql[\s\S]+70-central-bank-account-menu\.sql[\s\S]+80-central-bank-audit-log-menu\.sql/],
    ['ordered central-bank admin menu cleanup SQL', /90-central-bank-admin-menu-cleanup\.sql/],
  ])
  rejectSensitiveContent('docker-compose.yml')
}

for (const filePath of [
  'backend/sql/ry_20260417.sql',
  'backend/central-bank-business/src/main/resources/sql/central_bank_seed_mysql.sql',
  'backend/central-bank-business/src/main/resources/sql/central_bank_content_menu.sql',
  'backend/central-bank-business/src/main/resources/sql/central_bank_product_menu.sql',
  'backend/central-bank-business/src/main/resources/sql/central_bank_account_menu.sql',
  'backend/central-bank-business/src/main/resources/sql/central_bank_audit_log_menu.sql',
  'backend/central-bank-business/src/main/resources/sql/central_bank_admin_menu_cleanup.sql',
]) {
  requirePatterns(filePath, [
    ['utf8mb4 client charset declaration', /SET\s+NAMES\s+utf8mb4\s*;/i],
  ])
}

requirePatterns('backend/central-bank-business/src/main/resources/sql/central_bank_admin_menu_cleanup.sql', [
  ['default RuoYi menu disablement', /where\s+menu_id\s+<\s+2000/i],
  ['business menus preserved', /2100[\s\S]+2200[\s\S]+2300[\s\S]+2400/],
])

requirePatterns('backend/ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysLoginController.java', [
  ['current business account extension in getInfo', /account_extension/],
])

requirePatterns('ruoyi-ui/src/store/modules/user.js', [
  ['business account extension stored', /SET_ACCOUNT_EXTENSION/],
])

requirePatterns('ruoyi-ui/src/store/getters.js', [
  ['business account extension getter', /accountExtension/],
])

requirePatterns('ruoyi-ui/src/views/centralbank/content/index.vue', [
  ['non-admin office select is disabled', /:disabled="isOfficeLocked"/],
  ['locked office options are scoped', /availableFormOffices/],
  ['add form applies locked office default', /applyLockedOfficeDefaults/],
])

requirePatterns('ruoyi-ui/src/views/index.vue', [
  ['dashboard quick actions derive from permissions', /quickActions/],
  ['dashboard quick actions disable unauthorized entries', /:disabled="!action\.enabled"/],
  ['dashboard quick actions use auth permission check', /auth\.hasPermi/],
])

requirePatterns('backend/ruoyi-admin/src/main/resources/application-druid.yml', [
  ['MySQL 8 public key retrieval flag', /allowPublicKeyRetrieval=true/],
  ['MySQL SSL disabled for local Docker', /useSSL=false/],
])

for (const filePath of [
  'ruoyi-ui/src/api/centralbank/content.js',
  'ruoyi-ui/src/api/centralbank/product.js',
  'ruoyi-ui/src/api/centralbank/account.js',
  'ruoyi-ui/src/api/centralbank/auditLog.js',
  'ruoyi-ui/src/api/centralbank/dashboard.js',
]) {
  rejectPatterns(filePath, [
    ['Mock-only marker', /\[Mock\]|Mock-only|mock-only|模拟数据/i],
  ])
  const content = read(filePath)
  if (!/url:\s*['"]\/api\/admin\//.test(content)) {
    violations.push(`${filePath}: central-bank service must call /api/admin/* Spring Boot API`)
  }
}

if (requireFile('docs/startup.md')) {
  requirePatterns('docs/startup.md', [
    ['H5 startup command on Agent port 5199', /frontend[\s\S]+npm(?:\.cmd)?\s+run\s+dev[\s\S]+5199/i],
    ['H5 user acceptance port 5175', /frontend[\s\S]+5175/i],
    ['backend health profile startup and /health check', /spring\.profiles\.active=health[\s\S]+\/health/i],
    ['backend Agent and user ports', /8099[\s\S]+8003|8003[\s\S]+8099/],
    ['ruoyi-ui startup or build command', /ruoyi-ui[\s\S]+npm\.cmd\s+run\s+(?:dev|build:prod)/i],
    ['frontend config fields', /VITE_API_BASE_URL[\s\S]+VITE_USE_MOCK[\s\S]+VITE_BACKEND_PROXY_TARGET/],
    ['backend config fields', /DB_URL[\s\S]+DB_USERNAME[\s\S]+DB_PASSWORD[\s\S]+APP_STORAGE_ROOT[\s\S]+APP_JWT_SECRET/],
    ['ruoyi-ui config fields', /VUE_APP_BASE_API[\s\S]+VUE_APP_BACKEND_PROXY_TARGET/],
    ['Docker compose config command', /docker\s+compose\s+config/i],
    ['Docker compose startup command', /docker\s+compose\s+up\s+-d\s+mysql\s+redis/i],
    ['Docker or MySQL boundary', /Docker[\s\S]+MySQL|MySQL[\s\S]+Docker/i],
    ['MySQL 8 JDBC flags', /allowPublicKeyRetrieval=true[\s\S]+useSSL=false|useSSL=false[\s\S]+allowPublicKeyRetrieval=true/i],
    ['MySQL core table verification', /sys_user[\s\S]+cb_financial_product|cb_financial_product[\s\S]+sys_user/i],
    ['Redis boundary', /Redis/i],
    ['Redis ping verification', /redis-cli\s+ping/i],
    ['fallback boundary', /fallback|降级|H2/i],
    ['verification steps', /验证步骤|Verification/i],
  ])
  rejectSensitiveContent('docs/startup.md')
}

if (violations.length > 0) {
  console.error('Mock exit audit failed:')
  for (const violation of violations) {
    console.error(`- ${violation}`)
  }
  process.exit(1)
}

console.log('Mock exit audit passed')
