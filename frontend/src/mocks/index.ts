import type {
  ApiResponse,
  AttachmentFileType,
  ContentCategory,
  CountyCode,
  DashboardRecentContent,
  DashboardSummaryData,
  LoginResponseData,
  PaginatedData,
  PublicAttachment,
  PublicContentDetailData,
  PublicContentListItem,
  PublicProductDetailData,
  PublicProductListItem,
  PublicScope,
  ProductType,
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
  county_code: CountyCode | null
  rich_text_html: string
  scope: PublicScope
  attachments: PublicAttachment[]
  office_code: string
}

interface MockProductEntity extends PublicProductListItem {
  bank_code: string
  admission_conditions: string
  product_intro: string
  business_manager: string
  contact_info: string
}

const mockContents: MockContentEntity[] = [
  {
    id: 101,
    title: '[Mock] 肇州县金融服务便民联系指南',
    category: 'SERVICE_GUIDE',
    county_code: 'ZHAOZHOU',
    office_code: 'ZHAOZHOU',
    office_name: '肇州县金融服务队',
    published_at: '2026-05-29T09:30:00+08:00',
    rich_text_html:
      '<p>[Mock] 肇州县金融服务队提供涉农贷款、支付结算和征信咨询服务。</p><h2>办理说明</h2><p>可拨打服务队电话预约咨询。</p>',
    scope: 'RURAL',
    attachments: [],
  },
  {
    id: 102,
    title: '[Mock] 肇州县涉农贷款办理材料清单',
    category: 'SERVICE_GUIDE',
    county_code: 'ZHAOZHOU',
    office_code: 'MONETARY_CREDIT',
    office_name: '货币信贷政策管理科',
    published_at: '2026-05-21T09:30:00+08:00',
    rich_text_html:
      '<p>[Mock] 请按贷款银行要求准备经营主体身份证明、经营流水和用途材料。</p>',
    scope: 'RURAL',
    attachments: [],
  },
  {
    id: 103,
    title: '[Mock] 肇州县农村信用体系建设服务说明',
    category: 'SERVICE_GUIDE',
    county_code: 'ZHAOZHOU',
    office_code: 'CREDIT_REPORT',
    office_name: '征信管理科',
    published_at: '2026-05-13T09:30:00+08:00',
    rich_text_html:
      '<p>[Mock] 本说明用于展示县域信用信息采集和信用培育服务流程。</p>',
    scope: 'RURAL',
    attachments: [],
  },
  {
    id: 201,
    title: '[Mock] 征信-大庆市征信代理查询网点地址及电话',
    category: 'SERVICE_GUIDE',
    county_code: null,
    office_code: 'CREDIT_REPORT',
    office_name: '征信管理科',
    published_at: '2026-05-28T09:30:00+08:00',
    rich_text_html:
      '<p>[Mock] 为进一步提升县域征信服务便利度，现将相关查询办理渠道、服务网点和咨询方式公布如下。</p><h2>一、办理说明</h2><p>申请人可携带有效身份证件前往就近服务网点办理。具体业务范围及工作时间以网点现场公示为准。</p><ul><li>个人信用报告查询可就近办理。</li><li>企业信用报告查询需按规定提交授权材料。</li></ul><p><img src="/article-credit-service-long.svg" alt="[Mock] 县域征信服务网点示意长图"></p>',
    scope: 'FINANCIAL',
    attachments: [
      {
        id: 9001,
        file_name: '[Mock] 县域征信服务网点信息表.xlsx',
        file_type: 'EXCEL',
        file_size: 20480,
        download_url: '/api/public/attachments/9001/download',
      },
      {
        id: 9002,
        file_name: '[Mock] 个人信用报告查询指引.pdf',
        file_type: 'PDF',
        file_size: 51200,
        download_url: '/api/public/attachments/9002/download',
      },
      {
        id: 9003,
        file_name: '[Mock] 征信业务申请材料清单.docx',
        file_type: 'WORD',
        file_size: 36864,
        download_url: '/api/public/attachments/9003/download',
      },
      {
        id: 9004,
        file_name: '[Mock] 不应展示的第四个附件.pdf',
        file_type: 'PDF',
        file_size: 10240,
        download_url: '/api/public/attachments/9004/download',
      },
    ],
  },
  {
    id: 202,
    title: '[Mock] 外汇-大庆县域外汇服务站信息表',
    category: 'SERVICE_GUIDE',
    county_code: null,
    office_code: 'FOREIGN_EXCHANGE',
    office_name: '外汇管理科',
    published_at: '2026-05-26T09:30:00+08:00',
    rich_text_html:
      '<p>[Mock] 县域外汇服务站可提供政策咨询和业务指引。</p>',
    scope: 'FINANCIAL',
    attachments: [],
  },
  {
    id: 203,
    title: '[Mock] 国债-大庆市国债承销银行网点信息表',
    category: 'SERVICE_GUIDE',
    county_code: null,
    office_code: 'TREASURY',
    office_name: '国库科',
    published_at: '2026-05-24T09:30:00+08:00',
    rich_text_html:
      '<p>[Mock] 国债承销银行网点信息用于公众就近咨询和购买储蓄国债。</p>',
    scope: 'FINANCIAL',
    attachments: [],
  },
  {
    id: 204,
    title: '[Mock] 支付-大庆市支付服务便民网点清单',
    category: 'SERVICE_GUIDE',
    county_code: null,
    office_code: 'PAYMENT_SETTLEMENT',
    office_name: '支付结算科',
    published_at: '2026-05-20T09:30:00+08:00',
    rich_text_html:
      '<p>[Mock] 支付服务便民网点提供账户、支付工具和适老化服务咨询。</p>',
    scope: 'FINANCIAL',
    attachments: [],
  },
  {
    id: 301,
    title: '[Mock] 中国人民银行公告〔2025〕第12号',
    category: 'POLICY_PROMOTION',
    county_code: null,
    office_code: 'POLICY_PUBLICITY',
    office_name: '法规宣传',
    published_at: '2026-05-30T09:30:00+08:00',
    rich_text_html:
      '<p>[Mock] 本公告内容用于 H5 政策宣传详情展示。</p>',
    scope: 'FINANCIAL',
    attachments: [],
  },
  {
    id: 302,
    title: '[Mock] 中华人民共和国反洗钱法（2024年修订）',
    category: 'POLICY_PROMOTION',
    county_code: null,
    office_code: 'ANTI_MONEY_LAUNDERING',
    office_name: '反洗钱科',
    published_at: '2026-05-25T09:30:00+08:00',
    rich_text_html:
      '<p>[Mock] 反洗钱法修订要点用于政策宣传阅读。</p>',
    scope: 'FINANCIAL',
    attachments: [],
  },
  {
    id: 303,
    title: '[Mock] 金融消费者权益保护政策解读',
    category: 'POLICY_PROMOTION',
    county_code: null,
    office_code: 'FINANCIAL_CONSUMER',
    office_name: '金融消保科',
    published_at: '2026-05-18T09:30:00+08:00',
    rich_text_html:
      '<p>[Mock] 本文解读金融消费者权益保护相关政策。</p>',
    scope: 'FINANCIAL',
    attachments: [],
  },
]

