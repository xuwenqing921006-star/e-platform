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

for (const filePath of [
  'ruoyi-ui/.env.development',
  'ruoyi-ui/.env.production',
  'ruoyi-ui/.env.staging',
  'ruoyi-ui/src/settings.js',
  'ruoyi-ui/src/layout/components/Navbar.vue',
  'ruoyi-ui/src/views/login.vue',
]) {
  rejectPatterns(filePath, [
    ['RuoYi default branding', /若依|RuoYi|ruoyi/i],
    ['RuoYi source/doc links', /源码地址|文档地址|y_project|doc\.ruoyi\.vip/],
    ['Mock-only prompt', /MockJs|mock api|mock-server/i],
    ['default demo password', /admin123/],
  ])
}

rejectPatterns('ruoyi-ui/src/main.js', [
  ['Mock-only prompt', /MockJs|mock api|mock-server/i],
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
  ])
  rejectSensitiveContent('docker-compose.yml')
}

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
