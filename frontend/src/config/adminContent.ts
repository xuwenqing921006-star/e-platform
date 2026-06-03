import type { ContentCategory } from '../types/api'

export const adminContentCategoryOptions: {
  label: string
  value: ContentCategory
}[] = [
  { label: '服务指引', value: 'SERVICE_GUIDE' },
  { label: '政策宣传', value: 'POLICY_PROMOTION' },
]

export const adminContentOfficeOptions = [
  { label: '货币信贷政策管理科', value: 'MONETARY_CREDIT' },
  { label: '宏观审慎与金融市场管理科', value: 'MACRO_PRUDENTIAL' },
  { label: '金融稳定科', value: 'FINANCIAL_STABILITY' },
  { label: '统计研究科', value: 'STATISTICS_RESEARCH' },
  { label: '支付结算科', value: 'PAYMENT_SETTLEMENT' },
  { label: '货币金银科', value: 'CURRENCY_GOLD_SILVER' },
  { label: '国库科', value: 'TREASURY' },
  { label: '征信管理科', value: 'CREDIT_REPORT' },
  { label: '反洗钱科', value: 'ANTI_MONEY_LAUNDERING' },
  { label: '外汇管理科', value: 'FOREIGN_EXCHANGE' },
  { label: '肇州县', value: 'ZHAOZHOU' },
  { label: '肇源县', value: 'ZHAOYUAN' },
  { label: '林甸县', value: 'LINDIAN' },
  { label: '杜蒙县', value: 'DUMENG' },
]

export const adminContentCategoryLabels = Object.fromEntries(
  adminContentCategoryOptions.map((option) => [option.value, option.label]),
) as Record<ContentCategory, string>