function detailTitle(content: MockContentEntity) {
  return content.id === 201
    ? '[Mock] 征信-大庆市县域征信服务指引'
    : content.title
}

const featuredProducts: MockProductEntity[] = [
  {
    id: 2001,
    bank_code: 'ABC',
    bank_name: '[Mock] 中国农业银行',
    product_name: '[Mock] 惠农e贷',
    product_type: 'AGRICULTURAL',
    admission_conditions: '[Mock] 面向符合条件的涉农经营主体。',
    product_intro: '[Mock] 用于满足农业生产经营流动资金需求。',
    business_manager: '[Mock] 张经理',
    contact_info: '[Mock] 0459-0002001',
  },
  {
    id: 2002,
    bank_code: 'DAQING_RCC',
    bank_name: '[Mock] 大庆农商银行',
    product_name: '[Mock] 小微企业流动资金贷款',
    product_type: 'SMALL_MICRO',
    admission_conditions: '[Mock] 面向符合条件的小微企业。',
    product_intro: '[Mock] 用于满足企业日常流动资金需求。',
    business_manager: '[Mock] 李经理',
    contact_info: '[Mock] 0459-0002002',
  },
  {
    id: 2003,
    bank_code: 'CCB',
    bank_name: '[Mock] 中国建设银行',
    product_name: '[Mock] 裕农快贷',
    product_type: 'AGRICULTURAL',
    admission_conditions: '[Mock] 面向符合条件的县域经营主体。',
    product_intro: '[Mock] 用于支持农业经营与县域产业发展。',
    business_manager: '[Mock] 王经理',
    contact_info: '[Mock] 0459-0002003',
  },
  {
    id: 2004,
    bank_code: 'CEB',
    bank_name: '[Mock] 光大银行',
    product_name: '[Mock] 普惠经营贷',
    product_type: 'SMALL_MICRO',
    admission_conditions: '[Mock] 面向符合条件的普惠经营客户。',
    product_intro: '[Mock] 用于经营周转与生产投入。',
    business_manager: '[Mock] 赵经理',
    contact_info: '[Mock] 0459-0002004',
  },
]

