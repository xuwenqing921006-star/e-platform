import type { CountyCode } from '../types/api'

export const PRIMARY_TABS = [
  { id: 'RURAL', label: '乡村振兴' },
  { id: 'FINANCIAL', label: '金融服务' },
] as const

export const RURAL_TABS = [
  { id: 'ZHAOZHOU', label: '肇州县' },
  { id: 'ZHAOYUAN', label: '肇源县' },
  { id: 'LINDIAN', label: '林甸县' },
  { id: 'DUMENG', label: '杜蒙县' },
] as const

export const RURAL_SECTION_TABS = [
  { id: 'SERVICE_GUIDE', label: '服务指引' },
  { id: 'SERVICE_TEAM', label: '金融业务服务队' },
] as const

export const FINANCIAL_TABS = [
  { id: 'SERVICE_GUIDE', label: '服务指引' },
  { id: 'POLICY_PROMOTION', label: '政策宣传' },
  { id: 'PRODUCTS', label: '助企通道' },
] as const

interface ServiceTeamMember {
  name: string
  role: string
  phone: string
}

interface CountyServiceTeam {
  title: string
  description: string
  members: ServiceTeamMember[]
}

export const COUNTY_SERVICE_TEAMS: Record<CountyCode, CountyServiceTeam> = {
  ZHAOZHOU: {
    title: '肇州县金融业务服务队',
    description: '',
    members: [],
  },
  ZHAOYUAN: {
    title: '肇源县金融业务服务队',
    description: '',
    members: [],
  },
  LINDIAN: {
    title: '林甸县金融业务服务队',
    description: '',
    members: [],
  },
  DUMENG: {
    title: '杜蒙县金融业务服务队',
    description: '',
    members: [],
  },
}

/*
 * 服务队资料暂未正式提供，先保留原占位内容备查。
 * 有真实数据后，将对应县域的 description 和 members 替换回正式内容。
 *
 * ZHAOZHOU:
 * description: '面向县域企业、农户和新型农业经营主体，提供政策咨询、融资对接与金融服务转介。'
 * members:
 * - { name: '张三', role: '服务队负责人', phone: '0459-0001001' }
 * - { name: '李四', role: '融资对接专员', phone: '0459-0001002' }
 * - { name: '王芳', role: '政策咨询专员', phone: '0459-0001003' }
 *
 * ZHAOYUAN:
 * - { name: '赵强', role: '服务队负责人', phone: '0459-0002001' }
 * - { name: '孙丽', role: '融资对接专员', phone: '0459-0002002' }
 * - { name: '周敏', role: '政策咨询专员', phone: '0459-0002003' }
 *
 * LINDIAN:
 * - { name: '陈磊', role: '服务队负责人', phone: '0459-0003001' }
 * - { name: '杨雪', role: '融资对接专员', phone: '0459-0003002' }
 * - { name: '刘畅', role: '政策咨询专员', phone: '0459-0003003' }
 *
 * DUMENG:
 * - { name: '吴刚', role: '服务队负责人', phone: '0459-0004001' }
 * - { name: '郑楠', role: '融资对接专员', phone: '0459-0004002' }
 * - { name: '何静', role: '政策咨询专员', phone: '0459-0004003' }
 */
