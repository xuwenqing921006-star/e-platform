import { readFileSync } from 'node:fs'
import { join } from 'node:path'

const root = process.cwd()
const violations = []

const read = (relativePath) => readFileSync(join(root, relativePath), 'utf8')

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

if (violations.length > 0) {
  console.error('Mock exit audit failed:')
  for (const violation of violations) {
    console.error(`- ${violation}`)
  }
  process.exit(1)
}

console.log('Mock exit audit passed')
