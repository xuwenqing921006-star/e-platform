import type { PublicProductDetailData } from '../types/api'

export interface ProductContactItem {
  business_manager: string
  contact_info: string
}

export function splitProductContacts(
  product: Pick<PublicProductDetailData, 'business_manager' | 'contact_info'>,
): ProductContactItem[] {
  const managers = splitLines(product.business_manager)
  const contactInfos = splitLines(product.contact_info)
  const length = Math.max(managers.length, contactInfos.length, 1)

  return Array.from({ length }, (_, index) => ({
    business_manager: managers[index] || '',
    contact_info: contactInfos[index] || '',
  })).filter((item) => item.business_manager || item.contact_info)
}

function splitLines(value: string) {
  return String(value || '')
    .split(/\r?\n/)
    .map((item) => item.trim())
}
