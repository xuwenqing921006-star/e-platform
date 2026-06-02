import type {
  ApiResponse,
  ContentCategory,
  DashboardRecentContent,
  DashboardSummaryData,
  LoginResponseData,
  PaginatedData,
  PublicContentListItem,
} from '../types/api'

interface MockRequest {
  method?: string
  url?: string
  params?: Record<string, unknown>
  data?: unknown
}

interface MockResponse<T> {
  status: number
  data: ApiResponse<T>
}

interface MockContentEntity extends PublicContentListItem {
  county_code: string
}

const mockContents: MockContentEntity[] = [
  {
    id: 101,
    title: '[Mock] 肇州县金融服务便民联系指南',
    category: 'SERVICE_GUIDE',
    county_code: 'ZHAOZHOU',
    office_name: '肇州县金融服务队',
    published_at: '2026-05-29T09:30:00+08:00',
  },
  {
    id: 102,
    title: '[Mock] 肇州县涉农贷款办理材料清单',
    category: 'SERVICE_GUIDE',
    county_code: 'ZHAOZHOU',
    office_name: '货币信贷政策管理科',
    published_at: '2026-05-21T09:30:00+08:00',
  },
  {
    id: 103,
    title: '[Mock] 肇州县农村信用体系建设服务说明',
    category: 'SERVICE_GUIDE',
    county_code: 'ZHAOZHOU',
    office_name: '征信管理科',
    published_at: '2026-05-13T09:30:00+08:00',
  },
]

function success<T>(data: T): MockResponse<T> {
  return {
    status: 200,
    data: {
      code: 200,
      message: 'success',
      data,
    },
  }
}

function error(status: number, message: string): MockResponse<null> {
  return {
    status,
    data: {
      code: status,
      message,
      data: null,
    },
  }
}

function listPublicContents(params: Record<string, unknown> = {}) {
  const page = Number(params.page || 1)
  const pageSize = Number(params.page_size || 10)
  const category = (params.category || 'SERVICE_GUIDE') as ContentCategory
  const countyCode = String(params.county_code || 'ZHAOZHOU')
  const items: PublicContentListItem[] = mockContents
    .filter(
      (content) =>
        content.category === category && content.county_code === countyCode,
    )
    .map((content) => ({
      id: content.id,
      title: content.title,
      category: content.category,
      office_name: content.office_name,
      published_at: content.published_at,
    }))

  const data: PaginatedData<PublicContentListItem> = {
    items,
    total: items.length,
    page,
    page_size: pageSize,
  }

  return success(data)
}

function login(data: unknown) {
  const credentials =
    typeof data === 'string' ? (JSON.parse(data) as Record<string, unknown>) : data

  if (
    !credentials ||
    typeof credentials !== 'object' ||
    !('username' in credentials) ||
    !('password' in credentials)
  ) {
    return error(401, '用户名或密码错误')
  }

  const response: LoginResponseData = {
    access_token: 'mock-admin-token',
    token_type: 'Bearer',
    expires_in: 7200,
    user: {
      id: 1,
      username: String(credentials.username),
      display_name: '[Mock] 系统管理员',
      role: 'ADMIN',
      office_code: null,
      office_name: null,
      permissions: [
        'CONTENT_MANAGE',
        'PRODUCT_MANAGE',
        'ACCOUNT_MANAGE',
        'AUDIT_LOG_VIEW',
      ],
    },
  }

  return success(response)
}

function dashboardSummary() {
  const recentContents: DashboardRecentContent[] = mockContents.map((content) => ({
    id: content.id,
    title: content.title,
    category: content.category,
    published_at: content.published_at,
  }))
  const data: DashboardSummaryData = {
    published_content_count: 48,
    product_count: 112,
    account_count: 9,
    today_operation_count: 16,
    recent_contents: recentContents,
  }

  return success(data)
}

export function dispatchMockRequest(request: MockRequest): MockResponse<unknown> {
  const method = (request.method || 'GET').toUpperCase()
  const path = (request.url || '').replace(/^\/api/, '')

  if (method === 'GET' && path === '/public/contents') {
    return listPublicContents(request.params)
  }
  if (method === 'POST' && path === '/auth/login') {
    return login(request.data)
  }
  if (method === 'GET' && path === '/admin/dashboard/summary') {
    return dashboardSummary()
  }

  return error(404, 'Mock 接口不存在')
}
