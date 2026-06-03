export interface ApiResponse<T> {
  code: number
  message: string
  data: T | null
}

export interface PaginatedData<T> {
  items: T[]
  total: number
  page: number
  page_size: number
}

export type ContentCategory = 'SERVICE_GUIDE' | 'POLICY_PROMOTION'
export type CountyCode = 'ZHAOZHOU' | 'ZHAOYUAN' | 'LINDIAN' | 'DUMENG'
export type PublicScope = 'FINANCIAL' | 'RURAL'
export type ProductType = 'AGRICULTURAL' | 'SMALL_MICRO'
export type AttachmentFileType = 'PDF' | 'WORD' | 'EXCEL'
export type UserRole = 'ADMIN' | 'OFFICE_USER'

export interface PublicContentListItem {
  id: number
  title: string
  category: ContentCategory
  office_name: string
  published_at: string
}

export type PublicContentListData = PaginatedData<PublicContentListItem>

export interface PublicAttachment {
  id: number
  file_name: string
  file_type: AttachmentFileType
  file_size: number
  download_url: string
}

export interface PublicContentDetailData {
  id: number
  title: string
  category: ContentCategory
  office_name: string
  published_at: string
  rich_text_html: string
  attachments: PublicAttachment[]
}

export interface PublicProductListItem {
  id: number
  bank_name: string
  product_name: string
  product_type: ProductType
}

export type PublicProductListData = PaginatedData<PublicProductListItem>

export interface PublicProductDetailData {
  id: number
  bank_name: string
  product_name: string
  product_type: ProductType
  admission_conditions: string
  product_intro: string
  business_manager: string
  contact_info: string
}

export interface AuthUser {
  id: number
  username: string
  display_name: string
  role: UserRole
  office_code: string | null
  office_name: string | null
  permissions: string[]
}

export interface LoginResponseData {
  access_token: string
  token_type: 'Bearer'
  expires_in: number
  user: AuthUser
}

export interface LoginRequest {
  username: string
  password: string
}

export interface DashboardRecentContent {
  id: number
  title: string
  category: ContentCategory
  published_at: string
}

export interface DashboardSummaryData {
  published_content_count: number
  product_count: number
  account_count: number
  today_operation_count: number
  recent_contents: DashboardRecentContent[]
}

export interface AdminAttachmentUploadData {
  id: number
  file_name: string
  file_type: AttachmentFileType
  file_size: number
  download_url: string
}

export interface AdminAttachmentDeleteData {
  deleted: true
}

export interface AdminContentListItem {
  id: number
  title: string
  category: ContentCategory
  office_code: string
  office_name: string
  published_at: string
}

export type AdminContentListData = PaginatedData<AdminContentListItem>

export interface AdminContentListParams {
  keyword?: string
  category?: ContentCategory | ''
  office_code?: string
  published_from?: string
  published_to?: string
  page: number
  page_size: number
}

export interface AdminContentSaveRequest {
  title: string
  category: ContentCategory
  office_code: string
  rich_text_html: string
  attachment_ids: number[]
}

export interface AdminContentCreateData {
  id: number
  published_at: string
}

export interface AdminContentAttachment {
  id: number
  file_name: string
  file_type: AttachmentFileType
  file_size: number
  download_url: string
}

export interface AdminContentDetailData {
  id: number
  title: string
  category: ContentCategory
  office_code: string
  office_name: string
  rich_text_html: string
  published_at: string
  attachments: AdminContentAttachment[]
}

export interface AdminContentUpdateData {
  id: number
  updated: true
}

export interface AdminContentDeleteData {
  deleted: true
}