const mockProducts: MockProductEntity[] = [
  ...featuredProducts,
  ...Array.from({ length: 108 }, (_, index) => {
    const productType: ProductType =
      index % 2 === 0 ? 'AGRICULTURAL' : 'SMALL_MICRO'
    const sequence = index + 5

    return {
      id: 2000 + sequence,
      bank_code: `MOCK_BANK_${sequence}`,
      bank_name: `[Mock] 示例银行 ${sequence}`,
      product_name: `[Mock] 助企金融产品 ${sequence}`,
      product_type: productType,
      admission_conditions: '[Mock] 面向符合条件的经营主体。',
      product_intro: '[Mock] 用于模拟 H5 分页加载展示。',
      business_manager: '[Mock] 示例经理',
      contact_info: `[Mock] 0459-${String(sequence).padStart(7, '0')}`,
    }
  }),
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
  const scope = (params.scope || 'RURAL') as PublicScope
  const countyCode = params.county_code
    ? (String(params.county_code) as CountyCode)
    : undefined

  if (scope === 'RURAL' && 'scope' in params && !countyCode) {
    return error(400, '乡村振兴服务指引必须指定县域')
  }

  if (page < 1 || pageSize < 1 || pageSize > 50) {
    return error(400, '分页参数不合法')
  }

  const items: PublicContentListItem[] = mockContents
    .filter(
      (content) =>
        content.category === category &&
        content.scope === scope &&
        (scope === 'FINANCIAL' ||
          content.county_code === (countyCode || 'ZHAOZHOU')),
    )
    .map((content) => ({
      id: content.id,
      title: content.title,
      category: content.category,
      office_name: content.office_name,
      published_at: content.published_at,
    }))
  const offset = (page - 1) * pageSize

  const data: PaginatedData<PublicContentListItem> = {
    items: items.slice(offset, offset + pageSize),
    total: items.length,
    page,
    page_size: pageSize,
  }

  return success(data)
}

function getPublicContentDetail(id: number) {
  const content = mockContents.find((item) => item.id === id)

  if (!content) {
    return error(404, '内容不存在')
  }

  const data: PublicContentDetailData = {
    id: content.id,
    title: detailTitle(content),
    category: content.category,
    office_name: content.office_name,
    published_at: content.published_at,
    rich_text_html: content.rich_text_html,
    attachments: content.attachments.slice(0, 3).map((attachment) => ({
      id: attachment.id,
      file_name: attachment.file_name,
      file_type: attachment.file_type as AttachmentFileType,
      file_size: attachment.file_size,
      download_url: attachment.download_url,
    })),
  }

  return success(data)
}

function listPublicProducts(params: Record<string, unknown> = {}) {
  const page = Number(params.page || 1)
  const pageSize = Number(params.page_size || 10)

  if (page < 1 || pageSize < 1 || pageSize > 50) {
    return error(400, '分页参数不合法')
  }

  const offset = (page - 1) * pageSize
  const items: PublicProductListItem[] = mockProducts
    .slice(offset, offset + pageSize)
    .map((product) => ({
      id: product.id,
      bank_name: product.bank_name,
      product_name: product.product_name,
      product_type: product.product_type,
    }))
  const data: PaginatedData<PublicProductListItem> = {
    items,
    total: mockProducts.length,
    page,
    page_size: pageSize,
  }

  return success(data)
}

function getPublicProductDetail(id: number) {
  const product = mockProducts.find((item) => item.id === id)

  if (!product) {
    return error(404, '产品不存在')
  }

  const data: PublicProductDetailData = {
    id: product.id,
    bank_name: product.bank_name,
    product_name: product.product_name,
    product_type: product.product_type,
    admission_conditions: product.admission_conditions,
    product_intro: product.product_intro,
    business_manager: product.business_manager,
    contact_info: product.contact_info,
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
  if (method === 'GET' && /^\/public\/contents\/\d+$/.test(path)) {
    return getPublicContentDetail(Number(path.split('/').pop()))
  }
  if (method === 'GET' && path === '/public/products') {
    return listPublicProducts(request.params)
  }
  if (method === 'GET' && /^\/public\/products\/\d+$/.test(path)) {
    return getPublicProductDetail(Number(path.split('/').pop()))
  }
  if (method === 'POST' && path === '/auth/login') {
    return login(request.data)
  }
  if (method === 'GET' && path === '/admin/dashboard/summary') {
    return dashboardSummary()
  }

  return error(404, 'Mock 接口不存在')
}
